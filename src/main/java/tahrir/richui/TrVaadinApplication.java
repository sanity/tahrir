package tahrir.richui;

import java.util.concurrent.TimeUnit;

import org.vaadin.artur.icepush.ICEPush;

import tahrir.tools.TrUtils;

import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;

public class TrVaadinApplication extends Application {
	private final ICEPush pusher = new ICEPush();

	@Override
	public void init() {
		final Window mainWindow = new Window("Jettytest Application");

		// mainWindow.addComponent(pusher);

		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		final Label title = new Label("<h1>Tahrir</h1>");
		title.setContentMode(Label.CONTENT_XHTML);
		TrUtils.executor.scheduleAtFixedRate(new Runnable() {

			public void run() {
				title.setValue("Tahrir " + System.currentTimeMillis());
				pusher.push();
			}}, 10, 10, TimeUnit.SECONDS);
		layout.addComponent(title);
		addMessageComposeComponent(layout);
		final HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSizeFull();
		addMessageDisplayComponent(hLayout);
		addSidePanel(hLayout);
		layout.addComponent(hLayout);
		mainWindow.addComponent(layout);
		setMainWindow(mainWindow);
	}

	private void addSidePanel(final ComponentContainer container) {

	}

	private void addMessageComposeComponent(final ComponentContainer container) {
		final GridLayout gridLayout = new GridLayout(2, 1);
		gridLayout.setWidth("100%");
		final TextArea composeMessage = new TextArea("Compose Message");
		composeMessage.setColumns(80);
		composeMessage.setRows(3);
		gridLayout.addComponent(composeMessage, 0, 0);
		final Button sendMessage = new Button("Send");
		gridLayout.addComponent(sendMessage, 1, 0);
		gridLayout.setComponentAlignment(sendMessage, Alignment.BOTTOM_LEFT);
		container.addComponent(gridLayout);
	}

	private void addMessageDisplayComponent(final ComponentContainer container) {
		final TabSheet ts = new TabSheet();
		ts.setSizeFull();
		ts.addTab(getMessageList(), "Friends", null);
		ts.addTab(getMessageList(), "Firehose", null);
		ts.addTab(getMessageList(), "@sanity", null);
		ts.addTab(getMessageList(), "#tahrir", null);
		container.addComponent(ts);
	}

	private Component getMessageList() {
		final Panel pl = new Panel();
		pl.setSizeFull();
		for (int x = 0; x < 30; x++) {
			addMessage(pl);
		}
		return pl;
	}

	private void addMessage(final ComponentContainer container) {
		final GridLayout gl = new GridLayout(2, 3);
		gl.setSpacing(true);
		gl.setSizeFull();
		final Label message = new Label("I have something to say, and I'm going to say it");
		gl.addComponent(message, 0, 0, 0, 2);
		final Button reply = new Button("Reply");
		reply.setStyleName(BaseTheme.BUTTON_LINK);
		gl.addComponent(reply, 1, 2);
		container.addComponent(gl);
	}
}
