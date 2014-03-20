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
import at.ainf.diagnosis.partitioning.scoring.QSS;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.quickxplain.QuickXplain;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.Partition;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.EqualCostsEstimator;
import at.ainf.diagnosis.tree.Rounding;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import at.ainf.diagnosis.tree.exceptions.NoConflictException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
    private CostsEstimator<String> costsEstimator;
    private QSS<String> qss = null;
    private int maxDiagnosesNumber = 9;
    private BigDecimal SIGMA = new BigDecimal("100");


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
        addDebuggingExtension(kb);

        String claspPath = getOptionValue(args, "--clasp", "Clasp path is not specified! Use --clasp=<path> option.");
        ASPSolver solver = new ASPSolver(claspPath);
        ASPTheory theory = new ASPTheory(solver, kb);

        Application app = new Application(theory);

        String scoring = getOptionValueDefault(args, "--scoring", "minscore");
        String estimator = getOptionValueDefault(args, "--estimator", "equal");
        int diagnosesNumber = getOptionValueAsInt(args, "--n", 9);

        app.setScoring(scoring);
        app.setCostsEstimator(estimator);
        app.setMaxDiagnosesNumber(diagnosesNumber);

        app.start();


    }

    private static void addDebuggingExtension(ASPKnowledgeBase kb) {
        // add projections and minimization statements to a program
        try {
            URI path = ClassLoader.getSystemResource("extension.lp").toURI();
            kb.addBackgroundFormulas(
                    Collections.singleton(Charset.defaultCharset().decode(
                            ByteBuffer.wrap(Files.readAllBytes(Paths.get(path)))).toString())
            );
        } catch (IOException e) {
            logger.error("Resources are not found!", e);
            throw new RuntimeException("Resources are not found!");
        } catch (URISyntaxException e) {
            logger.error("Resources are not found!", e);
            throw new RuntimeException("Resources are not found!");
        }
    }

    private void start() throws SolverException {
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
            this.theory.getKnowledgeBase().getPositiveTests().add(testCase);
        else
            this.theory.getKnowledgeBase().getNegativeTests().add(testCase);
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


    private TreeSet<FormulaSet<String>> getDiagnoses(TreeSet<FormulaSet<String>> diagnoses, int number)
            throws SolverException {

        ASPSolver solver = theory.getReasoner();
        ASPKnowledgeBase kb = theory.getASPKnowledgeBase();
        final Set<FormulaSet<String>> diagnosisCandidates = new LinkedHashSet<FormulaSet<String>>();

        final int diagnosesNumber = number - diagnoses.size();

        while (true) {

            // clear cache and add all relevant parts of the program, namely
            // program, background knowledge and positive test cases
            solver.clearFormulasCache();
            solver.addFormulasToCache(solver.generateDebuggingProgram(kb));

            final Set<FormulaSet<String>> formulaSets = solver.computeDiagnoses(diagnosesNumber, this.costsEstimator);

            // verify if the returned candidates are consistent with negative test cases
            for (Iterator<FormulaSet<String>> it = formulaSets.iterator(); it.hasNext(); ) {
                final Set<String> candidate = it.next();
                // block found candidate by a constraint
                kb.addBackgroundFormulas(Collections.singleton(solver.generateConstraint(candidate)));
                if (!theory.verifyTestCasesForDiagnosisCandidate(candidate)) {
                    it.remove();
                }
            }

            diagnosisCandidates.addAll(formulaSets);
            if (diagnosisCandidates.size() >= diagnosesNumber || formulaSets.isEmpty())
                break;
        }

        for (FormulaSet<String> diagnosis : diagnosisCandidates) {
            diagnosis.setEntailments(theory.getEntailments(diagnosis));
            diagnoses.add(diagnosis);
        }

        theory.doBayesUpdate(diagnoses);

        return diagnoses;
    }

    private static List<Path> getFiles(String[] args) {
        List<Path> res = new LinkedList<Path>();

        for (String arg : args) {
            if (!arg.startsWith("--")) {
                try {
                    final Path path = Paths.get(arg);
                    res.add(path);
                } catch (InvalidPathException e) {
                    logger.error("The value " + arg + " is not a path!");
                    System.exit(1);
                }
            }
        }
        return res;
    }

    private static String getOptionValueDefault(String[] args, String option, String defaultValue) {
        for (String arg : args) {
            if (arg.startsWith(option))
                return arg.substring(option.length() + 1);
        }
        return defaultValue;
    }

    private static int getOptionValueAsInt(String[] args, String option, int defaultValue) {
        for (String arg : args) {
            if (arg.startsWith(option))
                try {
                    return Integer.parseInt(arg.substring(option.length() + 1));
                } catch (NumberFormatException e) {
                    logger.error(option + " expected an integer value!");
                    System.exit(1);
                }
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


    public int getMaxDiagnosesNumber() {
        return maxDiagnosesNumber;
    }

    public void setMaxDiagnosesNumber(int maxDiagnosesNumber) {
        this.maxDiagnosesNumber = maxDiagnosesNumber;
    }
}
