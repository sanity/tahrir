package tahrir.io.net.broadcasts.broadcastMessages;

import com.google.common.base.Optional;
import nu.xom.*;
import tahrir.TrConstants;
import tahrir.io.net.broadcasts.IdentityStore;
import tahrir.io.net.broadcasts.UserIdentity;

/**
 * BroadcastMessage in xml format.
 *
 * @author Ravi Tejasvi <ravitejasvi@gmail.com>
 */

public class ParsedBroadcastMessage {

    public Document broadcastMessageDocument;
    private long timeCreated;

    /**

     * Format for xml is
     * <mb>
     *     <txt>
     *         <mtn></mtn>
     *     </txt>
     * </mb>
     */

    public ParsedBroadcastMessage(){
        //for serialization
    }

    private ParsedBroadcastMessage(Document broadcastMessageDocument, long timeCreated){
        this.broadcastMessageDocument = broadcastMessageDocument;
        this.timeCreated = timeCreated;
    }

    public static ParsedBroadcastMessage createFromPlaintext(String plaintextBroadcastMessage, String languageCode, IdentityStore identityStore, long timeCreated){
        Element rootElement = new Element(TrConstants.FormatInfo.ROOT);

        Document broadcastMessageDocument = new Document(rootElement);
        Element plainText = generatePlainTextElement(plaintextBroadcastMessage, identityStore, languageCode);

        rootElement.appendChild(plainText);

        return new ParsedBroadcastMessage(broadcastMessageDocument, timeCreated);
    }

    private static Element generatePlainTextElement(final String plaintextBroadcastMessage, final IdentityStore identityStore, String languageCode) {
        Attribute language = new Attribute("lang", languageCode);
        Element plainText = new Element(TrConstants.FormatInfo.PLAIN_TEXT);
        plainText.addAttribute(language);
        int position = 0;
        while (position < plaintextBroadcastMessage.length()) {
            if (plaintextBroadcastMessage.charAt(position) == '@') {
                int endOfMention = position + 1;
                while (Character.isLetterOrDigit(plaintextBroadcastMessage.charAt(endOfMention))) {
                    endOfMention++;
                }
                String mentionNickname = plaintextBroadcastMessage.substring(position+1, endOfMention);
                Optional<UserIdentity> optionalIdentityWithNick = identityStore.getIdentityWithNick(mentionNickname);
                if (optionalIdentityWithNick.isPresent()) {
                    Element mention = createMentionElement(optionalIdentityWithNick.get());
                    plainText.appendChild(mention);
                } else {
                    plainText.appendChild("@"+mentionNickname);
                }
                position = endOfMention;
            } else {
                int indexOfNextMention = plaintextBroadcastMessage.indexOf('@', position);
                int readTextUpTo;
                if (indexOfNextMention != -1) {
                    readTextUpTo = indexOfNextMention;
                } else {
                    readTextUpTo = plaintextBroadcastMessage.length();
                }
                String nextTextBlock = plaintextBroadcastMessage.substring(position, readTextUpTo);
                plainText.appendChild(nextTextBlock);
                if (indexOfNextMention == -1) {
                    break;
                }
                position = indexOfNextMention;
            }
        }
        return plainText;
    }

    private static Element createMentionElement(final UserIdentity identityWithNick) {
        Attribute publicKey = new Attribute("pubKey", identityWithNick.getPubKey().toString());
        Element mention = new Element(TrConstants.FormatInfo.MENTION);
        mention.addAttribute(publicKey);
        mention.appendChild(identityWithNick.getNick());
        return mention;
    }

    @Override
    public int hashCode() {
        int result = broadcastMessageDocument != null ? broadcastMessageDocument.hashCode() : 0;
        result = 31 * result + (int) (timeCreated ^ (timeCreated >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParsedBroadcastMessage that = (ParsedBroadcastMessage) o;

        if (timeCreated != that.timeCreated) return false;
        if (broadcastMessageDocument != null ? !broadcastMessageDocument.equals(that.broadcastMessageDocument) : that.broadcastMessageDocument != null)
            return false;

        return true;
    }

    public long getTimeCreated(){
        return this.timeCreated;
    }

    public String asXmlString(){
        return this.broadcastMessageDocument.toXML();
    }

    public String getPlainTextBroadcastMessage(){
        StringBuilder plaintextBroadcastMessage = new StringBuilder();
        writeNode(broadcastMessageDocument.getRootElement(), plaintextBroadcastMessage);
        return plaintextBroadcastMessage.toString();
    }

    private void writeNode(Node node, StringBuilder stringBuilder) {
        if (node instanceof ParentNode) {
            ParentNode parentNode = (ParentNode) node;
            Element mentionElement = new Element((Element)node);
            if(mentionElement.getLocalName().equals(TrConstants.FormatInfo.MENTION)){
                stringBuilder.append('@');
            }
            for (int childIndex = 0; childIndex < parentNode.getChildCount(); childIndex++) {
                writeNode(parentNode.getChild(childIndex), stringBuilder);
            }
        } else if (node instanceof Text) {
            Text text = (Text) node;
            stringBuilder.append(text.getValue());
        }
    }
}