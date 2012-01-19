package at.ainf.owlcontroller;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.12.11
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public class OwlControllerMngr {

    private static OWLController controller;


    public static OWLController getOWLController() {
        if (controller==null) {
            controller = new OWLControllerImpl();
        }

        return controller;
    }
}
