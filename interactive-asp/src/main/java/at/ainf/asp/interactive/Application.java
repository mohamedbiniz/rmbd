package at.ainf.asp.interactive;

import at.ainf.asp.antlr.IntASPInputLexer;
import at.ainf.asp.antlr.IntASPInputParser;
import at.ainf.asp.interactive.input.IntASPInput;
import at.ainf.asp.interactive.solver.ASPKnowledgeBase;
import at.ainf.asp.interactive.solver.ASPSolver;
import at.ainf.asp.interactive.solver.ASPTheory;
import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.tree.SimpleCostsEstimator;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main class for interactive debugger prototype
 */
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class.getName());

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
        ASPSolver solver = new ASPSolver(claspPath);
        ASPTheory theory = new ASPTheory(solver, kb);

        Set<FormulaSet<String>> diagnoses = new HashSet<FormulaSet<String>>();
        diagnoses = getDiagnoses(theory,  new SimpleCostsEstimator<String>(), diagnoses, 9);

        Partitioning<String> queryGenerator = new CKK<String>(theory, QSSFactory.<String>createMinScoreQSS());

    }

    private static Set<FormulaSet<String>> getDiagnoses(ASPTheory theory, CostsEstimator<String> costsEstimator,
                                                        Set<FormulaSet<String>> diagnoses, int number)
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

        final List<Set<String>> diagnosisCandidates = solver.computeDiagnoses(number);

        // verify if the returned candidates are consistent with negative test cases
        for (Iterator<Set<String>> it = diagnosisCandidates.iterator(); it.hasNext();) {
            final Set<String> diag = it.next();
            if (!theory.diagnosisCandidateConsistent(diag)){

                // TODO block diagnosis
                it.remove();
            }
        }

        for (Set<String> diag : diagnosisCandidates) {

            final Set<String> entailments = theory.getEntailments(diag);
            FormulaSet<String> diagnosis =
                    new FormulaSetImpl<String>(costsEstimator.getFormulaSetCosts(diag), diag, entailments);
            diagnoses.add(diagnosis);
        }
        return diagnoses;
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

    private static String getOptionValue(String[] args, String option, String errorMessage) {
        for (String arg : args) {
            if (arg.startsWith(option))
                return arg.substring(option.length() + 1);
        }
        logger.error(errorMessage);
        System.exit(1);
        return null;
    }

    private static String getOptionValue(String arg, String option) {

        return null;
    }


}
