/**
 * 
 */
package at.ainf.asp.model;

import at.ainf.asp.model.ASPOutput;


/**
 * @author Melanie Frühstück
 *
 */
public class ASPOutput {

	protected static ASPOutput output;
	
	private boolean isSatisfiable;
	private boolean isUnknown;
	
	public static ASPOutput getASPOutputInstance() {
		if (output == null) {
			output = new ASPOutput();
		}
		return output;
	}
	
	/**
	 * ASP output can be satisfiable or not.
	 * @return true if ASP output is satisfiable, otherwise false
	 */
	public boolean isSatisfiabl() {
		return isSatisfiable;
	}
	
	/**
	 * Sets true if the ASP output is satisfiable.
	 * @param isSatisfiable
	 */
	public void setSatisfiable(boolean isSatisfiable) {
		this.isSatisfiable = isSatisfiable;
	}

	/**
	 * ASP output can also be unknown.
	 * @return true if the ASP output is unknown
	 */
	public boolean isUnknown() {
		return isUnknown;
	}

	/**
	 * Sets true if the ASP output is unknown.
	 * @param isUnknown
	 */
	public void setUnknown(boolean isUnknown) {
		this.isUnknown = isUnknown;
	}
}
