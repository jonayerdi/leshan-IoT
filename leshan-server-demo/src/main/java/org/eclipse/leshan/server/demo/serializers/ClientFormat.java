package org.eclipse.leshan.server.demo.serializers;

/**
 * Created by Jon Ayerdi on 13/01/2017.
 */
public class ClientFormat {
    String endpoint;
    public static ClientFormat create(String endpoint) {
        ClientFormat c = new ClientFormat();
        c.endpoint = endpoint;
        return c;
    }
}
