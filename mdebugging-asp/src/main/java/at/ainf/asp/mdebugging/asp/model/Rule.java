package at.ainf.asp.mdebugging.asp.model;

/**
 * @author Melanie Frühstück
 *
 */
public class Rule {

	private String _rule;
	private String _label;
	
	public Rule(String rule) {
		_rule = rule;
		_label = "";
	}
	
	public Rule(String rule, int labelNr) {
		_rule = rule;
		_label = "label("+labelNr+")";
	}

	/**
	 * Returns the rule.
	 * @return the rule
	 */
	public String getRule() {
		return _rule;
	}

	/**
	 * Sets the rule.
	 * @param rule
	 */
	public void setRule(String rule) {
		this._rule = rule;
	}
	
	/**
	 * Sets the label of the rules.
	 * @param label
	 */
	public void setLabel(String label) {
		_label = label;
	}
	
	/**
	 * Returns the label of the rule.
	 * @return the label of the rule
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Returns the extended rule, including the default negated label 
	 * in String representation.
	 * @return the extended rule
	 */
	public String getRuleIncludingLabel() {
		return _rule.substring(0, _rule.length()-1) + ";not " + this.getLabel() + ".";
	}

}
