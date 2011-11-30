package at.ainf.pluginprotege.queryaskingview.buttons;

import org.protege.editor.core.ui.list.MListButton;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.05.11
 * Time: 13:48
 * To change this template use File | Settings | File Templates.
 */
public class QueryNotEntaiButton extends MListButton {

    public QueryNotEntaiButton(ActionListener actionListener) {
        super("Not Entailed", Color.RED, actionListener);

    }

    public Color getBackground() {
        if (((QueryQuestListItem) getRowObject()).isNonEntailedMarked()) {
            return Color.RED;
        }
        else {
            return super.getBackground();
        }
    }

    public Color getRollOverColor() {
        /*if (((QueryQuestListItem) getRowObject()).isNonEntailedMarked()) {
            return super.getBackground();
        }
        else {
            return Color.RED;
        } */
        return getBackground();

    }

    public void paintButtonContent(Graphics2D g) {
        int inset = 5;
        Dimension dim = getBounds().getSize();
        int x = getBounds().x;
        int y = getBounds().y;

        g.drawLine(x + inset, y + dim.height / 2, x + dim.width - inset, y + dim.height / 2);
    }

}
