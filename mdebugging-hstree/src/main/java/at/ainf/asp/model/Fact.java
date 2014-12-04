package at.ainf.asp.model;

import at.ainf.asp.model.IProgramElement;

/**
 * @author Melanie Frühstück
 *
 */
public class Fact implements IProgramElement, Comparable<IProgramElement> {

	private String fact;
	
	public Fact(String fact) {
		this.fact = fact;
	}

	@Override
	public String getString() {
		return this.fact;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IProgramElement o) {
		return this.getString().compareTo(o.getString());
	}	
	
}
