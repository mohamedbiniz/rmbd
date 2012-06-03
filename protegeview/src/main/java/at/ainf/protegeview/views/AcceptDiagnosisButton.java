package at.ainf.protegeview.views;

import at.ainf.protegeview.controlpanel.DebugIconsLoader;
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
public class AcceptDiagnosisButton extends MListButton {

    public AcceptDiagnosisButton(ActionListener actionListener) {
        super("Accept Diagnosis", Color.GREEN, actionListener);

    }

    public void paintButtonContent(Graphics2D g) {
        //  int inset = 5;
        Dimension dim = getBounds().getSize();
        int x = getBounds().x;
        int y = getBounds().y;
        String image = DebugIconsLoader.YESFORALL;
        if (((ResultsListSection) getRowObject()).isUserMarkedThisTarget())
          image = DebugIconsLoader.YESFORALLACTIVATED;
        g.drawImage(DebugIconsLoader.getIcon(image).getImage(), x, y, dim.width, dim.height, null);
    }
}
