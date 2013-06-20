package at.ainf.asp.model;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 10.06.13
 * Time: 14:46
 * To change this template use File | Settings | File Templates.
 */
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
