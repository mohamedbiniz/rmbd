package at.ainf.owlapi3.module.iterative.modulecalc;

import at.ainf.owlapi3.reasoner.HornSatReasoner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
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

    private final OWLReasoner testReasoner;
    private OWLOntology testOnto;

    private OWLOntology ontology;
    private OWLReasoner reasoner;

    private Map<OWLClass, Set<OWLLogicalAxiom>> moduleMap = new HashMap<OWLClass, Set<OWLLogicalAxiom>>();
    //private Set<OWLClass> initialUnsatClasses;

    // TODO split into horn and non-horm implementations!

    public ModuleCalc(OWLOntology ontology, OWLReasonerFactory reasonerFactory) {

        //TODO retrieve constraints from the ontology and create a module for each constraint and save the signatures

        this.ontology = ontology;
        try {
            this.testOnto = ontology.getOWLOntologyManager().createOntology(IRI.create("http://ainf.at/debug" + System.nanoTime()));
        } catch (OWLOntologyCreationException e) {
            logger.error("Testing ontology cannot be created!");
        }
        this.testReasoner = reasonerFactory.createNonBufferingReasoner(testOnto);
        this.reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
    }

    protected OWLReasoner getReasoner() {
        return reasoner;
    }

    public void updatedLists(List<OWLClass> actualUnsat, List<OWLClass> allUnsat, int maxClasses) {
        for (Iterator<OWLClass> it = actualUnsat.iterator(); it.hasNext(); ) {
            OWLClass cl = it.next();
            if (isSatisfiable(cl)) {
                it.remove();
                allUnsat.remove(cl);
            }
        }

        //allUnsat.retainAll(actualUnsat);
        Set<OWLClass> classes = new HashSet<OWLClass>(getModuleMap().keySet());
        for (OWLClass owlClass : classes) {
            if (!actualUnsat.contains(owlClass))
                getModuleMap().remove(owlClass);
        }

        if (logger.isInfoEnabled())
            logger.info("Unsat classes all: " + allUnsat.size() + " actual: " + actualUnsat.size());

        refillActualUnsatClasses(actualUnsat, allUnsat, maxClasses);
    }

    protected void refillActualUnsatClasses(List<OWLClass> actualUnsat, List<OWLClass> allUnsat, int maxClasses) {

        Iterator<OWLClass> it = allUnsat.iterator();
        while (it.hasNext() && actualUnsat.size() < maxClasses) {
            OWLClass cl = it.next();
            if (isSatisfiable(cl)) {
                it.remove();
            } else {
                actualUnsat.add(cl);
            }
        }
    }

    public List<OWLClass> getInitialUnsatClasses(Collection<OWLClass> excludeClasses, int maxClasses) {
        return new ArrayList<OWLClass>(getReasoner().getUnsatisfiableClasses().getEntitiesMinusBottom());
    }

    public Collection<? extends OWLClass> getInitialUnsatClasses() {
        return getReasoner().getUnsatisfiableClasses().getEntitiesMinusBottom();
    }

    public Set<OWLClass> getUnsatisfiableClasses() {
        return getReasoner().getUnsatisfiableClasses().getEntitiesMinusBottom();
    }

    public boolean isSatisfiable(OWLClass unsatClass) {
        return getReasoner().isSatisfiable(unsatClass);
    }

    public Set<OWLLogicalAxiom> calculateModule(OWLClass unsatClass) {

        Set<OWLLogicalAxiom> result = moduleMap.get(unsatClass);

        if (result != null)
            return result;

        result = extractModule(ontology, Collections.singleton((OWLEntity) unsatClass));
        //if (moduleMap.containsValue(result))
        //    return null;
        moduleMap.put(unsatClass, result);
        if (logger.isDebugEnabled())
            logger.debug("Computed module for " + unsatClass.toString() + " with " + result.size() + " axioms");

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

    public Collection<OWLClass> calculateModules(Collection<OWLClass> unsatClasses) {
        Iterator<OWLClass> iterator = unsatClasses.iterator();
        while (iterator.hasNext()) {
            OWLClass owlClass = iterator.next();
            if (calculateModule(owlClass) == null) {
                iterator.remove();
                if (logger.isDebugEnabled())
                    logger.debug("The module is rejected!");
            }
        }
        return unsatClasses;
    }

    public Multimap<OWLAxiom, OWLClass> clusterModule(Set<? extends OWLAxiom> module){
        if (!testReasoner.getReasonerName().equals(HornSatReasoner.NAME))
            return HashMultimap.create(0, 0);
        this.testOnto.getOWLOntologyManager().addAxioms(this.testOnto, module);
        Multimap<OWLAxiom,OWLClass> modMap = ((HornSatReasoner) this.testReasoner).clusterAxioms(module);
        for (OWLAxiom owlAxiom : modMap.keySet()) {
            Collection<OWLClass> classes = modMap.get(owlAxiom);
            Set<OWLLogicalAxiom> subModule = extractModule(testOnto, new HashSet<OWLEntity>(classes));
            if (logger.isDebugEnabled())
                logger.debug("Submodule: constraint " + owlAxiom + ", classes " + classes.size() + ", axioms" + subModule.size());
        }
        testOnto.getOWLOntologyManager().removeAxioms(testOnto, module);
        return modMap;
    }

    public boolean isConsistent(Sets.SetView<OWLLogicalAxiom> intersection) {
        testOnto.getOWLOntologyManager().addAxioms(testOnto, intersection);
        boolean consistent = this.testReasoner.isConsistent();
        testOnto.getOWLOntologyManager().removeAxioms(testOnto, intersection);
        return consistent;
    }

    public void sortUnsatisfiableClasses(List<OWLClass> actualUnsatClasses) {
        if (testReasoner.getReasonerName().equals(HornSatReasoner.NAME))
            return;
        Collections.sort(actualUnsatClasses, new Comparator<OWLClass>() {
            @Override
            public int compare(OWLClass o1, OWLClass o2) {
                return Integer.valueOf(moduleMap.get(o1).size()).compareTo(moduleMap.get(o2).size());
            }
        });

    }
}
