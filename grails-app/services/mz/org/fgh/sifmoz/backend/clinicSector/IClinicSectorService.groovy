package mz.org.fgh.sifmoz.backend.clinicSector

import grails.gorm.services.Service


interface IClinicSectorService {

    ClinicSector get(Serializable id)

    List<ClinicSector> list(Map args)

    Long count()

    ClinicSector delete(Serializable id)

    ClinicSector save(ClinicSector clinicSector)

    List<ClinicSector> getAllByClinicId(String clinicId, int offset, int max)

}
