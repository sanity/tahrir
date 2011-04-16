package tahrir.io.net.sessions;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
	double value();
}
