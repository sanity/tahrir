package tahrir.io.net.microblogging.microblogs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.Sets;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import tahrir.TrConstants;
import tahrir.io.net.microblogging.BroadcastMessageParser.MentionPart;
import tahrir.io.net.microblogging.BroadcastMessageParser.ParsedPart;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Set;

/**
 * BroadcastMessage in xml format.
 *
 * @author Ravi Tejasvi <ravitejasvi@gmail.com>
 */

public class ParsedBroadcastMessage {

    private Document broadcastMessageDocument;
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
    }

    public static ParsedBroadcastMessage createParsedBroadcastMessage(String unparsedBroadcastMessage){
        Element rootElement = new Element(TrConstants.FormatInfo.ROOT);
        Element plainText = new Element(TrConstants.FormatInfo.PLAIN_TEXT);
        Element mention = new Element(TrConstants.FormatInfo.MENTION);
        Document broadcastMessageDocument = new Document(rootElement);
        broadcastMessageDocument.appendChild(plainText);
        for(int charPos = 0; charPos < unparsedBroadcastMessage.length(); charPos++){
            if(unparsedBroadcastMessage.charAt(charPos) == '@'){
                StringBuilder tempBroadcastMessageMentionPart = new StringBuilder();
                plainText.appendChild(mention);
                charPos++;
                while(unparsedBroadcastMessage.charAt(charPos)!= ' '){
                    tempBroadcastMessageMentionPart.append(unparsedBroadcastMessage.charAt(charPos));
                    charPos++;
                }
                mention.appendChild(tempBroadcastMessageMentionPart.toString());
            }
            else{
                StringBuilder tempBroadcastMessagePart = new StringBuilder();
                while(unparsedBroadcastMessage.charAt(charPos)!= ' '){
                    tempBroadcastMessagePart.append(unparsedBroadcastMessage.charAt(charPos));
                    charPos++;
                }
                plainText.appendChild(tempBroadcastMessagePart.toString());
            }
        }
        return new ParsedBroadcastMessage(broadcastMessageDocument);
    }

    public ParsedBroadcastMessage(String broadcastMessageInXml){
        try {
            this.broadcastMessageDocument = new Builder().build(broadcastMessageInXml, null);
        } catch (ParsingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String docToXmlString(){
        return this.broadcastMessageDocument.toXML();
    }

    public String getUnparsedBroadcastMessage(){
        String docInXmlString = this.docToXmlString();
        StringBuilder unparsedBroadcastMessage = new StringBuilder();
        for(char charPos = 0; charPos< docInXmlString.length(); charPos++){
            if(docInXmlString.charAt(charPos) == '<'){
                charPos++;
                if(docInXmlString.charAt(charPos) == 'm'){
                    unparsedBroadcastMessage.append('@');
                    charPos++;
                }
                while(docInXmlString.charAt(charPos)!= '>'){
                    charPos++;
                }
            }
            else{
                unparsedBroadcastMessage.append(docInXmlString.charAt(charPos));
            }
        }
        return unparsedBroadcastMessage.toString();
    }
}