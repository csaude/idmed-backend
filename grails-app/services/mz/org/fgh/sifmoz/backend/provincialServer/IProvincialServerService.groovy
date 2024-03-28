package mz.org.fgh.sifmoz.backend.provincialServer


interface IProvincialServerService {

    ProvincialServer get(Serializable id)

    List<ProvincialServer> list(Map args)

    Long count()

    ProvincialServer delete(Serializable id)

    ProvincialServer save(ProvincialServer provincialServer)

    ProvincialServer getByCodeAndDestination(String code, String destination)

}
