package tahrir.network.broadcasts.filters;

import com.google.common.base.Predicate;
import tahrir.network.broadcasts.broadcastMessages.BroadcastMessage;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 23/07/13
 */
public class Unfiltered implements Predicate<BroadcastMessage> {
    @Override
    public boolean apply(BroadcastMessage broadcastMessage) {
        return true;
    }
}
