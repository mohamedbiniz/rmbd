package at.ainf.protegeview.menuactions;

import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.03.11
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */
public class LoadTestcasesAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {
        getWS().loadTestcasesAction();
    }

}
