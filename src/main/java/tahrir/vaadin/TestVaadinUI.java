
package tahrir.vaadin;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;

public class TestVaadinUI extends UI {

    final Button postButton = new Button("Post");
    final Button exitTahrirButton = new Button("Exit Program");


    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout view = new VerticalLayout();
        setContent(view);


        view.addComponent(new Label("Hello Vaadin!"));

        TabSheet tabsheet = new TabSheet();
        view.addComponent(tabsheet);

        final VerticalLayout allTab = new VerticalLayout();
        allTab.addComponent(new Label("This is the 'firehose' tab"));
        tabsheet.addTab(allTab, "All");

        final TextField postField = new TextField();
        allTab.addComponent(postField);

        postField.setImmediate(true);

        postButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                String messageToPost = postField.getValue();
                allTab.addComponent(new Label(messageToPost));
            }
        });

        allTab.addComponent(postButton);

        postField.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                if(target==postField){ //limit the enters to only from the textfield from this form factory
                    // your code here

                    postButton.click();
                }

            }
        });

        postField.addTextChangeListener(new FieldEvents.TextChangeListener() {
            public void textChange(final FieldEvents.TextChangeEvent event) {

            }
        });







        VerticalLayout peopleUserIsFollowingTab = new VerticalLayout();
        peopleUserIsFollowingTab.addComponent(new Label("This tab filters for posts by all the people (@) that the user follows"));
        tabsheet.addTab(peopleUserIsFollowingTab, "People I Follow");

        VerticalLayout tagsUserIsFollowingTab = new VerticalLayout();
        tagsUserIsFollowingTab.addComponent(new Label("This tab filters for hashtags (#) that the user follows"));
        tabsheet.addTab(tagsUserIsFollowingTab, "Tags I Follow");


        exitTahrirButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                System.exit(0);
            }
        });
        view.addComponent(exitTahrirButton);



    }

    @Override
    public void markAsDirty() {
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
