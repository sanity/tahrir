package tahrir.io.net.broadcasts.filters;

import com.google.common.base.Predicate;
import com.sun.istack.internal.Nullable;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 23/07/13
 */
public class Unfiltered implements Predicate<BroadcastMessage> {
    @Override
    public boolean apply(@Nullable BroadcastMessage broadcastMessage) {
        return true;
    }
}
