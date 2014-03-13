package at.ainf.asp.interactive.solver;

import at.ainf.asp.antlr.IntASPOutputLexer;
import at.ainf.asp.antlr.IntASPOutputParser;
import at.ainf.asp.interactive.input.ASPListener;
import at.ainf.asp.interactive.input.IntASPDiagnosisListener;
import at.ainf.asp.interactive.input.IntASPInterpretationListener;
import at.ainf.diagnosis.model.IReasoner;
import at.ainf.diagnosis.model.KnowledgeBase;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
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
public class ASPSolver implements IReasoner<String> {


    private static Logger logger = LoggerFactory.getLogger(ASPSolver.class.getName());

    private final String clingoPath;
    private BlockingQueue<String> lines = new LinkedBlockingQueue<String>();

    private Set<String> program;
    private ASPListener listener;
    private List<String> options = new LinkedList<String>();
    private Boolean result = null;

    public void setProgram(Set<String> program) {
        resetReasoningCache();
        this.program = program;
    }

    private void resetReasoningCache() {
        this.result = null;
    }

    public void setListener(ASPListener listener) {
        resetReasoningCache();
        this.listener = listener;
    }

    public void setOptions(String... options) {
        resetReasoningCache();
        this.options.clear();
        Collections.addAll(this.options, options);
    }

    public ASPSolver(String clingoPath) {
        this.clingoPath = clingoPath;
    }

    @Override
    public boolean isConsistent() {
        if (this.result != null)
            return this.result;

        executeSolver();
        this.result = this.listener.hasResult();
        return this.result;
    }

    @Override
    public boolean isCoherent() {
        return isConsistent();
    }


    @Override
    public boolean isEntailed(Set<String> test) {
        return false;
    }

    @Override
    public IReasoner<String> newInstance() {
        return new ASPSolver(clingoPath);
    }

    public List<Set<String>> computeDiagnoses(KnowledgeBase<String> kb, int diagnosesNumber) throws IOException, InterruptedException {
        Set<String> program = new HashSet<String>(kb.getFaultyFormulas());
        program.addAll(kb.getBackgroundFormulas());
        setProgram(program);
        final IntASPDiagnosisListener lst = new IntASPDiagnosisListener();
        setListener(lst);
        setOptions("--opt-mode=optN", "--quiet=1,1", "--number=" + diagnosesNumber);
        executeSolver();
        return lst.getDiagnoses();
    }

    public Set<String> computeEntailments(KnowledgeBase<String> kb, Set<String> errorAtoms, Set<String> diagnosis)
            throws IOException, InterruptedException {

        Set<String> program = new HashSet<String>(kb.getFaultyFormulas());
        program.addAll(kb.getBackgroundFormulas());
        program.addAll(generateDiagnosisProgram(errorAtoms, diagnosis));
        setProgram(program);
        return getEntailments();
    }

    @Override
    public Set<String> getEntailments() {
        final IntASPInterpretationListener lst = new IntASPInterpretationListener();
        setListener(lst);
        setOptions("--enum-mode=cautious", "--quiet=1,1", "--opt-mode=ignore");
        executeSolver();
        final List<Set<String>> interpretations = lst.getInterpretations();
        if (interpretations.size() > 1)
            throw new IllegalStateException("Solver returned many intersections of all interpretations");
        if (interpretations.isEmpty()) return Collections.emptySet();
        return interpretations.get(0);
    }

    private Set<String> generateDiagnosisProgram(Set<String> errorAtoms, Set<String> diagnosis) {
        Set<String> ext = new HashSet<String>(errorAtoms.size());
        Set<String> remAtoms = new HashSet<String>(errorAtoms);
        remAtoms.removeAll(diagnosis);
        for (String atom : remAtoms) {
            ext.add(":- " + atom + ".\n");
        }
        return ext;
    }


    private class ExecHandler extends LineOutputStream {
        ExecHandler(int bufferSize) {
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

    public int executeSolver() {
        List<String> cmd = new LinkedList<String>();
        cmd.add(this.clingoPath);
        cmd.addAll(options);

        CommandLine cl = new CommandLine(cmd.remove(0));
        for (String s : cmd) {
            cl.addArgument(s);
        }


        DefaultExecuteResultHandler solver = null;
        try {
            Executor exec = new DefaultExecutor();
            PipedOutputStream pos = new PipedOutputStream();
            PipedInputStream pis = new PipedInputStream(pos);

            ExecHandler out = new ExecHandler(8000);
            exec.setStreamHandler(new PumpStreamHandler(out, null, pis));

            solver = new DefaultExecuteResultHandler();
            if (logger.isDebugEnabled()) logger.debug("Running clasp " + cl.toString());
            exec.execute(cl, solver);

            // provide the program to the solver input if there is no program (file) to process
            Writer pw = new BufferedWriter(new OutputStreamWriter(pos));
            for (String rule : this.program) {
                pw.write(rule);
            }
            pw.close();

            // wait for the solver to exit
            // wait for the solver to exit and process the results

            while (!solver.hasResult() || !this.lines.isEmpty()) {
                String ln = getLine();
                if (ln != null) {
                    ANTLRInputStream input = new ANTLRInputStream(ln);
                    IntASPOutputLexer lexer = new IntASPOutputLexer(input);
                    CommonTokenStream tokens = new CommonTokenStream(lexer);
                    IntASPOutputParser parser = new IntASPOutputParser(tokens);
                    ParseTree tree = parser.parse();
                    ParseTreeWalker.DEFAULT.walk(listener, tree);
                }
            }
        } catch (IOException e) {
            logger.error("Exception occurred while calling a reasoner!", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error("Exception occurred while calling a reasoner!", e);
            throw new RuntimeException(e);
        }

        this.lines.clear();
        //solver.waitFor();
        return solver.getExitValue();
    }
}
