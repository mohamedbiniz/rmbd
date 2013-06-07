package at.ainf.asp.model;

import java.util.Collections;

import at.ainf.asp.antlr.ASPProgramBaseListener;
import at.ainf.asp.antlr.ASPProgramParser.AspfactContext;
import at.ainf.asp.antlr.ASPProgramParser.AspruleContext;

/**
 * @author Melanie Fruehstueck
 *
 */
public class ProgramListener extends ASPProgramBaseListener {
	
	private ASPModel model;
	
	public ProgramListener() {
		model = ASPModel.getASPModelInstance();
	}
	
	@Override
	public void enterAspfact(AspfactContext ctx) {
		String fact = ctx.getText();
		System.out.println("Found fact: " + fact);
		Fact aspFact = new Fact(fact);
		model.addFact(aspFact);
	}

	@Override
	public void enterAsprule(AspruleContext ctx) {
		String rule = ctx.getText(); 
		System.out.println("Found rule: " + rule);
		Rule aspRule = new Rule(rule);
		model.addRule(aspRule);
	}

}
