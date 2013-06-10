package at.ainf.protegeview.menuactions;

import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 03.08.11
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationWizardAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {

        getWS().doConfigurationWizard();

    }
}
