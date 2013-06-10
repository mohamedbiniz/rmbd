package at.ainf.asp.model;

import at.ainf.asp.antlr.ASPOutputBaseListener;
import at.ainf.asp.antlr.ASPOutputParser.OtherContext;
import at.ainf.asp.antlr.ASPOutputParser.SatisfiableContext;
import at.ainf.asp.antlr.ASPOutputParser.UnsatisfiableContext;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.06.13
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */
/**
 * @author Melanie Fruehstueck
 *
 */
public class OutputListener extends ASPOutputBaseListener {

    private ASPOutput output;

    public OutputListener() {
        output = ASPOutput.getASPOutputInstance();
    }

    @Override
    public void enterSatisfiable(SatisfiableContext ctx) {
        output.setSatisfiable(true);
    }

    @Override
    public void enterUnsatisfiable(UnsatisfiableContext ctx) {
        output.setSatisfiable(false);
    }

}
