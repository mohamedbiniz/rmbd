package at.ainf.protegeview.gui.queryview;

import org.protege.editor.core.ui.list.MListButton;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.09.12
 * Time: 08:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAnswerButton extends MListButton {

    protected AbstractAnswerButton(String name, Color rollOverColor, ActionListener actionListener) {
        super(name, rollOverColor, actionListener);
    }

    protected void paintBackgroundColor (Graphics2D g, Color color) {
        Rectangle buttonBounds = getBounds();
        Color oldColor = g.getColor();
        g.setColor(color);
        g.fillOval(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height);
        g.setColor(oldColor);
    }

    @Override
    protected int getSizeMultiple() {
        return 4;
    }

}
