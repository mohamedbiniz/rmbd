/**
 * 
 */
package at.ainf.asp.model;

import at.ainf.asp.inputoutputactions.ASPConverter;
import at.ainf.asp.inputoutputactions.StreamGobbler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import at.ainf.asp.antlr.ASPOutputLexer;
import at.ainf.asp.antlr.ASPOutputParser;
import at.ainf.asp.antlr.ASPProgramLexer;
import at.ainf.asp.antlr.ASPProgramParser;

import java.io.IOException;


import java.util.Set;

/**
 * @author Melanie Fruehstueck
 *
 */
public class ASPSolver {

    public boolean solve(Set<IProgramElement> program) {
        boolean retVal = false;
        Process proc = null;
        Runtime rt = Runtime.getRuntime();

        System.out.println("\nProgram executed: ");
        for (IProgramElement pe : program) {
            System.out.println(pe.getString());
        }

        ASPConverter con = new ASPConverter();
        String filePath = "";
        try {
            filePath = con.write(program);
        } catch (IOException e) {
            System.out.println("Writing to file failed: " + e);
        }

        String[] cmd;
        if (System.getProperty("os.name").startsWith("Windows"))
            cmd = new String[]{ "cmd", "/c", "gringo " + filePath + " | clasp -q" };
        else
            cmd = new String[]{ "/bin/sh", "-c", "gringo " + filePath + " | clasp -q" };

        try {
            proc = rt.exec(cmd);
        } catch (IOException e) {
            System.out.println("Exception within execution of command: " + e);
        }

        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
        errorGobbler.start();
        outputGobbler.start();

        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Thread is waiting, sleeping, or otherwise occupied: " + e);
        }

        try {
            errorGobbler.join();
            outputGobbler.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String output = outputGobbler.getOutput();

        // parse output
        ANTLRInputStream input = new ANTLRInputStream(output);
        ASPOutputLexer lexer = new ASPOutputLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ASPOutputParser parser = new ASPOutputParser(tokens);
        ParseTree tree = parser.output();
        OutputListener pl = new OutputListener();
        ParseTreeWalker.DEFAULT.walk(pl, tree);

        //get output
        ASPOutput o = ASPOutput.getASPOutputInstance();

        if (o.isSatisfiabl()) {
            retVal = true;
        }
        System.err.println("sat test: " + retVal);
        return retVal;
    }


}
