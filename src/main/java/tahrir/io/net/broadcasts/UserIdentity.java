package tahrir.io.net.broadcasts;

import com.google.common.base.Optional;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 25/6/13
 */
public class UserIdentity {

    protected String nickName;
    protected RSAPublicKey pubKey;
    protected Optional<RSAPrivateKey> pvtKey;

    public UserIdentity(String nickName, RSAPublicKey pubKey, Optional<RSAPrivateKey> pvtKey) {
        this.nickName = nickName;
        this.pubKey = pubKey;
        this.pvtKey = pvtKey;
    }

    public UserIdentity(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserIdentity that = (UserIdentity) o;

        if (nickName != null ? !nickName.equals(that.nickName) : that.nickName != null) return false;
        if (!pubKey.equals(that.pubKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nickName != null ? nickName.hashCode() : 0;
        result = 31 * result + pubKey.hashCode();
        return result;
    }

    public String getNick(){
        return this.nickName;
    }

    public RSAPublicKey getPubKey() {
        return pubKey;
    }

    public  RSAPrivateKey getPvtKey(){
        return pvtKey.get();
    }

    public boolean hasPvtKey(){
        if (pvtKey.isPresent()){
            return true;
        }
        else return false;
    }

}
