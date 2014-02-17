package tahrir.vaadin;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletService;
import tahrir.TrNode;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by oliverl3 on 1/19/14.
 */
public class TahrirVaadinRequest extends VaadinServletRequest{

    private TrNode node;

    public TrNode getNode(){
        return node;
    }

    public TahrirVaadinRequest(HttpServletRequest request, VaadinServletService vaadinService, TrNode n) {
        super(request, vaadinService);
        node=n;
    }

}
