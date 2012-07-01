package tahrir.io.net.microblogging;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.tools.TrUtils;

import com.google.gson.JsonParseException;
import com.google.inject.internal.MapMaker;

public class ContactBook {
	public static Logger logger = LoggerFactory.getLogger(ContactBook.class);

	public ContactsContainer contactsInformation = new ContactsContainer();

	private int[] intsToUse = new int[4];

	private final File contactsFile;
	private final File publicKeyCharsFile;

	public ContactBook(final TrNode node) {
		contactsFile = new File(node.rootDirectory, node.config.contacts);
		publicKeyCharsFile = new File(node.rootDirectory, node.config.publicKeyChars);
		tryLoadContactsFromFile();
		tryLoadPublicKeyCharsFile();
	}

	/**
	 * Adds a contact to the contact book
	 * @return If the preferred nickname was given
	 */
	public boolean addContact(final String preferedNick, final RSAPublicKey publicKey) {
		boolean gotPreferedNick;
		if (!contactsHasNickName(preferedNick)) {
			contactsInformation.add(publicKey, new ContactInformation(preferedNick));
			gotPreferedNick = true;
		} else {
			// we need to append something to the end
			final StringBuilder builder = new StringBuilder();
			final String publicKeyString = publicKey.toString();
			for (final int intToUse : intsToUse) {
				builder.append(publicKeyString.charAt(intToUse));
			}
			final String toAppend = builder.toString();
			// TODO: does not check if the appended nick name with appended already exists
			contactsInformation.add(publicKey, new ContactInformation(preferedNick, toAppend));
			gotPreferedNick = false;
		}

		addContactsToFile();
		return gotPreferedNick;
	}

	private boolean contactsHasNickName(final String nickName) {
		for (final ContactInformation contact : contactsInformation.getContacts()) {
			if (contact.getFullNick().equals(nickName))
				return true;
		}
		return false;
	}

	/*
	 * If they are duplicates of a nick name then we need something to append to the end of it
	 * these ints are chosen by this method.
	 */
	private void setPublicKeyIntsToUse() {
		final int publicKeySize = 350;
		for (int i = 0; i < intsToUse.length; i++) {
			intsToUse[i] = TrUtils.rand.nextInt(publicKeySize);
		}
	}

	private void addContactsToFile() {
		// instead of doing entire object modify contact if exists?
		logger.info("Saving contact to file");
		try {
			final FileWriter contactsWriter = new FileWriter(contactsFile);
			contactsWriter.write(TrUtils.gson.toJson(contactsInformation));
			contactsWriter.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void tryLoadContactsFromFile() {
		if (contactsFile.exists()) {
			logger.info("Loading contacts from file");
			try {
				final FileReader json = new FileReader(contactsFile);
				contactsInformation = TrUtils.gson.fromJson(json, ContactsContainer.class);
				json.close();
			} catch (final JsonParseException jsonException) {
				throw new RuntimeException(jsonException);
			} catch (final IOException ioException) {
				throw new RuntimeException(ioException);
			}
		} else {
			logger.info("Making new contacts container");
			contactsInformation = new ContactsContainer();
		}
	}

	private void tryLoadPublicKeyCharsFile() {
		// might be better if this used Tahrir's serializer seeing as we don't want it to be edited
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
			logger.info("Creating new public key ints to use");
			setPublicKeyIntsToUse();
			try {
				final FileWriter intsToUseWriter = new FileWriter(publicKeyCharsFile);
				intsToUseWriter.write(TrUtils.gson.toJson(intsToUse));
				intsToUseWriter.close();
			} catch (final IOException ioException) {
				ioException.printStackTrace();
				logger.error("Error writing public key chars file");
			}
		}
	}

	public static class ContactInformation {
		private final String nickName;
		private final String appendedToNick;

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
		private final Map<RSAPublicKey, ContactInformation> contacts;

		public ContactsContainer() {
			contacts = new MapMaker().makeMap();
		}


		public void add(final RSAPublicKey publicKey, final ContactInformation contact) {
			contacts.put(publicKey, contact);
		}

		public ContactInformation getContact(final RSAPublicKey publicKey) {
			return contacts.get(publicKey);
		}

		public Collection<ContactInformation> getContacts() {
			return contacts.values();
		}
	}
}
