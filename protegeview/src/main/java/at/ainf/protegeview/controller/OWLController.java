package at.ainf.protegeview.controller;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.12.11
 * Time: 14:40
 * To change this template use File | Settings | File Templates.
 */
public interface OWLController {

    public void addControllerListener(OWLControllerListener listener, Class cls);

    public void removeControllerListener(OWLControllerListener listener, Class cls);


}
