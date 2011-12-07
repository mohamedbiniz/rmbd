package at.ainf.pluginprotege.testcasesentailmentsview;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 17.03.11
 * Time: 10:09
 * To change this template use File | Settings | File Templates.
 */
public enum SectionType {

    PT ("Positive Test Case"),
    NT ("Negative Test Case"),
    ET ("Entailed"),
    NET ("Not entailed");

    private final String label;

    SectionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }




}
