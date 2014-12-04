package at.ainf.asp.model;

import at.ainf.asp.antlr.ASPProgramBaseListener;
import at.ainf.asp.antlr.ASPProgramParser.AspfactContext;
import at.ainf.asp.antlr.ASPProgramParser.AspruleContext;
import at.ainf.asp.antlr.ASPProgramParser.DefaultNegationNOTContext;
import at.ainf.asp.antlr.ASPProgramParser.EvenConstraintContext;
import at.ainf.asp.antlr.ASPProgramParser.MinConstraintContext;
import at.ainf.asp.antlr.ASPProgramParser.OddConstraintContext;
import at.ainf.asp.antlr.ASPProgramParser.UpperBoundCardinalityNOTContext;
import at.ainf.asp.main.Application;
import at.ainf.asp.model.exceptions.NotMonotoneException;

/**
 * @author Melanie Frühstück
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
		if (Application.enableInfo) {
			System.out.println("Found fact: " + fact);
		}
		Fact aspFact = new Fact(fact);
		model.addFact(aspFact);
	}

	@Override
	public void enterAsprule(AspruleContext ctx) {
		String rule = ctx.getText();
		if (Application.enableInfo) {
			System.out.println("Found rule: " + rule);
		}
		Rule aspRule = new Rule(rule);
		model.addRule(aspRule);
	}

	@Override
	public void enterUpperBoundCardinalityNOT(
			UpperBoundCardinalityNOTContext ctx) {
		try {
			throw new NotMonotoneException("There are cardinality constraints with upper bounds! \nThese constructs are not allowed in monotone answer set programs.");
		} catch (NotMonotoneException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void enterDefaultNegationNOT(
			DefaultNegationNOTContext ctx) {
		try {
			throw new NotMonotoneException("There is default negation in the program! \nThis construct is not allowed in monotone answer set programs.");
		} catch (NotMonotoneException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void enterMinConstraint(MinConstraintContext ctx) {
		try {
			throw new NotMonotoneException("There are #min constraints in the program! \nThese constructs are not allowed in monotone answer set programs.");
		} catch (NotMonotoneException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void enterOddConstraint(OddConstraintContext ctx) {
		try {
			throw new NotMonotoneException("There are #odd constraints in the program! \nThese constructs are not allowed in monotone answer set programs.");
		} catch (NotMonotoneException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void enterEvenConstraint(EvenConstraintContext ctx) {
		try {
			throw new NotMonotoneException("There are #even constraints in the program! \nThese constructs are not allowed in monotone answer set programs.");
		} catch (NotMonotoneException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
