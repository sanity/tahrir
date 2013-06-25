package tahrir.io.net.microblogging.microblogs;

import tahrir.io.crypto.TrCrypto;

import java.security.interfaces.RSAPublicKey;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 25/6/13
 */
public class UserIdentity {

    private final String nickName;
    private final RSAPublicKey pubKey;

    public UserIdentity(String nickName, RSAPublicKey pubKey) {
        this.nickName = nickName;
        this.pubKey=pubKey;
    }

    public String getNick(){
        return this.nickName;
    }
}
