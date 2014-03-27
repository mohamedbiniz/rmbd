package at.ainf.asp.interactive;

import at.ainf.asp.antlr.IntASPInputLexer;
import at.ainf.asp.antlr.IntASPInputParser;
import at.ainf.asp.interactive.input.IntASPInput;
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

        String claspPath = getOptionValue(args, "--clingo", "Clingo (ver. 4 or higher) is not found! Use --clingo=<path> option.");
        ASPSolver solver = new ASPSolver(claspPath);
        InteractiveASPTheory theory = new InteractiveASPTheory(solver, kb);

        InteractiveSpock app = new InteractiveSpock(theory);

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
}
