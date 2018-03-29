package org.globsframework.json;

import com.google.gson.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.GlobTypeBuilder;
import org.globsframework.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.model.Glob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class GlobTypeGsonDeserializer {
    private static Logger LOGGER = LoggerFactory.getLogger(GlobGsonDeserializer.class);
    private final GlobGsonDeserializer globGsonDeserializer;

    GlobTypeGsonDeserializer(GlobGsonDeserializer globGsonDeserializer) {
        this.globGsonDeserializer = globGsonDeserializer;
    }

    GlobType deserialize(JsonElement json) throws JsonParseException {
        if (json == null || json instanceof JsonNull) {
            return null;
        }
        try {
            JsonObject jsonObject = (JsonObject) json;
            JsonElement typeElement = jsonObject.get(GlobsGson.TYPE_NAME);
            if (typeElement == null) {
                throw new RuntimeException("Missing " + GlobsGson.TYPE_NAME + " missing");
            }
            String name = typeElement.getAsString();
            GlobTypeBuilder globTypeBuilder = DefaultGlobTypeBuilder.init(name);
            JsonElement fields = jsonObject.get(GlobsGson.FIELDS);
            if (fields != null) {
                if (fields instanceof JsonObject) {
                    for (Map.Entry<String, JsonElement> entry : ((JsonObject) fields).entrySet()) {
                        readField(globTypeBuilder, entry);
                    }
                }
            }
            List<Glob> globAnnotations = readAnnotations(jsonObject);
            for (Glob globAnnotation : globAnnotations) {
                globTypeBuilder.addAnnotation(globAnnotation);
            }
            return globTypeBuilder.get();
        } catch (JsonParseException e) {
            Gson gson = new Gson();
            LOGGER.error("Fail to parse : " + gson.toJson(json));
            throw e;
        }
    }

    private void readField(GlobTypeBuilder globTypeBuilder, Map.Entry<String, JsonElement> entry) {
        String attrName = entry.getKey();
        JsonObject fieldContent = (JsonObject) entry.getValue();
        String type = fieldContent.get(GlobsGson.FIELD_TYPE).getAsString();
        List<Glob> globList = readAnnotations(fieldContent);
        switch (type) {
            case GlobsGson.INT_TYPE:
                globTypeBuilder.declareIntegerField(attrName, globList);
                break;
            case GlobsGson.INT_ARRAY_TYPE:
                globTypeBuilder.declareIntegerArrayField(attrName, globList);
                break;
            case GlobsGson.DOUBLE_TYPE:
                globTypeBuilder.declareDoubleField(attrName, globList);
                break;
            case GlobsGson.DOUBLE_ARRAY_TYPE:
                globTypeBuilder.declareDoubleArrayField(attrName, globList);
                break;
            case GlobsGson.STRING_TYPE:
                globTypeBuilder.declareStringField(attrName, globList);
                break;
            case GlobsGson.STRING_ARRAY_TYPE:
                globTypeBuilder.declareStringArrayField(attrName, globList);
                break;
            case GlobsGson.BOOLEAN_TYPE:
                globTypeBuilder.declareBooleanField(attrName, globList);
                break;
            case GlobsGson.BOOLEAN_ARRAY_TYPE:
                globTypeBuilder.declareBooleanArrayField(attrName, globList);
                break;
            case GlobsGson.LONG_TYPE:
                globTypeBuilder.declareLongField(attrName, globList);
                break;
            case GlobsGson.LONG_ARRAY_TYPE:
                globTypeBuilder.declareArrayLongField(attrName, globList);
                break;
            case GlobsGson.BIG_DECIMAL_TYPE:
                globTypeBuilder.declareBigDecimalField(attrName, globList);
                break;
            case GlobsGson.BIG_DECIMAL_ARRAY_TYPE:
                globTypeBuilder.declareBigDecimalArrayField(attrName, globList);
                break;
            case GlobsGson.DATE_TYPE:
                globTypeBuilder.declareDateField(attrName, globList);
                break;
            case GlobsGson.DATE_TIME_TYPE:
                globTypeBuilder.declareDateTimeField(attrName, globList);
                break;
            case GlobsGson.BLOB_TYPE:
                globTypeBuilder.declareBlobField(attrName, globList);
                break;
            default:
                throw new RuntimeException(type + " not managed");
        }
    }

    private List<Glob> readAnnotations(JsonObject fieldContent) {
        JsonArray annotations = fieldContent.getAsJsonArray(GlobsGson.ANNOTATIONS);
        List<Glob> globList = Collections.emptyList();
        if (annotations != null) {
            globList = new ArrayList<>();
            for (JsonElement annotation : annotations) {
                if (annotation != null) {
                    globList.add(globGsonDeserializer.deserialize(annotation));
                }
            }
        }
        return globList;
    }
}
