package tahrir.io.net.broadcasts.broadcastMessages;

import nu.xom.*;
import tahrir.TrConstants;
import tahrir.io.net.broadcasts.IdentityStore;

import java.io.IOException;
import java.util.Scanner;

/**
 * BroadcastMessage in xml format.
 *
 * @author Ravi Tejasvi <ravitejasvi@gmail.com>
 */

public class ParsedBroadcastMessage {

    public Document broadcastMessageDocument;
    private long timeCreated;

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

    @Override
    public int hashCode() {
        int result = broadcastMessageDocument != null ? broadcastMessageDocument.hashCode() : 0;
        result = 31 * result + (int) (timeCreated ^ (timeCreated >>> 32));
        return result;
    }

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

    private ParsedBroadcastMessage(Document broadcastMessageDocument){
        this.broadcastMessageDocument = broadcastMessageDocument;
        this.timeCreated = System.currentTimeMillis();
    }

    public long getTimeCreated(){
        return this.timeCreated;
    }

    public static ParsedBroadcastMessage createFromPlaintext(String plaintextBroadcastMessage, String languageCode, IdentityStore identityStore){
        Element rootElement = new Element(TrConstants.FormatInfo.ROOT);
        Element plainText = new Element(TrConstants.FormatInfo.PLAIN_TEXT);
        Element mention = new Element(TrConstants.FormatInfo.MENTION);
        Document broadcastMessageDocument = new Document(rootElement);
        int positionOfScanner = 0;
        Attribute language = new Attribute("lang", languageCode);
        plainText.addAttribute(language);
        rootElement.appendChild(plainText);
        Scanner pbmScanner = new Scanner(plaintextBroadcastMessage);
        while(pbmScanner.hasNext()){
            String mentionPartWithoutAtSymbol;
            String tempBroadcastMessagePart = pbmScanner.next();
            positionOfScanner++;

            if(tempBroadcastMessagePart.startsWith("@")){
                plainText.appendChild(mention);
                mentionPartWithoutAtSymbol = tempBroadcastMessagePart.substring(1);
                char tempChar = mentionPartWithoutAtSymbol.charAt(mentionPartWithoutAtSymbol.length()-1);
                if(!(Character.isLetter(tempChar)||Character.isDigit(tempChar))){
                    mentionPartWithoutAtSymbol = mentionPartWithoutAtSymbol.substring(0, mentionPartWithoutAtSymbol.length()-1);
                    Attribute publicKey = new Attribute("pubKey", identityStore.getIdentityWithNick(mentionPartWithoutAtSymbol).get().getPubKey().toString());
                    mention.addAttribute(publicKey);
                    mention.appendChild(mentionPartWithoutAtSymbol);
                    StringBuilder afterMention = new StringBuilder();
                    afterMention.append(tempChar);
                    afterMention.append(' ');
                    plainText.appendChild(afterMention.toString());
                }
                else{
                Attribute publicKey = new Attribute("pubKey", identityStore.getIdentityWithNick(mentionPartWithoutAtSymbol).get().getPubKey().toString());
                mention.addAttribute(publicKey);
                mention.appendChild(mentionPartWithoutAtSymbol+" ");
                }

            }
            else{
                Scanner tempScanner = new Scanner(plaintextBroadcastMessage);
                for(int i= 0 ; i<positionOfScanner; i++)
                    tempScanner.next();
                while(pbmScanner.hasNext()){
                    String temp = tempScanner.next();
                    if(!(temp.startsWith("@"))){
                        tempBroadcastMessagePart+=" "+temp;
                        pbmScanner.next();

                    }
                    else{
                        break;
                    }
                }
                plainText.appendChild(tempBroadcastMessagePart+" ");
            }
        }

        return new ParsedBroadcastMessage(broadcastMessageDocument);
    }

   /* public static ParsedBroadcastMessage createFromBroadcastMessageInXML(String broadcastMessageInXml){
        try {
            return new ParsedBroadcastMessage(new Builder().build(broadcastMessageInXml, ""));
        } catch (ParsingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public String asXmlString(){
        return this.broadcastMessageDocument.toXML();
    }

    public String getPlainTextBroadcastMessage(){
        StringBuilder plaintextBroadcastMessage = new StringBuilder();
        writeNode(broadcastMessageDocument.getRootElement(), plaintextBroadcastMessage);
        if(plaintextBroadcastMessage.toString().endsWith(" ")){
            //taking care of the additional " " added at the end while creating doc.
            plaintextBroadcastMessage.deleteCharAt(plaintextBroadcastMessage.length()-1);
        }
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