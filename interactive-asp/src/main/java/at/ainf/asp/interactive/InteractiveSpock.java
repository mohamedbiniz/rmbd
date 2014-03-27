package at.ainf.asp.interactive;

import at.ainf.asp.interactive.solver.ASPKnowledgeBase;
import at.ainf.asp.interactive.solver.ASPSolver;
import at.ainf.asp.interactive.solver.InteractiveASPTheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.EqualCostsEstimator;
import at.ainf.diagnosis.tree.Rounding;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Implements interactive debugging of an ASP program using spock
 */
public class InteractiveSpock {
    private static Logger logger = LoggerFactory.getLogger(InteractiveSpock.class.getName());

    private final InteractiveASPTheory theory;
    private final double thresholdQuery;
    private CostsEstimator<String> costsEstimator;
    private QSS<String> qss = null;
    private int maxDiagnosesNumber = 9;
    private BigDecimal SIGMA = new BigDecimal("100");


    public InteractiveSpock(InteractiveASPTheory theory) {
        this.theory = theory;
        this.costsEstimator = new EqualCostsEstimator<String>(Collections.<String>emptySet(), BigDecimal.valueOf(0.01d));
        this.thresholdQuery = 0.01d;
    }

    public void start() throws SolverException {
        TreeSet<FormulaSet<String>> diagnoses = new TreeSet<FormulaSet<String>>();

        boolean firstRun = true;
        while (true) {
            diagnoses = getDiagnoses(diagnoses, getMaxDiagnosesNumber());

            // check termination criteria
            if (checkTermination(diagnoses)) {
                break;
            }

            Partition<String> query = getQuery(diagnoses);

            // return the most probable diagnosis in case there is no query
            if (query == null) {
                break;
            }

            // ask query
            if (firstRun) {
                System.out.println("Classification for queries: 1 - cautiously true, " +
                        "2 - cautiously false, 3 - bravely true, 4 - bravely false");
                firstRun = false;
            }
            final Set<String> queryAtoms = extractAtoms(query);
            int classification = askQuery(queryAtoms);

            // update test cases
            updateTestCases(queryAtoms, classification);
        }
        // output the first (most probable) diagnosis
        System.out.println(diagnoses.iterator().next());
    }

    private boolean checkTermination(TreeSet<FormulaSet<String>> diagnoses) {

        // only one diagnosis left
        if (diagnoses.size() < 2) {
            return true;
        }

        // compare two best diagnoses
        Iterator<FormulaSet<String>> descendSet = (new TreeSet<FormulaSet<String>>(diagnoses)).descendingIterator();
        BigDecimal dp = descendSet.next().getMeasure();
        BigDecimal d1p = descendSet.next().getMeasure();
        BigDecimal temp = d1p.multiply(new BigDecimal("100"));
        temp = temp.divide(dp, Rounding.PRECISION, Rounding.ROUNDING_MODE);
        BigDecimal diff = new BigDecimal("100").subtract(temp);

        if (logger.isTraceEnabled())
            logger.trace("difference : " + (dp.subtract(d1p)) + " - " + diff + " %");

        return diff.compareTo(SIGMA) > 0;
    }

    private void updateTestCases(Set<String> queryAtoms, int classification) {
        Set<String> testCase = new HashSet<String>();
        Boolean positiveAnswer = null;
        for (String id : queryAtoms) {
            switch (classification) {
                // cautiously true
                case 1:
                    testCase.add("int(" + id + ")");
                    if (positiveAnswer == null) positiveAnswer = true;
                    break;
                // cautiously false
                case 2:
                    testCase.add("-int(" + id + ")");
                    if (positiveAnswer == null) positiveAnswer = false;
                    break;
                // bravely true
                case 3:
                    testCase.add("int(" + id + ")");
                    if (positiveAnswer == null) positiveAnswer = false;
                    break;
                // bravely false
                case 4:
                    testCase.add("-int(" + id + ")");
                    if (positiveAnswer == null) positiveAnswer = true;
                    break;
                default:
                    throw new IllegalArgumentException("Classification is unknown!");
            }
        }
        if (positiveAnswer == null) throw new IllegalStateException("A test case cannot be added to a knowledge base!");
        if (positiveAnswer)
            this.theory.getKnowledgeBase().addPositiveTest(testCase);
        else
            this.theory.getKnowledgeBase().addNegativeTest(testCase);
        qss.updateParameters(positiveAnswer);
    }

    private Set<String> extractAtoms(Partition<String> query) {
        Set<String> res = new HashSet<String>(query.partition.size());
        for (String atom : query.partition) {
            res.add(atom.substring(atom.indexOf("(") + 1, atom.indexOf(")")));
        }
        return res;
    }

