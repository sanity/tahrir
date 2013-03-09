package tahrir.io.net.microblogging;

import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.io.crypto.TrCrypto;
import tahrir.tools.TrUtils;

import java.io.*;
import java.security.interfaces.RSAPublicKey;

/**
 * For finding what the shortened public key is.
 *
 * The shortened public key (spk) is an abbreviated version of a public key in base64 format. It is useful, in
 * combination with an alias, for making public keys human readable, as the key would be too long otherwise. The indexes
 * used to create the spk are different for every node to prevent easy spoofing.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */

public class ShortenedPublicKeyFinder {
	private static final Logger logger = LoggerFactory.getLogger(ShortenedPublicKeyFinder.class);
	private static final int PUBLIC_KEY_SIZE_IN_BASE64 = 392;

	/**
	 * The offsets which this node is using for finding a spk.
	 */
	private int[] indexes;

	/**
	 * Create a spk finder.
	 * @param fileToUse A valid file, in json format, which specifies what indexes this node is using for a spk or
	 * an empty valid file if node is being setup.
	 */
	public ShortenedPublicKeyFinder(final File fileToUse) {
		FileReader fr = null;
		try {
			fr = new FileReader(fileToUse);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Invalid file given.", e);
		}
		indexes = TrUtils.gson.fromJson(fr, int[].class);
		// if gson loaded nothing node we need settings up
		if (indexes == null || indexes.length != TrConstants.SHORTENED_PUBLIC_KEY_SIZE) {
			logger.info("Creating new indexes to use");
			initIndexes(fileToUse);
		} else {
			logger.info("Successfully loaded previous indexes");
		}
	}

	/**
	 * Get a spk.
	 * @return The spk in base64 format.
	 */
	public String getShortenedKey(final RSAPublicKey publicKey) {
		String asBase64 = TrCrypto.toBase64(publicKey);
		StringBuilder builder = new StringBuilder();
		for (int offset : indexes) {
			builder.append(asBase64.charAt(offset));
		}
		return builder.toString();
	}

	private void initIndexes(File persistTo) {
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = TrUtils.rand.nextInt(PUBLIC_KEY_SIZE_IN_BASE64 + 1);
		}
		try {
			TrUtils.writeJson(indexes, persistTo);
		} catch (IOException e) {
			logger.warn("Couldn't write indexes.");
		}
	}
}
