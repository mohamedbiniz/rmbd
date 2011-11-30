package at.ainf.pluginprotege.debugmanager;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.08.11
 * Time: 16:06
 * To change this template use File | Settings | File Templates.
 */
public class AxiomChangedEvent extends EventObject {

    private OWLLogicalAxiom axiom;

    public AxiomChangedEvent(Object source, OWLLogicalAxiom axiom) {
        super(source);

        this.axiom = axiom;
    }

    public OWLLogicalAxiom getAxiom(){
        return axiom;
    }

}
