package mz.org.fgh.sifmoz.backend.utilities

import grails.web.*
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.nio.charset.StandardCharsets

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

    String getJSONLevel3() {
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

    private buildJSON = { obj ->
        setProperty("id", obj.id)
        obj.properties.each { propName, propValue ->
            if (!['class', 'metaClass'].contains(propName)) {

                if (isSimple(propValue)) {
                    // It seems "propName = propValue" doesn't work when propName is dynamic so we need to
                    // set the property on the builder using this syntax instead
                    setProperty(propName, propValue)
                } else {
                        Closure nestedObject = {
                            buildJSON(propValue)
                        }
                        setProperty(propName, nestedObject)
                    }

                }
            }
        }

    private buildJSONChild = { obj ->
        setProperty("id", obj.id)
        obj.properties.each { propName, propValue ->
            if (!['class', 'metaClass'].contains(propName)) {

                if (isSimpleProp(propValue)) {
                    // It seems "propName = propValue" doesn't work when propName is dynamic so we need to
                    // set the property on the builder using this syntax instead
                    setProperty(propName, propValue)
                } else {
                    if (propValue instanceof org.hibernate.collection.internal.PersistentSet) {
                        for (object in propValue) {
                            def serializedObject = new JSONObject(new JSONSerializer(object).getJSON())
                            Closure nestedObject = {
                                buildJSONChild(serializedObject)
                            }
                            setProperty(propName, nestedObject)
                        }
                    } else {
                        // propValue is a list of objects, so serialize each object in the list and add it to the JSON array
                        Closure nestedObject = {
                            buildJSONChild(propValue)
                        }
                        setProperty(propName, nestedObject)
                    }
                }

            }
        }
    }

    private buildJSONChildTest = { obj ->
        setProperty("id", obj.id)
        obj.properties.each { propName, propValue ->
            if (!['class', 'metaClass'].contains(propName)) {

                if (isSimpleProp(propValue)) {
                    // It seems "propName = propValue" doesn't work when propName is dynamic so we need to
                    // set the property on the builder using this syntax instead
                    setProperty(propName, propValue)
                } else {
                    println(propValue)
                    if (propValue instanceof Object) {
                        if (propValue instanceof org.hibernate.collection.internal.PersistentSet) {
                            JSONArray array = new JSONArray()
                            // ArrayList array = new ArrayList()
                            for (object in propValue) {
                                def serializedObject = new JSONObject(new JSONSerializer(object).getJSON())
                                array.add(serializedObject)
                                // Object nestedObject = {
                                //    buildJSON(object)
                                // }
                            }
                            setProperty(propName, array)
                        } else {
                            // create a nested JSON object and recursively call this function to serialize it
                            Closure nestedObject = {
                                buildJSONChild(propValue)
                            }
                            setProperty(propName, nestedObject)
                        }
                    } else {
                        // propValue is a list of objects, so serialize each object in the list and add it to the JSON array
                        JSONArray array = new JSONArray()
                        for (object in propValue) {
                            Closure nestedObject = {
                                buildJSONChild(object)
                            }
                            array.add(nestedObject)
                        }
                        setProperty(propName, array)
                    }
                }
            }
        }
    }

    private buildLightJSON = { obj ->
        setProperty("id", obj.id)
        obj.properties.each { propName, propValue ->
            if (!['class', 'metaClass'].contains(propName)) {

                if (isSimpleProp(propValue)) {
                    setProperty(propName, propValue)
                }
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

    static JSONArray setObjectListJsonResponse(List objectList) {
        JSONArray patientList = new JSONArray()

        for (object in objectList) {
            JSONObject jo = new JSONObject(new JSONSerializer(object).getJSON())
            patientList.add(jo)
        }

        return patientList
    }

    static JSONObject setJsonObjectResponse(Object object) {

        return new JSONObject(new JSONSerializer(object).getJSON())
    }

    static JSONObject setJsonObjectResponseLevel3(Object object) {

        return new JSONObject(new JSONSerializer(object).getJSONLevel3())
    }

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


}
