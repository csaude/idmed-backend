package mz.org.fgh.sifmoz.backend.packagedDrug

interface IPackagedDrugService {

    PackagedDrug get(Serializable id)

    List<PackagedDrug> list(Map args)

    Long count()

    PackagedDrug delete(Serializable id)

    PackagedDrug save(PackagedDrug packagedDrug)

    List<PackagedDrug> getAllByPackId(String packId)

}
