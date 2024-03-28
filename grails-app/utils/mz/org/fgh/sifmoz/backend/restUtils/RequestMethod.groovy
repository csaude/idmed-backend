package mz.org.fgh.sifmoz.backend.restUtils

enum RequestMethod {
    POST('POST'),
    PATCH('PATCH'),
    PUT('PUT'),
    GET('GET')

    String id
     RequestMethod(String id) {
        this.id = id
    }
}
