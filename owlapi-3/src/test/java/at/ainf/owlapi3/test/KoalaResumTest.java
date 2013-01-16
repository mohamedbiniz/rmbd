package at.ainf.owlapi3.test;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.diagnosis.tree.splitstrategy.MostProbableSplitStrategy;
import at.ainf.logging.SimulatedCalculationTest;
import at.ainf.logging.aop.ProfVarLogWatch;
import at.ainf.logging.aop.ProfiledVar;
import at.ainf.owlapi3.base.SimulatedSession;
import at.ainf.owlapi3.base.tools.TableList;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Ignore;
import org.junit.Test;
import org.perf4j.aop.Profiled;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 29.11.12
 * Time: 12:07
 * To change this template use File | Settings | File Templates.
 */
public class  KoalaResumTest {

    private static Logger logger = LoggerFactory.getLogger(KoalaResumTest.class.getName());

    private OWLLogicalAxiom getAxiom(Set<FormulaSet<OWLLogicalAxiom>> result, int hs, int axiom) {
        return ((OWLLogicalAxiom)((FormulaSet<OWLLogicalAxiom>)result.toArray()[hs]).toArray()[axiom]);
    }

    private String renderAxiom(OWLLogicalAxiom axiom) {
        return new ManchesterOWLSyntaxOWLObjectRendererImpl().render(axiom);
    }


