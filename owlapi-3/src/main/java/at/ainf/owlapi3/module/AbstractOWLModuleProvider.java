package at.ainf.owlapi3.module;

import at.ainf.owlapi3.reasoner.ExtendedStructuralReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.*;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 06.02.13
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractOWLModuleProvider implements OWLModuleProvider {

    private Logger logger = LoggerFactory.getLogger(AbstractOWLModuleProvider.class.getName());

    private OWLReasonerFactory reasonerFactory;
    private OWLOntology fullOntology;

    private boolean isElOntology = false;
    private Map<OWLClass, Set<OWLLogicalAxiom>> unsatClasses = new HashMap<OWLClass, Set<OWLLogicalAxiom>>();

    public AbstractOWLModuleProvider(OWLOntology ontology, OWLReasonerFactory factory, boolean isElOnto) {
        reasonerFactory = factory;
        fullOntology = ontology;
        isElOntology = isElOnto;

    }

    public OWLReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    public OWLOntology getFullOntology() {
        return fullOntology;
    }

    public Map<OWLClass, Set<OWLLogicalAxiom>> getUnsatClasses() {
        return Collections.unmodifiableMap(unsatClasses);
    }

    /**
     * Works only for EL
     */
    protected List<OWLClass> getTopUnsat (OWLOntology ontology, List<OWLClass> unsat) {
        ExtendedStructuralReasoner structuralReasoner = new ExtendedStructuralReasoner(ontology);
        List<OWLClass> unsatClasses = new ArrayList<OWLClass>();
        Set<OWLClass> excluded = new HashSet<OWLClass>();

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

    protected class SetComparator<X> implements Comparator<Set<X>> {
        @Override
        public int compare(Set<X> o1, Set<X> o2) {
            return ((Integer) o1.size()).compareTo(o2.size());
        }
    }

    protected Set<OWLLogicalAxiom> convertAxiom2LogicalAxiom (Set<OWLAxiom> axioms) {
        Set<OWLLogicalAxiom> result = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLAxiom axiom : axioms)
            result.add((OWLLogicalAxiom)axiom);
        return result;
    }

    protected <X> Set<X> createUnion (Collection<Set<X>> sets) {
        Set<X> result = new LinkedHashSet<X>();
        for (Set<X> set : sets)
            result.addAll(set);
        return result;
    }

    protected List<OWLClass> getUnsatClassesWithoutBot (OWLOntology ontology) {
        OWLReasoner reasoner = getReasonerFactory().createNonBufferingReasoner(ontology);
        List<OWLClass> initialUnsat = new LinkedList<OWLClass>(reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom());
        //initialUnsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());

        return initialUnsat;
    }

    public List<OWLClass> getStillUnsat (List<OWLClass> possUnsat, Set<OWLLogicalAxiom> axioms) {
        List<OWLClass> result = new LinkedList<OWLClass>();
        OWLReasoner reasoner = getReasonerFactory().createNonBufferingReasoner(createOntology(axioms));
        for (OWLClass possUnsatClass : possUnsat)
            if (!reasoner.isSatisfiable(possUnsatClass))
                result.add(possUnsatClass);
        return result;

    }

    protected SyntacticLocalityModuleExtractor createModuleExtractor(OWLOntology ontology) {
        return new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);
    }

    /*public Set<OWLLogicalAxiom> getModuleUnsatClassMult() {

        OWLOntology ontology = createOntology(getFullOntology().getLogicalAxioms());
        SyntacticLocalityModuleExtractor moduleStar = createModuleExtractor(ontology);
        List<OWLClass> initialUnsat = getUnsatClassesWithoutBot(ontology);
        List<OWLClass> topUnsat = initialUnsat;
        if (isElOntology)
            topUnsat = getTopUnsat(ontology,initialUnsat);


        for (OWLEntity unsatClass : topUnsat) {
            Set<OWLLogicalAxiom> owlLogicalAxioms = convertAxiom2LogicalAxiom(moduleStar.extract(Collections.singleton(unsatClass)));
            unsatClasses.put ((OWLClass) unsatClass, owlLogicalAxioms);
        }

        ExecutorService pool = Executors.newFixedThreadPool(3);
        Map<OWLClass,Future<Set<OWLLogicalAxiom>>> futures = new HashMap<OWLClass,Future<Set<OWLLogicalAxiom>>>();
        for (OWLEntity unsatClass : topUnsat) {
            Callable<Set<OWLLogicalAxiom>> callable = new ExtractorCallable(ontology,unsatClass);
            Future<Set<OWLLogicalAxiom>> future = pool.submit(callable);
            futures.put((OWLClass)unsatClass,future);
        }

        for (OWLEntity unsatClass : topUnsat) {
            try {
                unsatClasses.put((OWLClass) unsatClass, futures.get(unsatClass).get());
            } catch (InterruptedException e) {
                logger.info("thread interrupted");
            } catch (ExecutionException e) {
                logger.info("execution");
            }
        }


        return createUnion(unsatClasses.values());
    } */

    /* public class ExtractorCallable implements Callable<Set<OWLLogicalAxiom>> {
        private OWLOntology ontology;
        private OWLEntity entity;

        public ExtractorCallable(OWLOntology o, OWLEntity e) {

            this.ontology = o;
            this.entity = e;
        }

        public Set<OWLLogicalAxiom> call() {
            SyntacticLocalityModuleExtractor moduleStar = createModuleExtractor(ontology);
            return convertAxiom2LogicalAxiom(moduleStar.extract(Collections.singleton(entity)));
        }

    } */


    @Override
    public Set<OWLLogicalAxiom> getModuleUnsatClass() {

        OWLOntology ontology = createOntology(getFullOntology().getLogicalAxioms());
        SyntacticLocalityModuleExtractor moduleStar = createModuleExtractor(ontology);
        long time = System.currentTimeMillis();
        List<OWLClass> initialUnsat = getUnsatClassesWithoutBot(ontology);
        time = System.currentTimeMillis() - time;
        logger.info("time needed for unsat classes calc: " + time);
        List<OWLClass> topUnsat = initialUnsat;
        if (isElOntology)
            topUnsat = getTopUnsat(ontology,initialUnsat);

        long timeModule = System.currentTimeMillis();
        for (OWLEntity unsatClass : topUnsat) {
            Set<OWLLogicalAxiom> owlLogicalAxioms = convertAxiom2LogicalAxiom(moduleStar.extract(Collections.singleton(unsatClass)));
            unsatClasses.put ((OWLClass) unsatClass, owlLogicalAxioms);
        }
        timeModule = System.currentTimeMillis() - timeModule;
        logger.info("time needed for module extraction: " + timeModule);


        return createUnion(unsatClasses.values());
    }

}
