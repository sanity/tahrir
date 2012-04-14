package tahrir.io.serialization.serializers;

import java.io.*;
import java.lang.reflect.*;
import java.util.Collection;

import tahrir.io.serialization.*;

public class CollectionSerializer extends TrSerializer {

	public CollectionSerializer() {
		super(Collection.class);
	}

	@Override
	protected Object deserialize(final Type type_, final DataInputStream dis) throws TrSerializableException,
	IOException {
		final ParameterizedType type = (ParameterizedType) type_;
		final int size = dis.readInt();
		try {
			final Collection<Object> collection = ((Class<Collection<Object>>) type.getRawType())
					.newInstance();
			final Class<?> elementType = (Class<?>) type.getActualTypeArguments()[0];
			for (int x = 0; x < size; x++) {
				final Object element = deserializeFrom(elementType, dis);
				collection.add(element);
			}
			return collection;
		} catch (final Exception e) {
			throw new TrSerializableException(e);
		}
	}

	@Override
	protected void serialize(final Type type, final Object object,
			final DataOutputStream dos)
					throws TrSerializableException, IOException {
		final Collection<?> collection = (Collection<?>) object;
		dos.writeInt(collection.size());
		for (final Object element : collection) {
			serializeTo(element, dos);
		}
	}

}
