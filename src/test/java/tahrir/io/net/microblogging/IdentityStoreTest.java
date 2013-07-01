package tahrir.io.net.microblogging;

import org.testng.Assert;
import org.testng.annotations.Test;
import tahrir.io.crypto.TrCrypto;

import java.security.interfaces.RSAPublicKey;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 28/6/13
 */
public class IdentityStoreTest {

    RSAPublicKey user1Key = TrCrypto.createRsaKeyPair().a;
    final String user1nick = "name1";

    RSAPublicKey user2Key = TrCrypto.createRsaKeyPair().a;
    final String user2nick = "name2";

    final String boundingNick = "na";

    IdentityStore testStore=new IdentityStore();
    UserIdentity identityOne=new UserIdentity(user1nick, user1Key);
    UserIdentity identityTwo=new UserIdentity(user2nick, user2Key);
    final String label= "Following";

    @Test
    public void addLabelToIdentityTest(){
        testStore.addLabelToIdentity(label, identityOne);
        Assert.assertTrue(testStore.getIdentitiesWithLabel(label).contains(identityOne));
        Assert.assertTrue(testStore.getLabelsForIdentity(identityOne).contains(label));
    }

    @Test
    public void addIdentityToNickTest(){
        testStore.addIdentityToUsersWithNickname(identityTwo);
        Assert.assertTrue(testStore.getIdentitiesWithNick(identityTwo.getNick()).contains(identityTwo));
    }

    @Test
    public void removeLabelFromIdentityTest(){
        testStore.removeLabelFromIdentity(label, identityOne);
        Assert.assertFalse(testStore.getIdentitiesWithLabel(label).contains(identityOne));
        Assert.assertFalse(testStore.getLabelsForIdentity(identityOne).contains(label));
    }

    @Test
    public void getIdentitiesWithLabelTest(){
        testStore.addLabelToIdentity(label, identityTwo);
        Assert.assertTrue(testStore.getIdentitiesWithLabel(label).contains(identityTwo));
    }

    @Test
    public void removeIdentityFromNickTest(){
        testStore.removeIdentityFromNick(identityTwo);
        Assert.assertFalse(testStore.getIdentitiesWithNick(identityTwo.getNick()).contains(identityTwo));
    }

    @Test
    public void getLabelsForIdentityTest(){
        Assert.assertTrue(testStore.getLabelsForIdentity(identityTwo).contains(label));
    }

    @Test
    public void getUserIdentitiesStartingWithTest(){
        testStore.addIdentityToUsersWithNickname(identityOne);
        Assert.assertTrue(testStore.getUserIdentitiesStartingWith(boundingNick).contains(identityOne));
    }

    @Test
    public void getIdentitiesWithNick(){
        testStore.addIdentityToUsersWithNickname(identityOne);
        Assert.assertTrue(testStore.getIdentitiesWithNick(identityOne.getNick()).contains(identityOne));
    }

}