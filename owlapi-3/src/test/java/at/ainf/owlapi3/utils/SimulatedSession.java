package at.ainf.owlapi3.utils;

import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.quickxplain.NewQuickXplain;
import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.storage.AxiomSetFactory;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.Rounding;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.performance.query.table.TableList;
import at.ainf.owlapi3.performance.query.table.Time;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 27.06.12
 * Time: 11:45
 * To change this template use File | Settings | File Templates.
 */
public class SimulatedSession {

    private static Logger logger = Logger.getLogger(SimulatedSession.class.getName());

    public boolean showElRates = true;

    public int NUMBER_OF_HITTING_SETS = 9;
    public BigDecimal SIGMA = new BigDecimal("100");
    public boolean userBrk = true;

    public int diagnosesCalc = 0;
    public int conflictsCalc = 0;
    public String daStr = "";

    public enum QSSType {MINSCORE, SPLITINHALF, STATICRISK, DYNAMICRISK, PENALTY, NO_QSS};

    public boolean traceDiagnosesAndQueries = false;
    public boolean minimizeQuery = false;

    protected QSS<OWLLogicalAxiom> createQSSWithDefaultParam(QSSType type) {
        switch (type) {
            case MINSCORE:
                return QSSFactory.createMinScoreQSS();
            case SPLITINHALF:
                return QSSFactory.createSplitInHalfQSS();
            case STATICRISK:
                return QSSFactory.createStaticRiskQSS(0.3);
            case DYNAMICRISK:
                return QSSFactory.createDynamicRiskQSS(0, 0.5, 0.4);
            case PENALTY:
                return QSSFactory.createPenaltyQSS(10);
            default:
                return QSSFactory.createMinScoreQSS();
        }
    }

    protected <E extends OWLObject> void printc
            (Collection<? extends Collection<E>> c) {
        for (Collection<E> hs : c) {
            System.out.println("Test case:");
            print(hs);
        }
    }

