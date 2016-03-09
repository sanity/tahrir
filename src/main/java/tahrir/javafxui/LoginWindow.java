package tahrir.javafxui;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tahrir.TrConstants;

/**
 * User: ravisvi <ravitejasvi@gmail.com>
 * Date: 27/01/16
 */
public class LoginWindow extends Application{

    Button loginButton;
    Stage window;

    public static void main(String [] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception{
        window = stage;
        window.setTitle(TrConstants.APP_NAME);

        //TODO: Add Tahrir Logo.
        VBox layout = new VBox(10);
        //Setting the padding.
        layout.setPadding(new Insets(10, 15, 10, 15));

        final BooleanProperty firstTime = new SimpleBooleanProperty(true);

        //Elements inside the layout.
        loginButton = new Button();
        loginButton.setText(TrConstants.LOGIN_MESSAGE);
        loginButton.getStyleClass().add("btn-success");
        loginButton.setOnAction(e -> {
            //TODO : Create node with the username and pass it to the next window.
        });

        // Label usernameLabel = new Label("Username");

        String imageUrl = getClass().getResource(TrConstants.separator+"tahrir"+TrConstants.separator+"artwork"+TrConstants.separator+"tahrir-logo_small.png").toString();
        Image img = new Image(imageUrl);

        Label emptyLabel =  new Label("    ");  //This is a temp hack to add margin. Can't seem to find it in reference.
        ImageView imgView = new ImageView(img);
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Your Tahrir username");
        usernameInput.setMaxWidth(130);

        //End of Elements inside the layout.

        usernameInput.focusedProperty().addListener((observable,  oldValue,  newValue) -> {
            if(newValue && firstTime.get()){
                layout.requestFocus(); // Delegate the focus to container
                firstTime.setValue(false); // Variable value changed for future references
            }
        });

        layout.getChildren().addAll( imgView, emptyLabel, usernameInput, loginButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene  = new Scene(layout, 300, 350);
        String css = getClass().getResource(TrConstants.separator+"tahrir"+TrConstants.separator+"css"+TrConstants.separator+"Login.css").toExternalForm();
        scene.getStylesheets().add(css);

        window.setScene(scene);
        window.show();

    }
}
