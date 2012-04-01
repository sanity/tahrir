package tahrir.io.serialization;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.io.crypto.TrCrypto;
import tahrir.io.net.udpV1.UdpRemoteAddress;
import tahrir.peerManager.TrPeerManager.TrPeerInfo;

import com.google.inject.internal.*;

public class SerializationTest {
	@Test
	public void primitiveTypesTest() throws Exception {
		final PrimitiveTypes pt = new PrimitiveTypes();
		pt.b = 1;
		pt.s = 2;
		pt.c = 'a';
		pt.i = 42;
		pt.l = 534;
		pt.f = 0.33f;
		pt.d = 0.3;
		pt.bool = true;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		final DataOutputStream dos = new DataOutputStream(baos);
		TrSerializer.serializeTo(pt, dos);
		System.out.format("Primitive types serialized to %d bytes, compared to %d bytes for stock serialization.%n",
				baos.size(), testNormalJavaSerialization(pt));
		final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		final PrimitiveTypes pt2 = TrSerializer.deserializeFrom(PrimitiveTypes.class, dis);
		Assert.assertEquals(pt, pt2);
	}

	public static int testNormalJavaSerialization(final Serializable object) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		final ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(object);

		oos.flush();

		return baos.toByteArray().length;
	}

	@Test
	public void objectTypeTest() throws Exception {
		final ObjectTypes ot = new ObjectTypes();
		ot.subObj = new ObjectTypes.SubObjectType();
		ot.subObj.i = 33;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		final DataOutputStream dos = new DataOutputStream(baos);
		TrSerializer.serializeTo(ot, dos);
		System.out.format("Object types serialized to %d bytes, compared to %d bytes for stock serialization.%n",
				baos.size(), testNormalJavaSerialization(ot));
		final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		final ObjectTypes ot2 = TrSerializer.deserializeFrom(ObjectTypes.class, dis);
		Assert.assertNull(ot2.nullTest);
		Assert.assertEquals(ot2.subObj.i, ot.subObj.i);
	}

	@Test
	public void trPeerInfoTest() throws Exception {
		final TrPeerInfo ot = new TrPeerInfo(new UdpRemoteAddress(InetAddress.getLocalHost(), 1234), TrCrypto.createRsaKeyPair().a);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		final DataOutputStream dos = new DataOutputStream(baos);
		TrSerializer.serializeTo(ot, dos);
		final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		final TrPeerInfo ot2 = TrSerializer.deserializeFrom(TrPeerInfo.class, dis);
	}

	public static class ObjectTypes implements Serializable {
		private static final long serialVersionUID = -4822659189341304905L;

		SubObjectType subObj;

		Object nullTest = null;

		public static class SubObjectType implements Serializable {
			private static final long serialVersionUID = -8224513766509229887L;
			int i;
		}
	}

	@Test
	public void collectionsTypesTest() throws Exception {
		final CollectionsTypes ct = new CollectionsTypes();
		ct.hashMap = Maps.newHashMap();
		ct.hashMap.put("one", 1);
		ct.hashMap.put("two", 2);

		ct.hashSet = Sets.newHashSet();
		ct.hashSet.add("one");
		ct.hashSet.add("two");
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		final DataOutputStream dos = new DataOutputStream(baos);
		TrSerializer.serializeTo(ct, dos);
		System.out.format("Collections types serialized to %d bytes, compared to %d bytes for stock serialization.%n",
				baos.size(), testNormalJavaSerialization(ct));
		final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		final CollectionsTypes ct2 = TrSerializer.deserializeFrom(CollectionsTypes.class, dis);
		Assert.assertEquals(ct, ct2);
	}

	@SuppressWarnings("serial")
	public static class CollectionsTypes implements Serializable {
		public HashMap<String, Integer> hashMap;
		public HashSet<String> hashSet;

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof CollectionsTypes))
				return false;
			final CollectionsTypes other = (CollectionsTypes) obj;
			if (hashMap == null) {
				if (other.hashMap != null)
					return false;
			} else if (!hashMap.equals(other.hashMap))
				return false;
			if (hashSet == null) {
				if (other.hashSet != null)
					return false;
			} else if (!hashSet.equals(other.hashSet))
				return false;
			return true;
		}
	}

	public static class PrimitiveTypes implements Serializable {
		private static final long serialVersionUID = -5992856042046042767L;
		public byte b;
		public short s;
		public char c;
		public int i;
		public long l;
		private float f;
		public double d;
		public boolean bool;

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof PrimitiveTypes))
				return false;
			final PrimitiveTypes other = (PrimitiveTypes) obj;
			if (b != other.b)
				return false;
			if (bool != other.bool)
				return false;
			if (c != other.c)
				return false;
			if (Double.doubleToLongBits(d) != Double.doubleToLongBits(other.d))
				return false;
			if (Float.floatToIntBits(f) != Float.floatToIntBits(other.f))
				return false;
			if (i != other.i)
				return false;
			if (l != other.l)
				return false;
			if (s != other.s)
				return false;
			return true;
		}

	}
}
