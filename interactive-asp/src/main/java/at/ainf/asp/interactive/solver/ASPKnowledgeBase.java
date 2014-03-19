package at.ainf.asp.interactive.solver;

import at.ainf.diagnosis.model.KnowledgeBase;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Extended knowledge base to handle error atoms and creation of diagnosis program correctly.
 */
public class ASPKnowledgeBase extends KnowledgeBase<String> {

    private Set<String> errorAtoms = new HashSet<String>();

    public Set<String> getErrorAtoms() {
        return Collections.unmodifiableSet(errorAtoms);
    }

    public void setErrorAtoms(Set<String> errorAtoms) {
        this.errorAtoms = errorAtoms;
    }
}
