package tahrir.ui;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.net.microblogging.BroadcastMessageParser;
import tahrir.io.net.microblogging.BroadcastMessageParser.ParsedPart;
import tahrir.io.net.microblogging.microblogs.BroadcastMessage;
import tahrir.io.net.microblogging.microblogs.ParsedMicroblog;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Date;

/**
 * Represents a microblog in a panel for rendering by the table renderer.
 *
 * @author Kieran Donegan <kdonegan.92@gmail.com>
 */
public class BroadcastMessagePostPanel {
	private static final Logger logger = LoggerFactory.getLogger(BroadcastMessagePostPanel.class);
	private final JPanel content;
	private final TrMainWindow mainWindow;

	public BroadcastMessagePostPanel(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		this.mainWindow = mainWindow;
		content = new JPanel(new MigLayout());
        content.setBackground(Color.WHITE);


		addAuthorButton(mb, mainWindow);
		addPostTime(mb);
		addTextPane(mb, mainWindow);
		addReBroadcastButtons(mainWindow.node, mb);
	}

	public JComponent getContent() {
		return content;
	}

	private void addPostTime(final ParsedMicroblog mb) {
		final JLabel postTime = new JLabel(DateParser.parseTime(mb.getMbData().getTimeCreated()));
		postTime.setForeground(Color.GRAY);
		postTime.setFont(new Font("time", Font.PLAIN, postTime.getFont().getSize() - 2));
        content.add(postTime, "gap push, wrap");
	}

	private void addAuthorButton(final ParsedMicroblog mb, final TrMainWindow mainWindow) {
		final AuthorDisplayPageButton authorNick = new AuthorDisplayPageButton(mainWindow,
				mb.getMbData().getAuthorPubKey(), mb.getMbData().getAuthorNick());
		authorNick.setFont(new Font("bold", Font.BOLD, authorNick.getFont().getSize() + 2));
        authorNick.setForeground(new Color(65,131,196));     //SteelBlue color
		content.add(authorNick, "align left");
	}

    private JTextPane setTextPane(final JTextPane messageTextPane)
    {

        messageTextPane.setBackground(Color.WHITE);
        messageTextPane.setEditable(false);
        return messageTextPane;

    }
	private void addTextPane(final ParsedMicroblog mb, TrMainWindow mainWindow) {
        final JTextPane messageTextPane = new JTextPane();
        setTextPane(messageTextPane);

        Document doc = messageTextPane.getDocument();
        Style textStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		for (ParsedPart parsedPart : mb.getParsedParts()) {
			if (parsedPart.toSwingComponent(mainWindow).isPresent()) {
				JComponent asComponent = parsedPart.toSwingComponent(mainWindow).get();
				// make the component level with the text
				asComponent.setAlignmentY(0.7f);
				messageTextPane.insertComponent(asComponent);
			} else {
				// insert as text
				try {
					doc.insertString(doc.getLength(), parsedPart.toText(), textStyle);
				} catch (BadLocationException e) {
					throw new RuntimeException("Bad location in message text pane.");
				}
			}
		}

		content.add(messageTextPane, "wrap, width min:"+(TrConstants.GUI_WIDTH_PX-7));
	}

    private void addReBroadcastButtons(final TrNode node, final ParsedMicroblog pmb){

        final JButton reBroadcastButton = new JButton("Boost");
        reBroadcastButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                reBroadcastButton.setBackground(Color.BLUE);
            }

            public void mouseExited(MouseEvent evt) {
                reBroadcastButton.setBackground(UIManager.getColor("control"));
            }
        });
        setVotingButtonConfigs(reBroadcastButton, "Re-broadcast this");
        content.add(reBroadcastButton, "split 2, span, align right");
        reBroadcast action=new reBroadcast(node, pmb);
        reBroadcastButton.addActionListener(action);
    }

	private void setVotingButtonConfigs(final JButton button, final String tooltip) {
		button.setToolTipText(tooltip);
		button.setFocusable(false);
		button.setContentAreaFilled(false);
	}

	private static class DateParser {
		private static DateFormat dateFormater = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

		public static String parseTime(final long time) {
			final Date date = new Date(time);
			return dateFormater.format(date);
		}
	}


    /*  Auth: Ravi Tejasvi
    *   ReBroadcast button copies the message and then broadcasts the same with high priority.
    */
    private final class reBroadcast implements ActionListener
    {
        private final TrNode node;
        private final ParsedMicroblog pmb;
        public reBroadcast(final TrNode node, final ParsedMicroblog pmb) {
            this.node = node;
            this.pmb = pmb;
        }

        public void actionPerformed(ActionEvent actionEvent) {
                String xmlMessage = BroadcastMessageParser.getXML(pmb.getParsedParts());
                BroadcastMessage broadcastMessage = new BroadcastMessage(xmlMessage, pmb.getMbData());
                node.mbClasses.incomingMbHandler.handleInsertion(broadcastMessage);
        }


    }
}
