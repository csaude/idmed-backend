package mz.org.fgh.sifmoz.backend.healthInformationSystem



interface ISystemConfigsService {

    SystemConfigs get(Serializable id)

    List<SystemConfigs> list(Map args)

    Long count()

    SystemConfigs delete(Serializable id)

    SystemConfigs save(SystemConfigs systemConfigs)

   SystemConfigs getByKey(String key)

}
