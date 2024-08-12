package mz.org.fgh.sifmoz.backend.utilities

import grails.web.*
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.collection.internal.PersistentSet

class JSONSerializer {
    def target
    boolean searchMode
    List<String> toInclude
    String parent

    JSONSerializer(def target) {
        this.target = target
    }

    JSONSerializer(def target, boolean searchMode, List<String> toInclude) {
        this.target = target
        this.searchMode = searchMode
        this.toInclude = toInclude
    }

    HashSet<String> excludedProperties = ['class', 'metaClass', 'logExcluded', 'auditablePropertyNames', 'logIgnoreEvents',
                                          'logVerboseEvents', 'auditableDirtyPropertyNames', 'logMaskProperties', 'clearQueueEnabled',
                                          'newlyInstantiated'] as HashSet

    boolean isExclusiveProperty(String propName) {
        return excludedProperties.contains(propName)
    }

    static boolean isObject(Object propValue) {
        return propValue instanceof Object
    }

    static boolean isPersistentSet(Object propValue) {
        return propValue instanceof PersistentSet || propValue instanceof Collection || propValue instanceof List
    }

    static JSONArray getSerializedArray(Object propValue) {
        JSONArray array = new JSONArray()
        propValue.each { object ->
            def serializedObject = new JSONObject(new JSONSerializer(object).getJSON())
            array.add(serializedObject)
        }
        return array
    }

    String getJSONLevel0() {
        Closure jsonFormat = {
            // Set the delegate of buildJSON to ensure that missing methods called thereby are routed to the JSONBuilder
            if (this.searchMode) {
                buildLightJSON.delegate = delegate
                buildLightJSON(target)
            } else {
                buildLightJSON.delegate = delegate
                buildLightJSON(target)
            }
        }
        def json = new JSONBuilder().build(jsonFormat)
        return json.toString(true)
    }

    String getJSON() {
        Closure jsonFormat = {
            // Set the delegate of buildJSON to ensure that missing methods called thereby are routed to the JSONBuilder

            if (this.searchMode) {
                buildJSON.delegate = delegate
                buildJSON(target)
            } else {
                buildJSON.delegate = delegate
                buildJSON(target)
            }
        }
        def json = new JSONBuilder().build(jsonFormat)
        return json.toString(true)
    }

    String getJSONLevel2() {
        Closure jsonFormat = {
            // Set the delegate of buildJSON to ensure that missing methods called thereby are routed to the JSONBuilder

            if (this.searchMode) {
                buildJSONChild.delegate = delegate
                buildJSONChild(target)
            } else {
                buildJSONChild.delegate = delegate
                buildJSONChild(target)
            }
        }
        def json = new JSONBuilder().build(jsonFormat)
        return json.toString(true)
    }

    String getJSONLevel3() {
        Closure jsonFormat = {
            // Set the delegate of buildJSON to ensure that missing methods called thereby are routed to the JSONBuilder
            if (this.searchMode) {
                buildJSONParentChild.delegate = delegate
                buildJSONParentChild(target)
            } else {
                buildJSONParentChild.delegate = delegate
                buildJSONParentChild(target)
            }
        }
        def json = new JSONBuilder().build(jsonFormat)
        return json.toString(true)
    }

    private buildLightJSON = { obj ->
        setProperty("id", obj.id)
        obj.properties.each { propName, propValue ->
            if (isExclusiveProperty(propName as String)) return

            if (isSimpleProp(propValue)) {
                setProperty(propName as String, propValue)
                return
            }
        }
    }

    private buildJSON = { obj ->
        setProperty("id", obj.id)
        obj.properties.each { propName, propValue ->

            if (isExclusiveProperty(propName as String)) return

            if (isSimple(propValue)) {
                setProperty(propName as String, propValue)
                return
            }

            Closure nestedObject = {
                buildJSON(propValue)
            }
            setProperty(propName as String, nestedObject)
        }
    }

    private buildJSONChild = { obj ->
        setProperty("id", obj.id)
        obj.properties.each { propName, propValue ->
            if (isExclusiveProperty(propName as String)) return

            if (isSimpleProp(propValue)) {
                setProperty(propName as String, propValue)
                return
            }

            if (isPersistentSet(propValue)) {
                setProperty(propName as String, setJsonObjectResponse(propValue as List))
                return
            }
            // propValue is a list of objects, so serialize each object in the list and add it to the JSON array
            Closure nestedObject = {
                buildJSONChild(propValue)
            }
            setProperty(propName as String, nestedObject)
        }
    }

