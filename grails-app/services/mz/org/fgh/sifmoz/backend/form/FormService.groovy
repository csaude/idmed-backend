package mz.org.fgh.sifmoz.backend.form

import grails.gorm.services.Service

@Service(Form)
interface FormService {

    Form get(Serializable id)

    List<Form> list(Map args)

    Long count()

    Form delete(Serializable id)

    Form save(Form form)

}
