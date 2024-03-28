package mz.org.fgh.sifmoz.backend.healthInformationSystem

import grails.converters.JSON
import grails.gorm.services.Service
import org.grails.web.json.JSONObject


interface IHealthInformationSystemService {

    HealthInformationSystem get(Serializable id)

    List<HealthInformationSystem> list(Map args)

    Long count()

    HealthInformationSystem delete(Serializable id)

    HealthInformationSystem save(HealthInformationSystem healthInformationSystem)

    HealthInformationSystem helperUpdate(JSONObject objectJSON)
}
