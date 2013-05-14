package at.ainf.protegeview.testcasesentailmentsview;

import at.ainf.protegeview.controlpanel.DebugIconsLoader;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.08.11
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */
public class TcaeButtonsButton extends JButton {

    public TcaeButtonsButton(String name, String tooltip, String icon, int event, AbstractAction abstractAction) {

        super(abstractAction);
        setName(name);
        setText(name);
        setIcon(DebugIconsLoader.getIcon(icon));
        setToolTipText(tooltip);
        if (event != -1) setMnemonic(event);
        setEnabled(true);
    }

}
