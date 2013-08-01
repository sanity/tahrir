package tahrir.io.net.broadcasts.broadcastMessages;

import nu.xom.*;
import tahrir.TrConstants;

import java.io.IOException;

/**
 * BroadcastMessage in xml format.
 *
 * @author Ravi Tejasvi <ravitejasvi@gmail.com>
 */

public class ParsedBroadcastMessage {

    private Document broadcastMessageDocument;
    private long timeCreated;
    /**
     * Format for xml is
     * <mb>
     *     <txt>
     *         <mtn></mtn>
     *     </txt>
     * </mb>
     */

    private ParsedBroadcastMessage(Document broadcastMessageDocument){
        this.broadcastMessageDocument = broadcastMessageDocument;
        this.timeCreated = System.currentTimeMillis();
    }

    public long getTimeCreated(){
        return this.timeCreated;
    }

    public static ParsedBroadcastMessage createFromPlaintext(String plaintextBroadcastMessage, String languageCode){
        Element rootElement = new Element(TrConstants.FormatInfo.ROOT);
        Element plainText = new Element(TrConstants.FormatInfo.PLAIN_TEXT);
        Element mention = new Element(TrConstants.FormatInfo.MENTION);
        Document broadcastMessageDocument = new Document(rootElement);
        broadcastMessageDocument.appendChild(plainText);
        Attribute language = new Attribute("lang", languageCode);
        plainText.addAttribute(language);
        for(int charPos = 0; charPos < plaintextBroadcastMessage.length(); charPos++){
            if(plaintextBroadcastMessage.charAt(charPos) == '@'){
                StringBuilder tempBroadcastMessageMentionPart = new StringBuilder();
                plainText.appendChild(mention);
                charPos++;
                while(plaintextBroadcastMessage.charAt(charPos)!= ' '){
                    tempBroadcastMessageMentionPart.append(plaintextBroadcastMessage.charAt(charPos));
                    charPos++;
                }
                mention.appendChild(tempBroadcastMessageMentionPart.toString());
            }
            else{
                StringBuilder tempBroadcastMessagePart = new StringBuilder();
                while(plaintextBroadcastMessage.charAt(charPos)!= ' '){
                    tempBroadcastMessagePart.append(plaintextBroadcastMessage.charAt(charPos));
                    charPos++;
                }
                plainText.appendChild(tempBroadcastMessagePart.toString());
            }
        }
        return new ParsedBroadcastMessage(broadcastMessageDocument);
    }

    public static ParsedBroadcastMessage createFromBroadcastMessageInXML(String broadcastMessageInXml){
        try {
            return new ParsedBroadcastMessage(new Builder().build(broadcastMessageInXml, null));
        } catch (ParsingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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