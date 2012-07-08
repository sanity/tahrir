package tahrir.io.net.microblogging;

import java.io.*;
import java.security.interfaces.RSAPublicKey;

import org.slf4j.*;

import tahrir.tools.TrUtils;

import com.google.gson.JsonParseException;

public class DuplicateNameAppender {
	public static Logger logger = LoggerFactory.getLogger(DuplicateNameAppender.class);

	private static int LENGTH_TO_CHECK = 617;

	private final File publicKeyCharsFile;

	private int[] intsToUse = new int[4];

	public DuplicateNameAppender(final File publicKeyCharsFile) {
		this.publicKeyCharsFile = publicKeyCharsFile;
		tryLoadPublicKeyCharsFile();
	}

	public String getIntsToAppend(final RSAPublicKey publicKey) {
		// we need to append something to the end
		final StringBuilder builder = new StringBuilder();
		final String publicKeyString = publicKey.getModulus().toString();
		for (final int intToUse : intsToUse) {
			builder.append(publicKeyString.charAt(intToUse));
		}
		return builder.toString();
	}

	private void tryLoadPublicKeyCharsFile() {
		if (publicKeyCharsFile.exists()) {
			logger.info("Loading public key chars file");
			try {
				intsToUse = TrUtils.parseJson(publicKeyCharsFile, intsToUse.getClass());
			} catch (final JsonParseException jsonException) {
				logger.error("Error parsing public key chars Json");
			} catch (final IOException ioException) {
				logger.error("Error reading public key chars file");
			}
		} else {
			createNewPublicKeyCharsFile();
		}
	}

	private void createNewPublicKeyCharsFile() {
		logger.info("Creating new public key ints to use");
		setPublicKeyIntsToUse();
		try {
			final FileWriter intsToUseWriter = new FileWriter(publicKeyCharsFile);
			intsToUseWriter.write(TrUtils.gson.toJson(intsToUse));
			intsToUseWriter.close();
		} catch (final IOException ioException) {
			logger.error("Error writing public key chars file");
			throw new RuntimeException(ioException);
		}
	}


	private void setPublicKeyIntsToUse() {
		// TODO: 300 is temporary, what is proper value for this?
		for (int i = 0; i < intsToUse.length; i++) {
			intsToUse[i] = TrUtils.rand.nextInt(LENGTH_TO_CHECK);
		}
	}
}
