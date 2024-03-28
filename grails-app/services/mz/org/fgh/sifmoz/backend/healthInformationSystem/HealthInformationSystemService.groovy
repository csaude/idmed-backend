package mz.org.fgh.sifmoz.backend.healthInformationSystem

import grails.converters.JSON
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.interoperabilityAttribute.InteroperabilityAttribute
import org.grails.web.json.JSONObject
import org.springframework.validation.BindingResult

import static org.springframework.http.HttpStatus.NOT_FOUND


@Transactional
@Service(HealthInformationSystem)
abstract class HealthInformationSystemService implements IHealthInformationSystemService {

    @Override
    HealthInformationSystem helperUpdate(JSONObject objectJSON) {
        def removedAttributes
        def addedAttributes = new ArrayList<InteroperabilityAttribute>();

        if (objectJSON.id) {
            HealthInformationSystem healthInformationSystem = HealthInformationSystem.get(objectJSON.id)

            healthInformationSystem.properties = objectJSON as BindingResult

            healthInformationSystem.interoperabilityAttributes = [].withDefault { new InteroperabilityAttribute() }

            List<InteroperabilityAttribute> interoperabilityAttributesNew = new ArrayList<>()

            (objectJSON.interoperabilityAttributes as List).collect { item ->
                if (item) {
               //     def interoperabilityAttributeObject = item as InteroperabilityAttribute
             //       interoperabilityAttributeObject.id = item.id
                    def interoperabilityAttribute = InteroperabilityAttribute.get(item.id)
                    if (interoperabilityAttribute == null) {
                        interoperabilityAttribute = new InteroperabilityAttribute()
                        interoperabilityAttribute.id = item.id
                        addedAttributes.add(interoperabilityAttribute)
                    }
                    interoperabilityAttribute.interoperabilityType = interoperabilityAttribute.interoperabilityType
                    interoperabilityAttribute.value = item.value
                    interoperabilityAttribute.healthInformationSystem = healthInformationSystem
                    interoperabilityAttribute.validate()
                    InteroperabilityAttribute.withTransaction {
                        interoperabilityAttribute.save(flush:true)
                    }
                    interoperabilityAttributesNew.add(interoperabilityAttribute)
                }
            }
            removedAttributes = HealthInformationSystem.get(objectJSON.id).interoperabilityAttributes.minus(interoperabilityAttributesNew)
       //     addedAttributes = interoperabilityAttributesNew.minus(HealthInformationSystem.get(objectJSON.id).interoperabilityAttributes)

            healthInformationSystem.interoperabilityAttributes = interoperabilityAttributesNew
            HealthInformationSystem.withTransaction {
                removedAttributes.each { item ->
                    item.delete(flush: true)
                }
        //        addedAttributes.each { addedAtribute ->
        //            addedAtribute.save()
        //        }
                healthInformationSystem.save(flush: true)
            }
            return healthInformationSystem
        }

        }

    }



