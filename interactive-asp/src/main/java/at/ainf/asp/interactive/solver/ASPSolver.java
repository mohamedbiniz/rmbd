package at.ainf.asp.interactive.solver;

import at.ainf.asp.antlr.IntASPInputLexer;
import at.ainf.asp.antlr.IntASPInputParser;
import at.ainf.asp.antlr.IntASPOutputLexer;
import at.ainf.asp.antlr.IntASPOutputParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Calling ASP solver and providing its output
 */
public class ASPSolver {


    private static Logger logger = LoggerFactory.getLogger(ASPSolver.class.getName());

    private final String clingoPath;
    private BlockingQueue<String> lines = new LinkedBlockingQueue<String>();

    public ASPSolver(String clingoPath) {
        this.clingoPath = clingoPath;
    }

    private class ExecHandler extends LineOutputStream {

        ExecHandler(int bufferSize){
            super(bufferSize);
        }
        @Override
        protected void processLine(String line) {
            addLine(line);
        }
    }

    protected String getLine() throws InterruptedException {
        return lines.poll(10, TimeUnit.MILLISECONDS);
    }

    private void addLine(String line) {
        this.lines.add(line);
    }

    public int executeSolver(String program, ParseTreeListener outputListener, String... options)
            throws IOException, InterruptedException {

        return executeSolver(program, "", outputListener, options);
    }

    public int executeSolver(String program, String programExtension, ParseTreeListener outputListener, String... options)
            throws IOException, InterruptedException {
        List<String> cmd = new LinkedList<String>();
        cmd.add(this.clingoPath);
        Collections.addAll(cmd, options);

        CommandLine cl = new CommandLine(cmd.remove(0));
        for (String s : cmd) {
            cl.addArgument(s);
        }


        Executor exec = new DefaultExecutor();
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);

        ExecHandler out = new ExecHandler(8000);
        exec.setStreamHandler(new PumpStreamHandler(out, null, pis));

        DefaultExecuteResultHandler solver = new DefaultExecuteResultHandler();
        if (logger.isDebugEnabled()) logger.debug("Running clasp " + cl.toString());
        exec.execute(cl, solver);

        // provide the program to the solver input if there is no program (file) to process
        Writer pw = new BufferedWriter(new OutputStreamWriter(pos));
        pw.write(program);
        pw.write(programExtension);
        pw.close();

        // wait for the solver to exit
        // wait for the solver to exit and process the results

        while (!solver.hasResult() || !this.lines.isEmpty()) {
            String ln = getLine();
            if (ln != null){
                ANTLRInputStream input = new ANTLRInputStream(ln);
                IntASPOutputLexer lexer = new IntASPOutputLexer(input);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                IntASPOutputParser parser = new IntASPOutputParser(tokens);
                ParseTree tree = parser.parse();
                ParseTreeWalker.DEFAULT.walk(outputListener, tree);
            }
        }

        this.lines.clear();
        //solver.waitFor();
        return solver.getExitValue();
    }
}
