package org.eclipse.leshan.server.demo.serializers;

/**
 * Created by Jon Ayerdi on 13/01/2017.
 */
public class ClientFormat {
    String id;
    String endpoint;
    public static ClientFormat create(String id, String endpoint) {
        ClientFormat c = new ClientFormat();
        c.id = id;
        c.endpoint = endpoint;
        return c;
    }
}
