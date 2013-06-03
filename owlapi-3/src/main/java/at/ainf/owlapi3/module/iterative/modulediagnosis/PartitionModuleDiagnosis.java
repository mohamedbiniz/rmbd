package at.ainf.owlapi3.module.iterative.modulediagnosis;

import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.owlapi3.module.iterative.diagsearcher.ModuleDiagSearcher;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 11:51
 * To change this template use File | Settings | File Templates.
 */
public class PartitionModuleDiagnosis extends AbstractRootModuleDiagnosis {

    public PartitionModuleDiagnosis(Set<OWLLogicalAxiom> mappings, Set<OWLLogicalAxiom> ontoAxioms, OWLReasonerFactory factory, ModuleDiagSearcher moduleDiagSearcher) {
        super(mappings, ontoAxioms, factory, moduleDiagSearcher);
    }

    @Override
    public Set<FormulaSet<OWLLogicalAxiom>> start() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
