package at.ainf.pluginprotege.queryaskingview.buttons;

import org.protege.editor.core.ui.list.MListButton;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.05.11
 * Time: 14:16
 * To change this template use File | Settings | File Templates.
 */
public class QueryUnknownButton extends MListButton {

    public QueryUnknownButton(ActionListener actionListener) {
        super("Unknown  ", Color.blue.darker(), actionListener);

    }

    public Color getBackground() {
        if (((QueryQuestListItem) getRowObject()).isUnknowMarked()) {
            return Color.blue.darker();
        }
        else {
            return super.getBackground();
        }
    }

    public Color getRollOverColor() {

        return getBackground();
    }

    public void paintButtonContent(Graphics2D g) {
        int stringWidth = g.getFontMetrics().getStringBounds("?", g).getBounds().width;
        int w = getBounds().width;
        int h = getBounds().height;
        g.drawString("?",
                     getBounds().x + w / 2 - stringWidth / 2,
                     getBounds().y + g.getFontMetrics().getAscent() / 2 + h / 2);
    }
}
