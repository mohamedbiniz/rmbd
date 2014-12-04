/**
 * 
 */
package at.ainf.asp.model.exceptions;

/**
 * @author Melanie Frühstück
 *
 */
public class IllegalParserElementException extends Exception {

	private static final long serialVersionUID = 1920705282037528735L;

	/**
	 * Is thrown if the parser could not recognize an element in the program.
	 * @param message
	 */
	public IllegalParserElementException(String message) {
		super(message);
	}
	
}
