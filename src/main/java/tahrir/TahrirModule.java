package tahrir;

import dagger.Module;
import dagger.Provides;
import tahrir.network.RemoteNodeAddress;
import tahrir.transport.messaging.udpV1.PhysicalNetworkLocation;
import tahrir.transport.rpc.TrPeerManager;
import tahrir.ui.swingUI.GUIMain;
import tahrir.util.tools.Persistence;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;

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


    @Provides
    TrNodeConfig provideNodeCongfig(){
        return new TrNodeConfig();
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
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
