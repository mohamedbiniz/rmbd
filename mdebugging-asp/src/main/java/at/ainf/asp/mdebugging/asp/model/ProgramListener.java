package at.ainf.asp.mdebugging.asp.model;

import at.ainf.asp.antlr.ProgramBaseListener;
import at.ainf.asp.antlr.ProgramParser.AspfactContext;
import at.ainf.asp.antlr.ProgramParser.AspruleContext;

/**
 * @author Melanie Frühstück
 *
 */
public class ProgramListener extends ProgramBaseListener {

	private Program _program;
	
	public ProgramListener() {
		_program = new Program();
	}
	
	@Override
	public void enterAspfact(AspfactContext ctx) {
		String fact = ctx.getText();
		Rule rule = new Rule(fact);
		_program.addFact(rule);
	}

	@Override
	public void enterAsprule(AspruleContext ctx) {
		String ruleString = ctx.getText();
		_program.addRuleWithLabel(ruleString);
	}
	
	/**
	 * Returns the program.
	 * @return the program
	 */
	public Program getProgram() {
		return _program;
	}
	
}
