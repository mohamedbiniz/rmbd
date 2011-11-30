package at.ainf.pluginprotege.views;

import at.ainf.pluginprotege.queryaskingview.buttons.QueryQuestListItem;
import org.protege.editor.core.ui.list.MListButton;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 30.09.11
 * Time: 09:53
 * To change this template use File | Settings | File Templates.
 */
public class EntailmentsButton extends MListButton {

    public Color getBackground() {
        if (((ResultsListSection) getRowObject()).isShowEntailments()) {
            return Color.GREEN;
        }
        else {
            return super.getBackground();
        }
    }

    /*public Color getRollOverColor() {
        if (((ResultsListSection) getRowObject()).isShowEntailments()) {
            return super.getBackground();
        }
        else {
            return Color.GREEN;
        }
    }*/

    public EntailmentsButton(ActionListener actionListener) {
        super("Show Entailments", Color.GREEN, actionListener);

    }


    public void paintButtonContent(Graphics2D g) {
        int stringWidth = g.getFontMetrics().getStringBounds("?", g).getBounds().width;
        int w = getBounds().width;
        int h = getBounds().height;
        g.drawString("E",
                     getBounds().x + w / 2 - stringWidth / 2,
                     getBounds().y + g.getFontMetrics().getAscent() / 2 + h / 2);

    }
}
