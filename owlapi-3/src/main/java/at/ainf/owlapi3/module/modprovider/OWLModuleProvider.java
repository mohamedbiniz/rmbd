package at.ainf.owlapi3.module.modprovider;

import at.ainf.diagnosis.quickxplain.ModuleProvider;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.02.13
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
public interface OWLModuleProvider extends ModuleProvider<OWLLogicalAxiom> {

    /**
     * Calculates all unsat classes of the ontology and extracts modules for each of them.
     * @return the union of the modules of all unsat classes
     */
    public Set<OWLLogicalAxiom> getModuleUnsatClass();

}
