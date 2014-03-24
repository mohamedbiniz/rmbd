package at.ainf.asp.interactive.input;

import at.ainf.asp.antlr.IntASPInputBaseListener;
import at.ainf.asp.antlr.IntASPInputListener;
import at.ainf.asp.antlr.IntASPInputParser;
import at.ainf.asp.interactive.solver.ASPKnowledgeBase;
import at.ainf.diagnosis.model.KnowledgeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Input parser that creates a knowledge base from the input file
 */
public class IntASPInput extends IntASPInputBaseListener implements IntASPInputListener, ASPListener {

    private static Logger logger = LoggerFactory.getLogger(IntASPInput.class.getName());

    private Set<String> atomIDs = new HashSet<String>();
    private Set<String> ruleIDs = new HashSet<String>();

    private ASPKnowledgeBase kb = new ASPKnowledgeBase();

    @Override
    public void enterParse(IntASPInputParser.ParseContext ctx) {
        this.kb = new ASPKnowledgeBase();
    }

    @Override
    public boolean hasResult() {
        throw new RuntimeException("This listener cannot be used for diagnostic reasoning!");
    }

    @Override
    public void reset() {
        this.kb = new ASPKnowledgeBase();
        atomIDs.clear();
        ruleIDs.clear();
    }

    public enum Mode {ASP, BK, BT, BF, CT, CF}

    private Mode currentMode = Mode.ASP;

    public ASPKnowledgeBase getKnowledgeBase() {
        return this.kb;
    }

    @Override
    public void exitAspsection(IntASPInputParser.AspsectionContext ctx) {
        getKnowledgeBase().setErrorAtoms(getErrorAtoms());
    }

    private Set<String> getErrorAtoms() {
        Set<String> errorAtoms = new HashSet<String>(this.ruleIDs.size() * 2 + this.atomIDs.size() * 2);
        for (String ruleID : this.ruleIDs) {
            errorAtoms.add("unsatisfied(" + ruleID + ")");
            errorAtoms.add("violated(" + ruleID + ")");
        }
        for (String atomID : this.atomIDs) {
            errorAtoms.add("ufLoop(" + atomID + ")");
            errorAtoms.add("unsupported(" + atomID + ")");
        }
        return errorAtoms;
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
        getKnowledgeBase().addFormulas(Collections.singleton(ctx.getText()));
    }

    @Override
    public void enterCf(IntASPInputParser.CfContext ctx) {
        testCase = new LinkedHashSet<String>();
        setCurrentMode(Mode.CF);
    }

    @Override
    public void enterCt(IntASPInputParser.CtContext ctx) {
        testCase = new LinkedHashSet<String>();
        setCurrentMode(Mode.CT);
    }

    @Override
    public void enterBt(IntASPInputParser.BtContext ctx) {
        testCase = new LinkedHashSet<String>();
        setCurrentMode(Mode.BT);
    }

    @Override
    public void enterBk(IntASPInputParser.BkContext ctx) {
        setCurrentMode(Mode.BK);
    }

    @Override
    public void enterBf(IntASPInputParser.BfContext ctx) {
        testCase = new LinkedHashSet<String>();
        setCurrentMode(Mode.BF);
    }

    private Set<String> testCase;

    @Override
    public void enterValue(IntASPInputParser.ValueContext ctx) {
        String id = ctx.getText();
        switch (getCurrentMode()) {
            case BK:
                Set<String> bk = new LinkedHashSet<String>();
                bk.add(":- rule(" + id + "), violated(" + id + ").\n");
                bk.add(":- rule(" + id + "), unsatisfied(" + id + ").\n");
                getKnowledgeBase().addBackgroundFormulas(bk);
                break;

            case CT:
                testCase.add("int(" + id + ")");
                break;
            case CF:
                testCase.add("-int(" + id + ")");
                break;
            case BT:
                testCase.add("int(" + id + ")");
                break;
            case BF:
                testCase.add("-int(" + id + ")");
        }
    }

    @Override
    public void exitCt(IntASPInputParser.CtContext ctx) {
        getKnowledgeBase().addPositiveTest(testCase);
    }

    @Override
    public void exitBt(IntASPInputParser.BtContext ctx) {
        getKnowledgeBase().addNegativeTest(testCase);
    }

    @Override
    public void exitBf(IntASPInputParser.BfContext ctx) {
        getKnowledgeBase().addPositiveTest(testCase);
    }

    @Override
    public void exitCf(IntASPInputParser.CfContext ctx) {
        getKnowledgeBase().addNegativeTest(testCase);
    }
}
