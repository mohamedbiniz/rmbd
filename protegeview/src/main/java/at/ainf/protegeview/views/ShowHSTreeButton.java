package at.ainf.protegeview.views;

import org.protege.editor.core.ui.list.MListButton;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.09.11
 * Time: 09:04
 * To change this template use File | Settings | File Templates.
 */
public class ShowHSTreeButton extends MListButton {


    public ShowHSTreeButton(ActionListener actionListener) {
        super("Show Diagnosis in HS Tree", Color.GREEN, actionListener);

    }


    public void paintButtonContent(Graphics2D g) {
        int inset = 5;
        Dimension dim = getBounds().getSize();
        int x = getBounds().x;
        int y = getBounds().y;
        g.drawLine(x + dim.width / 2, y + inset, x + dim.width / 2, y + dim.height - inset);
        g.drawLine(x + dim.width / 2, y + dim.height - inset, x + inset, y + dim.height / 2);
        g.drawLine(x + dim.width / 2, y + dim.height - inset, x + dim.width - inset, y + dim.height / 2);

    }
}
