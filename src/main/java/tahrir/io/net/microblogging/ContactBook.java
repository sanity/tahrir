package tahrir.io.net.microblogging;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import org.slf4j.*;

import tahrir.tools.TrUtils;

import com.google.gson.JsonParseException;

/**
 * For persistent management of user's contacts.
 * 
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 *
 */
public class ContactBook {
	private static Logger logger = LoggerFactory.getLogger(ContactBook.class);

	private ContactsContainer contacts = new ContactsContainer();

	private File contactsFile;

	private final boolean persistingContacts;

	/**
	 * Create a ContactBook which loads previous contacts recorded.
	 * @param contactsFile File which contains the contacts previously recorded.
	 */
	public ContactBook(final File contactsFile) {
		this.contactsFile = contactsFile;
		persistingContacts = true;
		tryLoadContactsFromFile();
	}

	/**
	 * Create a ContactBook which doesn't try load contacts previously recored.
	 */
	public ContactBook() {
		persistingContacts = false;
	}

	public void addContact(final String nickName, final RSAPublicKey publicKey) {
		contacts.add(publicKey, new ContactInformation(nickName, publicKey));
		if (persistingContacts) {
			addContactsToFile();
		}
	}

	public ContactInformation getContact(final RSAPublicKey publicKey) {
		return contacts.getContact(publicKey);
	}

	public boolean hasContact(final RSAPublicKey publicKey) {
		return contacts.hasContact(publicKey);
	}

	private void addContactsToFile() {
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

	public static class ContactInformation {
		private String nickName;
		// this may be redundant as we are already storing public key in the map
		private RSAPublicKey pubKey;

		// For serialization
		public ContactInformation() {

		}

		public ContactInformation(final String nickName, final RSAPublicKey pubKey) {
			this.nickName = nickName;
			this.pubKey = pubKey;
		}

		public String getNickName() {
			return nickName;
		}
	}

	public static class ContactsContainer {
		private final LinkedHashMap<RSAPublicKey, ContactInformation> contacts;

		public ContactsContainer() {
			contacts = new LinkedHashMap<RSAPublicKey, ContactInformation>();
		}

		/**
		 * This method is only public for serialization purposes. Do NOT call it to add a contact normally
		 * as persistence will FAIL. This is no doubt a bad way to do things and should be refactored eventually.
		 * @param publicKey
		 * @param contactInfo
		 */
		public synchronized void add(final RSAPublicKey publicKey, final ContactInformation contactInfo) {
			contacts.put(publicKey, contactInfo);
		}

		public synchronized ContactInformation getContact(final RSAPublicKey publicKey) {
			return contacts.get(publicKey);
		}

		public synchronized List<ContactInformation> getContacts() {
			final ArrayList<ContactInformation> contactsSnapShot = new ArrayList<ContactInformation>(contacts.values());
			return contactsSnapShot;
		}

		public synchronized boolean hasContact(final RSAPublicKey publicKey) {
			if (getContact(publicKey) != null)
				return true;
			else
				return false;
		}
	}
}
