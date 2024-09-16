package mz.org.fgh.sifmoz.backend.patientVisitDetails

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.protection.Menu

class PatientVisitDetails extends BaseEntity {

    String id
    Episode episode
    Clinic clinic
    Prescription prescription
    Pack pack
    PatientVisit patientVisit
    Date creationDate = new Date()
    String origin

    static belongsTo = [PatientVisit]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_patientVisitDetails_Idx'
    }
    static constraints = {
        pack nullable: false
        creationDate nullable: true
        origin nullable: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
        if (!clinic) {
            clinic = Clinic.findByMainClinic(true)
        }
    }


    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode))
        }
        return menus
    }
}
