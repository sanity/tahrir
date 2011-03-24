package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.*;
import java.util.Map;

import tahrir.io.serialization.*;

public class MapSerializer extends TrSerializer {

	public MapSerializer() {
		super(Map.class);
	}

	@Override
	protected Object deserialize(final Type type_, final DataInputStream dis) throws TrSerializableException,
			IOException {
		final ParameterizedType type = (ParameterizedType) type_;
		final int size = dis.readInt();
		try {
			final Map<Object, Object> map = ((Class<Map<Object, Object>>) type.getRawType()).newInstance();
			final Class<?> keyType = (Class<?>) type.getActualTypeArguments()[0];
			final Class<?> valueType = (Class<?>) type.getActualTypeArguments()[1];
			for (int x = 0; x < size; x++) {
				final Object key = deserializeFrom(keyType, dis);
				final Object value = deserializeFrom(valueType, dis);
				map.put(key, value);
			}
			return map;
		} catch (final Exception e) {
			throw new TrSerializableException(e);
		}
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos)
			throws TrSerializableException, IOException {
		final Map<?, ?> map = (Map<?, ?>) object;
		dos.writeInt(map.size());
		for (final Map.Entry<?, ?> entry : map.entrySet()) {
			serializeTo(entry.getKey(), dos);
			serializeTo(entry.getValue(), dos);
		}
	}

}
