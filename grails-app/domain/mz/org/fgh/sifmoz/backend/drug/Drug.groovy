package mz.org.fgh.sifmoz.backend.drug

import com.fasterxml.jackson.annotation.JsonBackReference
import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.doctor.Doctor
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.form.Form
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen
import mz.org.fgh.sifmoz.backend.service.ClinicalService

class Drug extends BaseEntity {
    String id
    int packSize
    String name
    double defaultTreatment
    int defaultTimes
    String defaultPeriodTreatment
    String fnmCode
    String uuidOpenmrs
    String clinical_service_id
//    ClinicalService clinicalService
    Form form
    static belongsTo = [TherapeuticRegimen]
    boolean active
    static hasMany = [stockList: Stock, therapeuticRegimenList: TherapeuticRegimen]
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Drug_Idx'
    }
    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        name nullable: false, blank: false
        fnmCode nullable: false, unique: true
        packSize(min: 0)
        uuidOpenmrs nullable: true
        clinical_service_id nullable: true
        defaultTimes(min:1)
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,stockMenuCode,dashboardMenuCode,administrationMenuCode,homeMenuCode))
        }
        return menus
    }

}
