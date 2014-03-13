package at.ainf.asp.interactive.input;

import at.ainf.asp.antlr.IntASPInputBaseListener;
import at.ainf.asp.antlr.IntASPInputListener;
import at.ainf.asp.antlr.IntASPInputParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Kostya on 10.03.14.
 */
public class IntASPInput extends IntASPInputBaseListener implements IntASPInputListener {

    private static Logger logger = LoggerFactory.getLogger(IntASPInput.class.getName());
    private StringBuffer program = new StringBuffer();
    private List<List<String>> positive = new LinkedList<List<String>>();
    private List<List<String>> negative = new LinkedList<List<String>>();
    private Set<String> atomIDs = new HashSet<String>();
    private Set<String> ruleIDs = new HashSet<String>();

    public IntASPInput() {
        // add projections and minimization statements to a program
        try {
            URI path = ClassLoader.getSystemResource("extension.lp").toURI();
            program.append(
                    Charset.defaultCharset().decode(
                            ByteBuffer.wrap(
                                    Files.readAllBytes(Paths.get(path))
                            )));
        } catch (IOException e) {
            logger.error("Resources are not found!",e);
            throw new RuntimeException("Resources are not found!");
        } catch (URISyntaxException e) {
            logger.error("Resources are not found!",e);
            throw new RuntimeException("Resources are not found!");
        }
    }

    public enum Mode {ASP, BK, BT, BF, CT, CF}

    private Mode currentMode = Mode.ASP;

    public String getProgram() {
        return program.toString();
    }

    public List<List<String>> getPositive() {
        return positive;
    }

    public List<List<String>> getNegative() {
        return negative;
    }

    public Set<String> getErrorAtoms() {
        Set<String> errorAtoms = new HashSet<String>(this.ruleIDs.size()*2+this.atomIDs.size()*2);
        for (String ruleID : this.ruleIDs) {
            errorAtoms.add("unsatisfied("+ruleID+")");
            errorAtoms.add("violated("+ruleID+")");
        }
        for (String atomID : this.atomIDs) {
            errorAtoms.add("ufLoop("+atomID+")");
            errorAtoms.add("unsupported("+atomID+")");
        }
        return errorAtoms;
    }

    public Set<String> getAtomIDs() {
        return atomIDs;
    }

    private Mode getCurrentMode() {
        return currentMode;
    }

    private void setCurrentMode(Mode currentMode) {
        this.currentMode = currentMode;
    }

    @Override
    public void enterRuleid(IntASPInputParser.RuleidContext ctx) {
        this.ruleIDs.add(ctx.getText());
    }

    @Override
    public void enterAtomid(IntASPInputParser.AtomidContext ctx) {
        this.atomIDs.add(ctx.getText());
    }

    @Override
    public void enterAsprule(IntASPInputParser.AspruleContext ctx) {
        program.append(ctx.getText());
    }

    @Override
    public void enterCf(IntASPInputParser.CfContext ctx) {
        testCase = new LinkedList<String>();
        setCurrentMode(Mode.CF);
    }

    @Override
    public void enterCt(IntASPInputParser.CtContext ctx) {
        testCase = new LinkedList<String>();
        setCurrentMode(Mode.CT);
    }

    @Override
    public void enterBt(IntASPInputParser.BtContext ctx) {
        testCase = new LinkedList<String>();
        setCurrentMode(Mode.BT);
    }

    @Override
    public void enterBk(IntASPInputParser.BkContext ctx) {
        setCurrentMode(Mode.BK);
    }

    @Override
    public void enterBf(IntASPInputParser.BfContext ctx) {
        testCase = new LinkedList<String>();
        setCurrentMode(Mode.BF);
    }

    private List<String> testCase;

    @Override
    public void enterValue(IntASPInputParser.ValueContext ctx) {
        String id = ctx.getText();
        switch (getCurrentMode()){
            case BK:
                program.append(":- rule(").append(id).append("), violated(").append(id).append(").\n");
                program.append(":- rule(").append(id).append("), unsatisfied(").append(id).append(").\n");
                break;

            case CT:
                testCase.add("int(" + id + ")");
                break;
            case CF:
                testCase.add("-int("+id+")");
                break;
            case BT:
                testCase.add("int(" + id + ")");
                break;
            case BF:
                testCase.add("-int("+id+")");
        }
    }

    @Override
    public void exitCt(IntASPInputParser.CtContext ctx) {
        positive.add(testCase);
    }

    @Override
    public void exitBt(IntASPInputParser.BtContext ctx) {
        negative.add(testCase);
    }

    @Override
    public void exitBf(IntASPInputParser.BfContext ctx) {
        positive.add(testCase);
    }

    @Override
    public void exitCf(IntASPInputParser.CfContext ctx) {
        negative.add(testCase);
    }
}
