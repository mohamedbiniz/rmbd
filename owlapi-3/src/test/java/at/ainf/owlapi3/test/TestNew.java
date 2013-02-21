package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.tree.HsTreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.DepthFirstSearchStrategy;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLJustificationIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.module.OtfModuleProvider;
import at.ainf.owlapi3.module.SatisfiableQuickXplain;
import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 26.01.13
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class TestNew {

    private static Logger logger = LoggerFactory.getLogger(TestNew.class.getName());

    @Ignore
    @Test
    public void hsKoalaTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        String[] names = {"mouse2humangenlogmap"};
        // String[] names = {"koala", "Univ", "Economy-SDA", "mouse2humangenlogmap"};
        for (String name : names) {
            String onto = "ontologies/" + name + ".owl";
            OWLIncoherencyExtractor extractor = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory());
            OWLJustificationIncoherencyExtractor justExtractor
                    = new OWLJustificationIncoherencyExtractor(new Reasoner.ReasonerFactory());

            boolean isElOnto;
            if (name.endsWith("logmap"))
                isElOnto = true;
            else
                isElOnto = false;

            long timeNewer = System.currentTimeMillis();
            Set<? extends Set<OWLLogicalAxiom>> newerAxiomsResult = searchAllDiags(onto, isElOnto);
            timeNewer = System.currentTimeMillis() - timeNewer;

            /*long timeStandard = System.currentTimeMillis();
            Set<? extends Set<OWLLogicalAxiom>> standardAxiomsResult = searchAllDiags(extractor, onto);
            timeStandard = System.currentTimeMillis() - timeStandard;*/

            //long timeJust = System.currentTimeMillis();
            //Set<? extends Set<OWLLogicalAxiom>> justificationsAxiomsResult = searchAllDiags(justExtractor, onto);
            //timeJust = System.currentTimeMillis() - timeJust;

            //boolean ok = compareSetSets(standardAxiomsResult,justificationsAxiomsResult);
            //boolean ok2 = compareSetSets(standardAxiomsResult,newerAxiomsResult);

            //assertTrue("",ok && ok2);

            ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
            for (OWLLogicalAxiom axiom : newerAxiomsResult.iterator().next()) {
                logger.info("diagnosis axiom: " + renderer.render(axiom));
            }
            //logger.info("ont: " + name + " ok2: " + ok2
            //                      + " time newer: " + timeNewer + " time standard: " + timeStandard);
        }
    }

    @Test
    public void testFormula() {
        new FormulaSetImpl<Object>(BigDecimal.valueOf(0.5),Collections.emptySet(),null);
    }

    @Ignore
    @Test
    public void testRenderer() throws OWLOntologyCreationException {


        String onto = "ontologies/" + "Univ" + ".owl";
        boolean isElOnto = true;

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
        for (OWLLogicalAxiom axiom : ontFull.getLogicalAxioms())
            logger.info("axiom is: " + axiom);

    }

    protected boolean compareSetSets(Set<? extends Set<OWLLogicalAxiom>> a, Set<? extends Set<OWLLogicalAxiom>> b) {

        if (a.size()!=b.size())
            return false;

        for (Set<OWLLogicalAxiom> s : a) {
            boolean found = false;
            for (Set<OWLLogicalAxiom> t : b) {
                found = compareSets(s,t);
                if (found)
                    break;
            }
            if(!found)
                return false;
        }
        return true;

    }

    @Ignore
    @Test
    public void testMultModule() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {


          String onto = "ontologies/" + "fma2ncigenlogmap" + ".owl";
        boolean isElOnto = true;

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        OtfModuleProvider provider = new OtfModuleProvider(ontFull, new Reasoner.ReasonerFactory(), isElOnto);
        Set<OWLLogicalAxiom> unsatClassModule = provider.getModuleUnsatClass();

        long timeNormal = System.currentTimeMillis();
        OtfModuleProvider providerMult = new OtfModuleProvider(ontFull, new Reasoner.ReasonerFactory(), isElOnto);
        timeNormal = System.currentTimeMillis() - timeNormal;
        long timeMult = System.currentTimeMillis();
        //Set<OWLLogicalAxiom> unsatClassModuleMult = providerMult.getModuleUnsatClassMult();
        timeMult = System.currentTimeMillis() - timeMult;

        /*assertTrue(unsatClassModule.size()==unsatClassModuleMult.size() &&
                    unsatClassModule.containsAll(unsatClassModuleMult)); */

        logger.info("diagnosis axiom: " + timeNormal + timeMult);

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

    @Ignore  @Test
    public void overlap() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {


        String onto = "ontologies/fma2ncigenlogmap.owl";
        //String onto = "ontologies/TRANSPORTATION-SDA.owl";
        OWLIncoherencyExtractor extractor = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory());

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        List<Set<OWLLogicalAxiom>> modules = new LinkedList<Set<OWLLogicalAxiom>>();
        List<OWLOntology> ontologies = new LinkedList<OWLOntology>(extractor.getIncoherentPartAsMultipleOntologies(ontFull));
        for (OWLOntology module : ontologies) {
            modules.add(new LinkedHashSet<OWLLogicalAxiom>(module.getLogicalAxioms()));
        }
        Collections.sort(modules,new Comparator<Set<OWLLogicalAxiom>>() {
            @Override
            public int compare(Set<OWLLogicalAxiom> o1, Set<OWLLogicalAxiom> o2) {
                return - new Integer(o1.size()).compareTo(o2.size());
            }
        });

        List<Set<OWLLogicalAxiom>> withoutSame = new LinkedList<Set<OWLLogicalAxiom>>();
        for (int i = 0; i < modules.size(); i++) {
            boolean add = true;
            for (int j = i+1; j < modules.size(); j++) {
                if (compareSets(modules.get(i),modules.get(j)))
                    add = false;
            }
            if (add)
                withoutSame.add(modules.get(i));
        }

        List<Integer> onlyInSingleMod = new LinkedList<Integer>();
        for(int i = 0; i < withoutSame.size(); i++) {
            Set<OWLLogicalAxiom> module = new HashSet<OWLLogicalAxiom>(withoutSame.get(i));
            Set<OWLLogicalAxiom> sumOthers = new HashSet<OWLLogicalAxiom>();
            for (int j = 0; j < withoutSame.size(); j++) {
                if (j != i)
                    sumOthers.addAll(withoutSame.get(j));

            }
            module.removeAll(sumOthers);
            onlyInSingleMod.add(module.size());
        }

        String r = "";
        for(Integer n : onlyInSingleMod)
            r += n + ", ";
        logger.info(r);


        for (int i = 0; i < withoutSame.size(); i++) {
            String res = "";
            Set<OWLLogicalAxiom> modA = withoutSame.get(i);
            for (int j = 0; j < withoutSame.size(); j++) {
                if (j >= i) {
                    Set<OWLLogicalAxiom> modB = withoutSame.get(j);

                    Set<OWLLogicalAxiom> schnitt = new LinkedHashSet<OWLLogicalAxiom>(modA);
                    schnitt.retainAll(modB);

                    res += schnitt.size() + ", ";
                }
                else
                    res += ",";

            }
            logger.info(modA.size() + ",  " + res);
        }

        logger.info("");

    }

    protected Set<OWLLogicalAxiom> convertAxiom2LogicalAxiom (Set<OWLAxiom> axioms) {
        Set<OWLLogicalAxiom> result = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLAxiom axiom : axioms)
            result.add((OWLLogicalAxiom)axiom);
        return result;
    }

    @Ignore
    @Test
    public void treeOfUnSatClasses() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

         String onto = "ontologies/fma2ncigenlogmap.owl";
        // String onto = "ontologies/mouse2humangenlogmap.owl";
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ont2 = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        ModuleTools tools = new ModuleTools();
        List<OWLClass> topUnsat = tools.getTopUnsat(ont2.getLogicalAxioms());
        topUnsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());
        Map<OWLClass,Set<OWLLogicalAxiom>> topModules = tools.getModules(topUnsat,ont2.getLogicalAxioms());
        int unionTopSize = getUnion(topModules.values()).size();

        for (OWLClass topUnsatClass : topUnsat) {
            List<OWLClass> stillUnsat = tools.getStillUnsat(topUnsat,topModules.get(topUnsatClass));
            int size = topModules.get(topUnsatClass).size();
            String stillUnsatString = "";
            for (OWLClass u : stillUnsat)
                stillUnsatString += getNumber(u) + ", ";
            logger.info(getNumber(topUnsatClass) + " (" + size + "): " + stillUnsatString);
            List<OWLClass> stillUnsatWithout = new LinkedList<OWLClass>(stillUnsat);
            stillUnsatWithout.remove(topUnsatClass);
            Map<OWLClass,Set<OWLLogicalAxiom>> modules = tools.getModules(stillUnsatWithout,
                                                         topModules.get(topUnsatClass));
            for (OWLClass unsatClass : modules.keySet()) {
                List<OWLClass> stillUnsat2 = tools.getStillUnsat(stillUnsatWithout,modules.get(unsatClass));
                int siz2 = modules.get(unsatClass).size();
                String stillUnsatString2 = "";
                for (OWLClass u : stillUnsat2)
                    stillUnsatString2 += getNumber(u) + ", ";
                logger.info("  " + getNumber(unsatClass) + " (" + siz2 + "): " + stillUnsatString2);
            }
        }

        logger.info("" + topModules + unionTopSize);

    }


    @Ignore
    @Test
    public void listOfUnsatClasses() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        String onto = "ontologies/fma2ncigenlogmap.owl";
        // String onto = "ontologies/mouse2humangenlogmap.owl";
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ont2 = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        ModuleTools tools = new ModuleTools();
        List<OWLClass> topUnsat = tools.getTopUnsat(ont2.getLogicalAxioms());
        topUnsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());
        final Map<OWLClass,Set<OWLLogicalAxiom>> topModules = tools.getModules(topUnsat,ont2.getLogicalAxioms());
        int unionTopSize = getUnion(topModules.values()).size();
        Collections.sort(topUnsat,new Comparator<OWLClass>() {
            @Override
            public int compare(OWLClass o1, OWLClass o2) {
                return ((Integer) topModules.get(o1).size()).compareTo(topModules.get(o2).size());
            }
        });

        for (OWLClass topUnsatClass : topUnsat) {
            List<OWLClass> stillUnsat = tools.getStillUnsat(topUnsat,topModules.get(topUnsatClass));
            int size = topModules.get(topUnsatClass).size();
            String stillUnsatString = "";
            for (OWLClass u : stillUnsat)
                if (!u.equals(topUnsatClass))
                    stillUnsatString += getNumber(u) + ", ";
            logger.info(getNumber(topUnsatClass) + " (" + size + "): " + stillUnsatString);
        }

        logger.info("" + topModules + unionTopSize);




    }

    protected Set<OWLLogicalAxiom> getUnion(Collection<Set<OWLLogicalAxiom>> axiomSetSets) {
        Set<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
        for (Set<OWLLogicalAxiom> set : axiomSetSets)
            result.addAll(set);
        return result;
    }

    List<OWLClass> unsatClassesList = new LinkedList<OWLClass>();

    protected int getNumber(OWLClass unsat) {
        if (!unsatClassesList.contains(unsat))
            unsatClassesList.add(unsat);
        return unsatClassesList.indexOf(unsat);
    }

    @Ignore
    @Test
    public void smallestModuleTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        String onto = "ontologies/mouse2humangenlogmap.owl";
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ont2 = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        OWLOntology ontFull = createOntology(ont2.getLogicalAxioms());
        OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
        OtfModuleProvider provider = new OtfModuleProvider(ontFull, factory, true);

        provider.getModuleUnsatClass();
        List<Set<OWLLogicalAxiom>> modules = new LinkedList<Set<OWLLogicalAxiom>>(provider.getUnsatClasses().values());
        Collections.sort(modules,new SetComparator<OWLLogicalAxiom>());

        Collections.reverse(modules);
        List<Set<OWLLogicalAxiom>> withoutSame = new LinkedList<Set<OWLLogicalAxiom>>();
        for (int i = 0; i < modules.size(); i++) {
            boolean add = true;
            for (int j = i+1; j < modules.size(); j++) {
                if (compareSets(modules.get(i),modules.get(j)))
                    add = false;
            }
            if (add)
                withoutSame.add(modules.get(i));
        }

        List<Integer> onlyInSingleMod = new LinkedList<Integer>();
        for(int i = 0; i < withoutSame.size(); i++) {
            Set<OWLLogicalAxiom> module = new HashSet<OWLLogicalAxiom>(withoutSame.get(i));
            Set<OWLLogicalAxiom> sumOthers = new HashSet<OWLLogicalAxiom>();
            for (int j = 0; j < withoutSame.size(); j++) {
                if (j != i)
                    sumOthers.addAll(withoutSame.get(j));

            }
            module.removeAll(sumOthers);
            onlyInSingleMod.add(module.size());
        }

        String r = "";
        for(Integer n : onlyInSingleMod)
            r += n + ", ";
        logger.info(r);


        for (int i = 0; i < withoutSame.size(); i++) {
            String res = "";
            Set<OWLLogicalAxiom> modA = withoutSame.get(i);
            for (int j = 0; j < withoutSame.size(); j++) {
                if (j >= i) {
                    Set<OWLLogicalAxiom> modB = withoutSame.get(j);

                    Set<OWLLogicalAxiom> schnitt = new LinkedHashSet<OWLLogicalAxiom>(modA);
                    schnitt.retainAll(modB);

                    res += schnitt.size() + ", ";
                }
                else
                    res += ",";

            }
            logger.info(modA.size() + ",  " + res);
        }

        logger.info("");

        logger.info("ont: " + modules );

    }

    @Ignore  @Test
    public void numberConsChecksTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {


        //String onto = "ontologies/fma2ncigenlogmap.owl";
        String onto = "ontologies/mouse2humangenlogmap.owl";

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        SyntacticLocalityModuleExtractor extractor = createModuleExtractor(ontFull);
        OWLReasonerFactory factory = new Reasoner.ReasonerFactory();


        OWLReasoner reasoner = factory.createReasoner(ontFull);

        List<OWLClass> initialUnsat = new LinkedList<OWLClass>(getReasoner(ontFull).getUnsatisfiableClasses().getEntities());
        initialUnsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());

        List<OWLClass> unsat = getTopUnsat(ontFull,initialUnsat);

        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (OWLEntity entity : unsat)
            axioms.addAll(extractor.extract(Collections.singleton(entity)));

        BlackBoxExplanationCopy blackBoxExplanationCopy = new BlackBoxExplanationCopy(ontFull,factory,reasoner);
        blackBoxExplanationCopy.resetNumOfSatChecks();
        for (OWLClass entity : unsat)
            blackBoxExplanationCopy.getExplanation(entity);
        int num = blackBoxExplanationCopy.getNumOfSatChecks();
        long time = blackBoxExplanationCopy.getTimeSatChecks();

        Set<OWLAxiom> axiomsConf = extractor.extract(Collections.singleton((OWLEntity)unsat.get(0)));

        QuickXplain<OWLLogicalAxiom> quickXplain = new QuickXplain<OWLLogicalAxiom>();
        for (OWLClass entity : unsat) {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
            ontology.getOWLOntologyManager().addAxioms(ontology,extractSmallModule(extractor.extract(Collections.singleton((OWLEntity) entity))));
            OWLTheory theory = new OWLTheory(factory,ontology,Collections.<OWLLogicalAxiom>emptySet());
            Set<? extends Set<OWLLogicalAxiom>> res = Collections.emptySet();
            try {
                res = quickXplain.search(theory,ontology.getLogicalAxioms());
            } catch (NoConflictException e) {
                e.printStackTrace();
            }
        }
        int n = quickXplain.getNumOfChecks();
        long t = quickXplain.getTimeVerify();

        logger.info(num + " " + n + " " + time + " " + t  );

    }

    @Ignore @Test
    public void moduleTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {


        String onto = "ontologies/mouse2humangenlogmap.owl";
        OWLIncoherencyExtractor extractor = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory());
        OWLIncoherencyExtractor justExtr = new OWLJustificationIncoherencyExtractor(new Reasoner.ReasonerFactory());

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        long timeFull = System.currentTimeMillis();
        Set<OWLLogicalAxiom> all = extractor.getIncoherentPartAsOntology(ontFull).getLogicalAxioms();
        timeFull = System.currentTimeMillis() - timeFull;

        List<Set<OWLLogicalAxiom>> modules = new LinkedList<Set<OWLLogicalAxiom>>();
        long timeOntologies = System.currentTimeMillis();
        List<OWLOntology> ontologies = new LinkedList<OWLOntology>(extractor.getIncoherentPartAsMultipleOntologies(ontFull));
        timeOntologies = System.currentTimeMillis() - timeOntologies;
        long timeJust = System.currentTimeMillis();
        List<OWLOntology> justOntos = new LinkedList<OWLOntology>(justExtr.getIncoherentPartAsMultipleOntologies(ontFull));
        timeJust = System.currentTimeMillis() - timeJust;
        for (OWLOntology module : ontologies) {
            modules.add(new LinkedHashSet<OWLLogicalAxiom>(module.getLogicalAxioms()));
        }

        List<Integer> sizeModules = new LinkedList<Integer>();
        for (Set<OWLLogicalAxiom> module : modules)
            sizeModules.add(module.size());

        List<Integer> onlyInSingleMod = new LinkedList<Integer>();
        for(int i = 0; i < modules.size(); i++) {
            Set<OWLLogicalAxiom> module = new HashSet<OWLLogicalAxiom>(modules.get(i));
            Set<OWLLogicalAxiom> sumOthers = new HashSet<OWLLogicalAxiom>();
            for (int j = 0; j < modules.size(); j++) {
                  if (j != i)
                      sumOthers.addAll(modules.get(j));

            }
            module.removeAll(sumOthers);
            onlyInSingleMod.add(module.size());
        }

        Set<OWLLogicalAxiom> allModuleAx = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLOntology ontology : ontologies)
            allModuleAx.addAll(ontology.getLogicalAxioms());
        Set<OWLLogicalAxiom> allJustModuleAx = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLOntology ontology : justOntos)
            allJustModuleAx.addAll(ontology.getLogicalAxioms());

        logger.info(allModuleAx.size() + " " + allJustModuleAx.size() + "  " + timeFull + " " + timeJust
          + " " + timeOntologies  );

        int numIdent = 0;
        int numSubset = 0;
        for (int i = 0; i < modules.size(); i++) {
            for (int j = i+1; j < modules.size(); j++) {
                Set<OWLLogicalAxiom> modA = modules.get(i);
                Set<OWLLogicalAxiom> modB = modules.get(j);

                if (compareSets(modA,modB))
                    numIdent++;

                if (modA.size() != modB.size() && (subsetOf(modA,modB) || subsetOf(modB,modA))) {
                   numSubset++;
                }

            }
        }

        Collections.sort(ontologies,new Comparator<OWLOntology>() {
            @Override
            public int compare(OWLOntology o1, OWLOntology  o2 ) {
                return  -((Integer)o1.getLogicalAxioms().size()).compareTo(o2.getLogicalAxioms().size());
            }
        });



        Integer sumModules = 0;
        Integer min = Collections.min(sizeModules);
        Integer max = Collections.max(sizeModules);
        for (Integer n : sizeModules)
            sumModules += n;



        logger.info("all: " + all.size() + "num modules: " + modules.size() + " modules: " + sumModules + " " + min + " " + max);


    }

    private <E> boolean subsetOf(Set<E> setA, Set<E> setB) {
        if (setA.size() > setB.size())
            return false;

        for (E element : setA) {
            if (!setB.contains(element))
                return false;
        }
        return true;

    }

    @Ignore  @Test
    public void overlap2() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {


        String onto = "ontologies/fma2ncigenlogmap.owl";
        //String onto = "ontologies/TRANSPORTATION-SDA.owl";
        OWLIncoherencyExtractor extractor = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory());

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        List<Set<OWLLogicalAxiom>> modules = new LinkedList<Set<OWLLogicalAxiom>>();
        List<OWLOntology> ontologies = new LinkedList<OWLOntology>(extractor.getIncoherentPartAsMultipleOntologies(ontFull));
        for (OWLOntology module : ontologies) {
            modules.add(new LinkedHashSet<OWLLogicalAxiom>(module.getLogicalAxioms()));
        }
        Collections.sort(modules,new Comparator<Set<OWLLogicalAxiom>>() {
            @Override
            public int compare(Set<OWLLogicalAxiom> o1, Set<OWLLogicalAxiom> o2) {
                return - new Integer(o1.size()).compareTo(o2.size());
            }
        });

        List<Set<OWLLogicalAxiom>> withoutSame = new LinkedList<Set<OWLLogicalAxiom>>();
        for (int i = 0; i < modules.size(); i++) {
            boolean add = true;
            for (int j = i+1; j < modules.size(); j++) {
                if (compareSets(modules.get(i),modules.get(j)))
                    add = false;
            }
            if (add)
                withoutSame.add(modules.get(i));
        }

        List<Integer> onlyInSingleMod = new LinkedList<Integer>();
        for(int i = 0; i < withoutSame.size(); i++) {
            Set<OWLLogicalAxiom> module = new HashSet<OWLLogicalAxiom>(withoutSame.get(i));
            Set<OWLLogicalAxiom> sumOthers = new HashSet<OWLLogicalAxiom>();
            for (int j = 0; j < withoutSame.size(); j++) {
                if (j != i)
                    sumOthers.addAll(withoutSame.get(j));

            }
            module.removeAll(sumOthers);
            onlyInSingleMod.add(module.size());
        }

        String r = "";
        for(Integer n : onlyInSingleMod)
            r += n + ", ";
        logger.info(r);


        for (int i = 0; i < withoutSame.size(); i++) {
            String res = "";
            Set<OWLLogicalAxiom> modA = withoutSame.get(i);
            for (int j = 0; j < withoutSame.size(); j++) {
                if (j >= i) {
                    Set<OWLLogicalAxiom> modB = withoutSame.get(j);

                    Set<OWLLogicalAxiom> schnitt = new LinkedHashSet<OWLLogicalAxiom>(modA);
                    schnitt.retainAll(modB);

                    res += schnitt.size() + ", ";
                }
                else
                    res += ",";

            }
            logger.info(modA.size() + ",  " + res);
        }

        logger.info("");

    }

    /**
     * Repairs a bugs in StructuralReasoner.getDisjointClasses
     * @author ernesto
     *
     */
    public class StructuralReasonerExtended extends StructuralReasoner{

        public StructuralReasonerExtended(OWLOntology rootOntology) {
            super(rootOntology,  new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
        }


	/*public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct){
		try {
			return super.getSubClasses(ce, direct);
		}
		catch (Exception e){

			System.err.println(e.getMessage() +  " " + e.getCause());
			e.printStackTrace();
			return null;
		}
	}*/


        /**
         * It was an error in original method. the result set contained both the given class and its equivalents.
         */
        public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
            //super.ensurePrepared();
            OWLClassNodeSet nodeSet = new OWLClassNodeSet();
            if (!ce.isAnonymous()) {
                for (OWLOntology ontology : getRootOntology().getImportsClosure()) {
                    for (OWLDisjointClassesAxiom ax : ontology.getDisjointClassesAxioms(ce.asOWLClass())) {
                        for (OWLClassExpression op : ax.getClassExpressions()) {
                            if (!op.isAnonymous() && !op.equals(ce)) { //Op must be differnt to ce
                                nodeSet.addNode(getEquivalentClasses(op));
                            }
                        }
                    }
                }
            }



            return nodeSet;
        }


        public boolean isSubClassOf(OWLClass cls1, OWLClass cls2) {
            return getSubClasses(cls2, false).getFlattened().contains(cls1);
            //Checks only asserted axioms!!
            //isEntailed(super.getDataFactory().getOWLSubClassOfAxiom(cls1, cls2));
        }


        public boolean areEquivalent(OWLClass cls1, OWLClass cls2) {
            return (getEquivalentClasses(cls1).getEntities().contains(cls2)) ||
                    (getEquivalentClasses(cls2).getEntities().contains(cls1));
        }




        public String getReasonerName(){
            return "Extended Structural Reasoner";
        }


    }

    @Ignore @Test
    public void blackBoxParTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        String onto = "ontologies/fma2ncigenlogmap.owl";
        //String onto = "ontologies/mouse2humangenlogmap.owl";

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
        BlackBoxExplanation blackBoxExplanation = new BlackBoxExplanation(ontFull, factory, factory.createReasoner(ontFull));

        List<OWLClass> initialUnsat = new LinkedList<OWLClass>(blackBoxExplanation.getReasoner().getUnsatisfiableClasses().getEntities());
        initialUnsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());

        List<Set<OWLAxiom>> singleJustList = new LinkedList<Set<OWLAxiom>>();

        long timeOneJust = System.currentTimeMillis();
        for (OWLEntity unsatClass : initialUnsat)
            singleJustList.add(blackBoxExplanation.getExplanation((OWLClassExpression)unsatClass));
        timeOneJust = System.currentTimeMillis() - timeOneJust;

        logger.info("time " + timeOneJust);


    }

    protected List<OWLClass> getTopUnsat (OWLOntology ontology, List<OWLClass> unsat) {
        StructuralReasonerExtended structuralReasoner = new StructuralReasonerExtended(ontology);
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

    protected int getNumDiffAxioms (List<Set<OWLAxiom>> modules) {
        Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
        for (Set<OWLAxiom> module : modules) {
            allAxioms.addAll(module);
        }
        return allAxioms.size();
    }

    @Ignore @Test
    public void blackBoxVsModuleTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        String onto = "ontologies/mouse2humangenlogmap.owl";
        //String onto = "ontologies/fma2ncigenlogmap.owl";

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
        SyntacticLocalityModuleExtractor moduleStar = new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontFull, ModuleType.STAR);
        BlackBoxExplanation blackBoxExplanation = new BlackBoxExplanation(ontFull, factory, factory.createReasoner(ontFull));
        HSTExplanationGenerator hstExplanationGenerator = new HSTExplanationGenerator(blackBoxExplanation);

        List<OWLClass> initialUnsat = new LinkedList<OWLClass>(blackBoxExplanation.getReasoner().getUnsatisfiableClasses().getEntities());
        initialUnsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());


        List<OWLClass> unsatClasses = getTopUnsat(ontFull,initialUnsat);


        List<Set<OWLAxiom>> singleJustList = new LinkedList<Set<OWLAxiom>>();
        List<Set<OWLLogicalAxiom>> singleConflictList = new LinkedList<Set<OWLLogicalAxiom>>();
        List<Set<Set<OWLAxiom>>> allJustList = new LinkedList<Set<Set<OWLAxiom>>>();
        List<Set<OWLAxiom>> modulesAll = new LinkedList<Set<OWLAxiom>>();
        List<Set<OWLAxiom>> modules = new LinkedList<Set<OWLAxiom>>();

        long timeOneJust = System.currentTimeMillis();
        for (int i = 0; i < unsatClasses.size(); i++)
            singleJustList.add(blackBoxExplanation.getExplanation((OWLClassExpression)unsatClasses.get(i)));
        timeOneJust = System.currentTimeMillis() - timeOneJust;

        /*long timeModulesAll = System.currentTimeMillis();
        for (OWLEntity unsatClass : initialUnsat)
            modulesAll.add(moduleStar.extract(Collections.singleton(unsatClass)));
        timeModulesAll = System.currentTimeMillis() - timeModulesAll;*/

        long timeModulesStruct = System.currentTimeMillis();
        for (OWLEntity unsatClass : unsatClasses)
            modules.add(moduleStar.extract(Collections.singleton(unsatClass)));
        timeModulesStruct = System.currentTimeMillis() - timeModulesStruct;



        /*OWLOntology result = null;
        try {
            result = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://ainf.at/TempAnchorAssExtractionOntology"));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        result.getOWLOntologyManager().addAxioms(result, modules.get(0));

        File output = new File("fma2ncigenlogmapfirstmodule.owl");
        try {
            result.getOWLOntologyManager().saveOntology(result,IRI.create(output));
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        */


        int axiomsStruct = getNumDiffAxioms(modules);

        int axiomsAll = getNumDiffAxioms(modulesAll);


        long timeOneConflict = System.currentTimeMillis();
        long timeOnlySearch = 0  ;
        for (int i = 0; i < modules.size(); i++) {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
            ontology.getOWLOntologyManager().addAxioms(ontology,extractSmallModule(modules.get(i)));
            OWLTheory theory = new OWLTheory(factory,ontology,Collections.<OWLLogicalAxiom>emptySet());
            QuickXplain<OWLLogicalAxiom> quickXplain = new QuickXplain<OWLLogicalAxiom>();
            Set<? extends Set<OWLLogicalAxiom>> res = Collections.emptySet();
            try {
                long t = System.currentTimeMillis();
                res = quickXplain.search(theory,ontology.getLogicalAxioms());
                timeOnlySearch += System.currentTimeMillis() - t;
            } catch (NoConflictException e) {
                e.printStackTrace();
            }

            singleConflictList.add(res.iterator().next());
        }
        timeOneConflict = System.currentTimeMillis() - timeOneConflict;

        /*long timeAllJust = System.currentTimeMillis();
        for (OWLEntity unsatClass : unsatClasses)
            allJustList.add(hstExplanationGenerator.getExplanations((OWLClassExpression)unsatClass));
        timeAllJust = System.currentTimeMillis() - timeAllJust;*/

        logger.info(axiomsStruct + " " + axiomsAll);
        logger.info(timeOneConflict + " " + timeModulesStruct + " " + timeOnlySearch + " " + timeOneJust);



    }

    @Ignore @Test
    public void speedSat() throws OWLOntologyCreationException {

        //String onto = "ontologies/mouse2humangenlogmap.owl";
        String onto = "ontologies/fma2ncigenlogmap.owl";

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        //OWLReasoner reasoner = getReasoner(ontFull);

        OWLReasoner reasonerNew = getReasoner(ontFull);
        OWLReasoner reasonerNew2 = getReasoner(ontFull);
        long time = System.currentTimeMillis();
        Set<OWLClass> second = reasonerNew.getUnsatisfiableClasses().getEntities();
        time = System.currentTimeMillis() - time;
        long timeTesting = System.currentTimeMillis();
        for (OWLClass entity : second)
            reasonerNew2.isSatisfiable(entity);
        timeTesting = System.currentTimeMillis() - timeTesting;

        logger.info(time + " " + timeTesting + " " );

    }

    @Ignore @Test
    public void extractModulesTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {


        String onto = "ontologies/mouse2humangenlogmap.owl";
        //String onto = "ontologies/fma2ncigenlogmap.owl";

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = createOntology(OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream).getLogicalAxioms());

        OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
        SyntacticLocalityModuleExtractor moduleStar = new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontFull, ModuleType.STAR);
        BlackBoxExplanation blackBoxExplanation = new BlackBoxExplanation(ontFull, factory, factory.createReasoner(ontFull));
        HSTExplanationGenerator hstExplanationGenerator = new HSTExplanationGenerator(blackBoxExplanation);

        List<OWLClass> initialUnsat = new LinkedList<OWLClass>(blackBoxExplanation.getReasoner().getUnsatisfiableClasses().getEntities());
        initialUnsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());

        List<OWLClass> unsatClasses = getTopUnsat(ontFull,initialUnsat);

        Map<OWLEntity,Set<OWLAxiom>> modules = new HashMap<OWLEntity, Set<OWLAxiom>>();
        List<Set<OWLAxiom>> smallerModules = new LinkedList<Set<OWLAxiom>>();

        long timeModulesStruct = System.currentTimeMillis();
        for (OWLEntity unsatClass : unsatClasses) {
            modules.put(unsatClass, moduleStar.extract(Collections.singleton(unsatClass)));
        }
        timeModulesStruct = System.currentTimeMillis() - timeModulesStruct;

        for (Set<OWLAxiom> module : modules.values()) {
            OWLOntology unsatClassFirstOntology = createOntology(module);
            SyntacticLocalityModuleExtractor extractor = createModuleExtractor(unsatClassFirstOntology);
            for (OWLEntity unsatClass : getUnsatClasses(module)) {
                smallerModules.add(extractor.extract(Collections.singleton(unsatClass)));
            }
        }

        Collections.sort(smallerModules,new SetComparator());



        logger.info("");




    }

    protected class SetComparator<X> implements Comparator<Set<X>> {
        @Override
        public int compare(Set<X> o1, Set<X> o2) {
            return ((Integer) o1.size()).compareTo(o2.size());
        }
    }

    protected List<OWLClass> getUnsatClasses (Set<OWLAxiom> axioms) {
        return new LinkedList<OWLClass>(getReasoner(createOntology(axioms)).getUnsatisfiableClasses().getEntities());
    }

    protected SyntacticLocalityModuleExtractor createModuleExtractor(OWLOntology ontology) {
        return new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);
    }

    protected OWLOntology createOntology (Set<? extends OWLAxiom> axioms) {
        OWLOntology debuggingOntology = null;
        try {
            debuggingOntology = OWLManager.createOWLOntologyManager().createOntology();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        debuggingOntology.getOWLOntologyManager().addAxioms(debuggingOntology,axioms);
        return debuggingOntology;
    }

    protected OWLReasoner getReasoner(OWLOntology ontology) {
        return new Reasoner.ReasonerFactory().createReasoner(ontology);
    }


    @Ignore @Test
    public void oneModuleTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {


        String onto = "ontologies/mouse2humangenlogmap.owl";
        //String onto = "ontologies/fma2ncigenlogmap.owl";

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);

        OWLReasonerFactory factory = new Reasoner.ReasonerFactory();
        SyntacticLocalityModuleExtractor moduleStar = new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontFull, ModuleType.STAR);
        BlackBoxExplanation blackBoxExplanation = new BlackBoxExplanation(ontFull, factory, factory.createReasoner(ontFull));
        HSTExplanationGenerator hstExplanationGenerator = new HSTExplanationGenerator(blackBoxExplanation);

        List<OWLClass> initialUnsat = new LinkedList<OWLClass>(blackBoxExplanation.getReasoner().getUnsatisfiableClasses().getEntities());
        initialUnsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());


        List<OWLClass> unsatClasses = getTopUnsat(ontFull,initialUnsat);


        List<Set<OWLAxiom>> singleJustList = new LinkedList<Set<OWLAxiom>>();
        List<Set<OWLLogicalAxiom>> singleConflictList = new LinkedList<Set<OWLLogicalAxiom>>();
        List<Set<Set<OWLAxiom>>> allJustList = new LinkedList<Set<Set<OWLAxiom>>>();
        List<Set<OWLAxiom>> modulesAll = new LinkedList<Set<OWLAxiom>>();
        List<Set<OWLAxiom>> modules = new LinkedList<Set<OWLAxiom>>();

        long timeOneJust = System.currentTimeMillis();
        for (int i = 0; i < 1; i++)
            singleJustList.add(blackBoxExplanation.getExplanation((OWLClassExpression)unsatClasses.get(i)));
        timeOneJust = System.currentTimeMillis() - timeOneJust;

        /*long timeModulesAll = System.currentTimeMillis();
        for (OWLEntity unsatClass : initialUnsat)
            modulesAll.add(moduleStar.extract(Collections.singleton(unsatClass)));
        timeModulesAll = System.currentTimeMillis() - timeModulesAll;*/

        long timeModulesStruct = System.currentTimeMillis();
        for (OWLEntity unsatClass : unsatClasses)
            modules.add(moduleStar.extract(Collections.singleton(unsatClass)));
        timeModulesStruct = System.currentTimeMillis() - timeModulesStruct;


        extractSmallModule(modules.get(0));


        int axiomsStruct = getNumDiffAxioms(modules);

        int axiomsAll = getNumDiffAxioms(modulesAll);


        long timeOneConflict = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
            ontology.getOWLOntologyManager().addAxioms(ontology,modules.get(i));
            OWLTheory theory = new OWLTheory(factory,ontology,Collections.<OWLLogicalAxiom>emptySet());
            QuickXplain<OWLLogicalAxiom> quickXplain = new QuickXplain<OWLLogicalAxiom>();
            Set<? extends Set<OWLLogicalAxiom>> res = Collections.emptySet();
            try {
                res = quickXplain.search(theory,ontology.getLogicalAxioms());
            } catch (NoConflictException e) {
                e.printStackTrace();
            }

            singleConflictList.add(res.iterator().next());
        }
        timeOneConflict = System.currentTimeMillis() - timeOneConflict;

        long timeOneConflictSmaller = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
            ontology.getOWLOntologyManager().addAxioms(ontology,extractSmallerModule(modules.get(i), getUnsatClasses(modules.get(i), new HashSet<OWLClass>(unsatClasses))));
            OWLTheory theory = new OWLTheory(factory,ontology,Collections.<OWLLogicalAxiom>emptySet());
            QuickXplain<OWLLogicalAxiom> quickXplain = new QuickXplain<OWLLogicalAxiom>();
            Set<? extends Set<OWLLogicalAxiom>> res = Collections.emptySet();
            try {
                res = quickXplain.search(theory,ontology.getLogicalAxioms());
            } catch (NoConflictException e) {
                e.printStackTrace();
            }

            singleConflictList.add(res.iterator().next());
        }
        timeOneConflictSmaller = System.currentTimeMillis() - timeOneConflictSmaller;

        long timeOneConflictSmall = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
            ontology.getOWLOntologyManager().addAxioms(ontology,extractSmallModule(modules.get(i)));
            OWLTheory theory = new OWLTheory(factory,ontology,Collections.<OWLLogicalAxiom>emptySet());
            QuickXplain<OWLLogicalAxiom> quickXplain = new QuickXplain<OWLLogicalAxiom>();
            Set<? extends Set<OWLLogicalAxiom>> res = Collections.emptySet();
            try {
                res = quickXplain.search(theory,ontology.getLogicalAxioms());
            } catch (NoConflictException e) {
                e.printStackTrace();
            }

            singleConflictList.add(res.iterator().next());
        }
        timeOneConflictSmall = System.currentTimeMillis() - timeOneConflictSmall;

        /*long timeAllJust = System.currentTimeMillis();
        for (OWLEntity unsatClass : unsatClasses)
            allJustList.add(hstExplanationGenerator.getExplanations((OWLClassExpression)unsatClass));
        timeAllJust = System.currentTimeMillis() - timeAllJust;*/

        logger.info(axiomsStruct + " " + axiomsAll + "  " + timeOneConflictSmaller );
        logger.info(timeOneConflict + " " + timeModulesStruct + " " + timeOneJust + " " + timeOneConflictSmall);



    }

    protected Set<OWLAxiom> extractSmallModule(Set<OWLAxiom> axioms) {

        OWLOntology ontology = createOntology(axioms);
        OWLReasoner reasoner = getReasoner(ontology);
        SyntacticLocalityModuleExtractor extractor = createModuleExtractor(ontology);
        List<Set<OWLAxiom>> modules = new LinkedList<Set<OWLAxiom>>();
        Set<OWLClass> unsat = reasoner.getUnsatisfiableClasses().getEntities();
        unsat.remove(OWLManager.getOWLDataFactory().getOWLNothing());
        for (OWLEntity entity : unsat)
            modules.add(extractor.extract(Collections.singleton(entity)));
        Set<OWLAxiom> min = Collections.min(modules,new SetComparator<OWLAxiom>());

        if (min.size() == axioms.size())
            return min;

        return extractSmallModule(min);

    }

    protected Set<OWLAxiom> extractSmallerModule(Set<OWLAxiom> axioms,Set<OWLClass> unsat) {

        OWLOntology ontology = createOntology(axioms);
        SyntacticLocalityModuleExtractor extractor = createModuleExtractor(ontology);
        Map<Set<OWLAxiom>,Set<OWLClass>> modules = new HashMap<Set<OWLAxiom>,Set<OWLClass>>();

        for (OWLEntity entity : unsat) {
            Set<OWLAxiom> module = extractor.extract(Collections.singleton(entity));
            Set<OWLClass> unsatInModule = getUnsatClasses(module,unsat);
            modules.put(module, unsatInModule);

        }
        Set<OWLAxiom> min = Collections.min(modules.keySet(),new SetComparator<OWLAxiom>());

        if (min.size() == axioms.size())
            return min;

        return extractSmallerModule(min, modules.get(min));

    }

    public Set<OWLClass> getUnsatClasses(Set<OWLAxiom> axioms, Set<OWLClass> possibleUnsat) {
        Set<OWLClass> unsat = new HashSet<OWLClass>();
        OWLReasoner reasoner = getReasoner(createOntology(axioms));
        for (OWLClass possUnsat : possibleUnsat)
            if (!reasoner.isSatisfiable(possUnsat))
                unsat.add(possUnsat);
        return unsat;
    }


    protected Set<? extends Set<OWLLogicalAxiom>> searchAllDiags(OWLIncoherencyExtractor extractor, String onto)
            throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        OWLOntology ontology = extractor.getIncoherentPartAsOntology(ontFull);
        logger.info(extractor.toString() + " " + ontology.getLogicalAxioms().size());

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLTheory theory = new OWLTheory(new Reasoner.ReasonerFactory(), ontology, bax);

        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());

        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        search.setSearchable(theory);

        try {
            search.setMaxDiagnosesNumber(1);
            search.start();
        } catch (NoConflictException e) {

        }
        return search.getDiagnoses();

    }

    protected Set<? extends Set<OWLLogicalAxiom>> searchAllDiags(String onto, boolean isElOnto)
            throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {
        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();

        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(onto);
        OWLOntology ontFull = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        OtfModuleProvider provider = new OtfModuleProvider(ontFull, new Reasoner.ReasonerFactory(), isElOnto);
        OWLOntology ontology = createOntology(provider.getModuleUnsatClass());

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLTheory theory = new OWLTheory(new Reasoner.ReasonerFactory(), ontology, bax);

        search.setSearchStrategy(new DepthFirstSearchStrategy<OWLLogicalAxiom>());
        //search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());

        search.setSearcher(new SatisfiableQuickXplain<OWLLogicalAxiom>());

        ((SatisfiableQuickXplain<OWLLogicalAxiom>) search.getSearcher()).setModuleProvider(provider);

        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        search.setSearchable(theory);

        try {
            search.setMaxDiagnosesNumber(1);
            search.start();
        } catch (NoConflictException e) {

        }
        return search.getDiagnoses();

    }

    private List<OWLLogicalAxiom> knownAxioms = new LinkedList<OWLLogicalAxiom>();

    public Integer getNumber(OWLLogicalAxiom axiom) {
        int num = knownAxioms.indexOf(axiom);
        if (num == -1) {
            knownAxioms.add(axiom);
            num = knownAxioms.size() - 1;
        }

        return num;
    }

    @Ignore @Test
    public void blackKoalaTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {

        String path = "ontologies/";
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(path + "Univ.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        Reasoner.ReasonerFactory factory = new Reasoner.ReasonerFactory();
        OWLReasoner reasoner = factory.createReasoner(ontology);
        BlackBoxExplanation blackBox = new BlackBoxExplanation(ontology,factory,reasoner);

        HSTExplanationGenerator generator = new HSTExplanationGenerator(blackBox);

        List<Set<OWLAxiom>> explanations = new LinkedList<Set<OWLAxiom>>();

        for (OWLClass entity : reasoner.getUnsatisfiableClasses().getEntities()) {
            logger.info("entity: " + entity.toString());
            for (Set<OWLAxiom> just : generator.getExplanations(entity)) {
                String set = "";
                for(OWLAxiom axiom : just) {
                    set += getNumber((OWLLogicalAxiom) axiom) + ", ";
                }
                logger.info(set);
            }
        }

        //PlanExtractor planExtractor = new PlanExtractor(explanations);
        //planExtractor.extractPlans();
        //List<Set<OWLAxiom>> repair_plans = planExtractor.getAllPlansAx();
    }

    private Set<OWLAxiom> getAllJust(OWLOntology ontology, Reasoner.ReasonerFactory factory) {
        Set<OWLAxiom> allJust = new LinkedHashSet<OWLAxiom>();
        OWLReasoner reasoner = factory.createReasoner(ontology);
        BlackBoxExplanation blackBox = new BlackBoxExplanation(ontology,factory,reasoner);
        HSTExplanationGenerator generator = new HSTExplanationGenerator(blackBox);
        for (OWLClass entity : reasoner.getUnsatisfiableClasses().getEntities()) {
            for(Set<OWLAxiom> just : generator.getExplanations(entity))
                allJust.addAll(just);
        }
        return allJust;

    }

    @Ignore @Test
    public void testJustExtractor() throws OWLOntologyCreationException {

        String path = "ontologies/";
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(path + "koala.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        Reasoner.ReasonerFactory factory = new Reasoner.ReasonerFactory();

        long timeForModule = System.currentTimeMillis();
        OWLIncoherencyExtractor extractor = new OWLIncoherencyExtractor(factory);
        OWLOntology module = extractor.getIncoherentPartAsOntology(ontology);
        Set<OWLLogicalAxiom> allModule = module.getLogicalAxioms();
        timeForModule = System.currentTimeMillis() - timeForModule;

        long timeForModuleJust = System.currentTimeMillis();
        OWLJustificationIncoherencyExtractor justificationExtractor = new OWLJustificationIncoherencyExtractor(factory);
        OWLOntology justModule = justificationExtractor.getIncoherentPartAsOntology(ontology);
        Set<OWLLogicalAxiom> allJustModule = justModule.getLogicalAxioms();
        timeForModuleJust = System.currentTimeMillis() - timeForModuleJust;

        logger.info("time module: " + timeForModule + " module num: " + allModule.size());
        logger.info("time module just: " + timeForModuleJust + " just num: " + allJustModule.size());


    }

    @Ignore @Test
    public void moduleVsJust() throws OWLOntologyCreationException {

        String path = "ontologies/";
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream(path + "Economy-SDA.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        Reasoner.ReasonerFactory factory = new Reasoner.ReasonerFactory();

        long timeForModule = System.currentTimeMillis();
        OWLIncoherencyExtractor extractor = new OWLIncoherencyExtractor(factory);
        OWLOntology module = extractor.getIncoherentPartAsOntology(ontology);
        Set<OWLLogicalAxiom> allModule = module.getLogicalAxioms();
        timeForModule = System.currentTimeMillis() - timeForModule;

        long timeForJust = System.currentTimeMillis();
        Set<OWLAxiom> allJust = getAllJust(ontology,factory);
        timeForJust = System.currentTimeMillis() - timeForJust;

        long timeForModuleJust = System.currentTimeMillis();
        Set<OWLAxiom> allModuleJust = getAllJust(module,factory);
        timeForModuleJust = System.currentTimeMillis() - timeForModuleJust + timeForModule;

        logger.info("time module: " + timeForModule + " module num: " + allModule.size());
        logger.info("time just: " + timeForJust + " just num: " + allJust.size());
        logger.info("time module just: " + timeForModuleJust + " just num: " + allModuleJust.size());


    }




}
