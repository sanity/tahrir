package tahrir.io.net.microblogging;

import java.io.*;
import java.security.interfaces.RSAPublicKey;

import org.slf4j.*;

import tahrir.tools.TrUtils;

import com.google.gson.JsonParseException;

/**
 * For finding what the shortened public key is.
 * 
 * The shortened public key is a human readable sequence of ints derived from the public key (4 ints) useful for user to distinguish
 * two different users with the same nick name. Each node choices a different index to get the ints from for security reasons. The
 * indices to use are persisted for consistency.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class ShortenedPublicKeyFinder {
	public static Logger logger = LoggerFactory.getLogger(ShortenedPublicKeyFinder.class);

	private static int LENGTH_TO_CHECK = 617;

	private final File indicesToUseFile;

	private int[] indicesToUse = new int[4];

	public ShortenedPublicKeyFinder(final File publicKeyCharsFile) {
		indicesToUseFile = publicKeyCharsFile;
		tryLoadIndicesToUse();
	}

	public String getShortenedKey(final RSAPublicKey publicKey) {
		// we need to append something to the end
		final StringBuilder builder = new StringBuilder();
		final String publicKeyString = publicKey.getModulus().toString();
		for (final int intToUse : indicesToUse) {
			builder.append(publicKeyString.charAt(intToUse));
		}
		return builder.toString();
	}

	private void tryLoadIndicesToUse() {
		if (indicesToUseFile.exists()) {
			logger.info("Loading public key chars file");
			try {
				indicesToUse = TrUtils.parseJson(indicesToUseFile, indicesToUse.getClass());
			} catch (final JsonParseException jsonException) {
				logger.error("Error parsing public key chars Json");
			} catch (final IOException ioException) {
				logger.error("Error reading public key chars file");
			}
		} else {
			createNewIndicesToUseFile();
		}
	}

	private void createNewIndicesToUseFile() {
		logger.info("Creating new public key ints to use");
		setPublicKeyIntsToUse();
		try {
			final FileWriter intsToUseWriter = new FileWriter(indicesToUseFile);
			intsToUseWriter.write(TrUtils.gson.toJson(indicesToUse));
			intsToUseWriter.close();
		} catch (final IOException ioException) {
			logger.error("Error writing public key chars file");
			throw new RuntimeException(ioException);
		}
	}


	private void setPublicKeyIntsToUse() {
		for (int i = 0; i < indicesToUse.length; i++) {
			indicesToUse[i] = TrUtils.rand.nextInt(LENGTH_TO_CHECK);
		}
	}
}
