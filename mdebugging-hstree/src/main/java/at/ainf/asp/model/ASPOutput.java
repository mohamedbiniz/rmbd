/**
 * 
 */
package at.ainf.asp.model;


/**
 * @author Melanie Fruehstueck
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
	
	public boolean isSatisfiabl() {
		return isSatisfiable;
	}
	
	public void setSatisfiable(boolean isSatisfiable) {
		this.isSatisfiable = isSatisfiable;
	}

	public boolean isUnknown() {
		return isUnknown;
	}

	public void setUnknown(boolean isUnknown) {
		this.isUnknown = isUnknown;
	}
}
