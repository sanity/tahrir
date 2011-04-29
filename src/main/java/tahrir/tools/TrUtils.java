package tahrir.tools;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Random;
import java.util.concurrent.*;

import com.google.gson.*;

public class TrUtils {
	public static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

	public static final Random rand = new Random();

	public static final Gson gson = new GsonBuilder().create();

	public static final Runnable noopRunnable = new Runnable() {

		public void run() {
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> T parseJson(final File jsonFile, final Type type) throws JsonParseException, IOException {
		final FileReader json = new FileReader(jsonFile);
		json.close();
		return (T) gson.<Object> fromJson(json, type);
	}

	public static <T> T parseJson(final String json, final Class<T> type) throws JsonParseException {
		return gson.fromJson(json, type);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseJson(final String json, final Type type) throws JsonParseException {
		return (T) gson.<Object> fromJson(json, type);
	}
}
