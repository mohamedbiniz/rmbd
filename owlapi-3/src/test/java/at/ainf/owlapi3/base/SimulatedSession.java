package at.ainf.owlapi3.base;

import at.ainf.diagnosis.Searchable;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.AbstractTreeSearch;
import at.ainf.diagnosis.tree.Rounding;
import at.ainf.diagnosis.tree.TreeSearch;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import at.ainf.owlapi3.base.tools.NoDecisionPossibleException;
import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.owlapi3.base.tools.TableList;
import at.ainf.owlapi3.base.tools.Time;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SimulatedSession extends CalculateDiagnoses {

    private  Logger logger = LoggerFactory.getLogger(SimulatedSession.class.getName());

    private boolean showElRates = true;

    private int numberOfHittingSets = 9;
    private BigDecimal SIGMA = new BigDecimal("100");
    private boolean userBreak = true;



    //protected int diagnosesCalc = 0;
    //protected int conflictsCalc = 0;

    //protected String daStr = "";
    public enum QSSType {MINSCORE, SPLITINHALF, STATICRISK, DYNAMICRISK, PENALTY, NO_QSS};

    private boolean traceDiagnosesAndQueries = false;
    private boolean minimizeQuery = false;

    public int getNumberOfHittingSets() {
        return numberOfHittingSets;
    }

    public void setNumberOfHittingSets(int numberOfHittingSets) {
        this.numberOfHittingSets = numberOfHittingSets;
    }

    public boolean isUserBreak() {
        return userBreak;
    }

    public void setUserBreak(boolean userBreak) {
        this.userBreak = userBreak;
    }

    public BigDecimal getSIGMA() {
        return SIGMA;
    }

    public void setSIGMA(BigDecimal SIGMA) {
        this.SIGMA = SIGMA;
    }

    public boolean isShowElRates() {
        return showElRates;
    }

    public boolean isTraceDiagnosesAndQueries() {
        return traceDiagnosesAndQueries;
    }

    public boolean isMinimizeQuery() {
        return minimizeQuery;
    }

    public void setTraceDiagnosesAndQueries(boolean traceDiagnosesAndQueries) {
        this.traceDiagnosesAndQueries = traceDiagnosesAndQueries;
    }

    public void setMinimizeQuery(boolean minimizeQuery) {
        this.minimizeQuery = minimizeQuery;
    }

    public void setShowElRates(boolean showElRates) {
        this.showElRates = showElRates;
    }

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
            (Collection<FormulaSet<E>> c) {
        for (FormulaSet<E> hs : c) {
            logger.info(hs.getName());
            print(hs);
        }
    }

    protected int getEliminationRate(Searchable<OWLLogicalAxiom> theory, Set<FormulaSet<OWLLogicalAxiom>> d,
                                     boolean a, Partition<OWLLogicalAxiom> partition)
            throws SolverException {
        int deleted = 0;
        for (FormulaSet<OWLLogicalAxiom> diagnosis : d) {
            if (a && !((OWLTheory) theory).diagnosisConsistentWithoutEntailedTc(diagnosis, partition.partition))
                deleted++;
            else if (!a && ((OWLTheory) theory).diagnosisEntailsWithoutEntailedTC(diagnosis, partition.partition))
                deleted++;
        }
        return deleted;

    }

    protected boolean isInWindow(Set<OWLLogicalAxiom> targetDiag, Set<FormulaSet<OWLLogicalAxiom>> diagnoses) {
        for (FormulaSet<OWLLogicalAxiom> ps : diagnoses)
            if (targetDiag.containsAll(ps)) {
                if (logger.isDebugEnabled())
                    logger.debug("Target diagnosis is in window " + ps.getName());
                return true;
            }
        return false;
    }

    protected <E extends FormulaSet<OWLLogicalAxiom>> E containsItem(Collection<E> col, Set<OWLLogicalAxiom> item) {
        for (E o : col) {
            if (item.containsAll(o)) {
                if (logger.isTraceEnabled())
                    logger.trace("Target diagnosis " + o + "is in the window");
                return o;
            }
        }
        return null;
    }

    protected boolean generateQueryAnswer
            (Searchable<OWLLogicalAxiom> theory,
             Partition<OWLLogicalAxiom> actualQuery, Set<OWLLogicalAxiom> t) throws NoDecisionPossibleException {
        boolean answer;

        FormulaSet<OWLLogicalAxiom> target = new FormulaSetImpl<OWLLogicalAxiom>(BigDecimal.valueOf(0.5), t, new LinkedHashSet<OWLLogicalAxiom>());
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

    private TableList entry;

    public TableList getEntry() {
        return entry;
    }

    public void setEntry(TableList entryLoc) {
        this.entry = entryLoc;
    }

    private Set<FormulaSet<OWLLogicalAxiom>> allDiags;

    private TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search2;

    private OWLTheory theory3;

    public Set<FormulaSet<OWLLogicalAxiom>> getAllDiags() {
        return allDiags;
    }

    public void setAllDiags(Set<FormulaSet<OWLLogicalAxiom>> allDiags) {
        this.allDiags = allDiags;
    }

    public TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch2() {
        return search2;
    }

    public void setSearch2(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search2) {
        this.search2 = search2;
    }

    public OWLTheory getTheory3() {
        return theory3;
    }

    public void setTheory3(OWLTheory theory3) {
        this.theory3 = theory3;
    }

    private boolean logElRate = false;

    public boolean isLogElRate() {
        return logElRate;
    }

    public void setLogElRate(boolean logElRate) {
        this.logElRate = logElRate;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private QSSType scoringFunct;

    public QSSType getScoringFunct() {
        return scoringFunct;
    }

    public void setScoringFunct(QSSType scoringFunct) {
        this.scoringFunct = scoringFunct;
    }

    private Set<OWLLogicalAxiom> targetD;

    public Set<OWLLogicalAxiom> getTargetD() {
        return targetD;
    }

    public void setTargetD(Set<OWLLogicalAxiom> targetD) {
        this.targetD = targetD;
    }

    private TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search;

    private OWLTheory theory;

    private Searchable<OWLLogicalAxiom> queryAnswerTheory;

    public TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> getSearch() {
        return search;
    }

    public void setSearch(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search) {
        this.search = search;
    }

    public OWLTheory getTheory() {
        return theory;
    }

    public void setTheory(OWLTheory theory) {
        this.theory = theory;
    }

    public Searchable<OWLLogicalAxiom> getQueryAnswerTheory() {
        return queryAnswerTheory;
    }

    public void setQueryAnswerTheory(Searchable<OWLLogicalAxiom> queryAnswerTheory) {
        this.queryAnswerTheory = queryAnswerTheory;
    }

    public String simulateQuerySession() {

        QSS<OWLLogicalAxiom> qss = createQSSWithDefaultParam(getScoringFunct());
        //userBreak=false;
        Partition<OWLLogicalAxiom> actPa = null;

        Set<FormulaSet<OWLLogicalAxiom>> diagnoses = null;
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
        Partitioning<OWLLogicalAxiom> queryGenerator = new CKK<OWLLogicalAxiom>(getTheory(), qss);

        while (!querySessionEnd) {
            try {
                Collection<FormulaSet<OWLLogicalAxiom>> lastD = diagnoses;
                logger.trace("numOfQueries: " + num_of_queries + " start for diagnoses");

                userBreak = false;
                systemBreak = false;

                // marker
                long reactQuery = System.currentTimeMillis();

                if (actPa != null && actPa.dx.size() == 1 && actPa.dz.size() == 1 && actPa.dnx.isEmpty()) {
                    logger.error("Help!");
                    printc(getTheory().getKnowledgeBase().getEntailedTests());
                    printc(getTheory().getKnowledgeBase().getNonentailedTests());
                    print(actPa.partition);
                    prinths(actPa.dx);
                    prinths(actPa.dz);
                }

                diagnoses = calcDiagnoses(getSearch(), diagnoses, diagTime);

                if (diagnoses.isEmpty())
                    logger.error("No diagnoses found!");

                if (diagnoses.size() == 0) {

                    possibleError = true;
                    break;
                }

                logDiagnoses(getSearch(), diagnoses, num_of_queries);

                // cast should be corrected
                Iterator<FormulaSet<OWLLogicalAxiom>> descendSet = (new TreeSet<FormulaSet<OWLLogicalAxiom>>(diagnoses)).descendingIterator();
                FormulaSet<OWLLogicalAxiom> d = descendSet.next();
                FormulaSet<OWLLogicalAxiom> d1 = (descendSet.hasNext()) ? descendSet.next() : null;

                boolean isTargetDiagFirst = d.equals(getTargetD());
                BigDecimal dp = d.getMeasure();

                logTraceDiagnoses(getTargetD(), diagnoses, d, isTargetDiagFirst);

                if (d1 != null) {// && scoringFunc != QSSType.SPLITINHALF) {
                    BigDecimal d1p = d1.getMeasure();
                    BigDecimal temp = d1p.multiply(new BigDecimal("100"));
                    temp = temp.divide(dp, Rounding.PRECISION, Rounding.ROUNDING_MODE);
                    BigDecimal diff = new BigDecimal("100").subtract(temp);
                    logger.trace("difference : " + (dp.subtract(d1p)) + " - " + diff + " %");
                    if (this.userBreak && diff.compareTo(SIGMA) > 0 && isTargetDiagFirst && num_of_queries > 0) {
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

                logger.trace("numOfQueries: " + num_of_queries + " start for  query");

                long query = System.currentTimeMillis();
                //actPa = getBestQuery(start, diagnoses);

                actPa = queryGenerator.generatePartition(diagnoses);
                //TODO delete logger output
                logger.info("actPa: " + CalculateDiagnoses.renderAxioms(actPa.partition) + "\n");

                if (actPa == null) {
                    search.reset();
                    try {
                        search.start();
                    } catch (NoConflictException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    diagnoses = search.getDiagnoses();
                    actPa = queryGenerator.generatePartition(diagnoses);
                }

                if (minimizeQuery) {
                    minimizeQuery(getTheory(), actPa);
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
                        if (getQueryAnswerTheory() == null) {
                            setQueryAnswerTheory(getSearch().getSearchable());
                        }
                        answer = generateQueryAnswer(getQueryAnswerTheory(), actPa, getTargetD());
                        hasAn = true;
                    } catch (NoDecisionPossibleException e) {
                        hasQueryWithNoDecisionPossible = true;

                        logger.info("do not know query answer but query should be entailed so answering true");
                        hasAn = true;

                        /* actPa = queryGenerator.nextPartition(actPa);
                        if (actPa == null) {
                            logger.error("All partitions were tested and none provided an answer to the target diagnosis!");
                            String msgComma = getMessage() + "," + "act2,problem no queryanswer";
                            logger.info(msgComma);
                            return msgComma;
                            //break;
                        }*/
                    }
                }

                if (qss != null) qss.updateParameters(answer);

                if (traceDiagnosesAndQueries)
                    logger.info("query asked: " + renderAxioms(actPa.partition));

                // fine all dz diagnoses
                // TODO do we need this fine?
                for (FormulaSet<OWLLogicalAxiom> ph : actPa.dz) {
                    ph.setMeasure(new BigDecimal("0.5").multiply(ph.getMeasure()));
                }
                if (isLogElRate()) {

                    try {
                        search2.start();
                    } catch (NoConflictException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    if (answer)
                        search2.getSearchable().getKnowledgeBase().addEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                    else
                        search2.getSearchable().getKnowledgeBase().addNonEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));

                    logEliminationRateHelp(getSearch(), getTargetD(), allDiags, search2, theory3, actPa, diagnoses, answer);
                }

                int eliminatedInLeading = getEliminationRate(getSearch().getSearchable(), diagnoses, answer, actPa);
                if (showElRates)
                    logger.info("elimination rates: in leading ;" + eliminatedInLeading + "/" + diagnoses.size());

                if (answer) {
                    getSearch().getSearchable().getKnowledgeBase().addEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                    if (actPa.dnx.isEmpty() && diagnoses.size() < numberOfHittingSets)
                        querySessionEnd = true;
                } else {
                    getSearch().getSearchable().getKnowledgeBase().addNonEntailedTest(new TreeSet<OWLLogicalAxiom>(actPa.partition));
                    if (actPa.dx.isEmpty() && diagnoses.size() < numberOfHittingSets)
                        querySessionEnd = true;

                }

            } catch (SolverException e) {
                querySessionEnd = true;
                logger.error(e.toString());

            } catch (InconsistentTheoryException e) {
                querySessionEnd = true;
                logger.error(e.toString());

            }
        }
        time = System.currentTimeMillis() - time;
        boolean targetDiagnosisIsInWind = false;
        boolean targetDiagnosisIsMostProbable = false;
        if (diagnoses != null) {
            //TreeSet<ProbabilisticHittingSet> diags = new TreeSet<ProbabilisticHittingSet>(diagnoses);
            targetDiagnosisIsInWind = isInWindow(getTargetD(), diagnoses);
            if (diagnoses.size() >= 1 && getTargetD().
                    containsAll((new TreeSet<FormulaSet<OWLLogicalAxiom>>(diagnoses)).last())) {
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
        if (num_of_queries != 0) consistencyCount = getTheory().getConsistencyCount() / num_of_queries;
        if (num_of_queries != 0) reactionTime = reactionTime / num_of_queries;

        long consistencyTime = 0;
        if (consistencyCount != 0) consistencyTime = getTheory().getConsistencyTime() / consistencyCount;

//        message += " , Iteration finished within " + time + " ms, required " + num_of_queries + " queries, most probable "
//                + targetDiagnosisIsMostProbable + ", is in window " + targetDiagnosisIsInWind + ", size of window  " + diagWinSize
//                + ", reaction " + reactionTime + ", user " + userBreak +
//                ", systemBrake " + systemBreak + ", nd " + hasQueryWithNoDecisionPossible +
//                ", consistency checks " + consistencyCount;
        String msg = getMessage() + "," + "Time: " +time + ", \n" + "Number of queries: "+num_of_queries + ", \n"
                +"Query cardinality: "+queryCardinality+", \n" + "Target Diagnosis is most probable: "+targetDiagnosisIsMostProbable + ", \n" + "Target diagnosis is windows: "+targetDiagnosisIsInWind + ", \n" + "Diagnosis window size: "+diagWinSize
                + ", \n" + "Reaction time: "+ reactionTime + ", \n" + "User break: "+userBreak + ", \n" + possibleError +
                ",\n" + "System break: "+systemBreak + ", \n" + "Has query with no decision possible: "+hasQueryWithNoDecisionPossible +
                ", \n" + "Consistency count: "+consistencyCount + ",\n" +"Consistency time: "+ consistencyTime;
        logger.info (msg);

        String msgComma = getMessage() + "," + "act2," +
                time + "," +
                num_of_queries + "," +
                queryCardinality + "," +
                targetDiagnosisIsMostProbable + "," +
                targetDiagnosisIsInWind + "," +
                diagWinSize + "," +
                reactionTime + "," +
                userBreak + "," +
                possibleError + "," +
                systemBreak + "," +
                hasQueryWithNoDecisionPossible + "," +
                consistencyCount + "," +
                consistencyTime;
        logger.info (msgComma);

        //Probehalber
        logger.info("Conflict time: "+((AbstractTreeSearch)getSearch()).getAvgConflictTime() );
        logger.info("Conflict count: "+((AbstractTreeSearch)getSearch()).getConflicts().size() );
        logger.info("Time for first nine diagnoses: "+((AbstractTreeSearch)getSearch()).getNinthDiagnosisTime());

        if (possibleError) {
            logger.info("Possible an error occured: ");
            logger.info("target diagnosis: " + renderAxioms(getTargetD()));
            if (diagnoses == null) {
                logger.info("diagnoses is null!");
            } else {
                logger.info("diagnoses in window: " + diagnoses.size());
                for (Set<OWLLogicalAxiom> diagnosis : diagnoses)
                    logger.info("diagnosis: " + renderAxioms(diagnosis));
            }
        }

        String msg1 = "Total time: "+time + ", \n " +"Number of queries: " +num_of_queries + ", \\n"+"Query cardinality: +"+queryCardinality +", \\n"+"Target Diagnosis is most probable: "+ targetDiagnosisIsMostProbable + ", \\n" + "Target Diagnosis is in window: " + targetDiagnosisIsInWind + ", \\n" +"Diagnosis windows size: " +diagWinSize
                + ", \\n" + "Reaction time: "+ reactionTime + ", \\n" +"User break: " +userBreak + ",\\n" + "Possible error: "+possibleError +
                ", \\n" +"System break: "+ systemBreak + ", \\n" + "Hast query with no decision possible: " +hasQueryWithNoDecisionPossible +
                ", \\n" + "Consistency count: "+consistencyCount;
        this.entry.addEntr(num_of_queries, queryCardinality, targetDiagnosisIsInWind, targetDiagnosisIsMostProbable,
                diagWinSize, userBreak, systemBreak, time, queryTime, diagTime, reactionTime, consistencyCount);
        return msg1;
    }

    protected void logTraceDiagnoses(Set<OWLLogicalAxiom> targetDiag, Set<FormulaSet<OWLLogicalAxiom>> diagnoses, FormulaSet<OWLLogicalAxiom> d, boolean targetDiagFirst) {
        if (logger.isInfoEnabled()) {
            FormulaSet<OWLLogicalAxiom> o = containsItem(diagnoses, targetDiag);
            BigDecimal diagProbabilities = new BigDecimal("0");
            for (FormulaSet<OWLLogicalAxiom> tempd : diagnoses)
                diagProbabilities = diagProbabilities.add(tempd.getMeasure());
            logger.trace("diagnoses: " + diagnoses.size() +
                    " (" + diagProbabilities + ") first diagnosis: " + d +
                    " is target: " + targetDiagFirst + " is in window: " +
                    ((o == null) ? false : o.toString()));
        }
    }

    protected void logDiagnoses(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, Set<FormulaSet<OWLLogicalAxiom>> diagnoses, int num_of_queries) {
        if (traceDiagnosesAndQueries) {
            String diag1 = "";
            for (Set<OWLLogicalAxiom> diagnosis : diagnoses)
                diag1 += renderAxioms(diagnosis) + " ; ";
            logger.info("diagnoses before query " + num_of_queries + ":" + diag1);
        }

        String infoCa = "";
        for (Set<OWLLogicalAxiom> diagnose : diagnoses)
            infoCa += diagnose.size() + "/";
        logger.info("cardinality of diagnoses " + infoCa);
        logger.info("num of hitting sets " + search.getDiagnoses().size());
    }

    protected void minimizeQuery(OWLTheory theory, Partition<OWLLogicalAxiom> actPa) {
        QueryMinimizer<OWLLogicalAxiom> mnz = new QueryMinimizer<OWLLogicalAxiom>(actPa, theory);
        QuickXplain<OWLLogicalAxiom> q = new QuickXplain<OWLLogicalAxiom>();
        try {
            actPa.partition = q.search(mnz, actPa.partition).iterator().next();
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    protected Set<FormulaSet<OWLLogicalAxiom>> calcDiagnoses(TreeSearch<FormulaSet<OWLLogicalAxiom>,
            OWLLogicalAxiom> search, Set<FormulaSet<OWLLogicalAxiom>> diagnoses, Time diagTime) throws InconsistentTheoryException {
        try {
            long diag = System.currentTimeMillis();
            //start.reset();
            search.setMaxDiagnosesNumber(numberOfHittingSets);
            search.start();

            //daStr += start.getDiagnoses().size() + "/";
            //diagnosesCalc += start.getDiagnoses().size();
            //conflictsCalc += start.getConflicts().size();

            diagnoses = search.getDiagnoses();
            diagTime.setTime(System.currentTimeMillis() - diag);
        } catch (SolverException e) {
            diagnoses = new TreeSet<FormulaSet<OWLLogicalAxiom>>();

        } catch (NoConflictException e) {
            diagnoses = new TreeSet<FormulaSet<OWLLogicalAxiom>>(search.getDiagnoses());

        }
        return diagnoses;
    }

    protected void logEliminationRateHelp(TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> search, Set<OWLLogicalAxiom> targetDiag, Set<FormulaSet<OWLLogicalAxiom>> allDiagnoses, TreeSearch<FormulaSet<OWLLogicalAxiom>, OWLLogicalAxiom> secondsearch, OWLTheory t3, Partition<OWLLogicalAxiom> actPa, Set<FormulaSet<OWLLogicalAxiom>> diagnoses, boolean answer) throws SolverException, InconsistentTheoryException {
        Set<FormulaSet<OWLLogicalAxiom>> remainingAllDiags;
        remainingAllDiags = secondsearch.getDiagnoses();
        int eliminatedInLeading = getEliminationRate(search.getSearchable(), diagnoses, answer, actPa);
        int eliminatedInRemaining = getEliminationRate(secondsearch.getSearchable(), remainingAllDiags, answer, actPa);
        int eliminatedInRemainingSize = remainingAllDiags.size();
        int eliminatedInfull = getEliminationRate(t3, allDiagnoses, answer, actPa);
        // deleteDiag(start.getSearchable(),remainingAllDiags,answer,actPa.partition);

        FormulaSet<OWLLogicalAxiom> foundTarget;
        foundTarget = null;
        for (FormulaSet<OWLLogicalAxiom> formula : allDiagnoses)
            if (targetDiag.containsAll(formula)) {
                if (foundTarget != null)
                    logger.info("");
                foundTarget = formula;
            }

        logger.info("elimination rates: in all diags ;" + eliminatedInfull + "/" + allDiagnoses.size() +
                "; in all remaining diags ;" + eliminatedInRemaining + "/" + eliminatedInRemainingSize +
                "; in leading ;" + eliminatedInLeading + "/" + diagnoses.size() + " " + foundTarget);
    }

    public enum TargetSource {FROM_FILE, FROM_30_DIAGS}
}
