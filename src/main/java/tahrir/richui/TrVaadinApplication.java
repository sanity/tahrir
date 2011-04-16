package tahrir.richui;

import com.vaadin.Application;
import com.vaadin.ui.*;

public class TrVaadinApplication extends Application {
	@Override
	public void init() {
		final Window mainWindow = new Window("Jettytest Application");
		final VerticalLayout layout = new VerticalLayout();
		addMessageComposeComponent(layout);
		mainWindow.addComponent(layout);
		setMainWindow(mainWindow);
	}

	private void addMessageComposeComponent(final ComponentContainer container) {
		final GridLayout gridLayout = new GridLayout(2, 1);
		final TextArea composeMessage = new TextArea("Compose Message");
		composeMessage.setColumns(80);
		composeMessage.setRows(3);
		gridLayout.addComponent(composeMessage, 0, 0);
		final Button sendMessage = new Button("Send");
		gridLayout.addComponent(sendMessage, 1, 0);
		gridLayout.setComponentAlignment(sendMessage, Alignment.BOTTOM_LEFT);
		container.addComponent(gridLayout);
	}
}
