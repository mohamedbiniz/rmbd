package at.ainf.asp.model;

import at.ainf.asp.antlr.ASPOutputBaseListener;
import at.ainf.asp.antlr.ASPOutputParser.SatisfiableContext;
import at.ainf.asp.antlr.ASPOutputParser.UnknownContext;
import at.ainf.asp.antlr.ASPOutputParser.UnsatisfiableContext;

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
		output.setUnknown(false);
	}

	@Override
	public void enterUnsatisfiable(UnsatisfiableContext ctx) {
		output.setSatisfiable(false);
		output.setUnknown(false);
	}

	@Override
	public void enterUnknown(UnknownContext ctx) {
		output.setUnknown(true);
	}

}