    protected <E extends OWLObject> void print
            (Collection<E> c) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        for (E el : c) {
            System.out.print(renderer.render(el) + ",");
        }
    }

    protected <E extends OWLObject> void prinths
            (Collection<AxiomSet<E>> c) {
        for (AxiomSet<E> hs : c) {
            logger.info(hs);
            print(hs);
        }
    }

    private int getEliminationRate(ITheory<OWLLogicalAxiom> theory, Set<AxiomSet<OWLLogicalAxiom>> d,
                                   boolean a, Partition<OWLLogicalAxiom> partition)
            throws SolverException {
        int deleted = 0;
        for (AxiomSet<OWLLogicalAxiom> diagnosis : d) {
            if (a && !((OWLTheory) theory).diagnosisConsistentWithoutEntailedTc(diagnosis, partition.partition))
                deleted++;
            else if (!a && ((OWLTheory) theory).diagnosisEntailsWithoutEntailedTC(diagnosis, partition.partition))
                deleted++;
        }
        return deleted;

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

    protected boolean generateQueryAnswer
            (TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search,
             Partition<OWLLogicalAxiom> actualQuery, Set<OWLLogicalAxiom> t) throws NoDecisionPossibleException {
        boolean answer;
        ITheory<OWLLogicalAxiom> theory = search.getTheory();

        AxiomSet<OWLLogicalAxiom> target = AxiomSetFactory.createHittingSet(BigDecimal.valueOf(0.5), t, new LinkedHashSet<OWLLogicalAxiom>());
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

    public String simulateBruteForceOnl(TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> search,
                                           OWLTheory theory, Set<OWLLogicalAxiom> targetDiag,
                                           TableList entry, QSSType scoringFunc, String message, Set<AxiomSet<OWLLogicalAxiom>> allDiagnoses, TreeSearch<AxiomSet<OWLLogicalAxiom>,OWLLogicalAxiom> secondsearch, OWLTheory t3) {
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
                    //search.reset();
                    search.run(NUMBER_OF_HITTING_SETS);

                    daStr += search.getDiagnoses().size() + "/";
                    diagnosesCalc += search.getDiagnoses().size();
                    conflictsCalc += search.getConflicts().size();

                    diagnoses = search.getDiagnoses();
                    diagTime.setTime(System.currentTimeMillis() - diag);
                } catch (SolverException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>();

                } catch (NoConflictException e) {
                    diagnoses = new TreeSet<AxiomSet<OWLLogicalAxiom>>(search.getDiagnoses());

                }

                if (diagnoses.isEmpty())
                    logger.error("No diagnoses found!");

                if (diagnoses.size() == 0) {

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
                logger.info("num of hitting sets " + search.getDiagnoses().size());

                // cast should be corrected
                Iterator<AxiomSet<OWLLogicalAxiom>> descendSet = (new TreeSet<AxiomSet<OWLLogicalAxiom>>(diagnoses)).descendingIterator();
                AxiomSet<OWLLogicalAxiom> d = descendSet.next();
                AxiomSet<OWLLogicalAxiom> d1 = (descendSet.hasNext()) ? descendSet.next() : null;

                boolean isTargetDiagFirst = d.equals(targetDiag);
                BigDecimal dp = d.getMeasure();
                if (logger.isInfoEnabled()) {
                    AxiomSet<OWLLogicalAxiom> o = containsItem(diagnoses, targetDiag);
                    BigDecimal diagProbabilities = new BigDecimal("0");
                    for (AxiomSet<OWLLogicalAxiom> tempd : diagnoses)
                        diagProbabilities = diagProbabilities.add(tempd.getMeasure());
                    logger.trace("diagnoses: " + diagnoses.size() +
                            " (" + diagProbabilities + ") first diagnosis: " + d +
                            " is target: " + isTargetDiagFirst + " is in window: " +
                            ((o == null) ? false : o.toString()));
                }

                if (d1 != null) {// && scoringFunc != QSSType.SPLITINHALF) {
                    BigDecimal d1p = d1.getMeasure();
                    BigDecimal temp = d1p.multiply(new BigDecimal("100"));
                    temp = temp.divide(dp, Rounding.PRECISION,Rounding.ROUNDING_MODE);
                    BigDecimal diff = new BigDecimal("100").subtract(temp);
                    logger.trace("difference : " + (dp.subtract(d1p)) + " - " + diff + " %");
                    if (userBrk && diff.compareTo(SIGMA) > 0 && isTargetDiagFirst && num_of_queries > 0) {
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

                if (minimizeQuery) {
                    QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(actPa, theory);
                    NewQuickXplain<OWLLogicalAxiom> q = new NewQuickXplain<OWLLogicalAxiom>();
                    try {
                        actPa.partition = q.search(mnz, actPa.partition, null);
                    } catch (NoConflictException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (SolverException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InconsistentTheoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

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
                    ph.setMeasure(new BigDecimal("0.5").multiply(ph.getMeasure()));
                }
                if (allDiagnoses != null) {

                    try {
                        secondsearch.run();
                    } catch (NoConflictException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    remainingAllDiags = secondsearch.getDiagnoses();
                    int eliminatedInLeading = getEliminationRate(search.getTheory(), diagnoses, answer, actPa);
                    int eliminatedInRemaining = getEliminationRate(secondsearch.getTheory(), remainingAllDiags, answer, actPa);
                    int eliminatedInRemainingSize = remainingAllDiags.size();
                    int eliminatedInfull = getEliminationRate(t3, allDiagnoses, answer, actPa);
                    // deleteDiag(search.getTheory(),remainingAllDiags,answer,actPa.partition);

                    AxiomSet<OWLLogicalAxiom> foundTarget;
                    foundTarget = null;
                    for (AxiomSet<OWLLogicalAxiom> axiom : allDiagnoses)
                        if (targetDiag.containsAll(axiom)) {
                            if (foundTarget != null)
                                logger.info("");
                            foundTarget = axiom;
                        }
                    if (answer)
                        secondsearch.getTheory().addEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                    else
                        secondsearch.getTheory().addNonEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                    logger.info("elimination rates: in all diags ;" + eliminatedInfull + "/" + allDiagnoses.size() +
                            "; in all remaining diags ;" + eliminatedInRemaining + "/" + eliminatedInRemainingSize +
                            "; in leading ;" + eliminatedInLeading + "/" + diagnoses.size() + " " + foundTarget);
                }

                int eliminatedInLeading = getEliminationRate(search.getTheory(), diagnoses, answer, actPa);
                if (showElRates)
                    logger.info("elimination rates: in leading ;" + eliminatedInLeading + "/" + diagnoses.size());

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
        if (!targetDiagnosisIsInWind) {
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
            } else {
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

}
