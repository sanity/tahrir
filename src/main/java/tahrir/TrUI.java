package tahrir;

import com.vaadin.ui.VerticalLayout;

/**
 * Created by oliverl3 on 2/15/14.
 */
public interface TrUI {

    public TrNode getNode();

    public void createClosableTab(final String tabName, final java.awt.Component tabContents);


}
