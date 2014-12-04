/**
 * 
 */
package at.ainf.asp.model;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * @author Melanie Frühstück
 *
 */
public class ASPModel {

	protected static ASPModel model = null;
	
	private Set<IProgramElement> elements = new LinkedHashSet<IProgramElement>();
	private Set<IProgramElement> rules = new LinkedHashSet<IProgramElement>();
	private Set<IProgramElement> facts = new LinkedHashSet<IProgramElement>();
	
	public static ASPModel getASPModelInstance() {
		if (model == null) {
			model = new ASPModel();
		}
		return model;
	}
	
	/**
	 * Returns the program elements of the program, i.e. rules and facts.
	 * @return the program elements
	 */
	public Set<IProgramElement> getProgramElements() {
		return elements;
	}
	
	/**
	 * Adds a program element to a set of program elements.
	 * @param pe
	 */
	public void addProgramElement(IProgramElement pe) {
		elements.add(pe);
	}
	
	/**
	 * Adds a rule to the set of rules.
	 * @param rule
	 */
	public void addRule(Rule rule) {
		rules.add(rule);
	}
	
	/**
	 * Adds a fact to the set of facts.
	 * @param fact
	 */
	public void addFact(Fact fact) {
		facts.add(fact);
	}
	
	/**
	 * Removes a program element from the set of program elements.
	 * @param pe
	 */
	public void removeProgramElement(IProgramElement pe) {
		elements.remove(pe);
	}
	
	/**
	 * Returns the set of rules of the program.
	 * @return the set of rules
	 */
	public Set<IProgramElement> getRules() {
		return rules;
	}
	
	/**
	 * Returns the set of facts of the program.
	 * @return the set of facts
	 */
	public Set<IProgramElement> getFacts() {
		return facts;
	}
	
}
