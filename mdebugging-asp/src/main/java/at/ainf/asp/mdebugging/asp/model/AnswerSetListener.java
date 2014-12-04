package at.ainf.asp.mdebugging.asp.model;

import java.util.ArrayList;
import java.util.List;

import at.ainf.asp.antlr.AnswerSetBaseListener;
import at.ainf.asp.antlr.AnswerSetParser.AnswerSetContext;
import at.ainf.asp.antlr.AnswerSetParser.OptimumFoundContext;
import at.ainf.asp.antlr.AnswerSetParser.SatisfiableContext;
import at.ainf.asp.antlr.AnswerSetParser.UnknownContext;
import at.ainf.asp.antlr.AnswerSetParser.UnsatisfiableContext;

/**
 * @author Melanie Frühstück
 *
 */
public class AnswerSetListener extends AnswerSetBaseListener {

	private List<AnswerSet> _answerSetList;
	private AnswerSet _answerSet;
	
	public AnswerSetListener() {
		_answerSetList = new ArrayList<AnswerSet>();
	}
	
	@Override
	public void enterOptimumFound(OptimumFoundContext ctx) {
		_answerSet.setState(AnswerSet.SATISFIABLE);
	}

	@Override
	public void enterSatisfiable(SatisfiableContext ctx) {
		_answerSet.setState(AnswerSet.SATISFIABLE);
	}

	@Override
	public void enterUnsatisfiable(UnsatisfiableContext ctx) {
		_answerSet = new AnswerSet();		
		_answerSet.setState(AnswerSet.UNSATISFIABLE);
		_answerSetList.add(_answerSet);
	}

	@Override
	public void enterUnknown(UnknownContext ctx) {
		_answerSet = new AnswerSet();	
		_answerSet.setState(AnswerSet.UNKNOWN);
		_answerSetList.add(_answerSet);
	}

	@Override
	public void enterAnswerSet(AnswerSetContext ctx) {
		String answerSet = ctx.getText();
		_answerSet = new AnswerSet();
		_answerSet.setAnswerSet(answerSet);
		_answerSetList.add(_answerSet);
	}

	/**
	 * Returns the set of answer sets.
	 * @return the set of answer sets
	 */
	public List<AnswerSet> getAnswerSets() {
		return _answerSetList;
	}
	
}
