package mz.org.fgh.sifmoz.backend.drugDistributor

interface IDrugDistributorService {

    DrugDistributor get(Serializable id)

    List<DrugDistributor> list(Map args)

    Long count()

    DrugDistributor delete(Serializable id)

    DrugDistributor save(DrugDistributor drugDistributor)

    List<DrugDistributor> getAllByClinicId(String clinicId, int offset, int max)


}
