package at.ainf.diagnosis.storage;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 01.02.12
 * Time: 12:43
 * To change this template use File | Settings | File Templates.
 */
public interface FormulaRenderer<Id> {

    String renderAxiom(Id axiom);

    String renderAxioms(Collection<Id> axioms);

}
