package at.ainf.protegeview.queryaskingview.buttons;

import at.ainf.protegeview.controlpanel.DebugIconsLoader;
import at.ainf.protegeview.testcasesentailmentsview.TcaeButtonsButton;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 05.08.11
 * Time: 11:03
 * To change this template use File | Settings | File Templates.
 */
public class QueryButton extends TcaeButtonsButton {

    private boolean activated = false;

    private String icon;

    private String iconActivated;

    public boolean isActivated() {
        return activated;
    }

    public void setActivate (boolean active) {
        activated = active;
        if (active) {
            setIcon(DebugIconsLoader.getIcon(iconActivated));
        }
        else {
            setIcon(DebugIconsLoader.getIcon(icon));
        }
    }

    public boolean processClickFromUser() {
        setActivate (!isActivated());

        return activated;
    }

    public QueryButton(String name,
                       String tooltip, String icon, int event, AbstractAction abstractAction,
                       String iconActivated) {
        super(name,tooltip, icon, event, abstractAction);

        this.icon = icon;
        this.iconActivated = iconActivated;
    }

}
