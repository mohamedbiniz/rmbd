package at.ainf.queryselection;

/**
 * Created by IntelliJ IDEA.
 * User: pr8
 * Date: 17.03.11
 * Time: 19:32
 * To change this template use File | Settings | File Templates.
 */
public class DiagnosisMemento {

    private final String name;
    private final double probability;

    public DiagnosisMemento(String name, double prob) {
        this.name = name;
        this.probability = prob;
    }

    public String getSavedName() {
        return this.name;
    }

    public double getSavedProbability() {
        return this.probability;
    }


}
