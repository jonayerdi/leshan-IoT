package org.eclipse.leshan.server.demo.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by Jon Ayerdi on 13/01/2017.
 */
public class LightClientSerializer implements JsonSerializer<ClientFormat> {

    public JsonElement serialize(ClientFormat src, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject element = new JsonObject();

        element.addProperty("id", src.id);
        element.addProperty("endpoint", src.endpoint);

        return element;
    }
}
