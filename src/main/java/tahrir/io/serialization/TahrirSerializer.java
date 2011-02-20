package tahrir.io.serialization;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;

import tahrir.io.serialization.serializers.*;

import com.google.common.collect.Maps;
import com.google.inject.internal.Sets;

public abstract class TahrirSerializer {

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
		registerSerializer(new CollectionSerializer(), Collection.class);
		registerSerializer(new MapSerializer(), Map.class);
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

	public static TahrirSerializer getSerializerForType(final Class<?> type) {
		if (type == null || type.equals(Object.class))
			return null;
		final TahrirSerializer fieldSerializer = serializers.get(type);
		if (fieldSerializer != null) return fieldSerializer;
		for (final Class<?> iface : type.getInterfaces()) {
			final TahrirSerializer ifaceFS = getSerializerForType(iface);
			if (ifaceFS != null)
				return ifaceFS;
		}
		return getSerializerForType(type.getSuperclass());

	}

	private static Set<Field> getAllFields(final Class<?> c) {
		if (c.equals(Object.class))
			return Collections.emptySet();
		final HashSet<Field> ret = Sets.newHashSet();
		for (final Field f : c.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
				// Don't serialize static fields
				continue;
			}
			f.setAccessible(true);
			ret.add(f);
		}
		ret.addAll(getAllFields(c.getSuperclass()));
		return ret;
	}

	public static void serializeTo(final Object object, final ByteBuffer bb) throws TahrirSerializableException {
		// See if we can serialize directly
		final TahrirSerializer ts = getSerializerForType(object.getClass());
		if (ts != null) {
			ts.serialize(object.getClass(), object, bb);
		} else {

			try {
				final Set<Field> fields = getAllFields(object.getClass());
				if (fields.size() > 127)
					throw new TahrirSerializableException("Cannot serialize objects with more than 127 fields");
				byte nonNullFieldCount = 0;
				for (final Field field : fields) {
					if (field.get(object) != null) {
						nonNullFieldCount++;
					}
				}
				bb.put(nonNullFieldCount);
				for (final Field field : fields) {
					field.setAccessible(true);
					final Class<?> fieldType = field.getType();
					final Object fieldObject = field.get(object);

					if (fieldObject == null) {
						continue;
					}

					bb.putInt(field.getName().hashCode());

					if (fieldType.isArray()) {
						final int length = Array.getLength(fieldObject);
						bb.putInt(length);
						for (int x = 0; x < length; x++) {
							serializeTo(Array.get(fieldObject, x), bb);
						}

					} else {
						final TahrirSerializer fieldSerializer = getSerializerForType(field.getType());
						if (fieldSerializer != null) {
							fieldSerializer.serialize(field.getGenericType(), fieldObject, bb);
						} else {
							if (field.getGenericType() instanceof ParameterizedType)
								throw new TahrirSerializableException(
								"If you want to serialize a generic type you must register a TahrirSerializer for it");
							serializeTo(fieldObject, bb);
						}


					}
				}
			} catch (final Exception e) {
				throw new TahrirSerializableException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserializeFrom(final Class<T> c, final ByteBuffer bb) throws TahrirSerializableException {
		final TahrirSerializer ts = getSerializerForType(c);
		if (ts != null)
			return (T) ts.deserialize(c, bb);
		else {
			try {
				Map<Integer, Field> fMap = fieldMap.get(c);
				if (fMap == null) {
					fMap = Maps.newHashMap();
					final Set<Field> fields = getAllFields(c);
					for (final Field field : fields) {
						field.setAccessible(true);
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
						final TahrirSerializer serializer = getSerializerForType(field.getType());
						if (serializer != null) {
							field.set(returnObject, serializer.deserialize(field.getGenericType(), bb));
						} else {
							field.set(returnObject, deserializeFrom(field.getType(), bb));
						}
					}
				}
				return returnObject;
			} catch (final Exception e) {
				throw new TahrirSerializableException(e);
			}
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

	protected abstract Object deserialize(Type type, ByteBuffer bb)
	throws TahrirSerializableException;

	protected abstract void serialize(Type type, Object object, ByteBuffer bb)
	throws TahrirSerializableException;
}
