package tahrir.io.serialization.serializers;

import tahrir.io.serialization.TrSerializableException;
import tahrir.io.serialization.TrSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

public class SetSerializer extends TrSerializer {

	public SetSerializer() {
		super(Set.class);
	}

	@Override
	protected Object deserialize(final Type type_, final DataInputStream dis) throws TrSerializableException,
			IOException {
		final ParameterizedType type = (ParameterizedType) type_;
		final int size = dis.readInt();
		try {
			final Set<Object> set = ((Class<Set<Object>>) type.getRawType()).newInstance();
			final Class<?> sType = (Class<?>) type.getActualTypeArguments()[0];
			for (int x = 0; x < size; x++) {
				final Object o = deserializeFrom(sType, dis);
				set.add(o);
			}
			return set;
		} catch (final Exception e) {
			throw new TrSerializableException(e);
		}
	}

	@Override
	protected void serialize(final Type type, final Object object, final DataOutputStream dos)
			throws TrSerializableException, IOException {
		final Set<?> set = (Set<?>) object;
		dos.writeInt(set.size());
		for (Object o : set) {
			serializeTo(o, dos);
		}
	}

}
