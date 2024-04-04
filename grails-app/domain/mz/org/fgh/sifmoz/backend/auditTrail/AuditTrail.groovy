package mz.org.fgh.sifmoz.backend.auditTrail

class AuditTrail implements Serializable {
    private static final long serialVersionUID = 1L

    // Enable when you configured UUID(2) kind of ids in "auditLog.idMapping" in < 1.1.0 plugin version
    //String id

    static auditable = false

    Date dateCreated
    Date lastUpdated

    String actor
    String uri
    String className
    String persistedObjectId
    Long persistedObjectVersion = 0

    String eventName
    String propertyName
    String oldValue
    String newValue

    static constraints = {
        actor(nullable: true)
        uri(nullable: true)
        className(nullable: true)
        persistedObjectId(nullable: true)
        persistedObjectVersion(nullable: true)
        eventName(nullable: true)
        propertyName(nullable: true)

        //oldValue(nullable: true)
        //newValue(nullable: true)

        // for large column support (as in < 1.0.6 plugin versions), use
        oldValue(nullable: true, maxSize: 65534)
        newValue(nullable: true, maxSize: 65534)
    }

    static mapping = {

        // Set similiar when you used "auditLog.tablename" in < 1.1.0 plugin version.
        table 'audit_log'

        // Remove when you used "auditLog.cacheDisabled = true" in < 1.1.0 plugin version.
        cache usage: 'read-only', include: 'non-lazy'

        // Set similiar when you used "auditLog.useDatasource" in < 1.1.0 plugin version.
        // datasource "yourdatasource"

        // Set similiar when you used "auditLog.idMapping" in < 1.1.0 plugin version. Example:
        //id generator:"uuid2", type:"string", "length:36"

        // no HQL queries package name import (was default in < 1.0.x versions as well)
        autoImport false

        version false

        // default sorting example
        sort dateCreated: "desc"
    }

    /**
     * Deserializer that maps a stored map onto the object
     * assuming that the keys match attribute properties.
     */
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        def map = input.readObject()
        map.each { k, v -> this."\$k" = v }
    }

    /**
     * Because Closures do not serialize we can't send the constraints closure
     * to the Serialize API so we have to have a custom serializer to allow for
     * this object to show up inside a webFlow context.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        def map = [
                id: id,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,

                actor: actor,
                uri: uri,
                className: className,
                persistedObjectId: persistedObjectId,
                persistedObjectVersion: persistedObjectVersion,

                eventName: eventName,
                propertyName: propertyName,
                oldValue: oldValue,
                newValue: newValue,
        ]
        out.writeObject(map)
    }

    String toString() {
        String actorStr = actor ? "user \${actor}" : "user ?"
        "audit log \${dateCreated} \${actorStr} " +
                "\${eventName} \${className} " +
                "id:\${persistedObjectId} version:\${persistedObjectVersion}"
    }
}