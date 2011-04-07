package tahrir.io.net;

import java.lang.annotation.*;

public interface TrSession {

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Remote {
	}
}
