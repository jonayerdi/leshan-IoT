package org.eclipse.leshan.server.demo.serializers;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by Jon Ayerdi on 17/01/2017.
 */
public class UserDeserializer implements JsonDeserializer<HashMap> {

    @Override
    public HashMap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray json = jsonElement.getAsJsonArray();
        HashMap<String,String> ret = new HashMap<>();
        for(int i = 0 ; i < json.size() ; i++) {
            JsonObject o = json.get(i).getAsJsonObject();
            ret.put(o.get("UserID").getAsString(),o.get("Password").getAsString());
        }
        return ret;
    }
}
