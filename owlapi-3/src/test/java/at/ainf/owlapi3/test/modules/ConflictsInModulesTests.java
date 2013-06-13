package at.ainf.owlapi3.test.modules;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.module.modprovider.OtfModuleProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

import static at.ainf.owlapi3.util.OWLUtils.createOntology;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 01.03.13
 * Time: 09:19
 * To change this template use File | Settings | File Templates.
 */
public class ConflictsInModulesTests {

    private static Logger logger = LoggerFactory.getLogger(ConflictsInModulesTests.class.getName());

    @Ignore
    @Test
    public void testSortUsingDistanceFromTopMeasure() {
        String onto = "mouse2human";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");

        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(mappingAxioms);

        OWLOntology fullOntology = createOntology(allAxioms);
        OtfModuleProvider provider = new OtfModuleProvider(fullOntology, new Reasoner.ReasonerFactory(),false);
        Set<OWLLogicalAxiom> bigModule = provider.getModuleUnsatClass();


        List<OWLClass> unsatClasses = new LinkedList<OWLClass>(provider.getUnsatClasses().keySet());
        DistanceToTopComparator distanceToTopComparator = new DistanceToTopComparator(fullOntology);
        Collections.sort(unsatClasses, distanceToTopComparator);

        for (OWLClass unsatClass : unsatClasses)
            logger.info(unsatClass + " " + distanceToTopComparator.getMeasure(unsatClass));

        logger.info("");


    }

    private static final OWLClass BOT_CLASS = OWLManager.getOWLDataFactory().getOWLThing();

    @Test
    public void hierarchyOfUnsatclasses() throws OWLOntologyCreationException {
        String onto = "fma2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");

        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(onto1Axioms);
        allAxioms.addAll(onto2Axioms);
        allAxioms.addAll(mappingAxioms);

        OWLOntology fullOntology = createOntology(allAxioms);
        Set<OWLClass> unsatClasses = new Reasoner.ReasonerFactory().createReasoner(fullOntology).getUnsatisfiableClasses().getEntitiesMinusBottom();
        StructuralReasoner reasoner = new StructuralReasoner (fullOntology,
                new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
        Map<OWLClass,Set<OWLClass>> map = new LinkedHashMap<OWLClass, Set<OWLClass>>();
        for (OWLClass unsatClass : unsatClasses) {
            Set<OWLClass> childs = new HashSet<OWLClass>(reasoner.getSubClasses(unsatClass,false).getFlattened());
            childs.remove(BOT_CLASS);
            if(!childs.isEmpty())map.put(unsatClass, childs);
        }

        for (OWLClass unsatClass : unsatClasses)
            logger.info(unsatClass.toString() + ", " + getNumber(unsatClass));

        for (OWLClass unsatClass : map.keySet()) {
            String name = getNumber(unsatClass) + ": ";
            for (Iterator<OWLClass> iterator = map.get(unsatClass).iterator(); iterator.hasNext(); ) {
                name += getNumber(iterator.next());
                if (iterator.hasNext())
                    name += ", ";
            }
            logger.info(name);
        }

    }

    public Integer getNumber (OWLClass unsatClass) {
        if (!knownUnsatClasses.contains(unsatClass))
            knownUnsatClasses.add(unsatClass);
        return knownUnsatClasses.indexOf(unsatClass);

    }

    List<OWLClass> knownUnsatClasses = new LinkedList<OWLClass>();

    @Test
    public void findModulesSubsetOtherModules() throws OWLOntologyCreationException {

        String onto = "fma2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");

        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(mappingAxioms);

        OWLOntology fullOntology = createOntology(allAxioms);
        OtfModuleProvider provider = new OtfModuleProvider(fullOntology, new Reasoner.ReasonerFactory(),false);
        Set<OWLLogicalAxiom> bigModule = provider.getModuleUnsatClass();

        Map<OWLClass, Set<OWLLogicalAxiom>> unsatMap = provider.getUnsatClasses();
        Map<OWLClass,Set<OWLClass>> childMap = new HashMap<OWLClass, Set<OWLClass>>();

        for (OWLClass unsatClass : unsatMap.keySet()) {
            Set<OWLLogicalAxiom> module = unsatMap.get(unsatClass);
            Set<OWLClass> childs = new LinkedHashSet<OWLClass>();

            for (OWLClass child : unsatMap.keySet()) {
                if (!child.equals(unsatClass) && module.containsAll(unsatMap.get(child)))
                    childs.add(child);
            }
            childMap.put(unsatClass,childs);
        }

        for (OWLClass unsatClass : childMap.keySet())
            logger.info(unsatClass.toString() + ", " + getNumber(unsatClass) + ", " + unsatMap.get(unsatClass).size());

