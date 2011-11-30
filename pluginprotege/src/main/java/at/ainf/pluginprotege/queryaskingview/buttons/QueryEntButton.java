package at.ainf.pluginprotege.queryaskingview.buttons;

import org.protege.editor.core.ui.list.MListButton;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.05.11
 * Time: 08:41
 * To change this template use File | Settings | File Templates.
 */
public class QueryEntButton extends MListButton {


    public QueryEntButton(ActionListener actionListener) {
        super("Entailed", Color.GREEN, actionListener);

    }

    public Color getBackground() {
        if (((QueryQuestListItem) getRowObject()).isEntailedMarked()) {
            return Color.GREEN;
        }
        else {
            return super.getBackground();
        }
    }

    public Color getRollOverColor() {
        /*if (((QueryQuestListItem) getRowObject()).isEntailedMarked()) {
            return super.getBackground();
        }
        else {
            return Color.GREEN;
        }*/
        return getBackground();
    }

    public void paintButtonContent(Graphics2D g) {
        int inset = 5;
        Dimension dim = getBounds().getSize();
        int x = getBounds().x;
        int y = getBounds().y;
        g.drawLine(x + dim.width / 2, y + inset, x + dim.width / 2, y + dim.height - inset);
        g.drawLine(x + inset, y + dim.height / 2, x + dim.width - inset, y + dim.height / 2);
    }
}
