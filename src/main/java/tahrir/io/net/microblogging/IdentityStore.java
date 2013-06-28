package tahrir.io.net.microblogging;

import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.tools.TrUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 26/6/13
 */
public class IdentityStore {
    private static Logger logger = LoggerFactory.getLogger(ContactBook.class);

    //contains circles in String (label) i.e following, etc. And the id info in UserIdentity.
    private TreeMap<String, Set<UserIdentity>> usersInLabels=new TreeMap();

    private TreeMap<UserIdentity, Set<String>> labelsOfUser =new TreeMap();


    public void addLabelToIdentity(String label, UserIdentity identity){
        //checks whether the identity exists, if not, adds the identity first and then adds label.
        if(!(labelsOfUser.containsKey(identity))){
            Set<String> labels=new HashSet<String>();
            labels.add(label);
            labelsOfUser.put(identity, labels);
            logger.debug("New identity created and label added.");
            addIdentityToLabel(identity, label);
        }
        else{
            //adds to the already existing identity if the label isn't present.
            if(((labelsOfUser.get(identity)).contains(label))){
                logger.debug("Identity already contains the label.");
            }
            else{
                labelsOfUser.get(identity).add(label);
                logger.debug("Added label to the existing identity.");
                addIdentityToLabel(identity, label);

            }

        }
    }

    public void addIdentityToLabel(UserIdentity identity, String label){
        if(usersInLabels.get(label).contains(identity)){
            logger.debug("Label already contains identity.");
        }
        else{
            usersInLabels.get(label).add(identity);
            logger.debug("Added identity to label.");
        }
    }

    public void removeLabelFromIdentity(String label, UserIdentity identity){
        if(labelsOfUser.get(identity).contains(label)){
            labelsOfUser.get(identity).remove(label);
            logger.debug("Label removed from identity.");
            removeIdentityFromLabel(identity, label);
        }
        else{
            logger.debug("The identity doesn't contain the label.");
        }
    }

    public void removeIdentityFromLabel(UserIdentity identity, String label){
        if(usersInLabels.get(label).contains(identity)){
            usersInLabels.get(label).remove(identity);
            logger.debug("Removed identity from the label.");
        }
        else{
            logger.debug("Identity not present in the label.");
        }
    }

    public Set<UserIdentity> getIdentitiesWithLabel(String label){
        if(usersInLabels.containsKey(label)){
            logger.debug("Label was present, returning userIdentities");
            return usersInLabels.get(label);
        }
        else{
            return null;
        }
    }

    public Set<String> getLabelsForIdentity(UserIdentity identity){
        if(labelsOfUser.containsKey(identity)){
            logger.debug("Identity was present, returning corresponding labels");
            return labelsOfUser.get(identity);
        }
        else{
            return null;
        }
    }

}
