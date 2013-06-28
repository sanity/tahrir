package tahrir.io.net.microblogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 26/6/13
 */
public class IdentityStore {
    private static Logger logger = LoggerFactory.getLogger(ContactBook.class);

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

    //contains circles in String (label) i.e following, etc. And the id info in UserIdentity.
    private TreeMap<String, Set<UserIdentity>> usersInLabels=new TreeMap();

    private TreeMap<UserIdentity, Set<String>> labelsOfUser =new TreeMap();

    private TreeMap<String, Set<UserIdentity>> usersWithNickname =new TreeMap();


    public void addLabelToIdentity(String label, UserIdentity identity){
        //checks whether the identity exists, if not, adds the identity first and then adds label.
        if(!(labelsOfUser.containsKey(identity))){
            Set<String> labels=new HashSet<String>();
            labels.add(label);
            labelsOfUser.put(identity, labels);
            logger.debug("New identity created and label added.");
            addIdentityToNick(identity);
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

    public void addIdentityToNick(UserIdentity identity){
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
        }
        else{
            logger.debug("The identity doesn't contain the label.");
        }
    }

    public void removeIdentityFromLabel(UserIdentity identity, String label){
        if(usersInLabels.get(label).contains(identity)){
            usersInLabels.get(label).remove(identity);
            logger.debug("Removed identity from the label.");
            removeIdentityFromNick(identity);
        }
        else{
            logger.debug("Identity not present in the label.");
        }
    }

    public Set<UserIdentity> getIdentitiesWithLabel(String label){
        if(usersInLabels.containsKey(label)){
            logger.debug("Label was present, returning userIdentities.");
            return usersInLabels.get(label);
        }
        else{
            return null;
        }
    }

    private void removeIdentityFromNick(UserIdentity identity) {
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
            return null;
        }
    }

    public SortedMap<String, Set<UserIdentity>> getUserIdentitiesStartingWith(String nick){

        //Set<UserIdentity> setOfIdentitesHavingGivenNick=new HashSet();
        //creating tempNick to give the to point for the submap method
        int indexOfLastChar=nick.length()-1;
        String tempNick= nick.substring(0, indexOfLastChar);
        tempNick+=(nick.charAt(indexOfLastChar)+1);
        return usersWithNickname.subMap(nick, tempNick);

        //if only the nicks were required
        //return usersWithGivenNick.keySet();
    }

    public Set<UserIdentity> getIdentitiesWithNick(String nick){
        return usersWithNickname.get(nick);
    }

}


