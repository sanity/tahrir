package tahrir.io.net.microblogging;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.tools.TrUtils;
import tahrir.ui.IdentityModifiedEvent;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 26/6/13
 */
public class IdentityStore {
    private static Logger logger = LoggerFactory.getLogger(IdentityStore.class);
    private File identityStoreFile = new File(TrConstants.IDENTITY_STORE_TEST_FILE_PATH);
    //contains circles in String (label) i.e FOLLOWING, etc. And the id info in UserIdentity.
    private HashMap<String, Set<UserIdentity>> usersInLabels;

    public HashMap<UserIdentity, Set<String>> labelsOfUser = Maps.newHashMap();

    private TreeMap<String, Set<UserIdentity>> usersWithNickname = Maps.newTreeMap();

    public EventBus eventBus;


    public IdentityStore(File identityStoreFile){
        this.identityStoreFile=identityStoreFile;
        if(identityStoreFile.exists()){
            try {
                FileReader identityStoreFileReader = new FileReader(identityStoreFile);
                logger.info("Trying to load identity store.");
                Type idStoreType = new TypeToken<Map<String, Set<UserIdentity>>>() {}.getType();
                usersInLabels = TrUtils.gson.fromJson(identityStoreFileReader, idStoreType);

                if (usersInLabels == null) {
                    logger.info("Failed to load any idStore. Creating new Identity Store.");
                    usersInLabels = Maps.newHashMap();
                }
                else {
                    updateOtherMaps(usersInLabels);
                    logger.info("Identity Store successfully loaded from file.");
                }

            }
            catch (IOException e) {
                logger.info("The identity store file doesn't exist.");
            }

        }
    else{
        usersInLabels = Maps.newHashMap();
        logger.info("The identity store file doesn't exist.");
    }
    }

