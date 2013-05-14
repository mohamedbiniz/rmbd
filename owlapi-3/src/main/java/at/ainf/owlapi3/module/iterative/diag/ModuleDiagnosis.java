package at.ainf.owlapi3.module.iterative.diag;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.04.13
 * Time: 09:12
 * To change this template use File | Settings | File Templates.
 */
public interface ModuleDiagnosis {

    public Set<OWLLogicalAxiom> calculateTargetDiagnosis();

}
