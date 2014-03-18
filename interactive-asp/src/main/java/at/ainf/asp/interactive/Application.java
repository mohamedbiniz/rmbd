package at.ainf.asp.interactive;

import at.ainf.asp.antlr.IntASPInputLexer;
import at.ainf.asp.antlr.IntASPInputParser;
import at.ainf.asp.interactive.input.IntASPInput;
import at.ainf.asp.interactive.solver.ASPKnowledgeBase;
import at.ainf.asp.interactive.solver.ASPSolver;
import at.ainf.asp.interactive.solver.ASPTheory;
import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.QueryMinimizer;
import at.ainf.diagnosis.partitioning.scoring.DynamicRiskQSS;
import at.ainf.diagnosis.partitioning.scoring.MinScoreQSS;
import at.ainf.diagnosis.partitioning.scoring.Scoring;
import at.ainf.diagnosis.partitioning.scoring.SplitInHalfQSS;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.EqualCostsEstimator;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main class for interactive debugger prototype
 */
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class.getName());
    private final ASPTheory theory;
    private final double thresholdQuery;
    private final CostsEstimator<String> costsEstimator;
    private Scoring<String> scoring;


    public Application(ASPTheory theory) {
        this.theory = theory;
        this.costsEstimator = new EqualCostsEstimator<String>(Collections.<String>emptySet(), BigDecimal.valueOf(0.01d));
        this.thresholdQuery = 0.95;
    }

    public static void main(String[] args) throws IOException, InterruptedException, SolverException {

        List<Path> paths = getFiles(args);
        InputStream stream;
        if (paths.size() > 0) {
            stream = new FileInputStream(paths.get(0).toFile());
        } else
            stream = System.in;

        IntASPInput listener = new IntASPInput();
        ANTLRInputStream input = new ANTLRInputStream(stream);
        IntASPInputLexer lexer = new IntASPInputLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        IntASPInputParser parser = new IntASPInputParser(tokens);
        ParseTree tree = parser.parse();
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        logger.info("Parsing OK");

        ASPKnowledgeBase kb = listener.getKnowledgeBase();
        String claspPath = getOptionValue(args, "--clasp", "Clasp path is not specified! Use --clasp=<path> option.");
        String scoring = getOptionValue(args, "--scoring", "minscore", "Invalid scoring function. 1 - ");
        ASPSolver solver = new ASPSolver(claspPath);
        ASPTheory theory = new ASPTheory(solver, kb);

        Application app = new Application(theory);
        app.setScoring(scoring);
        app.start();


    }

    private void start() throws SolverException {
        Set<FormulaSet<String>> diagnoses = new TreeSet<FormulaSet<String>>();

        boolean terminate = false;
        boolean firstRun = true;
        while (!terminate) {
            diagnoses = getDiagnoses(diagnoses, 9);
            Partition<String> query = getQuery(diagnoses);
            // return the most probable diagnosis in case there is no query
            if (query == null) {
                System.out.println(diagnoses.iterator().next());
                break;
            }

            // ask query and update
            if (firstRun) {
                System.out.println("Classification for queries: 1 - cautiously true, " +
                        "2 - cautiously false, 3 - bravely true, 4 - bravely false");
                firstRun = false;
            }
            final Set<String> queryAtoms = extractAtoms(query);
            int classification = askQuery(queryAtoms);
            // update test cases
            updateTestCases(queryAtoms, classification);
            // check termination criteria

        }

    }

    private void updateTestCases(Set<String> queryAtoms, int classification) {
        Set<String> testCase = new HashSet<String>();
        Collection<Set<String>> tests = null;
        for (String id : queryAtoms) {
            switch (classification) {
                // cautiously true
                case 1:
                    testCase.add("int(" + id + ")");
                    if (tests == null) tests = this.theory.getKnowledgeBase().getPositiveTests();
                    break;
                // cautiously false
                case 2:
                    testCase.add("-int(" + id + ")");
                    if (tests == null) tests = this.theory.getKnowledgeBase().getNegativeTests();
                    break;
                // bravely true
                case 3:
                    testCase.add("int(" + id + ")");
                    if (tests == null) tests = this.theory.getKnowledgeBase().getNegativeTests();
                    break;
                // bravely false
                case 4:
                    testCase.add("-int(" + id + ")");
                    if (tests == null) tests = this.theory.getKnowledgeBase().getPositiveTests();
                    break;
                default:
                    throw new IllegalArgumentException("Classification is unknown!");
            }
        }
        if (tests == null) throw new IllegalStateException("A test case cannot be added to a knowledge base!");
        tests.add(testCase);
    }

    private Set<String> extractAtoms(Partition<String> query) {
        Set<String> res = new HashSet<String>(query.partition.size());
        for (String atom : query.partition) {
            res.add(atom.substring(atom.indexOf("(")+1, atom.indexOf(")")));
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


    public Scoring<String> setScoring(String func) {
        if ("split".equals(func)) this.scoring = new SplitInHalfQSS<String>();
        if ("dynamic".equals(func)) this.scoring = new DynamicRiskQSS<String>(0, 0.4, 0.5);
        if ("minscore".equals(func)) this.scoring = new MinScoreQSS<String>();
        throw new IllegalArgumentException("Scoring function " + func + " is unknown!");
    }

    public Scoring<String> getScoring() {
        return scoring;
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


    private Set<FormulaSet<String>> getDiagnoses(Set<FormulaSet<String>> diagnoses, int number)
            throws SolverException {

        ASPSolver solver = theory.getReasoner();
        ASPKnowledgeBase kb = theory.getASPKnowledgeBase();

        // clear cache and add all relevant parts of the program, namely
        // program, background knowledge and positive test cases
        solver.clearFormulasCache();
        solver.addFormulasToCache(kb.getFaultyFormulas());
        solver.addFormulasToCache(kb.getBackgroundFormulas());
        for (Set<String> testCase : kb.getPositiveTests()) {
            solver.addFormulasToCache(testCase);
        }

        final List<Set<String>> diagnosisCandidates = computeDiagnoses(theory, number);

        for (Set<String> diag : diagnosisCandidates) {
            final Set<String> entailments = theory.getEntailments(diag);
            FormulaSet<String> diagnosis =
                    new FormulaSetImpl<String>(costsEstimator.getFormulaSetCosts(diag), diag, entailments);
            diagnoses.add(diagnosis);
        }
        return diagnoses;
    }

    private List<Set<String>> computeDiagnoses(ASPTheory theory, int number) {
        ASPSolver solver = theory.getReasoner();
        ASPKnowledgeBase kb = theory.getASPKnowledgeBase();
        final List<Set<String>> diagnosisCandidates = new LinkedList<Set<String>>();

        while (diagnosisCandidates.size() < number) {
            diagnosisCandidates.addAll(solver.computeDiagnoses(number - diagnosisCandidates.size()));

            // verify if the returned candidates are consistent with negative test cases
            for (Iterator<Set<String>> it = diagnosisCandidates.iterator(); it.hasNext(); ) {
                final Set<String> candidate = it.next();
                if (!theory.diagnosisCandidateConsistent(candidate)) {
                    // block candidates that are not diagnoses due to negative test cases
                    kb.addFormulas(Collections.singleton(generateConstraint(candidate)));
                    it.remove();
                }
            }
        }
        return diagnosisCandidates;
    }

    private String generateConstraint(Set<String> atoms) {
        StringBuilder constraint = new StringBuilder(":- ");
        for (Iterator<String> iterator = atoms.iterator(); iterator.hasNext(); ) {
            String atom = iterator.next();
            constraint.append(atom);
            if (iterator.hasNext()) constraint.append(",");
            else constraint.append(".");
        }
        return constraint.toString();
    }

    private static List<Path> getFiles(String[] args) {
        List<Path> res = new LinkedList<Path>();

        for (String arg : args) {
            if (!arg.startsWith("--")) {
                try {
                    final Path path = Paths.get(arg);
                } catch (InvalidPathException e) {
                    logger.error("Option value " + arg + " is not a path!");
                    System.exit(1);
                }
            }
        }
        return res;
    }

    private static String getOptionValue(String[] args, String option, String defaultValue, String errorMessage) {
        for (String arg : args) {
            if (arg.startsWith(option))
                return arg.substring(option.length() + 1);
        }
        return defaultValue;
    }

    private static String getOptionValue(String[] args, String option, String errorMessage) {
        for (String arg : args) {
            if (arg.startsWith(option))
                return arg.substring(option.length() + 1);
        }
        logger.error(errorMessage);
        System.exit(1);
        return null;
    }


}
