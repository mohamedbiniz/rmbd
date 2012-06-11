package at.ainf.theory.storage;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 01.02.12
 * Time: 12:43
 * To change this template use File | Settings | File Templates.
 */
public interface AxiomRenderer<Id> {

    String renderAxiom(Id axiom);

    String renderAxioms(Collection<Id> axioms);

}
