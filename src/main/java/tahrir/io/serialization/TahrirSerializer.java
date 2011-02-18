package tahrir.io.serialization;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.Map;

import tahrir.io.serialization.serializers.*;

import com.google.common.collect.Maps;

public abstract class TahrirSerializer {
	public static void main(final String[] args) throws Exception {

		final Object testObject = new TestObject();

		System.out.println(((Class<?>) TestObject.class.getField("intArrayValue").getGenericType()).getComponentType());

		final ByteBuffer bb = ByteBuffer.allocate(1024);


		serializeTo(testObject, bb);

		final TestObject testObject2 = deserializeFrom(TestObject.class, bb);

		System.out.println(testObject2);
	}

	public static class TestObject {
		public int[] intArrayValue = new int[] { 1, 2, 4 };
	}

	protected final Type type;

	private static Map<Type, TahrirSerializer> serializers;

	static {
		serializers = Maps.newHashMap();
		registerSerializer(new IntegerSerializer(), Integer.class, Integer.TYPE);
		registerSerializer(new BooleanSerializer(), Boolean.class, Boolean.TYPE);
		registerSerializer(new ByteSerializer(), Byte.class, Byte.TYPE);
		registerSerializer(new CharSerializer(), Character.class, Character.TYPE);
		registerSerializer(new DoubleSerializer(), Double.class, Double.TYPE);
		registerSerializer(new FloatSerializer(), Float.class, Float.TYPE);
		registerSerializer(new LongSerializer(), Long.class, Long.TYPE);
		registerSerializer(new ShortSerializer(), Short.class, Short.TYPE);
		registerSerializer(new StringSerializer(), String.class);
	}

	private static final Map<Class<?>, Map<Integer, Field>> fieldMap = Maps.newHashMap();

	public static <T> void registerSerializer(final TahrirSerializer serializer, final Type... types) {
		for (final Type type : types) {
			final TahrirSerializer put = serializers.put(type, serializer);
			if (put != null)
				throw new RuntimeException("Tried to register serializer for "+type+" twice");
		}
	}

	protected TahrirSerializer(final Type type) {
		this.type = type;
	}

	public static void serializeTo(final Object object, final ByteBuffer bb) throws TahrirSerializableException {

		try {
			final Field[] fields = object.getClass().getFields();
			if (fields.length > 127)
				throw new TahrirSerializableException("Cannot serialize objects with more than 127 fields");
			bb.put((byte) fields.length);
			for (final Field field : fields) {
				bb.putInt(field.getName().hashCode());

				final Class<?> fieldType = field.getType();
				final Object fieldObject = field.get(object);

				if (fieldType.isArray()) {
					final int length = Array.getLength(fieldObject);
					bb.putInt(length);
					for (int x = 0; x < length; x++) {
						serializeTo(Array.get(fieldObject, x), bb);
					}

				} else {
					final TahrirSerializer fieldSerializer = serializers.get(field.getType());
					if (fieldSerializer != null) {
						fieldSerializer.serialize(fieldType, fieldObject, bb);
					} else {
						serializeTo(fieldObject, bb);
					}
				}
			}
		} catch (final Exception e) {
			throw new TahrirSerializableException(e);
		}
	}

	public static <T> T deserializeFrom(final Class<T> c, final ByteBuffer bb) throws TahrirSerializableException {
		try {
			Map<Integer, Field> fMap = fieldMap.get(c);
			if (fMap == null) {
				fMap = Maps.newHashMap();
				final Field[] fields = c.getFields();
				for (final Field field : fields) {
					final Field old = fMap.put(field.getName().hashCode(), field);
					if (old != null) // This is laughably unlikely
						throw new RuntimeException("Field "+field.getName()+" of "+c.getName()+" has the same hashCode() as field "+old.getName()+", one of them MUST be renamed");
				}
				fieldMap.put(c, fMap);
			}
			final T returnObject = c.newInstance();
			final int fieldCount = bb.get();
			for (int fix = 0; fix < fieldCount; fix++) {
				final int fieldHash = bb.getInt();
				final Field field = fMap.get(fieldHash);
				if (field == null)
					throw new TahrirSerializableException("Unrecognized fieldHash: " + fieldHash);
				if (field.getType().isArray()) {
					final int arrayLen = bb.getInt();
					final Object array = Array.newInstance(field.getType().getComponentType(), arrayLen);
					for (int x = 0; x < arrayLen; x++) {
						Array.set(array, x, deserializeFrom(field.getType().getComponentType(), bb));
					}
					field.set(returnObject, array);
				} else {
					final TahrirSerializer serializer = serializers.get(field.getType());
					field.set(returnObject, serializer.deserialize(field.getType(), bb));
				}
			}
			return returnObject;
		} catch (final Exception e) {
			throw new TahrirSerializableException(e);
		}
	}

	// This code is broken
	// -------------------
	// public static void writeLong(final ByteBuffer bb, long value) {
	// while (value < 0 || value > 127) {
	// bb.put((byte) (0x80 | (value & 0x7F)));
	// value = value >>> 7;
	// }
	// bb.put((byte) value);
	// }
	//
	// public static long readLong(final ByteBuffer bb) throws
	// TahrirSerializableException {
	// int shift = 0;
	// long value = 0;
	// while (true) {
	// final int b = bb.get();
	// if (b < 0) {
	// break;
	// }
	// value = value + (b & 0x7f) << shift;
	// shift += 7;
	// if ((b & 0x80) != 0)
	// return value;
	// }
	// throw new TahrirSerializableException("Malformed stop-bit encoding");
	// }

	protected abstract Object deserialize(Type type, ByteBuffer bb);

	protected abstract void serialize(Type type, Object object, ByteBuffer bb);
}
