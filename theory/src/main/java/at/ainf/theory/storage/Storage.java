package at.ainf.theory.storage;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 20.04.11
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
public interface Storage<T extends AxiomSet<Id>, Id> {

    boolean addNodeLabel(T nodeLabel);

    boolean removeNodeLabel(T nodeLabel);

    public Set<T> getNodeLabels();


    boolean addHittingSet(T hittingSet);

    boolean removeHittingSet(T hittingSet);

    Set<T> getHittingSets();


    void resetStorage();


    Set<T> getConflicts();

    Set<T> getDiagnoses();










}
