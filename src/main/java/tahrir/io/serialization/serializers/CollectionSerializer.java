package tahrir.io.serialization.serializers;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.Collection;

import tahrir.io.serialization.*;

public class CollectionSerializer extends TahrirSerializer {

	public CollectionSerializer() {
		super(Collection.class);
	}

	@Override
	protected Object deserialize(final Type type_, final ByteBuffer bb)
	throws TahrirSerializableException {
		final ParameterizedType type = (ParameterizedType) type_;
		final int size = bb.getInt();
		try {
			final Collection<Object> collection = ((Class<Collection<Object>>) type.getRawType())
			.newInstance();
			final Class<?> elementType = (Class<?>) type.getActualTypeArguments()[0];
			for (int x = 0; x < size; x++) {
				final Object element = deserializeFrom(elementType, bb);
				collection.add(element);
			}
			return collection;
		} catch (final Exception e) {
			throw new TahrirSerializableException(e);
		}
	}

	@Override
	protected void serialize(final Type type, final Object object,
			final ByteBuffer bb)
	throws TahrirSerializableException {
		final Collection<?> collection = (Collection<?>) object;
		bb.putInt(collection.size());
		for (final Object element : collection) {
			serializeTo(element, bb);
		}
	}

}
