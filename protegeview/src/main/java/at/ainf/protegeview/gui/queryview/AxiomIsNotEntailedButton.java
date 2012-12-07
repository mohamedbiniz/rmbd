package at.ainf.protegeview.gui.queryview;

import org.protege.editor.core.ui.list.MListButton;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.09.12
 * Time: 14:12
 * To change this template use File | Settings | File Templates.
 */
public class AxiomIsNotEntailedButton extends AbstractAnswerButton {

    private boolean isMarkedNonEntailed;

    public AxiomIsNotEntailedButton(final QueryAxiomList list, boolean isMarkedNonEntailed) {
        super("Not Entailed", Color.RED.darker(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                list.handleNotEntailed();
            }
        });
        this.isMarkedNonEntailed = isMarkedNonEntailed;

    }

    public void paintButtonContent(Graphics2D g) {

        if (isMarkedNonEntailed)
            paintBackgroundColor(g,Color.RED);
        int size = getBounds().height;
        int thickness = (Math.round(size / 8.0f) / 2) * 2;

        int x = getBounds().x;
        int y = getBounds().y;

        int insetX = size / 4;
        int insetWidth = size / 2;
        g.fillRect(x + insetX, y + size / 2 - thickness / 2, insetWidth, thickness);

    }

}
