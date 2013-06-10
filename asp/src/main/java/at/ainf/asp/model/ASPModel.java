/**
 * 
 */
package at.ainf.asp.model;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * @author Melanie Fruehstueck
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
	
	public Set<IProgramElement> getProgramElements() {
		return elements;
	}
	
	public void addProgramElement(IProgramElement pe) {
		elements.add(pe);
	}
	
	public void addRule(Rule rule) {
		rules.add(rule);
	}
	
	public void addFact(Fact fact) {
		facts.add(fact);
	}
	
	public void removeProgramElement(IProgramElement pe) {
		elements.remove(pe);
	}
	
	public Set<IProgramElement> getRules() {
		return rules;
	}
	
	public Set<IProgramElement> getFacts() {
		return facts;
	}
	
}
