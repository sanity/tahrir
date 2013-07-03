package tahrir.io.net.microblogging;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.tools.TrUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 26/6/13
 */
public class IdentityStore {
    private static Logger logger = LoggerFactory.getLogger(IdentityStore.class);
    private File identityStoreFile = new File(TrConstants.identityStoreTestFilePath);
    //contains circles in String (label) i.e following, etc. And the id info in UserIdentity.
    private HashMap<String, Set<UserIdentity>> usersInLabels= Maps.newHashMap();

    private HashMap<UserIdentity, Set<String>> labelsOfUser = Maps.newHashMap();

    private TreeMap<String, Set<UserIdentity>> usersWithNickname = Maps.newTreeMap();

    public IdentityStore(File identityStoreFile){

        if(identityStoreFile.exists()){
            this.identityStoreFile=identityStoreFile;
            FileReader identityStoreFileReader=null;
            try {
                identityStoreFileReader=new FileReader(identityStoreFile);
            }
            catch (FileNotFoundException e) {
                logger.info("The identity store file doesn't exist.");
                throw new RuntimeException("File not found, invalid file.");
            }
            logger.info("Trying to load identity store.");
            Type idStoreType = new TypeToken<Map<UserIdentity, Set<String>>>() {}.getType();
            labelsOfUser.putAll((Map<UserIdentity, Set<String>>)TrUtils.gson.fromJson(identityStoreFileReader, idStoreType));

            if (labelsOfUser == null) {
                logger.info("Failed to load any idStore. Creating new Identity Store.");
                labelsOfUser = Maps.newHashMap();
            }
            else {
                updateUsersInLabelsMap(labelsOfUser);
                updateUsersWithNickMap(labelsOfUser);
                logger.info("Identity Store successfully loaded from file.");
            }
        }
    else{
        logger.info("The identity store file doesn't exist.");
    }
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

    private void updateUsersInLabelsMap(HashMap<UserIdentity, Set<String>> labelsOfUser){
        Iterator<Map.Entry<UserIdentity, Set<String>>> labelsOfUserIterator = labelsOfUser.entrySet().iterator();
        while (labelsOfUserIterator.hasNext()) {
            Map.Entry<UserIdentity, Set<String>> pairs = labelsOfUserIterator.next();
            //iterate through the set of labels.
            Iterator<String> labelIterator = labelsOfUser.get(pairs.getValue()).iterator();
            while(labelIterator.hasNext()) {
                String label = labelIterator.next();
                updateUsersInLabel(pairs.getKey(), label);
                labelIterator.remove();
                }
            labelsOfUserIterator.remove();
            }
    }

    private void updateUsersWithNickMap(HashMap<UserIdentity, Set<String>> labelsOfUser){
        Iterator<Map.Entry<UserIdentity, Set<String>>> labelsOfUserIterator = labelsOfUser.entrySet().iterator();
        while (labelsOfUserIterator.hasNext()) {
            Map.Entry<UserIdentity, Set<String>> pairs = labelsOfUserIterator.next();
            addIdentityToUsersWithNickname(pairs.getKey());
            labelsOfUserIterator.remove();
        }
    }

    // adds the identity to all the maps.
    public void addIdentity(String label, UserIdentity identity){
        //checks whether the identity exists, if not, adds the identity first and then adds label.
        if(!(labelsOfUser.containsKey(identity))){
            Set<String> labels=Sets.newHashSet();
            labels.add(label);
            labelsOfUser.put(identity, labels);
            logger.debug("New identity created and label added.");
            updateIdentityInFile();
            addIdentityToUsersWithNickname(identity);
            updateUsersInLabel(identity, label);
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

    private void updateUsersInLabel(UserIdentity identity, String label){
        if(!(usersInLabels.containsKey(label))){
            Set<UserIdentity> identitySet = Sets.newHashSet();
            identitySet.add(identity);
            usersInLabels.put(label, identitySet);
        }
        if(usersInLabels.get(label).contains(identity)){
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
        if(labelsOfUser.get(identity).contains(label)){
            labelsOfUser.get(identity).remove(label);
            logger.debug("Label removed from identity.");
            removeIdentityFromLabel(identity, label);
            updateIdentityInFile();
        }
        else{
            logger.debug("The identity doesn't contain the label.");
        }
    }

    private void removeIdentityFromLabel(UserIdentity identity, String label){
        if(usersInLabels.get(label).contains(identity)){
            usersInLabels.get(label).remove(identity);
            logger.debug("Removed identity from the label.");
            removeIdentityFromNick(identity);
        }
        else{
            logger.debug("Identity not present in the label.");
        }
    }

    public boolean hasIdentityInIdStore(UserIdentity identity){
        if(usersWithNickname.containsKey(identity.getNick())){
            if (usersWithNickname.get(identity.getNick()).contains(identity)){
                return true;
            }
        }
        return false;
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
            identityStoreWriter.write(TrUtils.gson.toJson(labelsOfUser));
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


