package at.ainf.asp.interactive;

import at.ainf.asp.antlr.IntASPInputLexer;
import at.ainf.asp.antlr.IntASPInputParser;
import at.ainf.asp.interactive.input.IntASPInput;
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

/**
 * Main class for interactive debugger prototype
 */
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class.getName());

    public static void main(String[] args) throws IOException {

        InputStream stream;
        if (args.length > 0) {
            stream = new FileInputStream(args[0]);
        } else
            stream = System.in;
        IntASPInput listener = new IntASPInput();
        parseInput(stream, listener);

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
