package at.ainf.asp.mdebugging.asp.model;

/**
 * @author Melanie Frühstück
 *
 */
public class AnswerSet {

	public static String UNSATISFIABLE = "UNSATISFIABLE";
	public static String SATISFIABLE = "SATISFIABLE";
	public static String UNKNOWN = "UNKNOWN";
	
	private String _state;
	private String _answerSet;
	
	public AnswerSet() {
		_answerSet = "";
	}
	
	public AnswerSet(String answerSet) {
		_answerSet = answerSet;
	}
	
	/**
	 * Returns the state of an answer set (satisfiable, unsatisfiable or unknown).
	 * @return the state of an answer set
	 */
	public String getState() {
		return _state;
	}

	/**
	 * Sets the state of an answer set (satisfiable, unsatisfiable or unknown).
	 * @param state
	 */
	public void setState(String state) {
		this._state = state;
	}
	
	/**
	 * Sets the computed answer set.
	 * @param answerSet
	 */
	public void setAnswerSet(String answerSet) {
		_answerSet = answerSet;
	}
	
	/**
	 * Returns the computed answer set.
	 * @return the answer set
	 */
	public String getAnswerSet() {
		return _answerSet;
	}
	
}
