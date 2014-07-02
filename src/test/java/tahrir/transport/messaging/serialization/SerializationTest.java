package tahrir.transport.messaging.serialization;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.testng.Assert;
import org.testng.annotations.Test;
import tahrir.util.crypto.TrCrypto;
import tahrir.network.RemoteNodeAddress;
import tahrir.transport.rpc.TrPeerManager.TrPeerInfo;
import tahrir.transport.messaging.udpV1.UdpNetworkLocation;

import java.io.*;
import java.net.InetAddress;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
		final TrPeerInfo ot = new TrPeerInfo(new RemoteNodeAddress(new UdpNetworkLocation(InetAddress.getByName("127.0.0.1"), 1234), TrCrypto.createRsaKeyPair().a));
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

    @Test
    public void optionalTypeTest() throws Exception{
        Optionals optionals = new Optionals();
        optionals.stringPresent = Optional.of("hello");
        optionals.stringAbsent = Optional.absent();
        try{
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            final DataOutputStream dos = new DataOutputStream(baos);
            TrSerializer.serializeTo(optionals, dos);
            final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
            Assert.assertEquals(TrSerializer.deserializeFrom(Optionals.class, dis), optionals);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pvtKeySerialisationTest() throws Exception{
        RSAPrivateKey pvtKey = TrCrypto.createRsaKeyPair().b;
        try{

            final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            final DataOutputStream dos = new DataOutputStream(baos);

            TrSerializer.serializeTo(pvtKey, dos);

            final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
            RSAPrivateKey pvtKey1 = TrSerializer.deserializeFrom(RSAPrivateKey.class, dis);

            Assert.assertEquals(pvtKey1, pvtKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void documentSerialisationTest() throws Exception{
        Document doc;
        String xml = "<mb><txt>Hi <mtn>Jason</mtn>. Have I ever told you the definition of insanity?</txt></mb>";
        try{
            doc = new Builder().build(xml, null);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            final DataOutputStream dos = new DataOutputStream(baos);

            TrSerializer.serializeTo(doc, dos);

            final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
            Document doc1 = TrSerializer.deserializeFrom(Document.class, dis);
            String expectedXmlFromDoc = doc1.toXML();
            String actualXmlFromDoc = doc.toXML();
            Assert.assertEquals(actualXmlFromDoc, expectedXmlFromDoc);
        }
        catch (ParsingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	@Test
	public void duplicateObjectTest() throws Exception {
		final ArrayList<String> listWithDuplicate = new ArrayList<String>();

		final String string1 = "a";
		final String string2 = "b";
		final String string3 = "c";

		listWithDuplicate.add(string1);
		listWithDuplicate.add(string1);

		final ArrayList<String> listNoDuplicate = new ArrayList<String>();

		listNoDuplicate.add(string3);
		listNoDuplicate.add(string2);

		final ByteArrayOutputStream baos1 = new ByteArrayOutputStream(1024);
		final DataOutputStream dos1 = new DataOutputStream(baos1);
		TrSerializer.serializeTo(listWithDuplicate, dos1);

		final ByteArrayOutputStream baos2 = new ByteArrayOutputStream(1024);
		final DataOutputStream dos2 = new DataOutputStream(baos2);
		TrSerializer.serializeTo(listNoDuplicate, dos2);

		Assert.assertEquals(baos2.size(), baos1.size());
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

    public static class Optionals  {
        public Optional<String> stringPresent;
        public Optional<String> stringAbsent;

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Optionals optionals = (Optionals) o;

            if (stringAbsent != null ? !stringAbsent.equals(optionals.stringAbsent) : optionals.stringAbsent != null)
                return false;
            if (stringPresent != null ? !stringPresent.equals(optionals.stringPresent) : optionals.stringPresent != null)
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

    private static int testNormalJavaSerialization(final Serializable object) throws IOException {
   		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

   		final ObjectOutputStream oos = new ObjectOutputStream(baos);

   		oos.writeObject(object);

   		oos.flush();

   		return baos.toByteArray().length;
   	}
}
