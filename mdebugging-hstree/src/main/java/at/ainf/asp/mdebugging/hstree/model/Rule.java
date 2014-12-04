package at.ainf.asp.mdebugging.hstree.model;

import at.ainf.asp.mdebugging.hstree.model.IProgramElement;

/**
 * @author Melanie Frühstück
 *
 */
public class Rule implements IProgramElement, Comparable<IProgramElement> {

	private String rule;
	
	public Rule(String rule) {
		this.rule = rule;
	}

	@Override
	public String getString() {
		return this.rule;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IProgramElement o) {
		return this.getString().compareTo(o.getString());
	}
	
}
