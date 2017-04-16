/**
 * Created by Tejas Dharamsi on 6/11/2014.
 */
import static spark.Spark.*;
import spark.*;
import spark.ResponseTransformerRoute;
public class JsonExample {

    public static void main(String args[]) {

        get(new JsonTransformer("/hello", "application/json") {
            @Override
            public Model handle(Request request, Response response) {
                return new Model(new MyMessage("Hello World"));
            }
        });

    }

}
