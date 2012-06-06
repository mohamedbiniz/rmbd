package at.ainf.owlcontroller.queryeval;

import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.quickxplain.FastDiagnosis;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.tree.BreadthFirstSearch;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.DualTreeLogic;
import at.ainf.diagnosis.tree.UniformCostSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.DualTreeOWLTheory;
import at.ainf.owlapi3.model.OWLIncoherencyExtractor;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlcontroller.CreationUtils;
import at.ainf.owlcontroller.RDFUtils;
import at.ainf.owlcontroller.Utils;
import at.ainf.owlcontroller.costestimation.OWLAxiomCostsEstimator;
import at.ainf.owlcontroller.costestimation.OWLAxiomKeywordCostsEstimator;
import at.ainf.owlcontroller.distributiongenerators.DistributionGeneratorI;
import at.ainf.owlcontroller.distributiongenerators.ExtremeDistribution;
import at.ainf.owlcontroller.distributiongenerators.ModerateDistribution;
import at.ainf.owlcontroller.parser.MyOWLRendererParser;
import at.ainf.owlcontroller.queryeval.result.TableList;
import at.ainf.owlcontroller.queryeval.result.Time;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import at.ainf.theory.storage.*;
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
import java.util.*;

import static at.ainf.owlcontroller.queryeval.BasePerformanceTests.QSSType.MINSCORE;
import static at.ainf.owlcontroller.queryeval.BasePerformanceTests.QSSType.SPLITINHALF;
import static at.ainf.owlcontroller.queryeval.BasePerformanceTests.QSSType.DYNAMICRISK;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.03.12
 * Time: 08:26
 * To change this template use File | Settings | File Templates.
 */
public class UnsolvableTests extends BasePerformanceTests {

    private static Logger logger = Logger.getLogger(UnsolvableTests.class.getName());


    private boolean showElRates = true;

    private boolean traceDiagnosesAndQueries = false;

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("owlcontroller-log4j.properties").getFile();
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

    protected BreadthFirstSearch<OWLLogicalAxiom> createBreathFirstSearch(OWLTheory th, boolean dual) {

        SimpleStorage<OWLLogicalAxiom> storage;
        if (dual)
            storage = new DualStorage<OWLLogicalAxiom>();
        else
            storage = new SimpleStorage<OWLLogicalAxiom>();
        BreadthFirstSearch<OWLLogicalAxiom> search = new BreadthFirstSearch<OWLLogicalAxiom>(storage);
        if (dual) {
            search.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
            search.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());
        }
        else
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        search.setTheory(th);

