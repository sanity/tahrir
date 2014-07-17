package tahrir;

import dagger.Module;
import dagger.Provides;
import tahrir.transport.rpc.TrPeerManager;
import tahrir.ui.swingUI.GUIMain;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;

import static tahrir.util.tools.TrUtils.TestUtils.createTempDirectory;


/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 10/07/14
 */


@Module(
        injects = {GUIMain.class},
        complete = true
)

public class TahrirModule {


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

    @Provides @Named("rootDirectory")
    File provideRootDir() throws IOException {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete()))
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());

        if (!(temp.mkdir()))
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());

        return (temp);
    }


    @Provides @Named("publicNodeIdsDir")
    File providePublicNodeIdsDir(){
        File directory = null;
        try {
            directory = createTempDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(directory, "publicPeers");
    }

    @Provides @Named("privateNodeIdFile")
    File providePrivateNodeIdFile(String directory){
        return new File(directory, "myprivnodeid.dat");
    }
    @Provides @Named("publicNodeIdFile")
    File providePublicNodeIdFile(String directory){
        return new File(directory, "mypubnodeid.dat");
    }

    @Provides @Named("identityStoreFile")
    File provideIdentityStoreFile(String rootDirectory){
        return new File(rootDirectory+System.getProperty("file.separator")+"id-store.json");
    }
}
