package tahrir;

import com.google.common.base.Function;
import dagger.Module;
import dagger.Provides;
import tahrir.network.RemoteNodeAddress;
import tahrir.transport.messaging.udpV1.PhysicalNetworkLocation;
import tahrir.transport.messaging.udpV1.UdpNetworkInterface;
import tahrir.transport.rpc.TrNetworkInterface;
import tahrir.transport.rpc.TrPeerManager;
import tahrir.transport.rpc.TrRemoteConnection;
import tahrir.transport.rpc.TrSessionManager;
import tahrir.ui.swingUI.GUIMain;
import tahrir.util.tools.ByteArraySegment;
import tahrir.util.tools.Persistence;
import tahrir.util.tools.Tuple2;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static tahrir.util.tools.TrUtils.TestUtils.createTempDirectory;


/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 10/07/14
 */


@Module(
        injects = {GUIMain.class, TrPeerManager.class},
        complete = true
)

public class TahrirModule {
    private File rootDirectory = null;
    UdpNetworkInterface.UNIConfig config = null;


    @Provides
    TrNodeConfig provideNodeCongfig(){
        return new TrNodeConfig();
    }

    @Provides
    TrPeerManager.Config provideConfig(){
        return new TrPeerManager.Config();
    }

    @Provides @Named("keys")
    Tuple2<RSAPublicKey, RSAPrivateKey> providesTuple(){

    }

    @Provides
    TrNetworkInterface provideNetworkInterface(UdpNetworkInterface.UNIConfig config, Tuple2<RSAPublicKey, RSAPrivateKey> keyPair) throws SocketException {
        return new UdpNetworkInterface(config, keyPair);
    }

    @Provides @Named("AllowUnilateral")
    TrSessionManager provideTrSessionManagerUnilateral(TrNetworkInterface trNetworkInterface){
        return new TrSessionManager(trNetworkInterface, true);
    }


    @Provides @Named("DisallowUnilateral")
    TrSessionManager provideTrSessionManager(TrNetworkInterface trNetworkInterface){
        return new TrSessionManager(trNetworkInterface, false);
    }
    /*
    @Provides   //for rootDirectory file.
    File provideFile(String filename){
        return new File(filename);
    }

    @Provides @Named("fileInDirectory")
    File provideFileInDirectory(String directory, String filename){
       return new File(directory, filename);
    }
    */

    @Provides
    UdpNetworkInterface.UNIConfig provideUNIConfig(){
        if(this.config==null){
            this.config = new UdpNetworkInterface.UNIConfig();
        }
        return this.config;
    }

    //TODO: This may give a problem is provideRootDir isn't called before other functions. Should it be lazy?

    @Provides @Named("rootDirectory")
    File provideRootDir(){
        if(this.rootDirectory!=null){
            return this.rootDirectory;
        }
        else{

            try {
                this.rootDirectory = File.createTempFile("temp", Long.toString(System.nanoTime()));


            if (!(this.rootDirectory.delete()))
                throw new IOException("Could not delete temp file: " + this.rootDirectory.getAbsolutePath());

            if (!(this.rootDirectory.mkdir()))
                throw new IOException("Could not create temp directory: " + this.rootDirectory.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return (this.rootDirectory);
        }

    }


    @Provides @Named("publicNodeIdsDir")
    File providePublicNodeIdsDir(){
        this.rootDirectory = provideRootDir();
        //Is this uneccessary? I'm trying to avoid getting null if root directory wasn't set.

        return new File(this.rootDirectory, "publicPeers");
    }

    @Provides @Named("privateNodeIdFile")
    File providePrivateNodeIdFile(){
        this.rootDirectory = provideRootDir();
        return new File(this.rootDirectory, "myprivnodeid.dat");
    }
    @Provides @Named("publicNodeIdFile")
    File providePublicNodeIdFile(){
        this.rootDirectory = provideRootDir();
        return new File(this.rootDirectory, "mypubnodeid.dat");
    }

    @Provides @Named("identityStoreFile")
    File provideIdentityStoreFile(){
        this.rootDirectory = provideRootDir();
        return new File(rootDirectory+System.getProperty("file.separator")+"id-store.json");
    }

}