        return search;
    }

    protected UniformCostSearch<OWLLogicalAxiom> createUniformCostSearch(OWLTheory th, boolean dual) {

        SimpleStorage<OWLLogicalAxiom> storage;
        if (dual)
            storage = new DualStorage<OWLLogicalAxiom>();
        else
            storage = new SimpleStorage<OWLLogicalAxiom>();
        UniformCostSearch<OWLLogicalAxiom> search = new UniformCostSearch<OWLLogicalAxiom>(storage);
        if (dual) {
            search.setSearcher(new FastDiagnosis<OWLLogicalAxiom>());
            search.setLogic(new DualTreeLogic<AxiomSet<OWLLogicalAxiom>, OWLLogicalAxiom>());
        }
        else
            search.setSearcher(new NewQuickXplain<OWLLogicalAxiom>());
        search.setTheory(th);

        return search;
    }

    public AxiomSet<OWLLogicalAxiom> getTargetDiag(Set<AxiomSet<OWLLogicalAxiom>> diagnoses, final CostsEstimator<OWLLogicalAxiom> e, String m) {
        Comparator<AxiomSet<OWLLogicalAxiom>> c = new Comparator<AxiomSet<OWLLogicalAxiom>>() {
            public int compare(AxiomSet<OWLLogicalAxiom> o1, AxiomSet<OWLLogicalAxiom> o2) {
                int numOfOntologyAxiomsO1 = 0;
                int numOfMatchingAxiomO1 = 0;
                for (OWLLogicalAxiom axiom : o1) {
                    if (e.getAxiomCosts(axiom) != 0.001)
                        numOfMatchingAxiomO1++;
                    else
                        numOfOntologyAxiomsO1++;
                }
                double percAxiomFromOntO1 = (double) numOfOntologyAxiomsO1;// / (numOfOntologyAxiomsO1 + numOfMatchingAxiomO1);

                int numOfOntologyAxiomsO2 = 0;
                int numOfMatchingAxiomO2 = 0;
                for (OWLLogicalAxiom axiom : o2) {
                    if (e.getAxiomCosts(axiom) != 0.001)
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

    public boolean generateQueryAnswer
            (UniformCostSearch<OWLLogicalAxiom> search,
             Partition<OWLLogicalAxiom> actualQuery, Set<OWLLogicalAxiom> t) throws NoDecisionPossibleException {
        boolean answer;
        ITheory<OWLLogicalAxiom> theory = search.getTheory();

        AxiomSet<OWLLogicalAxiom> target = AxiomSetFactory.createHittingSet(0.5, t, new LinkedHashSet<OWLLogicalAxiom>());
        if (theory.diagnosisEntails(target, actualQuery.partition)) {
            answer = true;
            assertTrue(!actualQuery.dnx.contains(target));
        } else if (!theory.diagnosisConsistent(target, actualQuery.partition)) {
            answer = false;
            assertTrue(!actualQuery.dx.contains(target));
        } else {
            throw new NoDecisionPossibleException();
        }

        return answer;

    }

    protected <E extends AxiomSet<OWLLogicalAxiom>> E containsItem(Collection<E> col, Set<OWLLogicalAxiom> item) {
        for (E o : col) {
            if (item.containsAll(o)) {
                if (logger.isTraceEnabled())
                    logger.trace("Target dianosis " + o + "is in the window");
                return o;
            }
        }
        return null;
    }

    class NoDecisionPossibleException extends Exception {

    }

    protected boolean isInWindow(Set<OWLLogicalAxiom> targetDiag, Set<AxiomSet<OWLLogicalAxiom>> diagnoses) {
        for (AxiomSet<OWLLogicalAxiom> ps : diagnoses)
            if (targetDiag.containsAll(ps)) {
                if (logger.isDebugEnabled())
                    logger.debug("Target diagnosis is in window " + ps.getName());
                return true;
            }
        return false;
    }

    private int getEliminationRate(ITheory<OWLLogicalAxiom> theory, Set<AxiomSet<OWLLogicalAxiom>> d,
                                   boolean a, Partition<OWLLogicalAxiom> partition)
            throws SolverException {
        int deleted = 0;
        for (AxiomSet<OWLLogicalAxiom> diagnosis : d) {
            if (a && !((OWLTheory)theory).diagnosisConsistentWithoutEntailedTc(diagnosis, partition.partition))
                deleted++;
            else if (!a && ((OWLTheory)theory).diagnosisEntailsWithoutEntailedTC(diagnosis, partition.partition))
                deleted++;
        }
        return deleted;

    }

    protected String simulateBruteForceOnl(UniformCostSearch<OWLLogicalAxiom> search,
                                           OWLTheory theory, Set<OWLLogicalAxiom> targetDiag,
                                           TableList entry, BasePerformanceTests.QSSType scoringFunc, String message, Set<AxiomSet<OWLLogicalAxiom>> allDiagnoses, UniformCostSearch<OWLLogicalAxiom> secondsearch, OWLTheory t3) {
        //DiagProvider diagProvider = new DiagProvider(search, false, 9);

        QSS<OWLLogicalAxiom> qss = createQSSWithDefaultParam(scoringFunc);
        //userBrk=false;

        Partition<OWLLogicalAxiom> actPa = null;

        Set<AxiomSet<OWLLogicalAxiom>> diagnoses = null;
        int num_of_queries = 0;

        boolean userBreak = false;
        boolean systemBreak = false;

        boolean possibleError = false;

        boolean querySessionEnd = false;
        long time = System.currentTimeMillis();
        boolean hasQueryWithNoDecisionPossible = false;
        Time queryTime = new Time();
        Time diagTime = new Time();
        int queryCardinality = 0;
        long reactionTime = 0;
        Partitioning<OWLLogicalAxiom> queryGenerator = new CKK<OWLLogicalAxiom>(theory, qss);
        Set<AxiomSet<OWLLogicalAxiom>> remainingAllDiags = null;
        while (!querySessionEnd) {
            try {
                Collection<AxiomSet<OWLLogicalAxiom>> lastD = diagnoses;
                logger.trace("numOfQueries: " + num_of_queries + " search for diagnoses");

                userBreak = false;
                systemBreak = false;

                // marker
                long reactQuery = System.currentTimeMillis();

                if (actPa != null && actPa.dx.size() == 1 && actPa.dz.size() == 1 && actPa.dnx.isEmpty()) {
                    logger.error("Help!");
                    printc(theory.getEntailedTests());
                    printc(theory.getNonentailedTests());
                    print(actPa.partition);
                    prinths(actPa.dx);
                    prinths(actPa.dz);
                }

                try {
                    long diag = System.currentTimeMillis();
                    //search.clearSearch();
                    search.run(NUMBER_OF_HITTING_SETS);

                    daStr += search.getStorage().getDiagnoses().size() + "/";
                    diagnosesCalc += search.getStorage().getDiagnoses().size();
                    conflictsCalc += search.getStorage().getConflicts().size();

                    diagnoses = search.getStorage().getDiagnoses();
                    diagTime.setTime(System.currentTimeMillis() - diag);
                } catch (SolverException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>();

                } catch (NoConflictException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());

                }

                if (diagnoses.isEmpty())
                    logger.error("No diagnoses found!");

                if (diagnoses.size()==0) {

                    possibleError = true;
                    break;
                }

                if (traceDiagnosesAndQueries) {
                    String diag1 = "";
                    for (Set<OWLLogicalAxiom> diagnosis : diagnoses)
                        diag1 += Utils.renderAxioms(diagnosis) + " ; ";
                    logger.info("diagnoses before query " + num_of_queries + ":" + diag1);
                }

                String infoCa = "";
                for (Set<OWLLogicalAxiom> diagnose : diagnoses)
                        infoCa += diagnose.size() + "/";
                logger.info("cardinality of diagnoses " + infoCa);
                logger.info("num of hitting sets " + search.getStorage().getHittingSets().size());

                // cast should be corrected
                Iterator<AxiomSet<OWLLogicalAxiom>> descendSet = (new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses)).descendingIterator();
                AxiomSet<OWLLogicalAxiom> d = descendSet.next();
                AxiomSet<OWLLogicalAxiom> d1 = (descendSet.hasNext()) ? descendSet.next() : null;

                boolean isTargetDiagFirst = d.equals(targetDiag);
                double dp = d.getMeasure();
                if (logger.isInfoEnabled()) {
                    AxiomSet<OWLLogicalAxiom> o = containsItem(diagnoses, targetDiag);
                    double diagProbabilities = 0;
                    for (AxiomSet<OWLLogicalAxiom> tempd : diagnoses)
                        diagProbabilities += tempd.getMeasure();
                    logger.trace("diagnoses: " + diagnoses.size() +
                            " (" + diagProbabilities + ") first diagnosis: " + d +
                            " is target: " + isTargetDiagFirst + " is in window: " +
                            ((o == null) ? false : o.toString()));
                }

                if (d1 != null) {// && scoringFunc != QSSType.SPLITINHALF) {
                    double d1p = d1.getMeasure();
                    double diff = 100 - (d1p * 100) / dp;
                    logger.trace("difference : " + (dp - d1p) + " - " + diff + " %");
                    if (userBrk && diff > SIGMA && isTargetDiagFirst && num_of_queries > 0) {
                        // user brake
                        querySessionEnd = true;
                        userBreak = true;
                        break;
                    }
                }

                if (diagnoses.equals(lastD) || diagnoses.size() < 2) {
                    // system brake
                    querySessionEnd = true;
                    systemBreak = true;
                    break;
                }
                Partition<OWLLogicalAxiom> last = actPa;

                logger.trace("numOfQueries: " + num_of_queries + " search for  query");

                long query = System.currentTimeMillis();
                //actPa = getBestQuery(search, diagnoses);

                actPa = queryGenerator.generatePartition(diagnoses);

                if (actPa == null || actPa.partition == null || (last != null && actPa.partition.equals(last.partition))) {
                    // system brake
                    querySessionEnd = true;
                    break;
                }
                queryCardinality = actPa.partition.size();

                long querytime = System.currentTimeMillis() - query;
                queryTime.setTime(querytime);
                reactionTime += System.currentTimeMillis() - reactQuery;
                num_of_queries++;

                logger.trace("numOfQueries: " + num_of_queries + " generate answer");
                boolean answer = true;
                boolean hasAn = false;
                while (!hasAn) {
                    try {
                        answer = generateQueryAnswer(search, actPa, targetDiag);
                        hasAn = true;
                    } catch (NoDecisionPossibleException e) {
                        hasQueryWithNoDecisionPossible = true;
                        actPa = queryGenerator.nextPartition(actPa);
                        if (actPa == null) {
                            logger.error("All partitions were tested and none provided an answer to the target diagnosis!");
                            break;
                        }
                    }
                }

                if (qss != null) qss.updateParameters(answer);

                if (traceDiagnosesAndQueries)
                    logger.info("query asked: " + Utils.renderAxioms(actPa.partition));

                // fine all dz diagnoses
                // TODO do we need this fine?
                for (AxiomSet<OWLLogicalAxiom> ph : actPa.dz) {
                    ph.setMeasure(0.5d * ph.getMeasure());
                }
                if(allDiagnoses!=null) {

                    try {
                        secondsearch.run();
                    } catch (NoConflictException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    remainingAllDiags = secondsearch.getStorage().getDiagnoses();
                    int eliminatedInLeading = getEliminationRate(search.getTheory(),diagnoses,answer,actPa);
                    int eliminatedInRemaining = getEliminationRate(secondsearch.getTheory(),remainingAllDiags,answer,actPa);
                    int eliminatedInRemainingSize = remainingAllDiags.size();
                    int eliminatedInfull = getEliminationRate(t3,allDiagnoses,answer,actPa);
                    // deleteDiag(search.getTheory(),remainingAllDiags,answer,actPa.partition);

                    AxiomSet<OWLLogicalAxiom> foundTarget;
                    foundTarget=null;
                    for (AxiomSet<OWLLogicalAxiom> axiom : allDiagnoses)
                        if (targetDiag.containsAll(axiom)) {
                            if(foundTarget!=null)
                                logger.info("");
                            foundTarget = axiom;
                        }
                    if (answer)
                        secondsearch.getTheory().addEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                    else
                        secondsearch.getTheory().addNonEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                    logger.info("elimination rates: in all diags ;" + eliminatedInfull + "/" + allDiagnoses.size() +
                            "; in all remaining diags ;" + eliminatedInRemaining + "/" + eliminatedInRemainingSize +
                            "; in leading ;" + eliminatedInLeading + "/" + diagnoses.size()+" "+foundTarget);
                }

                int eliminatedInLeading = getEliminationRate(search.getTheory(),diagnoses,answer,actPa);
                if(showElRates)
                    logger.info("elimination rates: in leading ;" + eliminatedInLeading + "/" + diagnoses.size() );

                if (answer) {
                    try {
                        search.getTheory().addEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                        if (actPa.dnx.isEmpty() && diagnoses.size() < NUMBER_OF_HITTING_SETS)
                            querySessionEnd = true;
                    } catch (InconsistentTheoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                } else {
                    try {
                        search.getTheory().addNonEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                        if (actPa.dx.isEmpty() && diagnoses.size() < NUMBER_OF_HITTING_SETS)
                            querySessionEnd = true;
                    } catch (InconsistentTheoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

            } catch (SolverException e) {
                querySessionEnd = true;
                logger.error(e);

            } catch (InconsistentTheoryException e) {
                querySessionEnd = true;
                logger.error(e);

            }
        }
        time = System.currentTimeMillis() - time;
        boolean targetDiagnosisIsInWind = false;
        boolean targetDiagnosisIsMostProbable = false;
        if (diagnoses != null) {
            //TreeSet<ProbabilisticHittingSet> diags = new TreeSet<ProbabilisticHittingSet>(diagnoses);
            targetDiagnosisIsInWind = isInWindow(targetDiag, diagnoses);
            if (diagnoses.size() >= 1 && targetDiag.
                    containsAll((new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses)).last())) {
                targetDiagnosisIsMostProbable = true;
                targetDiagnosisIsInWind = true;
            }
        }
        if(!targetDiagnosisIsInWind)              {
            possibleError = true;
            logger.error("target diagnosis is not in window!");
        }
        int diagWinSize = 0;
        if (diagnoses != null)
            diagWinSize = diagnoses.size();

        int consistencyCount = 0;
        if (num_of_queries != 0) consistencyCount = theory.getConsistencyCount() / num_of_queries;
        if (num_of_queries != 0) reactionTime = reactionTime / num_of_queries;

        long consistencyTime = 0;
        if (consistencyCount != 0) consistencyTime = theory.getConsistencyTime() / consistencyCount;

//        message += " , Iteration finished within " + time + " ms, required " + num_of_queries + " queries, most probable "
//                + targetDiagnosisIsMostProbable + ", is in window " + targetDiagnosisIsInWind + ", size of window  " + diagWinSize
//                + ", reaction " + reactionTime + ", user " + userBreak +
//                ", systemBrake " + systemBreak + ", nd " + hasQueryWithNoDecisionPossible +
//                ", consistency checks " + consistencyCount;
        message += "," + time + "," + num_of_queries + ","
                + targetDiagnosisIsMostProbable + "," + targetDiagnosisIsInWind + "," + diagWinSize
                + "," + reactionTime + "," + userBreak + "," + possibleError +
                "," + systemBreak + "," + hasQueryWithNoDecisionPossible +
                "," + consistencyCount + "," + consistencyTime;
        logger.info(message);
        if (possibleError) {
            logger.info("Possible an error occured: ");
            logger.info("target diagnosis: " + Utils.renderAxioms(targetDiag));
            if (diagnoses == null) {
                logger.info("diagnoses is null!");
            }
            else {
                logger.info("diagnoses in window: " + diagnoses.size());
                for (Set<OWLLogicalAxiom> diagnosis : diagnoses)
                    logger.info("diagnosis: " + Utils.renderAxioms(diagnosis));
            }
        }

        String msg = time + ", " + num_of_queries + ", " + targetDiagnosisIsMostProbable + ", " + targetDiagnosisIsInWind + ", " + diagWinSize
                + ", " + reactionTime + ", " + userBreak + "," + possibleError +
                ", " + systemBreak + ", " + hasQueryWithNoDecisionPossible +
                ", " + consistencyCount;
        entry.addEntr(num_of_queries, queryCardinality, targetDiagnosisIsInWind, targetDiagnosisIsMostProbable,
                diagWinSize, userBreak, systemBreak, time, queryTime, diagTime, reactionTime, consistencyCount);
        return msg;
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
        Properties properties = AlignmentUtils.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = AlignmentUtils.readOntologiesFromFile(properties);

        for (boolean dual : new boolean[] {true, false}) {
            
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        for (int nd : new int[]{1,5,9}) {
                            String out ="STAT, " + m +  ", " + o;
                        
                            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = createOWLTheory(ontology, dual);
                            UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

                            OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                            OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLTheory theory2 = createOWLTheory(ontology, dual);
                            OWLTheory t3 = createOWLTheory(ontology, dual);
                            UniformCostSearch<OWLLogicalAxiom> search2 = createUniformCostSearch(theory2, dual);
                            search2.setCostsEstimator(new OWLAxiomCostsEstimator(theory2,path));

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

                            int minDiagnosisC = minCard(search.getStorage().getDiagnoses());
                            double meanDiagnosisC = meanCard(search.getStorage().getDiagnoses());
                            int maxDiagnosisC = maxCard(search.getStorage().getDiagnoses());
                            int minConfC = minCard(search.getStorage().getConflicts());
                            double meanConfC = meanCard(search.getStorage().getConflicts());
                            int maxConfC = maxCard(search.getStorage().getConflicts());

                            int c = search.getStorage().getConflicts().size();
                            String s = nd + ", " + minDiagnosisC + ", " + meanDiagnosisC + ", " + maxDiagnosisC + ", " +
                                       c + ", " + minConfC + ", " + meanConfC + ", " + maxConfC;

                        logger.info("Stat, " + m.trim() + ", " + o.trim() + ", "
                               + s + ", "
                               + theory.getConsistencyCount() + ", " + dual + ", " + Utils.getStringTime(time/1000000));
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
        }
        catch (NoSuchElementException e ) {

        }
        
        return r;
    }

    public int maxCard(Set<AxiomSet<OWLLogicalAxiom>> s) {
        int r = -1;

        try {
            for (AxiomSet<OWLLogicalAxiom> set : s)
                if (set.size() > r)
                    r = set.size();
        }
        catch (NoSuchElementException e ) {

        }

        return r;
    }
    
    public static double meanCard(Set<AxiomSet<OWLLogicalAxiom>> s) {
        double sum = 0;
        int cnt = 0;
        
        for (AxiomSet<OWLLogicalAxiom> set : s) {
            sum+=set.size();
            cnt++;
        }

        if (cnt==0) return -1;
        return sum / cnt;
    }

    @Test
    public void testDaReadMethods() throws IOException, SolverException, InconsistentTheoryException {
        Properties properties = AlignmentUtils.readProps("alignment.properties");
        Map<String, List<String>> mapOntos = AlignmentUtils.readOntologiesFromFile(properties);

        for (String m : mapOntos.keySet()) {
          for (String o : mapOntos.get(m)) {
            String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
            String[] targetAxioms2 = AlignmentUtils.getDiagnosis(m,o);
            boolean eq = AlignmentUtils.compareDiagnoses(targetAxioms,targetAxioms2);
            if(!eq) {
                OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                Set<OWLLogicalAxiom> targetDg = getDiagnosis(targetAxioms, ontology);
                System.out.println(targetAxioms.toString());
                System.out.println(targetAxioms2.toString());
            }
            assertTrue(m + " " + o,eq);
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
                man.applyChange(new AddAxiom(merged,axiom));

            return merged;
        } catch (OWLOntologyCreationException e) {
            return null;
        }
    }

    public Set<OWLLogicalAxiom> getAxiomsInMappingOAEI(String path, String source) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        Map<OWLLogicalAxiom,Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
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
                        targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target,man));
                        targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source,man));
                    }
                    else if(sub.contains(">"))
                        targetDiag.add(createAxiomOAEI(sourceNamespace, source, targetNamespace, target,man));
                    else if(sub.contains("<"))
                        targetDiag.add(createAxiomOAEI(targetNamespace, target, sourceNamespace, source,man));
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

        Map<OWLLogicalAxiom,Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
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
            try{
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(includedNames)));
                String strLine;
                while ((strLine = br.readLine()) != null)   {
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

    private UniformCostSearch<OWLLogicalAxiom> getSearch(OWLOntology ontology, boolean dual) throws SolverException, InconsistentTheoryException {

        OWLOntology extracted = new OWLIncoherencyExtractor(
                new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();
        OWLTheory theory = createTheoryOAEI(ontology, dual, true);
        UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);

        search.setCostsEstimator(es);
        search.clearSearch();

        return search;
    }

    private Set<OWLLogicalAxiom> getRandomDiag(File file, String directory) throws SolverException, InconsistentTheoryException {
        String matchingsDir = "oaei11conference/matchings/";
        String mapd = matchingsDir + directory;

        String fileName = file.getName();
        StringTokenizer t = new StringTokenizer(fileName,"-");
        String matcher = t.nextToken();

        String o1 = t.nextToken();
        String o2 = t.nextToken();
        o2 = o2.substring(0,o2.length()-4);

        String n = file.getName().substring(0,file.getName().length()-4);
        OWLOntology merged = RDFUtils.createOntologyWithMappings("oaei11conference/ontology",
                o1, o2, mapd, n + ".rdf");

        OWLOntology ontology = new OWLIncoherencyExtractor(
                new Reasoner.ReasonerFactory(),merged).getIncoherentPartAsOntology();
        OWLTheory theory = createTheoryOAEI(ontology, true, true);
        UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, true);

        LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
        OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology",o1+".owl");
        OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology",o2+".owl");
        bx.addAll(CreationUtils.getIntersection(ontology.getLogicalAxioms(),ontology1.getLogicalAxioms()));
        bx.addAll(CreationUtils.getIntersection(ontology.getLogicalAxioms(),ontology2.getLogicalAxioms()));
        theory.addBackgroundFormulas(bx);

        Map<OWLLogicalAxiom,Double> map1 = RDFUtils.readRdfMapping(mapd,n + ".rdf");

        OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);



        search.setCostsEstimator(es);

        search.clearSearch();


        Set<OWLLogicalAxiom> targetDg=null;


            OWLTheory th30 = createTheoryOAEI(ontology, true, true);
            UniformCostSearch<OWLLogicalAxiom> search30 = createUniformCostSearch(th30, true);
            th30.addBackgroundFormulas(bx);
            OWLAxiomCostsEstimator es30 = new OWLAxiomCostsEstimator(th30, RDFUtils.readRdfMapping(mapd,n + ".rdf"));
            search30.setCostsEstimator(es30);

            try {
                search30.run(30);
            } catch (NoConflictException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            Set<AxiomSet<OWLLogicalAxiom>> diagnoses = search30.getStorage().getDiagnoses();
            int rnd = random.nextInt(diagnoses.size());
            randomDiagNr = rnd ;
            logger.info(file.getName() + ",diagnosis selected as target," + rnd);
            targetDg = new LinkedHashSet<OWLLogicalAxiom>((AxiomSet<OWLLogicalAxiom>)diagnoses.toArray()[rnd]);
            logger.info(file.getName()+",target diagnosis axioms," + Utils.renderAxioms(targetDg));

            search30.clearSearch();
        return targetDg;
    }

    int randomDiagNr = -1;

    Random random = new Random(12311) ;

    @Test
    public void doTestsOAEIConference()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {

        String matchingsDir = "oaei11conference/matchings/";
        String directory = "incoherent";
        //String mapd = matchingsDir + directory;
        File incl = new File(ClassLoader.getSystemResource(matchingsDir+"includedIncoher.txt").getFile());
        MyFilenameFilter filter = new MyFilenameFilter(incl);
        File[] f = new File(ClassLoader.getSystemResource(matchingsDir+directory)
                .getFile()).listFiles(filter);
        String directory2 = "inconsistent";
        File incl2 = new File(ClassLoader.getSystemResource(matchingsDir+"included.txt").getFile());
        MyFilenameFilter filter2 = new MyFilenameFilter(incl2);
        File[] f2 = new File(ClassLoader.getSystemResource(matchingsDir+directory2)
                .getFile()).listFiles(filter2);
        Set<File> files = new LinkedHashSet<File>();
        Map<File,String> map = new HashMap<File, String>();
        for(File file : f) {
            files.add(file);
            map.put(file,"incoherent");
        }
        for(File file : f2) {
            files.add(file);
            map.put(file,"inconsistent");
        }

        showElRates = false;

        BasePerformanceTests.QSSType[] qssTypes = new BasePerformanceTests.QSSType[]
                {MINSCORE, SPLITINHALF};

                for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_30_DIAGS}) {

                    for (File file : files) {
                        logger.info("processing " + file.getName());

                        String out ="STAT, " + file;


                        Set<OWLLogicalAxiom> targetDg=getRandomDiag(file,map.get(file));

                        for (BasePerformanceTests.QSSType type : qssTypes) {


                            String fileName = file.getName();
                            StringTokenizer t = new StringTokenizer(fileName,"-");
                            String matcher = t.nextToken();

                            String o1 = t.nextToken();
                            String o2 = t.nextToken();
                            o2 = o2.substring(0,o2.length()-4);

                            String n = file.getName().substring(0,file.getName().length()-4);
                            OWLOntology merged = RDFUtils.createOntologyWithMappings("oaei11conference/ontology",
                                    o1, o2, matchingsDir+map.get(file), n + ".rdf");

                            long preprocessModulExtract = System.currentTimeMillis();
                            OWLOntology ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory(),merged).getIncoherentPartAsOntology();
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                             OWLTheory theory = createTheoryOAEI(ontology, true, true);
                            UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, true);

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            OWLOntology ontology1 = CreationUtils.createOwlOntology("oaei11conference/ontology",o1+".owl");
                            OWLOntology ontology2 = CreationUtils.createOwlOntology("oaei11conference/ontology",o2+".owl");
                            bx.addAll(CreationUtils.getIntersection(ontology.getLogicalAxioms(),ontology1.getLogicalAxioms()));
                            bx.addAll(CreationUtils.getIntersection(ontology.getLogicalAxioms(),ontology2.getLogicalAxioms()));
                            theory.addBackgroundFormulas(bx);

                            Map<OWLLogicalAxiom,Double> map1 = RDFUtils.readRdfMapping(matchingsDir+map.get(file),n + ".rdf");

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, map1);



                            search.setCostsEstimator(es);

                            search.clearSearch();





                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + file.getName() + "," + map.get(file) + "," + targetSource
                                    + "," + type + "," + preprocessModulExtract + "," + randomDiagNr;
                            out += simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                        }
                          logger.info(out);


            }
        }
    }


    @Test
    public void doSimpleQuerySession()
        throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {
        traceDiagnosesAndQueries = true;

        NUMBER_OF_HITTING_SETS = 2;
        QSSType type = MINSCORE;
        boolean dual = false;
        String name = "dualISWC2012.owl";

        OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies",name);

        Set<OWLLogicalAxiom> targetDg = new LinkedHashSet<OWLLogicalAxiom>();
        targetDg.add(new MyOWLRendererParser(ontology).parse("B SubClassOf D and (not (s some C))"));
        targetDg.add(new MyOWLRendererParser(ontology).parse("C SubClassOf not (D or E)"));

        long preprocessModulExtract = System.currentTimeMillis();
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();
        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;

        OWLTheory theory = createTheoryOAEI(ontology, dual, true);
        UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
        search.setCostsEstimator(es);

        TableList e = new TableList();
        String message = "act," + type + "," + dual + "," + name + "," + preprocessModulExtract;
        simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

    }


    @Test
    public void doTestAroma()

            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {
        Properties properties = AlignmentUtils.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = AlignmentUtils.readOntologiesFromFile(properties);
        //boolean background_add = false;
        showElRates = false;

        String[] files =
                new String[]{"Aroma"};
        //String[] files = new String[]{"Aroma"};

        BasePerformanceTests.QSSType[] qssTypes = new BasePerformanceTests.QSSType[]
                {BasePerformanceTests.QSSType.MINSCORE, BasePerformanceTests.QSSType.SPLITINHALF,
                        BasePerformanceTests.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[] {true}) {
            for (boolean background : new boolean[]{true}) {
                for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_FILE}) {
                    for (String file : files) {

                        String out ="STAT, " + file;
                        for (BasePerformanceTests.QSSType type : qssTypes) {

                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                            //String[] targetAxioms = AlignmentUtils.getDiagnosis(m,o);
                            //OWLOntology ontology = createOwlOntology(m.trim(), o.trim());

                            OWLOntology ontology = createOntologyFromTxtOAEI(file);

                            Set<OWLLogicalAxiom> targetDg;
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            OWLTheory theory = createTheoryOAEI(ontology, dual, true);
                            UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

                            LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                            bx.addAll(getLogicalAxiomsOfOntologiesOAEI());
                            bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                            theory.addBackgroundFormulas(bx);

                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

                            String path = ClassLoader.getSystemResource("oaei11/" +file+ ".txt").getPath();

                            OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);


                            targetDg = null;

                            search.setCostsEstimator(es);

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());
                            search.clearSearch();



                            if (targetSource == TargetSource.FROM_FILE)
                                targetDg = getTargetDOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/",
                                        file);

                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + file + "," + targetSource + "," + type + "," + dual + "," + background + "," + preprocessModulExtract;
                            //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                            out += simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

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
        Properties properties = AlignmentUtils.readProps("alignment.unsolvable.properties");
        Map<String, List<String>> mapOntos = AlignmentUtils.readOntologiesFromFile(properties);
        //boolean background_add = false;
        showElRates = false;

        String[] files =
                  new String[]{"AgrMaker", "Aroma", "GOMMA-bk", "GOMMA-nobk", "Lily", "LogMap", "LogMapLt", "MapSSS"};
        //String[] files = new String[]{"Aroma"};

        //BasePerformanceTests.QSSType[] qssTypes = new BasePerformanceTests.QSSType[]{DYNAMICRISK};
        BasePerformanceTests.QSSType[] qssTypes = new BasePerformanceTests.QSSType[]
                {BasePerformanceTests.QSSType.MINSCORE,  SPLITINHALF };
        for (boolean dual : new boolean[] {true, false}) {
            for (boolean background : new boolean[]{true}) {
                for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_FILE}) {
                    for (String file : files) {

                            String out ="STAT, " + file;
                            for (BasePerformanceTests.QSSType type : qssTypes) {

                                //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                                //String[] targetAxioms = AlignmentUtils.getDiagnosis(m,o);
                                //OWLOntology ontology = createOwlOntology(m.trim(), o.trim());

                                OWLOntology ontology = createOntologyFromTxtOAEI(file);

                                Set<OWLLogicalAxiom> targetDg;
                                long preprocessModulExtract = System.currentTimeMillis();
                                ontology = new OWLIncoherencyExtractor(
                                        new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();
                                preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                                OWLTheory theory = createTheoryOAEI(ontology, dual, true);
                                UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

                                LinkedHashSet<OWLLogicalAxiom> bx = new LinkedHashSet<OWLLogicalAxiom>();
                                bx.addAll(getLogicalAxiomsOfOntologiesOAEI());
                                bx.retainAll(theory.getOriginalOntology().getLogicalAxioms());
                                if (background) theory.addBackgroundFormulas(bx);

                                //ProbabilityTableModel mo = new ProbabilityTableModel();
                                HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

                                String path = ClassLoader.getSystemResource("oaei11/" +file+ ".txt").getPath();

                                OWLAxiomCostsEstimator es = new OWLAxiomCostsEstimator(theory, path);


                                targetDg = null;

                                search.setCostsEstimator(es);

                                Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());
                                search.clearSearch();



                                if (targetSource == TargetSource.FROM_FILE)
                                    targetDg = getTargetDOAEI(ClassLoader.getSystemResource("oaei11").getPath() + "/",
                                            file);

                                TableList e = new TableList();
                                out += "," + type + ",";
                                String message = "act," + file + "," + targetSource + "," + type + "," + dual + "," + background + "," + preprocessModulExtract;
                                //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                                out += simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                            }
                            logger.info(out);

                    }
                }
            }
        }
    }

    @Test
    public void doSearchNoDiagFound() throws IOException, SolverException, InconsistentTheoryException, NoConflictException {

        showElRates = false;
        BasePerformanceTests.QSSType[] qssTypes =
                new BasePerformanceTests.QSSType[]{ BasePerformanceTests.QSSType.MINSCORE};
        String[] norm = new String[]{"Transportation-SDA"};


        for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_30_DIAGS}) {
            for (String o : norm) {
                String out ="STAT, " + o;
                TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses = getAllD(o);
                for (BasePerformanceTests.QSSType type : qssTypes) {
                    for (DiagProbab diagProbab : new DiagProbab[]{DiagProbab.GOOD}) {
                        for (int i = 0; i < 1500; i++) {


                            OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies",o+".owl");
                            //OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                            long preprocessModulExtract = System.currentTimeMillis();
                            ontology = new OWLIncoherencyExtractor(
                                    new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();
                            preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = createOWLTheory(ontology, true);
                            UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, true);

                            HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();
                            OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
                            es.updateKeywordProb(map);
                            search.setCostsEstimator(es);

                            targetDg = null;

                            search.setCostsEstimator(es);

                            search.clearSearch();

                            //diags = getDualTreeTranspErrDiag();
                            targetDg = chooseTargetDiagnosis(diagProbab,diagnoses);


                            TableList e = new TableList();
                            out += "," + type + ",";
                            String message = "act," + "," + o.trim() + "," + targetSource + "," +
                                    ","+type + ","+preprocessModulExtract+","+diagProbab+","+i;
                            logger.info("target diagnosis:" + targetDg.size() + " " + Utils.renderAxioms(targetDg));
                            //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                            out += simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                            //logger.info(out);
                        }
                    }
                }
            }
        }
    }

    protected void doOverallTreeTestEconomy(boolean dual) throws IOException, SolverException, InconsistentTheoryException, NoConflictException {

        showElRates = false;
        BasePerformanceTests.QSSType[] qssTypes = new BasePerformanceTests.QSSType[]{BasePerformanceTests.QSSType.MINSCORE, BasePerformanceTests.QSSType.SPLITINHALF, BasePerformanceTests.QSSType.DYNAMICRISK};
        //String[] norm = new String[]{"Transportation-SDA"};
        String[] norm = new String[]{"Transportation-SDA", "Economy-SDA"};


        for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_30_DIAGS}) {
                for (String o : norm) {
                    String out ="STAT, " + o;
                    TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses = getAllD(o);
                    for (BasePerformanceTests.QSSType type : qssTypes) {
                        for (DiagProbab diagProbab : DiagProbab.values()) {
                            for (int i = 0; i < 20; i++) {


                        OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies",o+".owl");
                        //OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                        long preprocessModulExtract = System.currentTimeMillis();
                        ontology = new OWLIncoherencyExtractor(
                                new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();
                        preprocessModulExtract = System.currentTimeMillis() - preprocessModulExtract;
                        Set<OWLLogicalAxiom> targetDg;
                        OWLTheory theory = createOWLTheory(ontology, dual);
                        UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);

                        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();
                        OWLAxiomKeywordCostsEstimator es = new OWLAxiomKeywordCostsEstimator(theory);
                        es.updateKeywordProb(map);
                        search.setCostsEstimator(es);

                        targetDg = null;

                        search.setCostsEstimator(es);

                        search.clearSearch();

                        targetDg = chooseTargetDiagnosis(diagProbab,diagnoses);


                        TableList e = new TableList();
                        out += "," + type + ",";
                        String message = "act," + "," + o.trim() + "," + targetSource + "," + dual
                                + "," + type + ","+preprocessModulExtract+","+diagProbab+","+i;
                        //out += simulateBruteForceOnl(search, theory, diags, e, type, message, allD, search2, t3);

                        out += simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

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
                while(true) {
                    try {
                        while (System.in.available()>0) {
                            char pressedKey = (char) System.in.read();
                            switch(pressedKey) {
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

        while(true);
    }

    private TreeSet<AxiomSet<OWLLogicalAxiom>> getAllD(String o) {
        OWLOntology ontology = CreationUtils.createOwlOntology("queryontologies", o+".owl");
        ontology = new OWLIncoherencyExtractor(new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();
        OWLTheory theory = createOWLTheory(ontology, false);
        UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, false);
        HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();
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
        return new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());
    }

    private AxiomSet<OWLLogicalAxiom> chooseTargetDiagnosis
            (DiagProbab
                     diagProbab, TreeSet<AxiomSet<OWLLogicalAxiom>> diagnoses) {


        double sum = 0;
        TreeSet<AxiomSet<OWLLogicalAxiom>> res;
        TreeSet<AxiomSet<OWLLogicalAxiom>> good = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSet<AxiomSet<OWLLogicalAxiom>> avg = new TreeSet<AxiomSet<OWLLogicalAxiom>>();
        TreeSet<AxiomSet<OWLLogicalAxiom>> bad = new TreeSet<AxiomSet<OWLLogicalAxiom>>();

        for (AxiomSet<OWLLogicalAxiom> hs : diagnoses.descendingSet()) {
            if (sum <= 0.33) {
                good.add(hs);
            } else if (sum >= 0.33 && sum <= 0.66) {
                avg.add(hs);
            } else if (sum >= 0.66) {
                bad.add(hs);
            }
            sum += hs.getMeasure();
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
        Properties properties = AlignmentUtils.readProps("alignment.allFiles.properties");
        Map<String, List<String>> mapOntos = AlignmentUtils.readOntologiesFromFile(properties);
        //boolean background_add = false;
        showElRates = false;

        Timer timer = new Timer();

        BasePerformanceTests.QSSType[] qssTypes = new BasePerformanceTests.QSSType[]{BasePerformanceTests.QSSType.MINSCORE, BasePerformanceTests.QSSType.SPLITINHALF, BasePerformanceTests.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[] {false}) {
            for (boolean background : new boolean[]{false}) {
                for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_FILE,TargetSource.FROM_30_DIAGS}) {
                    for (String m : mapOntos.keySet()) {
                        for (String o : mapOntos.get(m)) {
                            String out ="STAT, " + m +  ", " + o;
                            for (BasePerformanceTests.QSSType type : qssTypes) {

                                //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");

                                String[] targetAxioms = AlignmentUtils.getDiagnosis(m,o);
                                OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                                ontology = new OWLIncoherencyExtractor(
                                      new Reasoner.ReasonerFactory(),ontology).getIncoherentPartAsOntology();
                                Set<OWLLogicalAxiom> targetDg;
                                OWLTheory theory = createOWLTheory(ontology, dual);
                                UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);
                                if (background) {
                                    OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                                    OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
                                    theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                    theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                                }
                                //ProbabilityTableModel mo = new ProbabilityTableModel();
                                HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

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

                                Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());
                                search.clearSearch();

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
                                            Collections.unmodifiableSet(search.getStorage().getDiagnoses());
                                    search.clearSearch();
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

                                //TimeoutTask task = new TimeoutTask (4*3600*1000,m.trim(),o.trim() );
                                //task.setSearch(search);
                                //timer.scheduleAtFixedRate(task, 0, TimeoutTask.CYCLE_TIME);
                                out += simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);
                                //task.cancel();
                                //task.setSearch(null);

                            }
                            logger.info(out);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testNormalCasesDual() throws SolverException, InconsistentTheoryException, IOException {

        showElRates = false;
        int MAX_RUNS = 15;
        rnd = new Random(121);

        for (String name : new String[]{"Univ.owl", "Economy-SDA.owl", "Transportation-SDA.owl"}) {
            for (boolean dual : new boolean[] {false,true}) {

                UniformCostSearch<OWLLogicalAxiom> search = getSearch(CreationUtils.createOwlOntology("queryontologies",name),false);
                try {
                    search.run();
                } catch (NoConflictException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                Set<AxiomSet<OWLLogicalAxiom>> diagnoses = Collections.unmodifiableSet(search.getStorage().getDiagnoses());
                ModerateDistribution moderateDistribution = new ModerateDistribution();
                ExtremeDistribution extremeDistribution = new ExtremeDistribution();
                search.clearSearch();

                String out = "";

                for (UsersProbab usersProbab : UsersProbab.values()) {
                    for (DiagProbab diagProbab : DiagProbab.values()) {
                        for (int run = 0; run < MAX_RUNS; run++) {

                            UniformCostSearch<OWLLogicalAxiom> search2 = getSearch(CreationUtils.createOwlOntology("queryontologies",name),dual);
                            diagnoses = chooseUserProbab(usersProbab, search2, diagnoses,extremeDistribution,moderateDistribution);
                            AxiomSet<OWLLogicalAxiom> targetDiag = chooseTargetDiagnosis(diagProbab, new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses));





                                TableList e = new TableList();
                                String message = "act," + name + "," + dual + "," + usersProbab + ","
                                        + diagProbab + "," + run  ;

                                out += simulateBruteForceOnl(search2, (OWLTheory)search2.getTheory(), targetDiag, e, MINSCORE, message, null, null, null);

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
                     usersProbab, UniformCostSearch<OWLLogicalAxiom> search, Set<AxiomSet<OWLLogicalAxiom>> diagnoses, ExtremeDistribution extremeDistribution, ModerateDistribution moderateDistribution) {
        Map<ManchesterOWLSyntax, Double> keywordProbs = new HashMap<ManchesterOWLSyntax, Double>();
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
                    keywordProbs.put(keyword, probabilities[keywordList.indexOf(keyword)]);
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
                    keywordProbs.put(keyword, probabilities[keywordList.indexOf(keyword)]);
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
                    keywordProbs.put(keyword, (1.0 / n));
                }
                break;
        }
        ((OWLAxiomKeywordCostsEstimator)search.getCostsEstimator()).setKeywordProbabilities(keywordProbs, diagnoses);
        return sortDiagnoses(diagnoses);


    }

    @Test
    public void doShowMappingAxiomsSizes() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = AlignmentUtils.readProps("alignment.allFiles.properties");
        Map<String, List<String>> mapOntos = AlignmentUtils.readOntologiesFromFile(properties);

            for (String m : mapOntos.keySet()) {
                for (String o : mapOntos.get(m)) {
                    AlignmentUtils.getDiagnosis(m,o);
                }
            }
    }

    @Test
    public void doUnsolvableTest() throws SolverException, InconsistentTheoryException, IOException {
        Properties properties = AlignmentUtils.readProps();
        Map<String, List<String>> mapOntos = AlignmentUtils.readOntologiesFromFile(properties);
        boolean background_add = false;

        showElRates=true;
        BasePerformanceTests.QSSType[] qssTypes = new BasePerformanceTests.QSSType[]{BasePerformanceTests.QSSType.MINSCORE, BasePerformanceTests.QSSType.SPLITINHALF, BasePerformanceTests.QSSType.DYNAMICRISK};
        for (boolean dual : new boolean[] {false}) {
            for (TargetSource targetSource : new TargetSource[]{TargetSource.FROM_FILE}) {
                for (String m : mapOntos.keySet()) {
                    for (String o : mapOntos.get(m)) {
                        String out ="STAT, " + m +  ", " + o;
                        for (BasePerformanceTests.QSSType type : qssTypes) {
                            //String[] targetAxioms = properties.getProperty(m.trim() + "." + o.trim()).split(",");
                            String[] targetAxioms = AlignmentUtils.getDiagnosis(m,o);
                            OWLOntology ontology = createOwlOntology(m.trim(), o.trim());
                            Set<OWLLogicalAxiom> targetDg;
                            OWLTheory theory = createOWLTheory(ontology, dual);
                            UniformCostSearch<OWLLogicalAxiom> search = createUniformCostSearch(theory, dual);
                            //ProbabilityTableModel mo = new ProbabilityTableModel();
                            if (background_add) {
                                OWLOntology ontology1 = createOwlOntology(o.split("-")[0].trim());
                                OWLOntology ontology2 = createOwlOntology(o.split("-")[1].trim());
                                theory.addBackgroundFormulas(ontology1.getLogicalAxioms());
                                theory.addBackgroundFormulas(ontology2.getLogicalAxioms());
                            }
                            HashMap<ManchesterOWLSyntax, Double> map = Utils.getProbabMap();

                            String path = ClassLoader.getSystemResource("alignment/evaluation/"
                                    + m.trim()
                                    + "-incoherent-evaluation/"
                                    + o.trim()
                                    + ".txt").getPath();

                            OWLTheory theory2 = createOWLTheory(ontology, dual);
                            OWLTheory t3 = createOWLTheory(ontology, dual);
                            UniformCostSearch<OWLLogicalAxiom> search2 = createUniformCostSearch(theory2, dual);
                            search2.setCostsEstimator(new OWLAxiomCostsEstimator(theory2,path));


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

                            Set<AxiomSet<OWLLogicalAxiom>> allD = new LinkedHashSet<AxiomSet<OWLLogicalAxiom>>(search.getStorage().getDiagnoses());
                            search.clearSearch();

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
                                        Collections.unmodifiableSet(search.getStorage().getDiagnoses());
                                search.clearSearch();
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

                            out += simulateBruteForceOnl(search, theory, targetDg, e, type, message, null, null, null);

                        }
                        logger.info(out);
                    }
                }
            }
        }
    }



}
