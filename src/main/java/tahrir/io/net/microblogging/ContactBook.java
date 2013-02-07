package tahrir.io.net.microblogging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tahrir.tools.TrUtils;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;

/**
 * Manages a user's contacts. The contacts are persisted.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class ContactBook {
	private static Logger logger = LoggerFactory.getLogger(ContactBook.class);

	private Map<RSAPublicKey, String> contacts = Maps.newConcurrentMap();

	private final File contactsFile;

	/**
	 * Create a ContactBook which loads previous contacts recorded.
	 * @param contactsFile File which contains the contacts previously recorded.
	 */
	public ContactBook(final File contactsFile) {
		this.contactsFile = contactsFile;
		tryLoadContactsFromFile();
	}

	public void addContact(final String nickName, final RSAPublicKey publicKey) {
		contacts.put(publicKey, nickName);
		addToFile();
	}

	public String getContact(final RSAPublicKey publicKey) {
		return contacts.get(publicKey);
	}

	public boolean hasContact(final RSAPublicKey publicKey) {
		return contacts.containsKey(publicKey);
	}

	/*
	 * This method is messy because it rewrites the entire map everytime, it would
	 * be better if it only wrote the new contact.
	 */
	private void addToFile() {
		logger.info("Adding contact to file");
		try {
			final FileWriter contactsWriter = new FileWriter(contactsFile);
			contactsWriter.write(TrUtils.gson.toJson(contacts));
			contactsWriter.close();
		} catch (final IOException ioException) {
			logger.error("Error writing to contacts file");
			throw new RuntimeException(ioException);
		}
	}

	private void tryLoadContactsFromFile() {
		if (contactsFile.exists()) {
			logger.info("Loading contacts from file");
			try {
				final BufferedReader br = new BufferedReader(new FileReader(contactsFile));
				final StringBuilder builder = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					builder.append(line);
				}

				final String json = builder.toString();
				contacts = TrUtils.gson.fromJson(json, ContactsContainer.class);

				br.close();
			} catch (final JsonParseException jsonException) {
				logger.error("Json exception when parsing contacts file");
				throw new RuntimeException(jsonException);
			} catch (final IOException ioException) {
				logger.error("Error reading public key chars file");
				throw new RuntimeException(ioException);
			}
		} else {
			logger.info("Making new contacts container");
			contacts = new ContactsContainer();
		}
	}
}
