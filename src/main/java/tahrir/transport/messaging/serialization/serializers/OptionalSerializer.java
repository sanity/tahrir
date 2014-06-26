package tahrir.transport.messaging.serialization.serializers;

import com.google.common.base.Optional;
import tahrir.transport.messaging.serialization.TrSerializableException;
import tahrir.transport.messaging.serialization.TrSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 31/08/13
 */
public class OptionalSerializer extends TrSerializer{
    public OptionalSerializer(){
        super(Optional.class);
    }

    @Override
    protected Object deserialize(Type type_, DataInputStream dis) throws TrSerializableException, IOException {
        final ParameterizedType type = (ParameterizedType) type_;
        try {
            final Class<?> elementType = (Class<?>) type.getActualTypeArguments()[0];
            final boolean isPresent = dis.readBoolean();
            if (isPresent) {
                final Object element = deserializeFrom(elementType, dis);
                Optional<Object> optional = Optional.of(element);
                return optional;
            } else {
                return Optional.absent();
            }
        } catch (final Exception e) {
            throw new TrSerializableException(e);
        }
    }

    @Override
    protected void serialize(Type type, Object object, DataOutputStream dos) throws TrSerializableException, IOException {
        final Optional<?> optional = (Optional<?>) object;
        dos.writeBoolean(optional.isPresent());
        if(optional.isPresent()){
            serializeTo(optional.get(), dos);
        }
    }
}
