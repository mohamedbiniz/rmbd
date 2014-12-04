package at.ainf.asp.mdebugging.asp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Melanie Frühstück
 *
 */
public class Program {

	private List<Rule> _rules;
	private List<Rule> _facts;
	private List<Rule> _labelConstraints;
	private int _labelCounter;
	
	public Program() {
		_rules = new ArrayList<Rule>();
		_facts = new ArrayList<Rule>();
		_labelConstraints = new ArrayList<Rule>();
		_labelCounter = 0;
	}
	
	/**
	 * Returns the list of rules (excluding facts).
	 * @return the list of rules
	 */
	public List<Rule> getRules() {
		return _rules;
	}
	
	/**
	 * Returns a rule according to its label (if there is a label for a rule).
	 * @param label
	 * @return the rule, or null if there is no label for the rule
	 */
	public Rule getRuleByLabel(String label) {
		for (Rule r : _rules) {
			if (label.equals(r.getLabel())) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Adds a rule to the list of rules.
	 * @param aspRule
	 */
	public void addRule(Rule aspRule) {
		this._rules.add(aspRule);
	}
	
	/**
	 * Adds a rule including its label to the list of rules.
	 * @param rule
	 */
	public void addRuleWithLabel(String rule) {
		Rule r = new Rule(rule,_labelCounter);
		_labelCounter++;
		this._rules.add(r);
	}
	
	/**
	 * Returns the list of facts of the program.
	 * @return the list of facts
	 */
	public List<Rule> getFacts() {
		return _facts;
	}

	/**
	 * Adds a fact to the list of facts.
	 * @param fact
	 */
	public void addFact(Rule fact) {
		this._facts.add(fact);
	}

	/**
	 * Returns the list of integrity constraints including the diagnoses (labels).
	 * @return the list of integrity constraints (labels)
	 */
	public List<Rule> getLabelConstraints() {
		return _labelConstraints;
	}

	/**
	 * Adds an integrity constraint to the list.
	 * @param labelConstraint
	 */
	public void addLabelConstraint(Rule labelConstraint) {
		this._labelConstraints.add(labelConstraint);
	}
	
	/**
	 * Adds the statements to the program (choice of labels, count rule and minimize rule).
	 * Is called when we already know all rules in the program.
	 */
	public void addAdditionalStatements() {
		this.addRule(new Rule("{ label(0.." + (this.getRules().size()-1) + ") }."));
		this.addRule(new Rule("count(N) :- N = #count {L:label(L)}."));
		this.addRule(new Rule("#minimize { N,count : count(N) }."));
	}
	
}
