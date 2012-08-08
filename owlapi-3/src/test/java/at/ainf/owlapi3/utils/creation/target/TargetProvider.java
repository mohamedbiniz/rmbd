package at.ainf.owlapi3.utils.creation.target;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.08.12
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
public interface TargetProvider {

    public Set<OWLLogicalAxiom> getDiagnosisTarget();

}
