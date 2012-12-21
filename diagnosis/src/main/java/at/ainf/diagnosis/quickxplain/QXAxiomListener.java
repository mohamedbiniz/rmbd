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

    void setFoundAxiom(Id axiom);

    Id getFoundAxiom() throws InterruptedException;
}