    @Test
    public void searchKoalaTest() throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {



        //HS tree
          HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
//InputStream koalaStream2 = ClassLoader.getSystemResourceAsStream("ontologies/koala.owl");
 InputStream koalaStream2 = ClassLoader.getSystemResourceAsStream("ontologies/Economy-SDA.owl");
OWLOntology ontology2 = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream2);
Set<OWLLogicalAxiom> bax2 = new LinkedHashSet<OWLLogicalAxiom>();
for (OWLIndividual ind : ontology2.getIndividualsInSignature()) {
bax2.addAll(ontology2.getClassAssertionAxioms(ind));
bax2.addAll(ontology2.getObjectPropertyAssertionAxioms(ind));
}
OWLTheory theory2 = new OWLTheory(new Reasoner.ReasonerFactory(), ontology2, bax2);
search2.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
search2.setSearcher(new QuickXplain<OWLLogicalAxiom>());
search2.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory2));
search2.setSearchable(theory2);
long hsStart=System.currentTimeMillis();
Set<FormulaSet<OWLLogicalAxiom>> resultHs = performSearch(search2,ontology2, theory2);
long hsEnd=System.currentTimeMillis();
long hsTime=hsEnd-hsStart;


        //Binary Tree
       BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        //InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/koala.owl");
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/Economy-SDA.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
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
           long binaryStart=System.currentTimeMillis();
        Set<FormulaSet<OWLLogicalAxiom>> resultBin = performSearch(search,ontology, theory);
        long binaryEnd=System.currentTimeMillis();
        long binaryTime=binaryEnd-binaryStart;

     System.out.println("HS: "+ hsTime);
       System.out.println("Binary: "+ binaryTime);

       assertTrue(resultBin.equals(resultHs));

    }



    public Set<FormulaSet<OWLLogicalAxiom>> performSearch(AbstractTreeSearch search, OWLOntology ontology, OWLTheory theory) throws OWLOntologyCreationException, SolverException, InconsistentTheoryException {


        try {
            search.setMaxDiagnosesNumber(900);
            search.start();
        } catch (NoConflictException e) {

        }

        Set<FormulaSet<OWLLogicalAxiom>> result = new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());


        /*assertTrue(result.size() == 3);
        assertTrue(renderAxiom(getAxiom(result,0,0)).equals("Marsupials DisjointWith Person"));
        assertTrue(renderAxiom(getAxiom(result,1,0)).equals("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)"));
        assertTrue(renderAxiom(getAxiom(result,1,1)).equals("isHardWorking Domain Person"));
        assertTrue(renderAxiom(getAxiom(result,2,0)).equals("Koala SubClassOf Marsupials"));
        assertTrue(renderAxiom(getAxiom(result,2,1)).equals("Quokka SubClassOf isHardWorking value true"));
             */
       // OWLLogicalAxiom testcase = new MyOWLRendererParser(ontology).parse("Marsupials DisjointWith Person");

        //New testcase
       // OWLLogicalAxiom testcase = new MyOWLRendererParser(ontology).parse("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)");
       /* OWLLogicalAxiom testcase = new MyOWLRendererParser(ontology).parse("Koala SubClassOf Marsupials");

        theory.getKnowledgeBase().addEntailedTest(Collections.singleton(testcase));

        try {
            search.setMaxDiagnosesNumber(3);
            search.start();
        } catch (NoConflictException e) {

        }

        result = new LinkedHashSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            */
        /**assertTrue(result.size() == 3);
        assertTrue(renderAxiom(getAxiom(result,0,0)).equals("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)"));
        assertTrue(renderAxiom(getAxiom(result,0,1)).equals("isHardWorking Domain Person"));
        assertTrue(renderAxiom(getAxiom(result,1,0)).equals("Koala SubClassOf Marsupials"));
        assertTrue(renderAxiom(getAxiom(result,1,1)).equals("Quokka SubClassOf isHardWorking value true"));
        assertTrue(renderAxiom(getAxiom(result,2,0)).equals("Koala SubClassOf Marsupials"));
        assertTrue(renderAxiom(getAxiom(result,2,1)).equals("isHardWorking Domain Person"));**/
          return result;
    }


    @Ignore
   @Test
    public void doSimpleQuerySessionHS()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

     try {
         Thread.sleep(10000);
     } catch (InterruptedException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
     }

        SimulatedSession session = new SimulatedSession();

        session.setTraceDiagnosesAndQueries(true);
        session.setMinimizeQuery(true);

        session.setNumberOfHittingSets(2);
        SimulatedSession.QSSType type = SimulatedSession.QSSType.MINSCORE;
        boolean dual = false;
        String name = "koala.owl";
        //String name = "dualpaper.owl";

       // OWLOntology ontology = getOntologySimple("ontologies", name);

        HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new HsTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        //InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/koala.owl");
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/Economy-SDA.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
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

        Set<OWLLogicalAxiom> targetDg = new LinkedHashSet<OWLLogicalAxiom>();
        //targetDg.add(new MyOWLRendererParser(ontology).parse("Marsupials DisjointWith Person"));

      // targetDg.add(new MyOWLRendererParser(ontology).parse("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)"));
      // targetDg.add(new MyOWLRendererParser(ontology).parse("isHardWorking Domain Person"));

        //targetDg.add(new MyOWLRendererParser(ontology).parse("C SubClassOf not (D or E)"));

        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf C"));
        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf D"));

        long preprocessModulExtract = System.currentTimeMillis();
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;

       // OWLTheory theory = getExtendTheory(ontology, dual);
        //theory.addEntailedTest(new MyOWLRendererParser(ontology).parse("w Type B"));
        theory.setIncludeClassAssertionAxioms(true);
        theory.setIncludeTrivialEntailments(false);
        theory.setIncludeSubClassOfAxioms(false);
       // BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, dual);
        //((QuickXplain<OWLLogicalAxiom>)start.getSearcher()).setAxiomRenderer(new MyOWLRendererParser(null));

        CostsEstimator es = new SimpleCostsEstimator();
        search.setCostsEstimator(es);

        TableList e = new TableList();
        String message = "act," + type + "," + dual + "," + name + "," + preprocessModulExtract;

        logger.info("start session");
       logger.info("HS Tree");
        loggingTest();

        session.setEntry(e);
        session.setScoringFunct(type);
        session.setTargetD(targetDg);
        session.setMessage(message);
        session.setTheory(theory);
        session.setSearch(search);
        session.simulateQuerySession();
        logger.info("stop session ");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Appender<ILoggingEvent> coalescingStatistics = loggerContext.getLogger(ProfVarLogWatch.DEFAULT_LOGGER_NAME).getAppender("CoalescingStatistics");

        coalescingStatistics.stop();

        System.out.println("Number of nodes: "+((HSTreeNode<OWLLogicalAxiom>)(search.getRoot())).countNodes());
    }




    @Ignore
    @Test
    public void doSimpleQuerySessionBHS()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        SimulatedSession session = new SimulatedSession();

        session.setTraceDiagnosesAndQueries(true);
        session.setMinimizeQuery(true);

        session.setNumberOfHittingSets(2);
        SimulatedSession.QSSType type = SimulatedSession.QSSType.MINSCORE;
        boolean dual = false;
        String name = "koala.owl";
        //String name = "dualpaper.owl";

        // OWLOntology ontology = getOntologySimple("ontologies", name);

        BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = new BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom>();
        //InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/koala.owl");
        InputStream koalaStream = ClassLoader.getSystemResourceAsStream("ontologies/Economy-SDA.owl");
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(koalaStream);
        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }



        OWLTheory theory = new OWLTheory(new Reasoner.ReasonerFactory(), ontology, bax);

        MostProbableSplitStrategy<OWLLogicalAxiom> split = new MostProbableSplitStrategy<OWLLogicalAxiom>();
        split.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));

        search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        //search.setSplitStrategy(split);
        search.setSearcher(new QuickXplain<OWLLogicalAxiom>());
        search.setCostsEstimator(new OWLAxiomKeywordCostsEstimator(theory));
        search.setSearchable(theory);

        Set<OWLLogicalAxiom> targetDg = new LinkedHashSet<OWLLogicalAxiom>();
       // targetDg.add(new MyOWLRendererParser(ontology).parse("Marsupials DisjointWith Person"));

       // targetDg.add(new MyOWLRendererParser(ontology).parse("KoalaWithPhD EquivalentTo Koala and (hasDegree value PhD)"));
       // targetDg.add(new MyOWLRendererParser(ontology).parse("isHardWorking Domain Person"));


        //targetDg.add(new MyOWLRendererParser(ontology).parse("C SubClassOf not (D or E)"));

        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf C"));
        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf D"));

        long preprocessModulExtract = System.currentTimeMillis();
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;

        // OWLTheory theory = getExtendTheory(ontology, dual);
        //theory.addEntailedTest(new MyOWLRendererParser(ontology).parse("w Type B"));
        theory.setIncludeClassAssertionAxioms(true);
        theory.setIncludeTrivialEntailments(false);
        theory.setIncludeSubClassOfAxioms(false);
        // BinaryTreeSearch<FormulaSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = getUniformCostSearch(theory, dual);
        //((QuickXplain<OWLLogicalAxiom>)start.getSearcher()).setAxiomRenderer(new MyOWLRendererParser(null));

        CostsEstimator es = new SimpleCostsEstimator();
        search.setCostsEstimator(es);

        TableList e = new TableList();
        String message = "act," + type + "," + dual + "," + name + "," + preprocessModulExtract;

        logger.info("start session");
        logger.info("BHS Tree");


        loggingTest();

        session.setEntry(e);
        session.setScoringFunct(type);
        session.setTargetD(targetDg);
        session.setMessage(message);
        session.setTheory(theory);
        session.setSearch(search);
        session.simulateQuerySession();
        logger.info("stop session ");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Appender<ILoggingEvent> coalescingStatistics = loggerContext.getLogger(ProfVarLogWatch.DEFAULT_LOGGER_NAME).getAppender("CoalescingStatistics");

        coalescingStatistics.stop();


       System.out.println("Number of nodes: "+((HSTreeNode<OWLLogicalAxiom>)(search.getRoot())).countNodes());
                   BHSTreeNode<OWLLogicalAxiom> root = (BHSTreeNode<OWLLogicalAxiom>)search.getRoot();
        System.out.print("");
    }



    @Profiled(tag="time_loggingTest")
    @ProfiledVar(tag = "loggingTest")
    public long loggingTest() {
        logger.info("loggingTest does work");
        new SimulatedCalculationTest().doSimulation();
        return 7;
    }

}
