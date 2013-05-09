package at.ainf.owlapi3.module.iterative;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import at.ainf.owlapi3.reasoner.HornSatReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.03.13
 * Time: 09:03
 * To change this template use File | Settings | File Templates.
 */
public class ModuleCalc {

    private static Logger logger = LoggerFactory.getLogger(ModuleCalc.class.getName());

    private OWLOntology ontology;
    private OWLReasoner reasoner;

    private Map<OWLClass, Set<OWLLogicalAxiom>> moduleMap = new HashMap<OWLClass, Set<OWLLogicalAxiom>>();
    //private Set<OWLClass> initialUnsatClasses;

    // TODO split into horn and non-horm implementations!

    public ModuleCalc(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {
        this.ontology = ontology;

        /*
        if (reasonerFactory.getReasonerName().equals(HornSatReasoner.NAME)) {
            HornSatReasonerFactory hornSatReasonerFactory = (HornSatReasonerFactory) reasonerFactory;
            //hornSatReasonerFactory.precomputeUnsatClasses(ontology);
            this.reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
            initialUnsatClasses = new HashSet<OWLClass>(hornSatReasonerFactory.getUnsatClasses());
        } else*/
        {
            this.reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
            //initialUnsatClasses = new HashSet<OWLClass>(reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom());
        }
    }

    public void updatedLists(List<OWLClass> actualUnsat, List<OWLClass> allUnsat, int maxClasses) {
        /*
        if (reasoner.getReasonerName().equals(HornSatReasoner.NAME)) {
            Set<OWLClass> unsat = reasoner.getUnsatisfiableClasses().getEntities();
            actualUnsat.retainAll(unsat);
            if (logger.isInfoEnabled())
                logger.info("Unsat classes old: " + allUnsat.size() + " new: " + unsat.size() + " actual: " + actualUnsat.size());
            allUnsat.retainAll(unsat);

            for (OWLClass owlClass : allUnsat) {
                if (!actualUnsat.contains(owlClass) && !reasoner.isSatisfiable(owlClass))
                    actualUnsat.add(owlClass);
                if (actualUnsat.size() == 10)
                    break;
            }
        } else*/
        {

            for (Iterator<OWLClass> it = actualUnsat.iterator(); it.hasNext(); ) {
                OWLClass cl = it.next();
                if (isSatisfiable(cl)) {
                    it.remove();
                    allUnsat.remove(cl);
                }
            }
            if (logger.isInfoEnabled())
                logger.info("Unsat classes all: " + allUnsat.size() + " actual: " + actualUnsat.size());
            Iterator<OWLClass> it = allUnsat.iterator();
            while (it.hasNext() && actualUnsat.size() < maxClasses) {
                OWLClass cl = it.next();
                if (isSatisfiable(cl)) {
                    it.remove();
                } else {
                    actualUnsat.add(cl);
                }

            }
            /*
            Set<OWLClass> toCheck = new HashSet<OWLClass>(actualUnsat);
            for (OWLClass unsatClass : toCheck) {
                //OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(createOntology(unsatMap.get(unsatClass)));
                if (isSatisfiable(unsatClass)) {
                    actualUnsat.remove(unsatClass);
                    allUnsat.remove(unsatClass);
                }
            }
            toCheck = new HashSet<OWLClass>(allUnsat);
            toCheck.removeAll(actualUnsat);
            for (OWLClass unsatClass : toCheck) {
                //OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(createOntology(unsatMap.get(unsatClass)));
                if (!isSatisfiable(unsatClass)) {
                    actualUnsat.add(unsatClass);
                    if (actualUnsat.size() == 10)
                        break;
                } else {
                    allUnsat.remove(unsatClass);
                }
            }
            */

        }
    }

    public List<OWLClass> getInitialUnsatClasses() {
        if (reasoner.getReasonerName().equals(HornSatReasoner.NAME))
            return ((HornSatReasoner)reasoner).getSortedUnsatisfiableClasses();
        return new ArrayList<OWLClass>(reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom());
    }

    public Set<OWLClass> getUnsatisfiableClasses() {
        return reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
    }

    public boolean isSatisfiable(OWLClass unsatClass) {
        return reasoner.isSatisfiable(unsatClass);
    }

    public Set<OWLLogicalAxiom> calculateModule(OWLClass unsatClass) {
        Set<OWLLogicalAxiom> result = moduleMap.get(unsatClass);

        if (result != null)
            return result;

        result = extractModule(ontology, Collections.singleton((OWLEntity)unsatClass));
        moduleMap.put(unsatClass, result);

        return result;
    }

    public Set<OWLLogicalAxiom> extractModule(OWLOntology ontology, Set<OWLEntity> unsatClass) {
        Set<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
        SyntacticLocalityModuleExtractor extractor =
                new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);

        Set<OWLAxiom> module = extractor.extract(unsatClass);
        for (OWLAxiom axiom : module)
            result.add((OWLLogicalAxiom) axiom);
        return result;
    }

    public Map<OWLClass, Set<OWLLogicalAxiom>> getModuleMap() {
        return moduleMap;
    }

    // TODO check this method for performance issues ontologyManager.removeAxioms is quite slow
    public void removeAxiomsFromOntologyAndModules(Set<OWLLogicalAxiom> axioms) {
        ontology.getOWLOntologyManager().removeAxioms(ontology, axioms);

        for (Set<OWLLogicalAxiom> module : moduleMap.values())
            module.removeAll(axioms);

    }

}
