package tahrir.vaadin;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Created with IntelliJ IDEA.
 * User: ian
 * Date: 10/12/13
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */

public class TestVaadinUI extends UI {
    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout view = new VerticalLayout();
        view.addComponent(new Label("Hello Vaadin!"));
        setContent(view);
    }

    @Override
    public void markAsDirty() {
    }
}
