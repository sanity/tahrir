package tahrir.io.serialization.serializers;

import com.google.common.base.Optional;
import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 02/09/13
 */
    public class GsonOptionalSerializer<T>
            implements JsonSerializer<Optional<T>>, JsonDeserializer<Optional<T>> {

        @Override
        public Optional<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            final JsonArray asJsonArray = json.getAsJsonArray();
            final JsonElement jsonElement = asJsonArray.get(0);
            final T value = context.deserialize(jsonElement, ((ParameterizedType) typeOfT).getActualTypeArguments()[0]);
            return Optional.fromNullable(value);
        }

        @Override
        public JsonElement serialize(Optional<T> src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonElement element = context.serialize(src.orNull());
            final JsonArray result = new JsonArray();
            result.add(element);
            return result;
        }
    }

