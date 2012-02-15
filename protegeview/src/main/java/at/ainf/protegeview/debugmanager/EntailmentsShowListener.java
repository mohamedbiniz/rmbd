package at.ainf.protegeview.debugmanager;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 30.09.11
 * Time: 10:26
 * To change this template use File | Settings | File Templates.
 */
public interface EntailmentsShowListener extends EventListener {

    void entailmentSetChanged (EntailmentsShowEvent e);

}
