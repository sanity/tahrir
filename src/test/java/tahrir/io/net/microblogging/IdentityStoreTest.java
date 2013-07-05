package tahrir.io.net.microblogging;

import org.testng.Assert;
import org.testng.annotations.Test;
import tahrir.TrConstants;
import tahrir.io.crypto.TrCrypto;

import java.io.File;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 28/6/13
 */
public class IdentityStoreTest {

    File identityStoreTestFile=new File(TrConstants.identityStoreTestFilePath);
    RSAPublicKey user1Key = TrCrypto.createRsaKeyPair().a;
    final String user1nick = "name1";

    RSAPublicKey user2Key = TrCrypto.createRsaKeyPair().a;
    final String user2nick = "name2";

    final String boundingNick = "na";

    IdentityStore testStore=new IdentityStore(identityStoreTestFile);
    UserIdentity identityOne=new UserIdentity(user1nick, user1Key);
    UserIdentity identityTwo=new UserIdentity(user2nick, user2Key);
    final String label= "Following";

    @Test
    public void addLabelToIdentityTest(){
        testStore.addIdentity(label, identityOne);
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
        testStore.addIdentity(label, identityTwo);
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
    @Test
    public void duplicateUsersTest(){
        UserIdentity testUser2 = new UserIdentity("TestUser2", TrCrypto.createRsaKeyPair().a);
        testStore.addIdentity("Friends", testUser2);
        testStore.addIdentity("Friends", testUser2);
        Assert.assertFalse((testStore.getIdentitiesWithNick(testUser2.getNick()).size())>1);
    }

   @Test
    public void fileLoadingTest(){
        testStore.addIdentity("Following", identityTwo);
        UserIdentity testUser2 = new UserIdentity("TestUser2", TrCrypto.createRsaKeyPair().a);
        testStore.addIdentity("Friends", testUser2);
       UserIdentity testUser3 = new UserIdentity("TestUser2", TrCrypto.createRsaKeyPair().a);
       testStore.addIdentity("Friends", testUser3);
        IdentityStore testStore2=new IdentityStore(identityStoreTestFile);
        Assert.assertTrue(testStore2.hasIdentityInIdStore(testUser2));
    }

}