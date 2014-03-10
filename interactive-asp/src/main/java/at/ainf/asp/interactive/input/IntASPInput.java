package at.ainf.asp.interactive.input;

import at.ainf.asp.antlr.IntASPInputBaseListener;
import at.ainf.asp.antlr.IntASPInputListener;
import at.ainf.asp.antlr.IntASPInputParser;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kostya on 10.03.14.
 */
public class IntASPInput extends IntASPInputBaseListener implements IntASPInputListener {

    public enum Mode {ASP, BK, BT, BF, CT, CF}

    private Mode currentMode = Mode.ASP;

    private StringBuffer program = new StringBuffer();
    private List<List<String>> positive = new LinkedList<List<String>>();
    private List<List<String>> negative = new LinkedList<List<String>>();

    public IntASPInput(){
        // add projections and minimization statements to a program
        String path = ClassLoader.getSystemResource("extension.lp").getPath();
        try {
            program.append(
                    Charset.defaultCharset().decode(
                            ByteBuffer.wrap(
                                    Files.readAllBytes(Paths.get(path))
                            )));
        } catch (IOException e) {
            throw new RuntimeException("Resources are not found!");
        }
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(Mode currentMode) {
        this.currentMode = currentMode;
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
                program.append(":- rule(").append(id).append("), violated(").append(id).append(").");
                program.append(":- rule(").append(id).append("), unsatisfied(").append(id).append(").");
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
