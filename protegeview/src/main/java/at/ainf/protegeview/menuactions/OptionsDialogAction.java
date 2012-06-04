package at.ainf.protegeview.menuactions;

import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 07.07.11
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
public class OptionsDialogAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {

        getWS().doConfigOptions();

    }
}
