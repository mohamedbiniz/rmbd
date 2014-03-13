package at.ainf.asp.interactive;

import at.ainf.asp.antlr.IntASPInputLexer;
import at.ainf.asp.antlr.IntASPInputParser;
import at.ainf.asp.interactive.input.IntASPDiagnosisListener;
import at.ainf.asp.interactive.input.IntASPInput;
import at.ainf.asp.interactive.solver.ASPSolver;
import at.ainf.asp.interactive.solver.ASPTheory;
import at.ainf.diagnosis.model.KnowledgeBase;
import at.ainf.diagnosis.partitioning.CKK;
import at.ainf.diagnosis.partitioning.Partitioning;
import at.ainf.diagnosis.partitioning.scoring.QSSFactory;
import at.ainf.diagnosis.storage.FormulaSet;
import at.ainf.diagnosis.storage.FormulaSetImpl;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Set;

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
        ANTLRInputStream input = new ANTLRInputStream(stream);
        IntASPInputLexer lexer = new IntASPInputLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        IntASPInputParser parser = new IntASPInputParser(tokens);
        ParseTree tree = parser.parse();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        KnowledgeBase<String> kb = listener.getKnowledgeBase();

        logger.info("Parsing OK");

        ASPSolver solver = new ASPSolver(args[1]);
        IntASPDiagnosisListener diagnoses = new IntASPDiagnosisListener();
        final Set<String> errorAtoms = listener.getErrorAtoms();
        solver.computeDiagnoses(kb, 9);
        ASPTheory theory = new ASPTheory();
        for (Set<String> diag : diagnoses.getDiagnoses()) {
            final Set<String> entailments = solver.computeEntailments(kb, errorAtoms, diag);
            FormulaSet<String> diagnosis =
                    new FormulaSetImpl<String>(BigDecimal.valueOf(0.001), diag, entailments);

        }

        Partitioning<String> queryGenerator = new CKK<String>(theory, QSSFactory.<String>createMinScoreQSS());

    }


}
