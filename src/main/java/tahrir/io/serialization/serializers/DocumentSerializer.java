package tahrir.io.serialization.serializers;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import tahrir.io.serialization.TrSerializableException;
import tahrir.io.serialization.TrSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 05/08/13
 */
public class DocumentSerializer extends TrSerializer {
    public DocumentSerializer(){
        super(Document.class);
    }

    @Override
    protected Object deserialize(Type type, DataInputStream dis) throws TrSerializableException, IOException {
        Document doc;
        try {
             doc = new Builder().build(dis.readUTF(), null);
            return (Object)doc;
        }catch (final Exception e) {
            throw new TrSerializableException(e);
        }
    }

    @Override
    protected void serialize(Type type, Object object, DataOutputStream dos) throws TrSerializableException, IOException {
        final Document doc = (Document) object;
        String docInXml = doc.toXML();
        dos.writeUTF(docInXml);
    }
}
