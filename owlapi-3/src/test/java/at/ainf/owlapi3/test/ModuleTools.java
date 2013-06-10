package at.ainf.owlapi3.test;

import at.ainf.owlapi3.reasoner.ExtendedStructuralReasoner;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.*;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.02.13
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */
public class ModuleTools {

    public OWLReasoner createReasoner(OWLOntology ontology) {
        return new Reasoner.ReasonerFactory().createNonBufferingReasoner(ontology);
    }

    public SyntacticLocalityModuleExtractor createModuleExtractor(OWLOntology ontology) {
        return new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);
    }

    public List<OWLClass> getUnsatClasses (Set<OWLLogicalAxiom> axioms) {
        return new LinkedList<OWLClass>(createReasoner(createOntology(axioms)).getUnsatisfiableClasses().getEntities());
    }

    protected List<OWLClass> getTopUnsat (Set<OWLLogicalAxiom> axioms) {
        ExtendedStructuralReasoner structuralReasoner = new ExtendedStructuralReasoner(createOntology(axioms));
        List<OWLClass> unsatClasses = new ArrayList<OWLClass>();
        Set<OWLClass> excluded = new HashSet<OWLClass>();
        List<OWLClass> unsat = getUnsatClasses(axioms);

        boolean isTop;

        for (int i=0; i<unsat.size(); i++){
            if (excluded.contains(unsat.get(i)))
                continue; //is not a top class
            isTop=true;
            for (int j=0; j<unsat.size(); j++){
                if (i==j)
                    continue;

                if (structuralReasoner.areEquivalent(unsat.get(i), unsat.get(j))){ //equivalence
                    excluded.add(unsat.get(j)); //we repair only one side
                    continue; //
                }
                else if (structuralReasoner.isSubClassOf(unsat.get(j), unsat.get(i))){
                    excluded.add(unsat.get(j));
                }
                else if (structuralReasoner.isSubClassOf(unsat.get(i), unsat.get(j))){
                    isTop=false;
                    break;
                }

            }//For j

            //is top
            if (isTop){
                unsatClasses.add(unsat.get(i));
            }

        }
        return unsatClasses;

    }

    protected Set<OWLLogicalAxiom> convertAxiom2LogicalAxiom (Set<OWLAxiom> axioms) {
        Set<OWLLogicalAxiom> result = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLAxiom axiom : axioms)
            result.add((OWLLogicalAxiom)axiom);
        return result;
    }

    public Map<OWLClass,Set<OWLLogicalAxiom>> getModules (List<OWLClass> unsat, Set<OWLLogicalAxiom> axioms) {
        Map<OWLClass,Set<OWLLogicalAxiom>> result = new LinkedHashMap<OWLClass, Set<OWLLogicalAxiom>>();
        SyntacticLocalityModuleExtractor extractor = createModuleExtractor(createOntology(axioms));
        for (OWLEntity unsatClass : unsat)
            result.put((OWLClass) unsatClass,convertAxiom2LogicalAxiom(extractor.extract(Collections.singleton(unsatClass))));
        return result;

    }

    public List<OWLClass> getStillUnsat (List<OWLClass> possUnsat, Set<OWLLogicalAxiom> axioms) {
        List<OWLClass> result = new LinkedList<OWLClass>();
        OWLReasoner reasoner = createReasoner(createOntology(axioms));
        for (OWLClass possUnsatClass : possUnsat)
            if (!reasoner.isSatisfiable(possUnsatClass))
                result.add(possUnsatClass);
        return result;

    }

}
