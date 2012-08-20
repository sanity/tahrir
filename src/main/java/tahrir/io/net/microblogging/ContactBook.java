package tahrir.io.net.microblogging;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.tools.TrUtils;

import com.google.gson.JsonParseException;

public class ContactBook {
	private static Logger logger = LoggerFactory.getLogger(ContactBook.class);

	private ContactsContainer contactsContainer = new ContactsContainer();

	private final File contactsFile;

	private final TrNode node;

	public ContactBook(final TrNode node, final File contactsFile) {
		this.contactsFile = contactsFile;
		this.node = node;
		tryLoadContactsFromFile();
	}

	public boolean addContact(final String preferedNick, final RSAPublicKey publicKey) {
		boolean gotPreferedNick;
		if (!contactsContainer.hasNickName(preferedNick)) {
			contactsContainer.add(publicKey, new ContactInformation(preferedNick));
			gotPreferedNick = true;
		} else {
			// need to append something to end to make it unique
			final String toAppend = node.mbClasses.duplicateNameAppender.getIntsToAppend(publicKey);
			// TODO: very unlikely but does not check if the appended nick name with appended already exists
			contactsContainer.add(publicKey, new ContactInformation(preferedNick, toAppend));
			gotPreferedNick = false;
		}

		addContactsToFile();
		return gotPreferedNick;
	}

	public ContactInformation getContact(final RSAPublicKey publicKey) {
		return contactsContainer.getContact(publicKey);
	}

	public boolean hasContact(final RSAPublicKey publicKey) {
		return contactsContainer.hasContact(publicKey);
	}

	public List<ContactInformation> getContacts() {
		return contactsContainer.getContacts();
	}

	private void addContactsToFile() {
		logger.info("Adding contact to file");
		try {
			final FileWriter contactsWriter = new FileWriter(contactsFile);
			contactsWriter.write(TrUtils.gson.toJson(contactsContainer));
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
				contactsContainer = TrUtils.gson.fromJson(json, ContactsContainer.class);

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
			contactsContainer = new ContactsContainer();
		}
	}

	public static class ContactInformation {
		private String nickName;
		private String appendedToNick;

		// For serialization
		public ContactInformation() {

		}

		public ContactInformation(final String nickName, final String appenedToNick) {
			this.nickName = nickName;
			appendedToNick = appenedToNick;
		}

		public ContactInformation(final String nickName) {
			this(nickName, null);
		}

		public String getFullNick() {
			String fullNick = nickName;
			if (appendedToNick != null) {
				fullNick += appendedToNick;
			}
			return fullNick;
		}
	}

	public static class ContactsContainer {
		private final LinkedHashMap<RSAPublicKey, ContactInformation> contacts;

		private ContactsContainer() {
			contacts = new LinkedHashMap<RSAPublicKey, ContactInformation>();
		}

		private synchronized void add(final RSAPublicKey publicKey, final ContactInformation contactInfo) {
			contacts.put(publicKey, contactInfo);
		}

		private synchronized ContactInformation getContact(final RSAPublicKey publicKey) {
			return contacts.get(publicKey);
		}

		private synchronized List<ContactInformation> getContacts() {
			final ArrayList<ContactInformation> contactsSnapShot = new ArrayList<ContactInformation>(contacts.values());
			return contactsSnapShot;
		}

		private synchronized boolean hasNickName(final String nickName) {
			for (final ContactInformation contact : contacts.values()) {
				if (contact.getFullNick().equals(nickName))
					return true;
			}
			return false;
		}

		public synchronized boolean hasContact(final RSAPublicKey publicKey) {
			if (getContact(publicKey) != null)
				return true;
			else
				return false;
		}
	}
}
