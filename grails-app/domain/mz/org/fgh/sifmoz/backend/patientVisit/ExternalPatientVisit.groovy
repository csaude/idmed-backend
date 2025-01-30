package mz.org.fgh.sifmoz.backend.patientVisit

class ExternalPatientVisit {

    String id
    String patientId
    String patientName
    String patientGender
    Date patientDateOfBirth
    String patientCellphone
    String nid
    String jsonObject
    char syncStatus
    String sourceProvinceId
    String sourceProvinceName
    String sourceDistrictId
    String sourceDistrictName
    String sourceClinicId
    String sourceClinicName
    String targetProvinceId
    String targetProvinceName
    String targetDistrictId
    String targetDistrictName
    String targetClinicId
    String targetClinicName
    Date dateCreated

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_external_pv_Idx'
        nid column: 'nid', index: 'index_nid_Idx'
        patientId column: 'patientId', index: 'index_patient_id_Idx'
    }

    static constraints = {
        patientName nullable: true
        patientGender nullable: true
        patientDateOfBirth nullable: true
        patientCellphone nullable: true
        syncStatus nullable: true
        sourceProvinceName nullable: true
        sourceDistrictId nullable: true
        sourceDistrictName nullable: true
        sourceClinicId nullable: true
        sourceClinicName nullable: true
        targetProvinceId nullable: true
        targetDistrictId nullable: true
        targetProvinceName nullable: true
        targetDistrictName nullable: true
        targetClinicId nullable: true
        targetClinicName nullable: true
        jsonObject(nullable: true,maxSize: 20000)
    }

    def beforeInsert() {
        dateCreated = new Date()
        if (!id) {
            id = UUID.randomUUID()
        }
    }
}