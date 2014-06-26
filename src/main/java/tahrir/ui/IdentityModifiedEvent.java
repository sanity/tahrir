package tahrir.ui;


import tahrir.network.broadcasts.UserIdentity;

/**
 * Author   : Ravisvi <ravitejasvi@gmail.com>
 * Date     : 11/7/13
 */
public class IdentityModifiedEvent {
    UserIdentity identity;
    public enum IdentityModificationType {
        ADD, MODIFY, DELETE
    }
    IdentityModificationType type;
    public IdentityModifiedEvent(UserIdentity identity, IdentityModificationType type){
        this.identity = identity;
         this.type = type;
    }
}
