package tahrir.api; /**
 * Created by Tejas Dharamsi on 6/11/2014.
 */

import static spark.Spark.*;

import com.google.gson.Gson;
import spark.*;

import spark.ResponseTransformerRoute;

public abstract class JsonTransformer extends ResponseTransformerRoute {

    private Gson gson = new Gson();

    protected JsonTransformer(String path) {
        super(path);
    }

    protected JsonTransformer(String path, String acceptType) {
        super(path, acceptType);
    }

    @Override
    public String render(Model model) {
        return gson.toJson(model.getModel());
    }

}