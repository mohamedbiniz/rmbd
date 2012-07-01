package at.ainf.owlapi3.performance.query;

import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.quickxplain.DirectDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.*;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.diagnosis.tree.searchstrategy.UniformCostSearchStrategy;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlapi3.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlapi3.parser.MyOWLRendererParser;
import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.storage.*;
import at.ainf.owlapi3.performance.OAEI2011;
import at.ainf.owlapi3.utils.distribution.ExtremeDistribution;
import at.ainf.owlapi3.utils.distribution.ModerateDistribution;
import at.ainf.owlapi3.performance.query.table.TableList;
import at.ainf.owlapi3.performance.query.table.Time;
import at.ainf.owlapi3.utils.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import static at.ainf.owlapi3.utils.SimulatedSession.QSSType;

import static at.ainf.owlapi3.utils.Constants.DiagProbab;
import static at.ainf.owlapi3.utils.Constants.UsersProbab;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.03.12
 * Time: 08:26
 * To change this template use File | Settings | File Templates.
 */
public class UnsolvableTests {

    private static Logger logger = Logger.getLogger(UnsolvableTests.class.getName());

    //private boolean showElRates = true;


    //public static int NUMBER_OF_HITTING_SETS = 9;
    //protected static BigDecimal SIGMA = new BigDecimal("100");
    //protected static boolean userBrk = true;

    //protected int diagnosesCalc = 0;
    //protected int conflictsCalc = 0;
    //protected String daStr = "";

    //public enum QSSType {MINSCORE, SPLITINHALF, STATICRISK, DYNAMICRISK, PENALTY, NO_QSS};

    protected Random rnd = new Random();








