package at.ainf.asp.model;

import java.util.Collections;

import at.ainf.asp.antlr.ASPProgramBaseListener;
import at.ainf.asp.antlr.ASPProgramParser.AspfactContext;
import at.ainf.asp.antlr.ASPProgramParser.AspruleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Melanie Fruehstueck
 *
 */
public class ProgramListener extends ASPProgramBaseListener {


    private static Logger logger = LoggerFactory.getLogger(ASPSolver.class.getName());

	private ASPModel model;
	
	public ProgramListener() {
		model = ASPModel.getASPModelInstance();
	}
	
	@Override
	public void enterAspfact(AspfactContext ctx) {
		String fact = ctx.getText();
		logger.info("Found fact: " + fact);
		Fact aspFact = new Fact(fact);
		model.addFact(aspFact);
	}

	@Override
	public void enterAsprule(AspruleContext ctx) {
		String rule = ctx.getText(); 
		logger.info("Found rule: " + rule);
		Rule aspRule = new Rule(rule);
		model.addRule(aspRule);
	}

}
