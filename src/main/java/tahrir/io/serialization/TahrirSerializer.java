package tahrir.io.serialization;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.Map;

import tahrir.io.serialization.TahrirSerializable.TahrirSerializableException;

import com.google.common.collect.Maps;

public abstract class TahrirSerializer {
	protected final Type type;

	private static Map<Type, TahrirSerializer> serializers;

	static {
		serializers = Maps.newHashMap();
	}

	private static final Map<Class<?>, Map<Integer, Field>> fieldMap = Maps.newHashMap();

	public static <T> void registerSerializer(final Type type, final TahrirSerializer serializer) {
		serializers.put(type, serializer);
	}

	protected TahrirSerializer(final Type type) {
		this.type = type;
	}

	public static void serializeTo(final Object object, final ByteBuffer bb) throws TahrirSerializableException {

		try {
			final Field[] fields = object.getClass().getFields();
			writeLong(bb, fields.length);
			for (final Field field : fields) {
				bb.putInt(field.getName().hashCode());
				final TahrirSerializer fieldSerializer = serializers.get(field.getType());
				if (fieldSerializer != null) {
					fieldSerializer.serialize(field.getType(), field.get(object), bb);
				} else {
					serializeTo(field.get(object), bb);
				}
			}
		} catch (final Exception e) {
			throw new TahrirSerializable.TahrirSerializableException(e);
		}
	}

	public static <T> T deserializeFrom(final Class<T> c, final ByteBuffer bb) throws TahrirSerializableException {
		try {
			Map<Integer, Field> fMap = fieldMap.get(c);
			if (fMap == null) {
				fMap = Maps.newHashMap();
				for (final Field field : c.getFields()) {
					final Field old = fMap.put(field.getName().hashCode(), field);
					if (old != null) // This is laughably unlikely
						throw new RuntimeException("Field "+field.getName()+" of "+c.getName()+" has the same hashCode() as field "+old.getName()+", one of them MUST be renamed");
				}
				fieldMap.put(c, fMap);
			}
			final T returnObject = c.newInstance();
			final long fieldCount = readLong(bb);
			for (int fix = 0; fix < fieldCount; fix++) {
				final int fieldHash = bb.getInt();
				final Field field = fMap.get(fieldHash);
				if (field == null)
					throw new TahrirSerializableException("Unrecognized fieldHash: " + fieldHash);
				final TahrirSerializer serializer = serializers.get(field.getType());
				field.set(returnObject, serializer.deserialize(field.getType(), bb));
			}
			return returnObject;
		} catch (final Exception e) {
			throw new TahrirSerializableException(e);
		}
	}

	public static void writeLong(final ByteBuffer bb, long value) {
		while (value < 0 || value > 127) {
			bb.put((byte) (0x80 | (value & 0x7F)));
			value = value >>> 7;
		}
		bb.put((byte) value);
	}

	public static long readLong(final ByteBuffer bb) throws TahrirSerializableException {
		int shift = 0;
		int b;
		long value = 0;
		while ((b = bb.getInt()) >= 0) {
			value = value + (b & 0x7f) << shift;
			shift += 7;
			if ((b & 0x80) != 0)
				return value;
		}
		throw new TahrirSerializableException("Malformed stop-bit encoding");
	}

	protected abstract Object deserialize(Type type, ByteBuffer bb);

	protected abstract void serialize(Type type, Object object, ByteBuffer bb);
}