    //private boolean traceDiagnosesAndQueries = false;
    //private boolean minimizeQuery = false;

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlapi3-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    protected Set<OWLLogicalAxiom> getDiagnosis(String[] targetAxioms, OWLOntology ontology) {

        Set<OWLLogicalAxiom> res = new LinkedHashSet<OWLLogicalAxiom>();
        for (String targetAxiom : targetAxioms) {
            for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                if (axiom.toString().contains(targetAxiom.trim()))
                    res.add(axiom);
            }
        }
        return res;
    }

    enum TargetSource {FROM_FILE, FROM_30_DIAGS}

    protected OWLOntology createOwlOntology(String matcher, String name) {
        String path = ClassLoader.getSystemResource("alignment/" + matcher + "_incoherent_matched_ontologies").getPath();
        File ontF = new File(path + "/" + name + ".owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontF);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }

    protected OWLTheory createOWLTheory(OWLOntology ontology, boolean dual) {
        OWLTheory result = null;

        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
            bax.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }

        /*String iri = "http://ainf.at/testiri#";

        for (OWLClass ind : ontology.getClassesInSignature()) {
            OWLDataFactory fac = OWLManager.getOWLDataFactory();
            OWLIndividual test_individual = fac.getOWLNamedIndividual(IRI.create(iri + "{"+ind.getIRI().getFragment()+"}"));

            bax.add(fac.getOWLClassAssertionAxiom (ind,test_individual));
        }*/

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        try {
            //ProbabilityTableModel mo = new ProbabilityTableModel();
            //HashMap<ManchesterOWLSyntax, Double> map = mo.getProbMap();

            if (dual)
                result = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
            else
                result = new OWLTheory(reasonerFactory, ontology, bax);
            result.activateReduceToUns();

            result.setIncludeTrivialEntailments(false);
            // QueryDebuggerPreference.getInstance().setTestIncoherencyToInconsistency(true);

            result.setIncludeSubClassOfAxioms(false);
            result.setIncludeClassAssertionAxioms(false);
            result.setIncludeEquivalentClassAxioms(false);
            result.setIncludeDisjointClassAxioms(false);
            result.setIncludePropertyAssertAxioms(false);
            result.setIncludeReferencingThingAxioms(false);
            result.setIncludeOntologyAxioms(true);
            //  result.setIncludeTrivialEntailments(true);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    /*protected BreadthFirstSearch<OWLLogicalAxiom> createBreathFirstSearch(OWLTheory th, boolean dual) {

        SimpleStorage<OWLLogicalAxiom> storage;
        if (dual)
            storage = new DualStorage<OWLLogicalAxiom>();
        else
            storage = new SimpleStorage<OWLLogicalAxiom>();
        BreadthFirstSearch<OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        if (dual) {
            search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
            search.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());
        } else
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        search.setTheory(th);

        return search;
    } */

    protected TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> createUniformCostSearch(OWLTheory th, boolean dual) {

        /*SimpleStorage<OWLLogicalAxiom> storage;
        if (dual)
            storage = new SimpleStorage<OWLLogicalAxiom>();
        else
            storage = new SimpleStorage<OWLLogicalAxiom>();*/
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search;
        if (dual) {
            search = new InvHsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new DirectDiagnosis<OWLLogicalAxiom>());
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        } else {
            search = new HsTreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>();
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
            search.setSearchStrategy(new UniformCostSearchStrategy<OWLLogicalAxiom>());
        }
        search.setTheory(th);

        return search;
    }

    public AxiomSet<OWLLogicalAxiom> getTargetDiag(Set<AxiomSet<OWLLogicalAxiom>> diagnoses, final CostsEstimator<OWLLogicalAxiom> e, String m) {
        Comparator<AxiomSet<OWLLogicalAxiom>> c = new Comparator<AxiomSet<OWLLogicalAxiom>>() {
            public int compare(AxiomSet<OWLLogicalAxiom> o1, AxiomSet<OWLLogicalAxiom> o2) {
                int numOfOntologyAxiomsO1 = 0;
                int numOfMatchingAxiomO1 = 0;
                for (OWLLogicalAxiom axiom : o1) {
                    if (e.getAxiomCosts(axiom).compareTo(new BigDecimal("0.001")) != 0)
                        numOfMatchingAxiomO1++;
                    else
                        numOfOntologyAxiomsO1++;
                }
                double percAxiomFromOntO1 = (double) numOfOntologyAxiomsO1;// / (numOfOntologyAxiomsO1 + numOfMatchingAxiomO1);

                int numOfOntologyAxiomsO2 = 0;
                int numOfMatchingAxiomO2 = 0;
                for (OWLLogicalAxiom axiom : o2) {
                    if (e.getAxiomCosts(axiom).compareTo(new BigDecimal("0.001")) != 0)
                        numOfMatchingAxiomO2++;
                    else
                        numOfOntologyAxiomsO2++;
                }
                double percAxiomFromOntO2 = (double) numOfOntologyAxiomsO2;// / (numOfOntologyAxiomsO2 + numOfMatchingAxiomO2);


                if (percAxiomFromOntO1 < percAxiomFromOntO2)
                    return -1;
                else if (percAxiomFromOntO1 == percAxiomFromOntO2)
                    return 0;
                else
                    return 1;
            }
        };

        return Collections.max(diagnoses, c);

    }










    protected OWLOntology createOwlOntology(String name) {
        String path = ClassLoader.getSystemResource("alignment").getPath();
        File ontF = new File(path + "/" + name + ".owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontF);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ontology;
    }

    @Test
    public void calcOneDiagAndMore() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = Utils.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = Utils.readOntologiesFromFile(properties);

        for (boolean dual : new boolean[]{true, false}) {

            for (String m : mapOntos.keySet()) {
                for (String o : mapOntos.get(m)) {
                    for (int nd : new int[]{1, 5, 9}) {
                        String out = "STAT, " + m + ", " + o;

                        String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                        OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                        Set<OWLLogicalAxiom> targetDg;
                        OWLTheory theory = createOWLTheory(ontology, dual);
                        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);
                        //ProbabilityTableModel mo = new ProbabilityTableModel();
                        HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();

                        OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                        OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());

                        String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                + m.trim()
                                + "-incoherent-evaluation/"
                                + o.trim()
                                + ".txt").getPath();

                        OWLTheory theory2 = createOWLTheory(ontology, dual);
                        OWLTheory t3 = createOWLTheory(ontology, dual);
                        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = createUniformCostSearch(theory2, dual);
                        search2.setCostsEstimator(new OWLAxiomCostsEstimator(theory2, path));

                        //theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                        //theory.addBackgroundFormulas(ontology2.getLogicalAxioms());

                        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);

                        //es.updateKeywordProb(map);
                        targetDg = null;

                        search.setCostsEstimator(es);
                        //
                        long time = System.nanoTime();
                        try {
                            search.run(nd);
                        } catch (SolverException e) {
                            logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (NoConflictException e) {
                            logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (InconsistentTheoryException e) {
                            logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        time = System.nanoTime() - time;

                        int minDiagnosisC = minCard(search.getDiagnoses());
                        double meanDiagnosisC = meanCard(search.getDiagnoses());
                        int maxDiagnosisC = maxCard(search.getDiagnoses());
                        int minConfC = minCard(search.getConflicts());
                        double meanConfC = meanCard(search.getConflicts());
                        int maxConfC = maxCard(search.getConflicts());

                        int c = search.getConflicts().size();
                        String s = nd + ", " + minDiagnosisC + ", " + meanDiagnosisC + ", " + maxDiagnosisC + ", " +
                                c + ", " + minConfC + ", " + meanConfC + ", " + maxConfC;

                        logger.info("Stat, " + m.trim() + ", " + o.trim() + ", "
                                + s + ", "
                                + theory.getConsistencyCount() + ", " + dual + ", " + Utils.getStringTime(time / 1000000));
                    }
                }
            }

        }
    }

    public int minCard(Set<AxiomSet<OWLLogicalAxiom>> s) {
        int r = -1;

        try {
            for (AxiomSet<OWLLogicalAxiom> set : s)
                if (r == -1 || set.size() < r)
                    r = set.size();
        } catch (NoSuchElementException e) {

        }

        return r;
    }

    public int maxCard(Set<AxiomSet<OWLLogicalAxiom>> s) {
        int r = -1;

        try {
            for (AxiomSet<OWLLogicalAxiom> set : s)
                if (set.size() > r)
                    r = set.size();
        } catch (NoSuchElementException e) {

        }

        return r;
    }

    public static double meanCard(Set<AxiomSet<OWLLogicalAxiom>> s) {
        double sum = 0;
        int cnt = 0;

        for (AxiomSet<OWLLogicalAxiom> set : s) {
            sum += set.size();
            cnt++;
        }

        if (cnt == 0) return -1;
        return sum / cnt;
    }

    @Test
    public void testDaReadMethods() throws IOException, SolverException, InconsistentTheoryException {
        Properties properties = Utils.readProps("alignment.properties");
        Map<String, List<String>> mapOntos = Utils.readOntologiesFromFile(properties);

        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                String[] targetAxioms2 = Utils.getDiagnosis(m, o);
                boolean eq = Utils.compareDiagnoses(targetAxioms, targetAxioms2);
                if (!eq) {
                    OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                    Set<OWLLogicalAxiom> targetDg = getDiagnosis(targetAxioms, ontology);
                    System.out.println(targetAxioms.toString());
                    System.out.println(targetAxioms2.toString());
                }
                assertTrue(m + " " + o, eq);
            }
        }
    }

    public OWLOntology createOntologyFromTxtOAEI(String file) {

        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();

            InputStream st = ClassLoader.getSystemResourceAsStream("oaei11/mouse.owl");
            OWLOntology mouse = man.loadOntologyFromOntologyDocument(st);
            st = ClassLoader.getSystemResourceAsStream("oaei11/human.owl");
            OWLOntology human = man.loadOntologyFromOntologyDocument(st);

            OWLOntologyMerger merger = new OWLOntologyMerger(man);
            OWLOntology merged = merger.createMergedOntology(man, IRI.create("matched" + file + ".txt"));
            Set<OWLLogicalAxiom> mappAx = getAxiomsInMappingOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/", file);
            for (OWLLogicalAxiom axiom : mappAx)
                man.applyChange(new AddAxiom(merged, axiom));

            return merged;
        } catch (OWLOntologyCreationException e) {
            return null;
        }
    }

    public Set<OWLLogicalAxiom> getAxiomsInMappingOAEI(String path, String source) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        Map<OWLLogicalAxiom, Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
        Set<OWLLogicalAxiom> targetDiagnosis = new LinkedHashSet<OWLLogicalAxiom>();
        try {
            readDataOAEI(path + source + ".txt", axioms, targetDiagnosis, man);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return axioms.keySet();
    }

    public void readDataOAEI(String filename, Map<OWLLogicalAxiom, Double> axioms, Set<OWLLogicalAxiom> targetDiag, OWLOntologyManager man) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        String sourceNamespace = "";
        String targetNamespace = "";
        while ((line = br.readLine()) != null) {
            if (line.startsWith("sourceNamespace"))
                sourceNamespace = line.substring(line.indexOf("=") + 1).trim();
            if (line.startsWith("targetNamespace"))
                targetNamespace = line.substring(line.indexOf("=") + 1).trim();
            if (line.startsWith(">") || line.startsWith("<") || line.startsWith("+") || line.startsWith("-")) {
                String status = line.substring(0, 2).trim();
                String sub = line.substring(2);
                String source = "";
                String target = "";
                if (sub.contains("=")) {
                    source = sub.substring(0, sub.indexOf("=")).trim();
                    target = sub.substring(sub.indexOf("=") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                    axioms.put(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains(">")) {
                    source = sub.substring(0, sub.indexOf(">")).trim();
                    target = sub.substring(sub.indexOf(">") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (sub.contains("<")) {
                    source = sub.substring(0, sub.indexOf("<")).trim();
                    target = sub.substring(sub.indexOf("<") + 1, sub.indexOf("|")).trim();
                    axioms.put(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man),
                            Double.parseDouble(sub.substring(sub.indexOf("|") + 1)));
                }
                if (status.equals("-")) {
                    if (sub.contains("=")) {
                        targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man));
                        targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man));
                    } else if (sub.contains(">"))
                        targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man));
                    else if (sub.contains("<"))
                        targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man));
                }
                if (status.equals(">")) {
                    targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man));
                }
                if (status.equals("<")) {
                    targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source, man));
                }

            }
        }
    }

    public Set<OWLLogicalAxiom> getTargetDOAEI(String path, String source) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        Map<OWLLogicalAxiom, Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
        Set<OWLLogicalAxiom> targetDiagnosis = new LinkedHashSet<OWLLogicalAxiom>();
        try {
            readDataOAEI(path + source + ".txt", axioms, targetDiagnosis, man);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return targetDiagnosis;
    }

    protected OWLTheory createTheoryOAEI(OWLOntology ontology, boolean dual, boolean reduceIncoherency) {
        OWLTheory result = null;

        //ontology = new OWLIncoherencyExtractor(
        //        new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();


        Set<OWLLogicalAxiom> bax = new LinkedHashSet<OWLLogicalAxiom>();

        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
            bax.addAll(ontology.getDataPropertyAssertionAxioms(ind));
        }

        /*String iri = "http://ainf.at/testiri#";

        for (OWLClass ind : ontology.getClassesInSignature()) {
            OWLDataFactory fac = OWLManager.getOWLDataFactory();
            OWLIndividual test_individual = fac.getOWLNamedIndividual(IRI.create(iri + "{"+ind.getIRI().getFragment()+"}"));

            bax.add(fac.getOWLClassAssertionAxiom (ind,test_individual));
        }*/

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        try {
            //ProbabilityTableModel mo = new ProbabilityTableModel();
            //HashMap<ManchesterOWLSyntax, Double> map = mo.getProbMap();

            if (dual)
                result = new DualTreeOWLTheory(reasonerFactory, ontology, bax);
            else
                result = new OWLTheory(reasonerFactory, ontology, bax);
            if (reduceIncoherency)
                result.activateReduceToUns();

            result.setIncludeTrivialEntailments(false);
            // QueryDebuggerPreference.getInstance().setTestIncoherencyToInconsistency(true);

            result.setIncludeSubClassOfAxioms(false);
            result.setIncludeClassAssertionAxioms(false);
            result.setIncludeEquivalentClassAxioms(false);
            result.setIncludeDisjointClassAxioms(false);
            result.setIncludePropertyAssertAxioms(false);
            result.setIncludeReferencingThingAxioms(false);
            result.setIncludeOntologyAxioms(true);
            //  result.setIncludeTrivialEntailments(true);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }


    public Set<OWLLogicalAxiom> getLogicalAxiomsOfOntologiesOAEI() throws OWLOntologyCreationException {
        Set<OWLLogicalAxiom> r = new LinkedHashSet<OWLLogicalAxiom>();

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        InputStream st = ClassLoader.getSystemResourceAsStream("oaei11/mouse.owl");
        OWLOntology mouse = man.loadOntologyFromOntologyDocument(st);
        st = ClassLoader.getSystemResourceAsStream("oaei11/human.owl");
        OWLOntology human = man.loadOntologyFromOntologyDocument(st);

        r.addAll(mouse.getLogicalAxioms());
        r.addAll(human.getLogicalAxioms());

        return r;
    }

    private OWLLogicalAxiom createAxiomOAEI(String sourceNamespace, String source, String targetNamespace, String target, OWLOntologyManager man) {
        OWLLogicalAxiom axiom = OAEI2011.createAxiomOAEI(sourceNamespace, source, targetNamespace, target, man);

        return axiom;
        // "<" + sourceNamespace + "#" + source + "> <" + targetNamespace + "#" + target + ">";
    }

    private class MyFilenameFilter implements FilenameFilter {
        private Set<String> acceptedNames;

        public MyFilenameFilter(File includedNames) {
            acceptedNames = new LinkedHashSet<String>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(includedNames)));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    if (!strLine.startsWith("#") || !strLine.endsWith(".rdf"))
                        acceptedNames.add(strLine);
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        public boolean accept(File dir, String name) {
            return acceptedNames.contains(name);
        }
    }

    private Set<OWLLogicalAxiom> getRandomDiag(File file, String directory) throws SolverException, InconsistentTheoryException {
        String matchingsDir = "oaei11conference/matchings/";
        String mapd = matchingsDir + directory;

        String fileName = file.getName();
        StringTokenizer t = new StringTokenizer(fileName, "-");
        String matcher = t.nextToken();

        String o1 = t.nextToken();
        String o2 = t.nextToken();
        o2 = o2.substring(0, o2.length() - 4);

        String n = file.getName().substring(0, file.getName().length() - 4);
        OWLOntology merged = Utils.createOntologyWithRdfMappings("oaei11conference/ontology",
                o1, o2, mapd, n + ".rdf");

        OWLOntology ontology = new OWLIncoherencyExtractor(
                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
        OWLTheory theory = createTheoryOAEI(ontology, true, true);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, true);

        LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
        OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology", o1 + ".owl");
        OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology", o2 + ".owl");
        bx.addAll(Utils.getIntersection(ontology.getLogicalAxioms(), ontology1.getLogicalAxioms()));
        bx.addAll(Utils.getIntersection(ontology.getLogicalAxioms(), ontology2.getLogicalAxioms()));
        theory.addBackgroundFormulas(bx);

        Map<OWLLogicalAxiom, BigDecimal> map1 = Utils.readRdfMapping(mapd, n + ".rdf");

        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);


        search.setCostsEstimator(es);

        search.reset();


        Set<OWLLogicalAxiom> targetDg = null;


        OWLTheory th30 = createTheoryOAEI(ontology, true, true);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search30 = createUniformCostSearch(th30, true);
        th30.addBackgroundFormulas(bx);
        OWLAxiomCostsEstimator es30 = new OWLAxiomCostsEstimator(th30, Utils.readRdfMapping(mapd, n + ".rdf"));
        search30.setCostsEstimator(es30);

        try {
            search30.run(30);
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Set<AxiomSet<OWLLogicalAxiom>> diagnoses = search30.getDiagnoses();
        int rnd = random.nextInt(diagnoses.size());
        randomDiagNr = rnd;
        logger.info(file.getName() + ",diagnosis selected as target," + rnd);
        targetDg = new LinkedHashSet<OWLLogicalAxiom>((AxiomSet<OWLLogicalAxiom>) diagnoses.toArray()[rnd]);
        logger.info(file.getName() + ",target diagnosis axioms," + Utils.renderAxioms(targetDg));

        search30.reset();
        return targetDg;
    }

    int randomDiagNr = -1;

    Random random = new Random(12311);

    @Test
    public void doTestsOAEIConference()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();

        String matchingsDir = "oaei11conference/matchings/";
        String directory = "incoherent";
        //String mapd = matchingsDir + directory;
        File incl = new File(ClassLoader.getSystemResource(matchingsDir + "includedIncoher.txt").getFile());
        MyFilenameFilter filter = new MyFilenameFilter(incl);
        File[] f = new File(ClassLoader.getSystemResource(matchingsDir + directory)
                .getFile()).listFiles(filter);
        String directory2 = "inconsistent";
        File incl2 = new File(ClassLoader.getSystemResource(matchingsDir + "included.txt").getFile());
        MyFilenameFilter filter2 = new MyFilenameFilter(incl2);
        File[] f2 = new File(ClassLoader.getSystemResource(matchingsDir + directory2)
                .getFile()).listFiles(filter2);
        Set<File> files = new LinkedHashSet<File>();
        Map<File, String> map = new HashMap<File, String>();
        for (File file : f) {
            files.add(file);
            map.put(file, "incoherent");
        }
        for (File file : f2) {
            files.add(file);
            map.put(file, "inconsistent");
        }

        session.showElRates = false;

        SimulatedSession.QSSType[] qssTypes = new SimulatedSession.QSSType[]
                {SimulatedSession.QSSType.MINSCORE, SimulatedSession.QSSType.SPLITINHALF};

        for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_30_DIAGS}) {

            for (File file : files) {
                logger.info("processing " + file.getName());

                String out = "STAT, " + file;


                Set<OWLLogicalAxiom> targetDg = getRandomDiag(file, map.get(file));

                for (QSSType type : qssTypes) {


                    String fileName = file.getName();
                    StringTokenizer t = new StringTokenizer(fileName, "-");
                    String matcher = t.nextToken();

                    String o1 = t.nextToken();
                    String o2 = t.nextToken();
                    o2 = o2.substring(0, o2.length() - 4);

                    String n = file.getName().substring(0, file.getName().length() - 4);
                    OWLOntology merged = Utils.createOntologyWithRdfMappings("oaei11conference/ontology",
                            o1, o2, matchingsDir + map.get(file), n + ".rdf");

                            long preprocessModulExtract = System.currentTimeMillis();
                            OWLOntology ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(merged);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                             OWLTheory theory = createTheoryOAEI(ontology, true, true);
                    TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, true);

                    LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                    OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology", o1 + ".owl");
                    OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology", o2 + ".owl");
                    bx.addAll(Utils.getIntersection(ontology.getLogicalAxioms(), ontology1.getLogicalAxioms()));
                    bx.addAll(Utils.getIntersection(ontology.getLogicalAxioms(), ontology2.getLogicalAxioms()));
                    theory.addBackgroundFormulas(bx);

                    Map<OWLLogicalAxiom, BigDecimal> map1 = Utils.readRdfMapping(matchingsDir + map.get(file), n + ".rdf");

                    OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);


                    search.setCostsEstimator(es);

                    search.reset();


                    TableList e = new TableList();
                    out += "," + type + ",";
                    String message = "act," + file.getName() + "," + map.get(file) + "," + targetSource
                            + "," + type + "," + preprocessModulExtract + "," + randomDiagNr;
                    out += session.simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                }
                logger.info(out);


            }
        }
    }


    @Test
    public void doSimpleQuerySession()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();

        session.traceDiagnosesAndQueries = true;
        session.minimizeQuery = true;

        session.NUMBER_OF_HITTING_SETS = 2;
        QSSType type = QSSType.MINSCORE;
        boolean dual = true;
        String name = "dualISWC2012.owl";
        //String name = "dualpaper.owl";

        OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies", name);

        Set<OWLLogicalAxiom> targetDg = new LinkedHashSet<OWLLogicalAxiom>();
        targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf D and (not (s some C))"));
        targetDg.add(new MyOWLRendererParser(ontology).parse("C SubClassOf not (D or E)"));

        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf C"));
        //targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf D"));

        long preprocessModulExtract = System.currentTimeMillis();
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;

        OWLTheory theory = createTheoryOAEI(ontology, dual, true);
        theory.addEntailedTest(new MyOWLRendererParser(ontology).parse("w Type B"));
        theory.setIncludeClassAssertionAxioms(true);
        theory.setIncludeTrivialEntailments(false);
        theory.setIncludeSubClassOfAxioms(false);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);
        ((NewQuickXplain<OWLLogicalAxiom>)search.getSearcher()).setAxiomRenderer(new MyOWLRendererParser(null));

        CostsEstimator es = new SimpleCostsEstimator();
        search.setCostsEstimator(es);

        TableList e = new TableList();
        String message = "act," + type + "," + dual + "," + name + "," + preprocessModulExtract;
        session.simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

    }


    @Test
    public void doTestAroma()

            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();

        Properties properties = Utils.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = Utils.readOntologiesFromFile(properties);
        //boolean background_add = false;
        session.showElRates = false;

        String[] files =
                new String[]{"Aroma"};
        //String[] files = new String[]{"Aroma"};

        QSSType[] qssTypes = new QSSType[]
                {QSSType.MINSCORE, QSSType.SPLITINHALF,
                        QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{true}) {
            for (boolean background : new boolean[]{true}) {
                for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_FILE}) {
                    for (String file : files) {

                        String out = "STAT, " + file;
                        for (QSSType type : qssTypes) {

                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                            //String[] targetAxioms = AlignmentUtils.getDiagnosis(m,o);
                            //OWLOntology ontology = createOwlOntology(m.trim(), o.trim());

                            OWLOntology ontology = createOntologyFromTxtOAEI(file);

                            Set<OWLLogicalAxiom> targetDg;
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            OWLTheory theory = createTheoryOAEI(ontology, dual, true);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            bx.addAll(getLogicalAxiomsOfOntologiesOAEI());
                            bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                            theory.addBackgroundFormulas(bx);

                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();

                            String path = ClassLoader.getSystemResource("oaei11/" + file + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();


                            if (targetSource == TargetSource.FROM_FILE)
                                targetDg = getTargetDOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/",
                                        file);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + file + "," + targetSource + "," + type + "," + dual + "," + background + "," + preprocessModulExtract;
                            //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                        }
                        logger.info(out);

                    }
                }
            }
        }
    }

    @Test
    public void doTestsOAEIAnatomyTrack()

            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        SimulatedSession session = new SimulatedSession();

        Properties properties = Utils.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = Utils.readOntologiesFromFile(properties);
        //boolean background_add = false;
        session.showElRates = false;

        //String[] files =
                  //new String[]{"AgrMaker", "GOMMA-bk", "GOMMA-nobk", "Lily", "LogMap", "LogMapLt", "MapSSS"};
        String[] files = new String[]{"AgrMaker"};

        //QSSType[] qssTypes = new QSSType[]{DYNAMICRISK};
        QSSType[] qssTypes = new QSSType[]
                { QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK };
        for (boolean dual : new boolean[] {false}) {
            for (boolean background : new boolean[]{false}) {
                for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_FILE}) {
                    for (String file : files) {

                        String out = "STAT, " + file;
                        for (QSSType type : qssTypes) {

                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                            //String[] targetAxioms = AlignmentUtils.getDiagnosis(m,o);
                            //OWLOntology ontology = createOwlOntology(m.trim(), o.trim());

                            OWLOntology ontology = createOntologyFromTxtOAEI(file);

                                Set<OWLLogicalAxiom> targetDg;
                                long preprocessModulExtract = System.currentTimeMillis();
                                ontology = new OWLIncoherencyExtractor(
                                        new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                                preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                                OWLTheory theory = createTheoryOAEI(ontology, dual, true);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            bx.addAll(getLogicalAxiomsOfOntologiesOAEI());
                            bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                            if (background) theory.addBackgroundFormulas(bx);

                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();

                            String path = ClassLoader.getSystemResource("oaei11/" + file + ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();


                            if (targetSource == TargetSource.FROM_FILE)
                                targetDg = getTargetDOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/",
                                        file);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + file + "," + targetSource + "," + type + "," + dual + "," + background + "," + preprocessModulExtract;
                            //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                        }
                        logger.info(out);

                    }
                }
            }
        }
    }

    @Test
    public void doSearchNoDiagFound() throws IOException, SolverException, InconsistentTheoryException, NoConflictException {

        SimulatedSession session = new SimulatedSession();

        session.showElRates = false;
        QSSType[] qssTypes =
                new QSSType[]{QSSType.MINSCORE};
        String[] norm = new String[]{"Transportation-SDA"};


        for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_30_DIAGS}) {
            for (String o : norm) {
                String out = "STAT, " + o;
                TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses = getAllD(o);
                for (QSSType type : qssTypes) {
                    for (DiagProbab diagProbab : new DiagProbab[]{DiagProbab.GOOD}) {
                        for (int i = 0; i < 1500; i++) {


                            OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies", o + ".owl");
                            //OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = createOWLTheory(ontology, true);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, true);

                            HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();
                            OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
                            es.updateKeywordProb(map);
                            search.setCostsEstimator(es);

                            targetDg = null;

                            search.setCostsEstimator(es);

                            search.reset();

                            //diags = getDualTreeTranspErrDiag();
                            targetDg = chooseTargetDiagnosis(diagProbab, diagnoses);


                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + "," + o.trim() + "," + targetSource + "," +
                                    "," + type + "," + preprocessModulExtract + "," + diagProbab + "," + i;
                            logger.info("target diagnosis:" + targetDg.size() + " " + Utils.renderAxioms(targetDg));
                            //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                            //logger.info(out);
                        }
                    }
                }
            }
        }
    }

    protected void doOverallTreeTestEconomy(boolean dual) throws IOException, SolverException, InconsistentTheoryException, NoConflictException {

        SimulatedSession session = new SimulatedSession();

        session.showElRates = false;
        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        //String[] norm = new String[]{"Transportation-SDA"};
        String[] norm = new String[]{"Transportation-SDA", "Economy-SDA"};


        for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_30_DIAGS}) {
            for (String o : norm) {
                String out = "STAT, " + o;
                TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses = getAllD(o);
                for (QSSType type : qssTypes) {
                    for (DiagProbab diagProbab : DiagProbab.values()) {
                        for (int i = 0; i < 20; i++) {


                        OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies",o+".owl");
                        //OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                        long preprocessModulExtract = System.currentTimeMillis();
                        ontology = new OWLIncoherencyExtractor(
                                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                        Set<OWLLogicalAxiom> targetDg;
                        OWLTheory theory = createOWLTheory(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

                            HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();
                            OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
                            es.updateKeywordProb(map);
                            search.setCostsEstimator(es);

                            targetDg = null;

                            search.setCostsEstimator(es);

                            search.reset();

                            targetDg = chooseTargetDiagnosis(diagProbab, diagnoses);


                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + "," + o.trim() + "," + targetSource + "," + dual
                                    + "," + type + "," + preprocessModulExtract + "," + diagProbab + "," + i;
                            //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                            //logger.info(out);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void doOverallDualTreeTestEconomy()
            throws SolverException, InconsistentTheoryException, IOException, NoConflictException {
        doOverallTreeTestEconomy(true);
    }

    @Test
    public void doOverallHsTreeTestEconomy()
            throws SolverException, InconsistentTheoryException, IOException, NoConflictException {
        doOverallTreeTestEconomy(false);
    }

    @Test
    public void hookTest() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("User aborted shutdown hook");
            }
        });

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        while (System.in.available() > 0) {
                            char pressedKey = (char) System.in.read();
                            switch (pressedKey) {
                                case 'c':
                                    System.out.println("statistics");
                                    System.exit(0);
                                case 'i':
                                    System.out.println("statistics");
                                default:
                                    System.out.println("press <i> for statistics or <c> to cancel");
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }.start();

        while (true) ;
    }

    private TreeSet<AxiomSet<OWLLogicalAxiom>> getAllD(String o) {
        OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies", o + ".owl");
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        OWLTheory theory = createOWLTheory(ontology, false);
        TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
        HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();
        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
        es.updateKeywordProb(map);
        search.setCostsEstimator(es);
        try {
            search.run();
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
    }

    private Set<OWLLogicalAxiom> getDualTreeTranspErrDiag() {
        Set<OWLLogicalAxiom> result = new LinkedHashSet<OWLLogicalAxiom>();
        Set<String> target = new LinkedHashSet<String>();
        target.add("ContainerAndRoRoCargoShip SubClassOf PartialContainerShip");
        target.add("InternationalAirport SubClassOf CommercialAirport");
        target.add("LightTruck SubClassOf Automobile");
        target.add("ShipCabin SubClassOf HumanHabitationArtifact");
        target.add("AirTransitway DisjointWith TransitRoute");
        target.add("CargoShip DisjointWith PassengerShip");
        OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies", "Transportation-SDA.owl");
        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
            if (target.contains(new ManchesterOWLSyntaxOWLObjectRendererImpl().render(axiom)))
                result.add(axiom);
        }
        return result;
    }

    private AxiomSet<OWLLogicalAxiom> chooseTargetDiagnosis
            (DiagProbab
                     diagProbab, TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses) {


        BigDecimal sum = new BigDecimal("0");
        TreeSet<AxiomSet<OWLLogicalAxiom>> res;
        TreeSet<AxiomSet<OWLLogicalAxiom>> good = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSet<AxiomSet<OWLLogicalAxiom>> avg = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSet<AxiomSet<OWLLogicalAxiom>> bad = new TreeSet<AxiomSet<OWLLogicalAxiom>>();

        for (AxiomSet<OWLLogicalAxiom> hs : diagnoses.descendingSet()) {
            if (sum.compareTo(BigDecimal.valueOf(0.33)) <= 0) {
                good.add(hs);
            } else if (sum.compareTo(BigDecimal.valueOf(0.33)) >= 0 && sum.compareTo(BigDecimal.valueOf(0.66)) <= 0) {
                avg.add(hs);
            } else if (sum.compareTo(BigDecimal.valueOf(0.66)) >= 0) {
                bad.add(hs);
            }
            sum = sum.add(hs.getMeasure());
        }
        switch (diagProbab) {
            case GOOD:
                while (good.size() < 3) {
                    if (!avg.isEmpty()) {
                        good.add(avg.pollLast());
                    } else if (!bad.isEmpty())
                        good.add(bad.pollLast());
                    else
                        break;
                }
                res = good;
                break;
            case AVERAGE:
                if (avg.size() < 3 && !good.isEmpty())
                    avg.add(good.pollFirst());
                while (avg.size() < 3) {
                    if (!bad.isEmpty())
                        avg.add(bad.pollLast());
                    else break;
                }
                res = avg;
                break;
            default: {
                if (bad.size() < 3)
                    logger.error("No diagnoses in bad! " + diagnoses);
                while (bad.size() < 3) {
                    if (!avg.isEmpty()) {
                        bad.add(avg.pollFirst());
                    } else if (!good.isEmpty())
                        bad.add(good.pollFirst());
                    else
                        break;
                }
                res = bad;
            }
        }

        int number = rnd.nextInt(res.size());


        int i = 1;
        AxiomSet<OWLLogicalAxiom> next = null;
        for (Iterator<AxiomSet<OWLLogicalAxiom>> it = res.descendingIterator(); it.hasNext(); i++) {
            next = it.next();
            if (i == number)
                break;
        }
        logger.info(diagProbab + ": selected target diagnosis " + next + " positioned " + number + " out of " + res.size());
        return next;
    }

    @Test
    public void docomparehsdual() throws SolverException, InconsistentTheoryException, IOException {
        SimulatedSession session = new SimulatedSession();

        Properties properties = Utils.readProps("alignment.allFiles.properties");
        Map<String, List<String>> mapOntos = Utils.readOntologiesFromFile(properties);
        //boolean background_add = false;

        session.showElRates = false;

        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (boolean background : new boolean[]{true, false}) {
                for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_FILE, TargetSource.FROM_30_DIAGS}) {
                    for (String m : mapOntos.keySet()) {
                        for (String o : mapOntos.get(m)) {
                            String out = "STAT, " + m + ", " + o;
                            for (QSSType type : qssTypes) {

                                //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                                String[] targetAxioms = Utils.getDiagnosis(m, o);
                                OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                                ontology = new OWLIncoherencyExtractor(
                                      new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
                                Set<OWLLogicalAxiom> targetDg;
                                OWLTheory theory = createOWLTheory(ontology, dual);
                                TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);
                                if (background) {
                                    OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                                    OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
                                    theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                    theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                                }
                                //ProbabilityTableModel mo = new ProbabilityTableModel();
                                HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();

                                String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                        + m.trim()
                                        + "-incoherent-evaluation/"
                                        + o.trim()
                                        + ".txt").getPath();

                                OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);

                                //                        double[] p = new ModerateDistribution().getProbabilities(ontology.getLogicalAxioms().size());
                                //                        Map<OWLLogicalAxiom,Double> axmap = new LinkedHashMap<OWLLogicalAxiom, Double>();
                                //                        int i = 0;
                                //                        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                                //                            axmap.put(axiom,p[i]);
                                //                            i++;
                                //                        }
                                //                        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, axmap);


                                //es.updateKeywordProb(map);
                                targetDg = null;

                                search.setCostsEstimator(es);
                                //
                                //                            try {
                                //                                search.run();
                                //                            } catch (SolverException e) {
                                //                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                //                            } catch (NoConflictException e) {
                                //                                logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                //                            } catch (InconsistentTheoryException e) {
                                //                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                //                            }

                                Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                                search.reset();

                                if (targetSource == TargetSource.FROM_30_DIAGS) {
                                    try {
                                        search.run(30);
                                    } catch (SolverException e) {
                                        logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    } catch (NoConflictException e) {
                                        logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    } catch (InconsistentTheoryException e) {
                                        logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }

                                    Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                            Collections.unmodifiableSet(search.getDiagnoses());
                                    search.reset();
                                    AxiomSet<OWLLogicalAxiom> targD = getTargetDiag(diagnoses, es, m);
                                    targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                    for (OWLLogicalAxiom axiom : targD)
                                        targetDg.add(axiom);
                                }

                                if (targetSource == TargetSource.FROM_FILE)
                                    targetDg = getDiagnosis(targetAxioms, ontology);

                                TableList e = new TableList();
                                out += "," + type + ",";
                                String message = "act," + m.trim() + "," + o.trim() + "," + targetSource + "," + type + "," + dual + "," + background;
                                //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                                out += session.simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                            }
                            logger.info(out);
                        }
                    }
                }
            }
        }
    }

    private TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch(OWLOntology ontology, boolean dual) throws SolverException, InconsistentTheoryException {

        OWLOntology extracted = new OWLIncoherencyExtractor(
                new Reasoner.ReasonerFactory()).getIncoherentPartAsOntology(ontology);
        OWLTheory theory = createTheoryOAEI(extracted, dual, true);
        TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);

        search.setCostsEstimator(es);
        search.reset();

        return search;
    }

    @Test
    public void testNormalCasesDual() throws SolverException, InconsistentTheoryException, IOException {

        SimulatedSession session = new SimulatedSession();

        session.showElRates = false;
        int MAX_RUNS = 7+1;
        rnd = new Random(121);

        for (String name : new String[]{"Economy-SDA.owl"}) {
            for (boolean dual : new boolean[] {true}) {

                TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search = getSearch(CreationUtils.createOwlOntology("queryontologies",name),false);



                try {
                    search.run();
                } catch (NoConflictException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                Set<AxiomSet<OWLLogicalAxiom>> diagnoses = Collections.unmodifiableSet(search.getDiagnoses());
                ModerateDistribution moderateDistribution = new ModerateDistribution();
                ExtremeDistribution extremeDistribution = new ExtremeDistribution();
                search.reset();

                String out = "";

                for (UsersProbab usersProbab : new UsersProbab[]{UsersProbab.MODERATE}) {
                    for (DiagProbab diagProbab : new DiagProbab[]{DiagProbab.BAD}) {
                        for (int run = 7; run < MAX_RUNS; run++) {

                            TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search2 = getSearch(CreationUtils.createOwlOntology("queryontologies",name),dual);
                            diagnoses = chooseUserProbab(usersProbab, search2, diagnoses,extremeDistribution,moderateDistribution);
                            AxiomSet<OWLLogicalAxiom> targetDiag = chooseTargetDiagnosis(diagProbab, new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses));





                            TableList e = new TableList();
                            String message = "act," + name + "," + dual + "," + usersProbab + ","
                                    + diagProbab + "," + run  ;

                            out += session.simulateBruteForceOnl(search2, (OWLTheory)search2.getTheory(), targetDiag, e, QSSType.MINSCORE, message, null, null, null);

                        }
                    }
                }
            }
        }





    }

    private Set<AxiomSet<OWLLogicalAxiom>> sortDiagnoses(Set<AxiomSet<OWLLogicalAxiom>> axiomSets) {
        TreeSet<AxiomSet<OWLLogicalAxiom>> phs = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        for (AxiomSet<OWLLogicalAxiom> hs : axiomSets)
            phs.add(hs);
        return Collections.unmodifiableSet(phs);
    }

    private void shuffleKeyword(ArrayList<ManchesterOWLSyntax> keywordList) {
        ArrayList<ManchesterOWLSyntax> cp = new ArrayList<ManchesterOWLSyntax>(keywordList.size());
        cp.addAll(keywordList);
        keywordList.clear();
        for (int i = 0; cp.size() > 0; i++) {
            int j = rnd.nextInt(cp.size());
            keywordList.add(i, cp.remove(j));
        }
        keywordList.addAll(cp);
    }

    private Set<AxiomSet<OWLLogicalAxiom>> chooseUserProbab
            (UsersProbab
                     usersProbab, TreeSearch<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, Set<AxiomSet<OWLLogicalAxiom>> diagnoses, ExtremeDistribution extremeDistribution, ModerateDistribution moderateDistribution) {
        Map<ManchesterOWLSyntax, BigDecimal> keywordProbs = new HashMap<ManchesterOWLSyntax, BigDecimal>();
        //ProbabilityTableModel m = new ProbabilityTableModel();
        ArrayList<ManchesterOWLSyntax> keywordList = new ArrayList<ManchesterOWLSyntax>(EnumSet.copyOf(Utils.getProbabMap().keySet()));
        ManchesterOWLSyntax[] selectedKeywords = new ManchesterOWLSyntax[]{ManchesterOWLSyntax.SOME, ManchesterOWLSyntax.ONLY,
                ManchesterOWLSyntax.DISJOINT_CLASSES, ManchesterOWLSyntax.DISJOINT_WITH, ManchesterOWLSyntax.SUBCLASS_OF,
                ManchesterOWLSyntax.EQUIVALENT_CLASSES, ManchesterOWLSyntax.NOT, ManchesterOWLSyntax.AND};

        /*
        keywordList = new ArrayList<ManchesterOWLSyntax>(Arrays.asList(selectedKeywords));
        shuffleKeyword(keywordList);
        */
        //Set<Integer> highKeywordPos = new HashSet<Integer>();

        List<ManchesterOWLSyntax> c = new ArrayList<ManchesterOWLSyntax>(Arrays.asList(selectedKeywords));
        for (int i = 0; i < c.size() / 2; i++) {
            int j = rnd.nextInt(c.size());
            keywordList.remove(c.get(j));
        }
        c.removeAll(keywordList);
        shuffleKeyword(keywordList);
        for (int i = 0; c.size() > 0; i++) {
            int j = rnd.nextInt(c.size());
            keywordList.add(i, c.remove(j));
        }
        //keywordList.addAll(c);

        int n = keywordList.size();
        int k = n / 4;
        double[] probabilities;

        switch (usersProbab) {
            case EXTREME:
                probabilities = extremeDistribution.getProbabilities(n);
                for (ManchesterOWLSyntax keyword : keywordList) {
                    keywordProbs.put(keyword, BigDecimal.valueOf(probabilities[keywordList.indexOf(keyword)]));
                }
                /*highKeywordPos.add(rnd.nextInt(n));
                for(ManchesterOWLSyntax keyword : keywordList) {
                    if (highKeywordPos.contains(keywordList.indexOf(keyword)))
                        keywordProbs.put(keyword,getProbabBetween(LOWER_BOUND_EXTREME_HIGH,HIGHER_BOUND_EXTREME_HIGH));
                    else
                        keywordProbs.put(keyword, getProbabBetween(LOWER_BOUND_EXTREME_LOW,HIGHER_BOUND_EXTREME_LOW));
                }*/
                break;
            case MODERATE:
                probabilities = moderateDistribution.getProbabilities(n);
                for (ManchesterOWLSyntax keyword : keywordList) {
                    keywordProbs.put(keyword, BigDecimal.valueOf(probabilities[keywordList.indexOf(keyword)]));
                }
                /*for (int i = 0; i < k; i++) {
                    int num = rnd.nextInt(n);
                    while (!highKeywordPos.add(num))
                        num = rnd.nextInt();
                }
                for(ManchesterOWLSyntax keyword : keywordList) {
                    if (highKeywordPos.contains(keywordList.indexOf(keyword)))
                        keywordProbs.put(keyword,getProbabBetween(LOWER_BOUND_MODERATE_HIGH,HIGHER_BOUND_MODERATE_HIGH));
                    else
                        keywordProbs.put(keyword, getProbabBetween(LOWER_BOUND_MODERATE_LOW,HIGHER_BOUND_MODERATE_LOW));
                }*/
                break;
            case UNIFORM:
                for (ManchesterOWLSyntax keyword : keywordList) {
                    keywordProbs.put(keyword, BigDecimal.valueOf(1.0 / n));
                }
                break;
        }
        ((OWLAxiomKeywordCostsEstimator)search.getCostsEstimator()).setKeywordProbabilities(keywordProbs, diagnoses);
        return sortDiagnoses(diagnoses);


    }

    @Test
    public void doShowMappingAxiomsSizes() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = Utils.readProps("alignment.allFiles.properties");
        Map<String, List<String>> mapOntos = Utils.readOntologiesFromFile(properties);

        for (String m : mapOntos.keySet()) {
            for (String o : mapOntos.get(m)) {
                Utils.getDiagnosis(m, o);
            }
        }
    }

    @Test
    public void doUnsolvableTest() throws SolverException, InconsistentTheoryException, IOException {

        SimulatedSession session = new SimulatedSession();

        Properties properties = Utils.readProps();
        Map<String, List<String>> mapOntos = Utils.readOntologiesFromFile(properties);
        boolean background_add = false;
        session.showElRates = true;
        QSSType[] qssTypes = new QSSType[]{QSSType.MINSCORE, QSSType.SPLITINHALF, QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[]{false}) {
            for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_FILE}) {
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        String out = "STAT, " + m + ", " + o;
                        for (QSSType type : qssTypes) {
                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            String[] targetAxioms = Utils.getDiagnosis(m, o);
                            OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = createOWLTheory(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            if (background_add) {
                                OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                                OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
                                theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                            }
                            HashMap<ManchesterOWLSyntax, BigDecimal> map = Utils.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLTheory theory2 = createOWLTheory(ontology, dual);
                            OWLTheory t3 = createOWLTheory(ontology, dual);
                            TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search2 = createUniformCostSearch(theory2, dual);
                            search2.setCostsEstimator(new OWLAxiomCostsEstimator(theory2, path));


                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);

                            //                        double[] p = new ModerateDistribution().getProbabilities(ontology.getLogicalAxioms().size());
                            //                        Map<OWLLogicalAxiom,Double> axmap = new LinkedHashMap<OWLLogicalAxiom, Double>();
                            //                        int i = 0;
                            //                        for (OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
                            //                            axmap.put(axiom,p[i]);

                            //                            i++;
                            //                        }
                            //                        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, axmap);


                            //es.updateKeywordProb(map);
                            targetDg = null;

                            search.setCostsEstimator(es);
                            //
//                            try {
//                                search.run();
//                            } catch (SolverException e) {
//                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                            } catch (NoConflictException e) {
//                                logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                            } catch (InconsistentTheoryException e) {
//                                logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                            }

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());
                            search.reset();

                            if (targetSource == TargetSource.FROM_30_DIAGS) {
                                try {
                                    search.run(30);
                                } catch (SolverException e) {
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (NoConflictException e) {
                                    logger.error(e);//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                } catch (InconsistentTheoryException e) {
                                    logger.error(e);//.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                                Set<AxiomSet<OWLLogicalAxiom>> diagnoses =
                                        Collections.unmodifiableSet(search.getDiagnoses());
                                search.reset();
                                AxiomSet<OWLLogicalAxiom> targD = getTargetDiag(diagnoses, es, m);
                                targetDg = new LinkedHashSet<OWLLogicalAxiom>();
                                for (OWLLogicalAxiom axiom : targD)
                                    targetDg.add(axiom);
                            }

                            if (targetSource == TargetSource.FROM_FILE)
                                targetDg = getDiagnosis(targetAxioms, ontology);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act " + m + " - " + o + " - " + targetSource + " " + type + " d " + dual;
                            //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                            out += session.simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                        }
                        logger.info(out);
                    }
                }
            }
        }
    }

}