    public void setEventBus(EventBus eventBus){
        this.eventBus = eventBus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdentityStore that = (IdentityStore) o;

        if (labelsOfUser != null ? !labelsOfUser.equals(that.labelsOfUser) : that.labelsOfUser != null) return false;
        if (usersInLabels != null ? !usersInLabels.equals(that.usersInLabels) : that.usersInLabels != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = usersInLabels != null ? usersInLabels.hashCode() : 0;
        result = 31 * result + (labelsOfUser != null ? labelsOfUser.hashCode() : 0);
        return result;
    }


    private void updateOtherMaps(HashMap<String, Set<UserIdentity>> usersInLabel){

        for (Map.Entry<String, Set<UserIdentity>> pairs : usersInLabel.entrySet()){
            for(UserIdentity userIdentity: pairs.getValue()){
                updateLabelsOfUsers(userIdentity, pairs.getKey());
                addIdentityToUsersWithNickname(userIdentity);
            }
        }

    }

    public void addIdentity(UserIdentity identity){
        //As label is not specified, this contact is not added to the file, but added temporarily to the map
        if(!(labelsOfUser.containsKey(identity))){

            Set<String> labels=Collections.emptySet();
            labelsOfUser.put(identity, labels);
            logger.debug("New identity created and label added.");
            addIdentityToUsersWithNickname(identity);

        }
        else{
                logger.debug("Identity already exists");
        }
    }

    public void removeIdentity(UserIdentity identity){
        if(labelsOfUser.containsKey(identity)){
            removeIdentityFromNick(identity);
            for(String label: labelsOfUser.get(identity)){
            removeIdentityFromLabel(identity, label);
            }
            labelsOfUser.remove(identity);
            logger.debug("Identity removed.");
            eventBus.post(new IdentityModifiedEvent(identity, IdentityModifiedEvent.IdentityModificationType.DELETE));
            updateIdentityInFile();
        }
        else{
            //adds to the already existing identity if the label isn't present.
                logger.debug("Identity doesn't exist.");
        }
    }

    public void addIdentityWithLabel(String label, UserIdentity identity){
        //checks whether the identity exists, if not, adds the identity first and then adds label.
        if(!(labelsOfUser.containsKey(identity))){
            Set<String> labels=Sets.newHashSet();
            labels.add(label);
            labelsOfUser.put(identity, labels);
            logger.debug("New identity created and label added.");
            addIdentityToUsersWithNickname(identity);
            updateUsersInLabel(identity, label);
            this.eventBus.post(new IdentityModifiedEvent(identity, IdentityModifiedEvent.IdentityModificationType.ADD));
            updateIdentityInFile();
        }
        else{
            //adds to the already existing identity if the label isn't present.
            if(((labelsOfUser.get(identity)).contains(label))){
                logger.debug("Identity already contains the label.");
            }
            else{
                labelsOfUser.get(identity).add(label);
                logger.debug("Added label to the existing identity.");
                updateUsersInLabel(identity, label);

            }

        }
    }

    private void updateLabelsOfUsers(UserIdentity identity, String label){
        if (!(labelsOfUser.containsKey(identity))){
            Set<String> labelSet = Sets.newHashSet();
            labelSet.add(label);
            labelsOfUser.put(identity, labelSet);
        }
        else if(!(labelsOfUser.get(identity).contains(label))){
            labelsOfUser.get(identity).add(label);
            logger.debug("Label was not present on the existing identity, added it.");

        }
        else{
            logger.debug("Identity already conains the label.");
        }
    }

    private void updateUsersInLabel(UserIdentity identity, String label){
        if (!(usersInLabels.containsKey(label))){
            Set<UserIdentity> identitySet = Sets.newHashSet();
            identitySet.add(identity);
            usersInLabels.put(label, identitySet);
        }
        else if(usersInLabels.get(label).contains(identity)){
            logger.debug("Label already contains identity.");
        }
        else{
            usersInLabels.get(label).add(identity);
            logger.debug("Added identity to label.");
        }
    }

    public void addIdentityToUsersWithNickname(UserIdentity identity){
        if(usersWithNickname.containsKey(identity.getNick())){
            usersWithNickname.get(identity.getNick()).add(identity);
            logger.debug("Nick was already present, added identity to it.");

        }
        else{
            Set<UserIdentity> identitySet=new HashSet();
            identitySet.add(identity);
            usersWithNickname.put(identity.getNick(), identitySet);
            logger.debug("Nick created and identity added.");
        }
    }

    public void removeLabelFromIdentity(String label, UserIdentity identity){
        if(labelsOfUser.containsKey(identity)){
        if(labelsOfUser.get(identity).contains(label)){
            labelsOfUser.get(identity).remove(label);
            logger.debug("Label removed from identity.");
            removeIdentityFromLabel(identity, label);
            eventBus.post(new IdentityModifiedEvent(identity, IdentityModifiedEvent.IdentityModificationType.DELETE));
            updateIdentityInFile();
        }

        else{
            logger.debug("The identity doesn't contain the label.");
        }
        }
        else{
            logger.debug("The identity doesn't exist");
        }
    }

    private void removeIdentityFromLabel(UserIdentity identity, String label){
        if(usersInLabels.containsKey(label)){
        if(usersInLabels.get(label).contains(identity)){
            usersInLabels.get(label).remove(identity);
            logger.debug("Removed identity from the label.");
            removeIdentityFromNick(identity);
        }
        else{
            logger.debug("Identity not present in the label.");
        }
        }
        else{
            logger.debug("Label doesn't exist");
        }
    }

    public boolean hasIdentityInLabel(UserIdentity identity){
        //currently checks if user is FOLLOWING the identity.
        if(labelsOfUser.containsKey(identity)){
        if(labelsOfUser.get(identity).contains(TrConstants.FOLLOWING)){
            return true;
        }
        }

        return false;
    }

    public boolean hasIdentityInIdStore(UserIdentity identity){

        if(usersWithNickname.containsKey(identity.getNick())){
            if (usersWithNickname.get(identity.getNick()).contains(identity)){
                return true;
            }
            else return false;
        }
        else return false;
    }

    public Set<UserIdentity> getIdentitiesWithLabel(String label){
        if(usersInLabels.containsKey(label)){
            logger.debug("Label was present, returning userIdentities.");
            return usersInLabels.get(label);
        }
        else{
            return Collections.emptySet();
        }
    }

    public void removeIdentityFromNick(UserIdentity identity) {
        if(usersWithNickname.containsKey(identity.getNick())){
            logger.debug("Nickname exists, removing identity from it.");
            usersWithNickname.get(identity.getNick()).remove(identity);
            eventBus.post(new IdentityModifiedEvent(identity, IdentityModifiedEvent.IdentityModificationType.DELETE));
        }
        else{
            logger.debug("Nickname isn't present so identity is also not present.");
        }
    }

    public Set<String> getLabelsForIdentity(UserIdentity identity){
        if(labelsOfUser.containsKey(identity)){
            logger.debug("Identity was present, returning corresponding labels.");
            return labelsOfUser.get(identity);
        }
        else{
            return Collections.emptySet();
        }
    }

    public SortedSet<UserIdentity> getUserIdentitiesStartingWith(String nick){

        int indexOfLastChar=nick.length()-1;
        String upperBoundNick= nick.substring(0, indexOfLastChar);
        upperBoundNick += (char)(nick.charAt(indexOfLastChar)+1);
        final SortedMap<String, Set<UserIdentity>> sortedMap = usersWithNickname.subMap(nick, upperBoundNick);

        SortedSet<UserIdentity> sortedUserIdentities = Sets.newTreeSet(new NickNameComparator());

        for (Set<UserIdentity> userIdentities : sortedMap.values()) {
            sortedUserIdentities.addAll(userIdentities);
        }

        return sortedUserIdentities;
    }

    private void updateIdentityInFile() {
        logger.info("Adding identities to file");
        try {
            //TODO: Try to append idStore into the file rather than rewriting the whole thing.
            FileWriter identityStoreWriter = new FileWriter(identityStoreFile);
            identityStoreWriter.write(TrUtils.gson.toJson(usersInLabels));
            identityStoreWriter.close();
        } catch (final IOException ioException) {
            logger.error("Problem writing identities to file: the identities weren't saved.");
            ioException.printStackTrace();
        }
    }

    public Set<UserIdentity> getIdentitiesWithNick(String nick){
        if(usersWithNickname.containsKey(nick)){
            return usersWithNickname.get(nick);
        }
        else{
            return Collections.emptySet();
        }
    }

    private static class NickNameComparator implements Comparator<UserIdentity> {

        @Override
        public int compare(final UserIdentity o1, final UserIdentity o2) {
            int nickComparison = o1.getNick().compareTo(o2.getNick());
            if (nickComparison != 0) {
                return nickComparison;
            } else {
                return o1.getPubKey().toString().compareTo(o2.getPubKey().toString());
            }
        }
    }
}


