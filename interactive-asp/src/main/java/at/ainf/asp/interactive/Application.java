package at.ainf.asp.interactive;

import at.ainf.asp.antlr.IntASPInputLexer;
import at.ainf.asp.antlr.IntASPInputParser;
import at.ainf.asp.interactive.input.IntASPDiagnosisListener;
import at.ainf.asp.interactive.input.IntASPInput;
import at.ainf.asp.interactive.input.IntASPInterpretationListener;
import at.ainf.asp.interactive.solver.ASPSolver;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Main class for interactive debugger prototype
 */
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {

        InputStream stream;
        if (args.length > 0) {
            stream = new FileInputStream(args[0]);
        } else
            stream = System.in;
        IntASPInput listener = new IntASPInput();
        parseInput(stream, listener);

        ASPSolver solver = new ASPSolver(args[1]);
        IntASPDiagnosisListener diagnoses = new IntASPDiagnosisListener();
        String program = listener.getProgram();
        solver.executeSolver(program, diagnoses, "--opt-mode=optN", "--quiet=1,1", "--number=" + 9);
        for (List<String> diag : diagnoses.getDiagnoses()) {
            String programExtension = generateDiagnosisProgram(program, diag);
            IntASPInterpretationListener entailments = new IntASPInterpretationListener();
            solver.executeSolver(program, programExtension, entailments, "--enum-mode=cautious", "--quiet=1,1");
        }


    }

    private static String generateDiagnosisProgram(String program, List<String> diag) {
        StringBuffer ext = new StringBuffer();

        return ext.toString();
    }

    private static void parseInput(InputStream stream, ParseTreeListener listener) throws IOException {
        ANTLRInputStream input = new ANTLRInputStream(stream);
        IntASPInputLexer lexer = new IntASPInputLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        IntASPInputParser parser = new IntASPInputParser(tokens);
        ParseTree tree = parser.parse();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        logger.info("Parsing OK");
    }

}
