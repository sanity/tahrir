package tahrir.io.serialization.serializers;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.Map;

import tahrir.io.serialization.*;

public class MapSerializer extends TahrirSerializer {

	public MapSerializer() {
		super(Map.class);
	}

	@Override
	protected Object deserialize(final Type type_, final ByteBuffer bb) throws TahrirSerializableException {
		final ParameterizedType type = (ParameterizedType) type_;
		final int size = bb.getInt();
		try {
			final Map<Object, Object> map = ((Class<Map<Object, Object>>) type.getRawType()).newInstance();
			final Class<?> keyType = (Class<?>) type.getActualTypeArguments()[0];
			final Class<?> valueType = (Class<?>) type.getActualTypeArguments()[1];
			for (int x = 0; x < size; x++) {
				final Object key = deserializeFrom(keyType, bb);
				final Object value = deserializeFrom(valueType, bb);
				map.put(key, value);
			}
			return map;
		} catch (final Exception e) {
			throw new TahrirSerializableException(e);
		}
	}

	@Override
	protected void serialize(final Type type, final Object object, final ByteBuffer bb)
	throws TahrirSerializableException {
		final Map<?, ?> map = (Map<?, ?>) object;
		bb.putInt(map.size());
		for (final Map.Entry<?, ?> entry : map.entrySet()) {
			serializeTo(entry.getKey(), bb);
			serializeTo(entry.getValue(), bb);
		}
	}

}
