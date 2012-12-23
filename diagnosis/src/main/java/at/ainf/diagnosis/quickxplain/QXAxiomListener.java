package at.ainf.diagnosis.quickxplain;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 21.12.12
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */
public interface QXAxiomListener<Id> {
    void release();

    Id getFoundAxiom() throws InterruptedException;

    void setFoundAxiom(Id axiom);

    QXAxiomListener<Id> newInstance();

    boolean isReleased();

    public boolean hasAxioms();
}
