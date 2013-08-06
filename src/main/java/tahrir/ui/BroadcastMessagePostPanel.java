package tahrir.ui;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tahrir.TrConstants;
import tahrir.TrNode;
import tahrir.io.net.broadcasts.broadcastMessages.BroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.ParsedBroadcastMessage;
import tahrir.io.net.broadcasts.broadcastMessages.SignedBroadcastMessage;

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

	public BroadcastMessagePostPanel(final BroadcastMessage bm, final TrMainWindow mainWindow) {
		this.mainWindow = mainWindow;
		content = new JPanel(new MigLayout());
        content.setBackground(Color.WHITE);

        addAuthorButton(bm, mainWindow);
		addPostTime(bm);
		addTextPane(bm, mainWindow);
		addReBroadcastButtons(mainWindow.node, bm);
	}

	public JComponent getContent() {
		return content;
	}

	private void addPostTime(final BroadcastMessage bm) {
		final JLabel postTime = new JLabel(DateParser.parseTime(bm.signedBroadcastMessage.parsedBroadcastMessage.getTimeCreated()));
		postTime.setForeground(Color.GRAY);
		postTime.setFont(new Font("time", Font.PLAIN, postTime.getFont().getSize() - 2));
        content.add(postTime, "gap push, wrap");
	}

	private void addAuthorButton(final BroadcastMessage bm, final TrMainWindow mainWindow) {
		final AuthorDisplayPageButton authorNick = new AuthorDisplayPageButton(mainWindow,
				bm.signedBroadcastMessage.getAuthor());
		authorNick.setFont(new Font("bold", Font.BOLD, authorNick.getFont().getSize() + 2));
        authorNick.setForeground(new Color(65,131,196));     //SteelBlue color
		content.add(authorNick, "left align");
	}

    private JTextPane setTextPane(final JTextPane messageTextPane)
    {

        messageTextPane.setBackground(Color.WHITE);
        messageTextPane.setEditable(false);
        return messageTextPane;

    }
	private void addTextPane(final BroadcastMessage bm, TrMainWindow mainWindow) {
        final JTextPane messageTextPane = new JTextPane();
        setTextPane(messageTextPane);
        messageTextPane.setText(bm.signedBroadcastMessage.parsedBroadcastMessage.getPlainTextBroadcastMessage());
		content.add(messageTextPane, "wrap, width min:"+(TrConstants.GUI_WIDTH_PX-7));
	}

    private void addReBroadcastButtons(final TrNode node, final BroadcastMessage bm){

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
        reBroadcast action=new reBroadcast(node, bm);
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
        private final BroadcastMessage bm;
        public reBroadcast(final TrNode node, final BroadcastMessage bm) {
            this.node = node;
            this.bm = bm;
        }

        public void actionPerformed(ActionEvent actionEvent) {
                bm.resetPriority();
                node.mbClasses.incomingMbHandler.handleInsertion(bm);
        }


    }
}
