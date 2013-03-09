package tahrir.io.net.microblogging;

import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.tools.TrUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages a user's contacts which is analogous to who you are "following" on Twitter.
 * <p/>
 * The contacts are persisted.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class ContactBook {
	private static Logger logger = LoggerFactory.getLogger(ContactBook.class);

	private ConcurrentMap<RSAPublicKey, String> contacts;
	private final File contactsFile;

	/**
	 * Create a ContactBook which loads previous contacts recorded.
	 *
	 * @param contactsFile File which contains the contacts previously recorded.
	 */
	public ContactBook(final File contactsFile) {
		this.contactsFile = contactsFile;
		try {
			Reader reader = new FileReader(contactsFile);
			logger.info("Trying to load contacts.");
			loadContacts(reader);
		} catch (FileNotFoundException e) {
			logger.error("Couldn't find contacts file");
			e.printStackTrace();
		}
	}

	public void addContact(final String alias, final RSAPublicKey publicKey) {
		contacts.put(publicKey, alias);
		persistContact();
	}

	public String getAlias(final RSAPublicKey publicKey) {
		return contacts.get(publicKey);
	}

	public boolean hasContact(final RSAPublicKey publicKey) {
		return contacts.containsKey(publicKey);
	}

	public Map<RSAPublicKey, String> getContacts() {
		// should be safe as map is concurrent
		return contacts;
	}

	private void persistContact() {
		logger.info("Adding contact to file");
		try {
			// bad implementation: has to write entire map every time
			final FileWriter contactsWriter = new FileWriter(contactsFile);
			contactsWriter.write(TrUtils.gson.toJson(contacts));
			contactsWriter.close();
		} catch (final IOException ioException) {
			logger.error("Problem writting contacts to file: the contacts weren't saved.");
			ioException.printStackTrace();
		}
	}

	private void loadContacts(Reader reader) {
		Type contactsType = new TypeToken<Map<RSAPublicKey, String>>() {}.getType();
		contacts = TrUtils.gson.fromJson(reader, contactsType);
		// if the json wasn't loaded successfully
		if (contacts == null) {
			logger.info("Failed to load any contacts. Creating new contacts book.");
			contacts = Maps.newConcurrentMap();
		} else {
			logger.info("Contacts loaded from file.");
		}
	}
}
