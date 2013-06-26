package tahrir.io.net.microblogging;

import java.security.interfaces.RSAPrivateKey;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 26/6/13
 */
public class PrivateUserIdentity extends UserIdentity{
    private final RSAPrivateKey pvtKey;

    PrivateUserIdentity(RSAPrivateKey pvtKey){
        this.pvtKey=pvtKey;
    }

}
