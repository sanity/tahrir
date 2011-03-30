package tahrir.tools;

import java.util.Random;
import java.util.concurrent.*;

public class TrUtils {
	public static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

	public static final Random rand = new Random();
}
