
package tahrir.vaadin;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import tahrir.TrNode;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import tahrir.TrUI;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.SignedBroadcastMessage;
import tahrir.io.net.broadcasts.filters.Unfiltered;
import tahrir.ui.BroadcastMessageDisplayPage;
import tahrir.ui.LoginWindow;
import tahrir.ui.RegisterWindow;
import tahrir.ui.TrMainWindow;

import java.awt.*;


public class TestVaadinUI extends UI implements TrUI{

    final Button postButton = new Button("Post");
    final Button exitTahrirButton = new Button("Exit Program");

    public TrNode node;


    TabSheet tabsheet;

    @Override
    protected void init(VaadinRequest request) {

        node=((TahrirVaadinRequest)request).getNode();

        VerticalLayout view = new VerticalLayout();
        setContent(view);


        view.addComponent(new Label("Hello Vaadin!"));

        tabsheet = new TabSheet();
        view.addComponent(tabsheet);

        final VerticalLayout allTab = new VerticalLayout();
        allTab.addComponent(new Label("This is the 'firehose' tab"));
        tabsheet.addTab(allTab, "All");

        //final BroadcastMessageDisplayPage unfilteredPostPage = new BroadcastMessageDisplayPage(new Unfiltered(), this);



        final TextField postField = new TextField();
        allTab.addComponent(postField);

        postField.setImmediate(true);

        postButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                String messageToPost = postField.getValue();
                allTab.addComponent(new Label(messageToPost));


                if(node.getConfig().currentUserIdentity.getNick().equals("Default")){
                    if(node.mbClasses.identityStore.labelsOfUser.keySet().isEmpty()){
                        final RegisterWindow registerWindow = new RegisterWindow(node);
                    }
                    else{
                        final LoginWindow loginWindow = new LoginWindow(node);
                    }
                }
                else{
                    String message = postField.getValue();
                    //TODO: get the language from config or settings page.
                    ParsedBroadcastMessage parsedBroadcastMessage = ParsedBroadcastMessage.createFromPlaintext(message, "en", node.mbClasses.identityStore, System.currentTimeMillis());
                    SignedBroadcastMessage signedBroadcastMessage = new SignedBroadcastMessage(parsedBroadcastMessage, node.getConfig().currentUserIdentity);
                    final BroadcastMessage broadcastMessage = new BroadcastMessage(signedBroadcastMessage);
                    node.mbClasses.incomingMbHandler.handleInsertion(broadcastMessage);
                }
            }
        });

        allTab.addComponent(postButton);

        postField.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                if(target==postField){ //limit it so that only when the user presses enters from the postfield does this happen
                    postButton.click();
                }
            }
        });

        postField.addTextChangeListener(new FieldEvents.TextChangeListener() {
            public void textChange(final FieldEvents.TextChangeEvent event) {

            }
        });



        /*
        VerticalLayout peopleUserIsFollowingTab = new VerticalLayout();
        peopleUserIsFollowingTab.addComponent(new Label("This tab filters for posts by all the people (@) that the user follows"));
        tabsheet.addTab(peopleUserIsFollowingTab, "People I Follow");

        VerticalLayout tagsUserIsFollowingTab = new VerticalLayout();
        tagsUserIsFollowingTab.addComponent(new Label("This tab filters for hashtags (#) that the user follows"));
        tabsheet.addTab(tagsUserIsFollowingTab, "Tags I Follow");*/


        exitTahrirButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                System.exit(0);
            }
        });
        view.addComponent(exitTahrirButton);



    }

    public void createClosableTab(final String tabName, final java.awt.Component tabContents) {

        final VerticalLayout newtab = new VerticalLayout();

        tabsheet.addTab(newtab, tabName);
    }

    @Override
    public void markAsDirty() {
    }

    @Override
    public TrNode getNode() {
        return node;
    }

/*
    class SubmitFormOnEnterKeyHandler extends TextField implements Action.Handler {
        
        public SubmitFormOnEnterKeyHandler(){

            getApplication().getMainWindow.addActionHandler(this);

        }

        private final Action enterKeyShortcutAction = new ShortcutAction(null, ShortcutAction.KeyCode.ENTER, null);

        public Action[] getActions(Object target, Object sender) {
            return new Action[]{enterKeyShortcutAction};
        }

        public void handleAction(Action action, Object sender, Object target) {
            if (action == enterKeyShortcutAction) {
                
                
                postButton.click();
            }
        }
    }*/
}
