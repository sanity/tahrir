package tahrir.io.net.broadcasts.broadcastMessages;

import nu.xom.*;
import tahrir.TrConstants;

import java.io.IOException;
import java.util.Scanner;

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
        Scanner pbmScanner = new Scanner(plaintextBroadcastMessage);
        while(pbmScanner.hasNext()){
            String tempBroadcastMessagePart = pbmScanner.next();
            if(tempBroadcastMessagePart.startsWith("@")){
                plainText.appendChild(mention);
                mention.appendChild(tempBroadcastMessagePart);
            }
            else{
                plainText.appendChild(tempBroadcastMessagePart);
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