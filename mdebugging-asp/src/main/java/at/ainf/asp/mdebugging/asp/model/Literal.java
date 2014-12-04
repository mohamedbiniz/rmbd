package at.ainf.asp.mdebugging.asp.model;

/**
 * @author Melanie Frühstück
 *
 */
public class Literal {

	private String _literal;
	
	public Literal(String literal) {
		_literal = literal;
	}
	
	/**
	 * Returns the literal.
	 * @return the literal
	 */
	public String getLiteral() {
		return _literal;
	}
	
	/**
	 * Sets the literal.
	 * @param literal
	 */
	public void setLiteral(String literal) {
		_literal = literal;
	}
	
}
