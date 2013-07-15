package tahrir.io.net.microblogging;

import com.google.common.base.Optional;
import org.testng.Assert;
import org.testng.annotations.Test;
import tahrir.TrConstants;
import tahrir.io.crypto.TrCrypto;

import java.io.File;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 28/6/13
 */
public class IdentityStoreTest {





    final String boundingNick = "na";


    @Test
    public void addLabelToIdentityTest(){
        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        RSAPublicKey user1Key = TrCrypto.createRsaKeyPair().a;
        final String user1nick = "name1";
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);
        UserIdentity identityOne=new UserIdentity(user1nick, user1Key, Optional.<RSAPrivateKey>absent());
        final String label= "Following";

        testStore.addIdentityWithLabel(label, identityOne);
        Assert.assertTrue(testStore.getIdentitiesWithLabel(label).contains(identityOne));
        Assert.assertTrue(testStore.getLabelsForIdentity(identityOne).contains(label));
        identityStoreTestFile.delete();
    }

    @Test
    public void addIdentityToNickTest(){
        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);
        RSAPublicKey user2Key = TrCrypto.createRsaKeyPair().a;
        final String user2nick = "name2";
        UserIdentity identityTwo=new UserIdentity(user2nick, user2Key, Optional.<RSAPrivateKey>absent());



        testStore.addIdentityToUsersWithNickname(identityTwo);
        Assert.assertTrue(testStore.getIdentitiesWithNick(identityTwo.getNick()).contains(identityTwo));

        identityStoreTestFile.delete();
    }

    @Test
    public void removeLabelFromIdentityTest(){

        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        RSAPublicKey user1Key = TrCrypto.createRsaKeyPair().a;
        final String user1nick = "name1";
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);
        UserIdentity identityOne=new UserIdentity(user1nick, user1Key, Optional.<RSAPrivateKey>absent());
        final String label= "Following";

        testStore.addIdentityWithLabel(label, identityOne);

        testStore.removeLabelFromIdentity(label, identityOne);
        Assert.assertFalse(testStore.getIdentitiesWithLabel(label).contains(identityOne));
        Assert.assertFalse(testStore.getLabelsForIdentity(identityOne).contains(label));
        identityStoreTestFile.delete();
    }

    @Test
    public void getIdentitiesWithLabelTest(){
        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        RSAPublicKey user1Key = TrCrypto.createRsaKeyPair().a;
        final String user1nick = "name2";
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);
        UserIdentity identityTwo=new UserIdentity(user1nick, user1Key, Optional.<RSAPrivateKey>absent());
        final String label= "Following";

        testStore.addIdentityWithLabel(label, identityTwo);
        Assert.assertTrue(testStore.getIdentitiesWithLabel(label).contains(identityTwo));
        identityStoreTestFile.delete();
    }

    @Test
    public void removeIdentityFromNickTest(){
        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);
        RSAPublicKey user2Key = TrCrypto.createRsaKeyPair().a;
        final String user2nick = "name2";
        UserIdentity identityTwo=new UserIdentity(user2nick, user2Key, Optional.<RSAPrivateKey>absent());

        testStore.addIdentityToUsersWithNickname(identityTwo);
        testStore.removeIdentityFromNick(identityTwo);
        Assert.assertFalse(testStore.getIdentitiesWithNick(identityTwo.getNick()).contains(identityTwo));
        identityStoreTestFile.delete();
    }

    @Test
    public void getLabelsForIdentityTest(){
        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        RSAPublicKey user1Key = TrCrypto.createRsaKeyPair().a;
        final String user1nick = "name2";
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);
        UserIdentity identityTwo=new UserIdentity(user1nick, user1Key, Optional.<RSAPrivateKey>absent());
        final String label= "Following";

        testStore.addIdentityWithLabel(label, identityTwo);
        Assert.assertTrue(testStore.getLabelsForIdentity(identityTwo).contains(label));
        identityStoreTestFile.delete();
    }

    @Test
    public void getUserIdentitiesStartingWithTest(){
        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        RSAPublicKey user1Key = TrCrypto.createRsaKeyPair().a;
        final String user1nick = "name1";
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);
        UserIdentity identityOne=new UserIdentity(user1nick, user1Key, Optional.<RSAPrivateKey>absent());

        testStore.addIdentityToUsersWithNickname(identityOne);
        Assert.assertTrue(testStore.getUserIdentitiesStartingWith(boundingNick).contains(identityOne));
        identityStoreTestFile.delete();
    }

    @Test
    public void getIdentitiesWithNick(){
        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        RSAPublicKey user1Key = TrCrypto.createRsaKeyPair().a;
        final String user1nick = "name1";
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);
        UserIdentity identityOne=new UserIdentity(user1nick, user1Key, Optional.<RSAPrivateKey>absent());

        testStore.addIdentityToUsersWithNickname(identityOne);
        Assert.assertTrue(testStore.getIdentitiesWithNick(identityOne.getNick()).contains(identityOne));
        identityStoreTestFile.delete();
    }
    @Test
    public void duplicateUsersTest(){
        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);

        UserIdentity testUser2 = new UserIdentity("TestUser2", TrCrypto.createRsaKeyPair().a, Optional.<RSAPrivateKey>absent());

        testStore.addIdentityWithLabel("Friends", testUser2);
        testStore.addIdentityWithLabel("Friends", testUser2);

        Assert.assertFalse((testStore.getIdentitiesWithNick(testUser2.getNick()).size())>1);
        identityStoreTestFile.delete();
    }

   @Test
    public void fileLoadingTest(){
       File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
       IdentityStore testStore=new IdentityStore(identityStoreTestFile);

       UserIdentity testUser3 = new UserIdentity("TestUser3", TrCrypto.createRsaKeyPair().a, Optional.<RSAPrivateKey>absent());
       testStore.addIdentityWithLabel("Friends", testUser3);
       IdentityStore testStore2=new IdentityStore(identityStoreTestFile);
       Assert.assertTrue(testStore2.hasIdentityInIdStore(testUser3));
       identityStoreTestFile.delete();
    }

    @Test
    public void emptyLabelTest(){
        File identityStoreTestFile=new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
        IdentityStore testStore=new IdentityStore(identityStoreTestFile);

        UserIdentity testUser3 = new UserIdentity("TestUser3", TrCrypto.createRsaKeyPair().a, Optional.<RSAPrivateKey>absent());
        testStore.addIdentity(testUser3);
        Assert.assertTrue(testStore.hasIdentityInIdStore(testUser3));
        identityStoreTestFile.delete();
    }

}