    private Integer askQuery(Set<String> query) {
        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(generateQuery(query) + " \n > ");
            try {
                String input = br.readLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect classification value!");
            } catch (IOException e) {
                logger.error("Cannot read input stream!", e);
                throw new RuntimeException(e);
            }
        }
    }

    private String generateQuery(Set<String> query) {
        StringBuilder res = new StringBuilder("Provide classification for the following set of atoms: {");
        for (Iterator<String> iterator = query.iterator(); iterator.hasNext(); ) {
            String atom = iterator.next();
            res.append(atom);
            if (iterator.hasNext()) res.append(", ");
        }
        res.append("}");
        return res.toString();
    }


    public void setScoring(String func) {
        if ("split".equals(func)) this.qss = QSSFactory.createSplitInHalfQSS();
        if ("dynamic".equals(func)) this.qss = QSSFactory.createDynamicRiskQSS(0, 0.4, 0.5);
        if (!"minscore".equals(func)) logger.error("Unknown scoring function " + func + ", using \"miscore\"!");
        this.qss = QSSFactory.createMinScoreQSS();
    }

    public void setCostsEstimator(String estimator) {
        if ("simple".equals(estimator)) {
            // this estimator prefers unsatisfied and violated explanations
            Set<String> preferredAtoms = new HashSet<String>();
            for (String atom : theory.getASPKnowledgeBase().getErrorAtoms()) {
                if (atom.startsWith("unsatisfied") || atom.startsWith("violated"))
                    preferredAtoms.add(atom);
            }
            this.costsEstimator = new SimpleCostsEstimator<String>(preferredAtoms);
        }
        if (!"equal".equals(estimator))
            logger.error("Unknown scoring function \"" + estimator + "\", using \"equal\"!");

        this.costsEstimator = new EqualCostsEstimator<String>(Collections.<String>emptySet(), BigDecimal.valueOf(0.01d));
    }

    public QSS<String> getScoring() {
        return this.qss;
    }

    public double getThresholdQuery() {
        return thresholdQuery;
    }

    public Partition<String> getQuery(Set<FormulaSet<String>> diagnoses) throws SolverException {
        CKK<String> ckk = new CKK<String>(theory, getScoring());
        ckk.setThreshold(getThresholdQuery());

        Partition<String> query = null;
        try {
            query = ckk.generatePartition(diagnoses);
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();
        }

        if (query == null || query.partition == null) return null;
        QueryMinimizer<String> mnz = new QueryMinimizer<String>(query, theory);
        QuickXplain<String> q = new QuickXplain<String>();
        try {
            query.partition = q.search(mnz, query.partition).iterator().next();
        } catch (NoConflictException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SolverException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InconsistentTheoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return query;
    }


    private TreeSet<FormulaSet<String>> getDiagnoses(TreeSet<FormulaSet<String>> diagnoses, final int diagnosesNumber)
            throws SolverException {

        ASPSolver solver = theory.getReasoner();
        ASPKnowledgeBase kb = theory.getASPKnowledgeBase();
        final Set<FormulaSet<String>> diagnosisCandidates = new LinkedHashSet<FormulaSet<String>>();

        Set<FormulaSet<String>> oldDiagnoses = new HashSet<FormulaSet<String>>(diagnosesNumber);
        oldDiagnoses.addAll(diagnoses);

        while (true) {

            // clear cache and add all relevant parts of the program, namely
            // program, background knowledge and positive test cases
            solver.clearFormulasCache();
            solver.addFormulasToCache(solver.generateDebuggingProgram(kb, diagnoses));

            final Set<FormulaSet<String>> formulaSets = solver.computeDiagnoses(diagnosesNumber, this.costsEstimator);

            // verify if the returned candidates are consistent with negative test cases
            for (Iterator<FormulaSet<String>> it = formulaSets.iterator(); it.hasNext(); ) {
                final Set<String> candidate = it.next();
                // block found candidate by a constraint
                if (!theory.verifyTestCasesForDiagnosisCandidate(candidate)) {
                    it.remove();
                }
                kb.addBackgroundFormulas(Collections.singleton(solver.generateConstraint(candidate)));
            }

            diagnosisCandidates.addAll(formulaSets);

            if (diagnosisCandidates.size() >= diagnosesNumber || formulaSets.isEmpty() ||
                    oldDiagnoses.containsAll(diagnosisCandidates))
                break;
        }

        oldDiagnoses.retainAll(diagnosisCandidates);

        diagnoses.clear();
        diagnoses.addAll(oldDiagnoses);

        for (FormulaSet<String> candidate : diagnosisCandidates) {
            if (diagnoses.contains(candidate))
                continue;
            Set<String> entailments = theory.getEntailments(candidate);
            FormulaSet<String> diagnosis =
                    new FormulaSetImpl<String>(costsEstimator.getFormulaSetCosts(candidate), candidate, entailments);
            diagnoses.add(diagnosis);
        }

        theory.doBayesUpdate(diagnoses);

        return diagnoses;
    }

    public int getMaxDiagnosesNumber() {
        return maxDiagnosesNumber;
    }

    public void setMaxDiagnosesNumber(int maxDiagnosesNumber) {
        this.maxDiagnosesNumber = maxDiagnosesNumber;
    }
}
