/**
 * 
 */
package at.ainf.asp.model.exceptions;

/**
 * @author Melanie Frühstück
 *
 */
public class NotMonotoneException extends Exception {

	private static final long serialVersionUID = 304820602674297832L;

	/**
	 * Is thrown if the given program is not monotone according to our 
	 * definition of monotonicity.
	 * @param message
	 */
	public NotMonotoneException(String message) {
		super(message);
	}
	
}
