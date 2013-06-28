package tahrir.io.net.microblogging;

import java.security.interfaces.RSAPublicKey;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 25/6/13
 */
public class UserIdentity {

    protected String nickName;
    protected RSAPublicKey pubKey;

    public UserIdentity(String nickName, RSAPublicKey pubKey) {
        this.nickName = nickName;
        this.pubKey=pubKey;
    }

    public UserIdentity(){

    }
    public String getNick(){
        return this.nickName;
    }
    public RSAPublicKey getPubKey() {
        return pubKey;
    }
}
