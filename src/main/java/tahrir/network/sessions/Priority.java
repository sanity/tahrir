package tahrir.network.sessions;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
	double value();
}