        for (OWLClass unsatClass : childMap.keySet()) {
            String name = getNumber(unsatClass) + ": ";
            for (Iterator<OWLClass> iterator = childMap.get(unsatClass).iterator(); iterator.hasNext(); ) {
                OWLClass child = iterator.next();
                name += getNumber(child);
                if (iterator.hasNext())
                    name += ", ";
            }
            logger.info(name);
        }

        logger.info("");

    }

    @Test
    public void conflictsForEachModuleUsingBackground() throws OWLOntologyCreationException {

        String onto = "fma2nci";
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        Set<OWLLogicalAxiom> onto1Axioms = getAxioms("ontologies/" + onto + "genonto1.owl");
        Set<OWLLogicalAxiom> onto2Axioms = getAxioms("ontologies/" + onto + "genonto2.owl");
        Set<OWLLogicalAxiom> mappingAxioms = getAxioms("ontologies/" + onto + "genmapp.owl");

        Set<OWLLogicalAxiom> ontoAxioms = new HashSet<OWLLogicalAxiom>();
        ontoAxioms.addAll(onto1Axioms);
        ontoAxioms.addAll(onto2Axioms);

        Set<OWLLogicalAxiom> allAxioms = new HashSet<OWLLogicalAxiom>();
        allAxioms.addAll(ontoAxioms);
        allAxioms.addAll(mappingAxioms);

        OWLOntology fullOntology = createOntology(allAxioms);
        OtfModuleProvider provider = new OtfModuleProvider(fullOntology, new Reasoner.ReasonerFactory(),false);
        DistanceToTopComparator distanceToTopComparator = new DistanceToTopComparator(fullOntology);
        Set<OWLLogicalAxiom> bigModule = provider.getModuleUnsatClass();
        Map<OWLClass,Set<OWLLogicalAxiom>> map = new LinkedHashMap<OWLClass, Set<OWLLogicalAxiom>>(
                provider.getUnsatClasses());

        /*Set<OWLClass> interleukins = new HashSet<OWLClass>();
        for (OWLClass unsatClass : new LinkedHashSet<OWLClass>(map.keySet())) {
            if (unsatClass.toString().equals("CC_chemokine"))
                interleukins.add(unsatClass);
        }
        logger.info(interleukins.toString());
        for (Iterator<OWLClass> i = map.keySet().iterator(); i.hasNext();) {
            if (!i.next().equals(interleukins.iterator().next()))
                i.remove();
        }*/

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        Set<Future<String>> futures = new HashSet<Future<String>>();
        for (OWLClass unsat : map.keySet()) {
            Set<OWLLogicalAxiom> moduleAxioms = map.get(unsat);
            Callable<String> callable = new ExtractorCallable(unsat,moduleAxioms,mappingAxioms,
                    ontoAxioms,distanceToTopComparator);
            Future<String> future = pool.submit(callable);
            futures.add(future);

        }

        for (Future f : futures)
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        /*for (OWLClass unsat : map.keySet()) {
            Set<OWLLogicalAxiom> moduleAxioms = map.get(unsat);

            Set<OWLLogicalAxiom> ontoAxiomsInModule = new HashSet<OWLLogicalAxiom>();
            ontoAxiomsInModule.addAll(ontoAxioms);
            ontoAxiomsInModule.retainAll(moduleAxioms);

            Set<OWLLogicalAxiom> mappingAxiomsInModule = new HashSet<OWLLogicalAxiom>();
            mappingAxiomsInModule.addAll(mappingAxioms);
            mappingAxiomsInModule.retainAll(moduleAxioms);

            HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = searchConflicts(moduleAxioms, ontoAxiomsInModule);
            int numDiagnoses = search.getDiagnoses().size();
            int numConflicts = search.getConflicts().size();
            String sets = getNumbersForConflicts(search);

            logger.info("unsat class: " + unsat + " module: " + moduleAxioms.size() + " from onto: " +
                         ontoAxiomsInModule.size() + " from mappings: " + mappingAxiomsInModule.size() + " " +
                         "conflicts: " + numConflicts + " diagnoses: " + numDiagnoses + " found: " + sets);

        }*/

        /*Set<OWLLogicalAxiom> ontoAxiomsInModule = new HashSet<OWLLogicalAxiom>();
        ontoAxiomsInModule.addAll(ontoAxioms);
        ontoAxiomsInModule.retainAll(bigModule);
        Set<OWLLogicalAxiom> mappingAxiomsInModule = new HashSet<OWLLogicalAxiom>();
        mappingAxiomsInModule.addAll(mappingAxioms);
        mappingAxiomsInModule.retainAll(bigModule);
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = searchConflicts(bigModule, ontoAxioms);
        int numDiagnoses = search.getDiagnoses().size();
        int numConflicts = search.getConflicts().size();
        String sets = getNumbersForConflicts(search);
        logger.info("module overall: " + bigModule.size() + " from onto: " +
                ontoAxiomsInModule.size() + " from mappings: " + mappingAxiomsInModule.size() + " " +
                "conflicts: " + numConflicts + " diagnoses: " + numDiagnoses + " found: " + sets);*/

        logger.info("");

    }

    public class ExtractorCallable implements Callable<String> {

        private final OWLClass unsat;
        private final Set<OWLLogicalAxiom> moduleAxioms;
        private final Set<OWLLogicalAxiom> mappingAxioms;
        private final Set<OWLLogicalAxiom> ontoAxioms;
        private final DistanceToTopComparator distanceToTopComparator;

        public ExtractorCallable(OWLClass unsat, Set<OWLLogicalAxiom> moduleAxioms,
                                 Set<OWLLogicalAxiom> mappingAxioms, Set<OWLLogicalAxiom> ontoAxioms, DistanceToTopComparator distanceToTopComparator) {
            this.unsat = unsat;
            this.moduleAxioms = moduleAxioms;
            this.mappingAxioms = mappingAxioms;
            this.ontoAxioms = ontoAxioms;
            this.distanceToTopComparator = distanceToTopComparator;

        }

        public String call() {

            Set<OWLLogicalAxiom> ontoAxiomsInModule = new HashSet<OWLLogicalAxiom>();
            ontoAxiomsInModule.addAll(ontoAxioms);
            ontoAxiomsInModule.retainAll(moduleAxioms);

            Set<OWLLogicalAxiom> mappingAxiomsInModule = new HashSet<OWLLogicalAxiom>();
            mappingAxiomsInModule.addAll(mappingAxioms);
            mappingAxiomsInModule.retainAll(moduleAxioms);

            long timeForTreeSearch = System.currentTimeMillis();
            HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = searchConflicts(moduleAxioms, ontoAxiomsInModule);
            timeForTreeSearch = System.currentTimeMillis() - timeForTreeSearch;
            int numDiagnoses = search.getDiagnoses().size();
            int numConflicts = search.getConflicts().size();
            String sets = getNumbersForConflicts(search);
            int distanceToTop = distanceToTopComparator.getMeasure(unsat);

            String result = "unsat class: " + unsat + " module: " + moduleAxioms.size() + " from onto: " +
                    ontoAxiomsInModule.size() + " from mappings: " + mappingAxiomsInModule.size() + " " +
                    "conflicts: " + numConflicts + " diagnoses: " + numDiagnoses
                    + " time: " + timeForTreeSearch + " distance: " + distanceToTop + " found: " + sets;
            logger.info(result);
            return result;

        }

    }

    private String getNumbersForConflicts(HsTreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search) {
        String result = "";
        for (Set<OWLLogicalAxiom> conflict : search.getConflicts()) {
            result += getNumber(conflict) + "(" + conflict.size() + ")~";
        }
        return result;
    }

    private synchronized int getNumber(Set<OWLLogicalAxiom> conflict) {
        for (Set<OWLLogicalAxiom> knowConflict : knownConflicts) {
            if (compareSets(knowConflict,conflict))
                return knownConflicts.indexOf(knowConflict);
        }
        knownConflicts.add(conflict);
        return knownConflicts.size()-1;

    }

    protected boolean compareSets(Set<OWLLogicalAxiom> a, Set<OWLLogicalAxiom> b) {
        if (a.size() != b.size())
            return false;
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        for (OWLLogicalAxiom axiom : a) {
            boolean found = false;
            String renderedAxA = renderer.render(axiom);
            for (OWLLogicalAxiom axiom1 : b) {
                String renderedAxB = renderer.render(axiom1);
                if (renderedAxA.equals(renderedAxB))
                    found = true;
            }
            if(!found) return false;
        }
        return true;

    }

    protected Set<OWLLogicalAxiom> getAxioms (String filename) {

        InputStream stream = ClassLoader.getSystemResourceAsStream(filename);
        OWLOntology ontology = null;
        try {
            ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(stream);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology.getLogicalAxioms();
    }

    protected HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> searchConflicts(Set<OWLLogicalAxiom> axioms, Set<OWLLogicalAxiom> background) {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        OWLTheory theory = null;
        try {
            theory = new OWLTheory(new Reasoner.ReasonerFactory(), createOntology(axioms), background);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());

        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        search.setSearchable(theory);

        search.setMaxDiagnosesNumber(-1);
        try {
            search.start();
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return search;


    }


    List<Set<OWLLogicalAxiom>> knownConflicts = new LinkedList<Set<OWLLogicalAxiom>>();

}
