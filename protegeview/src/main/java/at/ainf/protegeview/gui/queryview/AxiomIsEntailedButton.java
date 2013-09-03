package at.ainf.protegeview.gui.queryview;

import at.ainf.protegeview.menuactions.AbstractAction;
import org.protege.editor.core.ui.list.MListButton;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.09.12
 * Time: 14:11
 * To change this template use File | Settings | File Templates.
 */
public class AxiomIsEntailedButton extends AbstractAnswerButton {

    private boolean isMarkedEntailed;

    public AxiomIsEntailedButton(final QueryAxiomList list, boolean isMarkedEntailed) {
        super("Entailed", Color.GREEN.darker(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                list.handleEntailed();
            }
        });
        this.isMarkedEntailed = isMarkedEntailed;

    }

    public void paintButtonContent(Graphics2D g) {

        if (isMarkedEntailed)
            paintBackgroundColor(g,Color.GREEN);
        int size = getBounds().height;
        int thickness = (Math.round(size / 8.0f) / 2) * 2;

        int x = getBounds().x;
        int y = getBounds().y;

        int insetX = size / 4;
        int insetY = size / 4;
        int insetHeight = size / 2;
        int insetWidth = size / 2;
        g.fillRect(x + size / 2  - thickness / 2, y + insetY, thickness, insetHeight);
        g.fillRect(x + insetX, y + size / 2 - thickness / 2, insetWidth, thickness);
    }



}
