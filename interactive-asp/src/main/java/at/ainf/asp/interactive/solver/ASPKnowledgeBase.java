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

    public Set<String> generateDiagnosisProgram(Set<String> diagnosis) {
        Set<String> ext = generateDebuggingProgram();

        Set<String> remAtoms = new HashSet<String>(errorAtoms);
        remAtoms.removeAll(diagnosis);
        for (String atom : remAtoms) {
            ext.add(":- " + atom + ".\n");
        }

        return ext;
    }

    public Set<String> generateDebuggingProgram() {
        Set<String> bg = getBackgroundFormulas();
        Set<String> ext = new HashSet<String>(this.errorAtoms.size() + this.knowledgeBase.size() + bg.size());
        ext.addAll(this.knowledgeBase);
        ext.addAll(bg);
        return ext;
    }
}
