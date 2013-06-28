package tahrir.io.net.microblogging;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 26/6/13
 */
public class PrivateUserIdentity extends UserIdentity{
    private RSAPrivateKey pvtKey;

    PrivateUserIdentity(RSAPrivateKey pvtKey, RSAPublicKey pubKey, String nickName){
        super(nickName, pubKey);
        this.pvtKey=pvtKey;
    }

    protected PrivateUserIdentity(){
    }



}
