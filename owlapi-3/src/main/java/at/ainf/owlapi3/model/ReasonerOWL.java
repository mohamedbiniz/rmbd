package at.ainf.owlapi3.model;

import at.ainf.diagnosis.model.AbstractReasoner;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 19.10.12
 * Time: 10:03
 * To change this template use File | Settings | File Templates.
 */
public class ReasonerOWL extends AbstractReasoner<OWLLogicalAxiom> {

    @Override
    public boolean isConsistent() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
