package at.ainf.theory.model;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 07.03.11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
public class UnsatisfiableFormulasException extends Exception {

    private static final long serialVersionUID = 3763099660981905812L;

    public UnsatisfiableFormulasException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public UnsatisfiableFormulasException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public UnsatisfiableFormulasException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public UnsatisfiableFormulasException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