    private buildJSONParentChild = { obj ->
        setProperty("id", obj.id)
        obj.properties.each { propName, propValue ->
            if (isExclusiveProperty(propName as String)) return

            if (isSimpleProp(propValue)) {
                setProperty(propName as String, propValue)
                return
            }

            if (isObject(propValue)) {
                if (isPersistentSet(propValue)) {
                    setProperty(propName as String, setObjectListJsonResponseLevel2(propValue as List))
                    return
                }
                setProperty(propName as String, setJsonObjectResponseLevel2(propValue))
                return
            }
        }
    }

    private boolean isSimpleIncluded(String propName) {
        if (Utilities.listHasElements(this.toInclude as ArrayList<?>)) {
            for (String includePropName : this.toInclude) {
                if (propName == includePropName) return true
            }
        }
        return false
    }

    private boolean iscompositionIncluded(String propName) {
        if (Utilities.listHasElements(this.toInclude as ArrayList<?>)) {
            for (int j = 0; this.toInclude.size() - 1 > j; j++) {
                if (isComposition(this.toInclude[j] && !isSimpleProp(propName))) {
                    String[] exploded = this.toInclude[j].split(java.util.regex.Pattern.quote("."))
                    //List<String> compositionList = Utilities.splitString(includePropName, ".")
                    List<String> compositionList = new ArrayList<>()
                    for (int i = 0; i < exploded.length; i++) {
                        compositionList.add(exploded[i]);
                    }
                    if (propName == compositionList.get(0)) {
                        compositionList.remove(0)
                        this.toInclude[j] = Utilities.excludeProcessedProp(compositionList)
                        return true
                    }
                }
            }
        }
        return false
    }

    private boolean isComposition(String s) {
        return s.contains(".")
    }
    /**
     * A simple object is one that can be set directly as the value of a JSON property, examples include strings,
     * numbers, booleans, etc.
     *
     * @param propValue
     * @return
     */
    private boolean isSimpleProp(propValue) {
        // This is a bit simplistic as an object might very well be Serializable but have properties that we want
        // to render in JSON as a nested object. If we run into this issue, replace the test below with an test
        // for whether propValue is an instanceof Number, String, Boolean, Char, etc.
        !(propValue instanceof BaseEntity) && !(propValue instanceof Collection) // || propValue == null
    }

    private boolean isSimple(propValue) {
        // This is a bit simplistic as an object might very well be Serializable but have properties that we want
        // to render in JSON as a nested object. If we run into this issue, replace the test below with an test
        // for whether propValue is an instanceof Number, String, Boolean, Char, etc.
        propValue instanceof Serializable || propValue == null
    }
    // Level 0
    static JSONObject setJsonLightObjectResponse(Object object) {

        return new JSONObject(new JSONSerializer(object).getJSONLevel0())
    }

    static JSONArray setLightObjectListJsonResponse(List objectList) {
        JSONArray patientList = new JSONArray()

        for (object in objectList) {
            JSONObject jo = new JSONObject(new JSONSerializer(object).getJSONLevel0())
            patientList.add(jo)
        }

        return patientList
    }

    // Level 1
    static JSONObject setJsonObjectResponse(Object object) {

        return new JSONObject(new JSONSerializer(object).getJSON())
    }
    static JSONArray setObjectListJsonResponse(List objectList) {
        JSONArray allObjectList = new JSONArray()

        for (object in objectList) {
            JSONObject jo = new JSONObject(new JSONSerializer(object).getJSON())
            allObjectList.add(jo)
        }
        return allObjectList
    }
    // Level 2
    static JSONObject setJsonObjectResponseLevel2(Object object) {

        return new JSONObject(new JSONSerializer(object).getJSON())
    }
    static JSONArray setObjectListJsonResponseLevel2(List objectList) {
        JSONArray allObjectList = new JSONArray()

        for (object in objectList) {
            JSONObject jo = new JSONObject(new JSONSerializer(object).getJSONLevel2())
            allObjectList.add(jo)
        }
        return allObjectList
    }

    // Level 3
    static JSONObject setJsonObjectResponseLevel3(Object object) {

        return new JSONObject(new JSONSerializer(object).getJSONLevel3())
    }
    static JSONArray setObjectListJsonResponseLevel3(List objectList) {
        JSONArray allObjectList = new JSONArray()

        for (object in objectList) {
            JSONObject jo = new JSONObject(new JSONSerializer(object).getJSONLevel3())
            allObjectList.add(jo)
        }

        return allObjectList
    }
}
