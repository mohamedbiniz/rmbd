package at.ainf.owlapi3.utils.creation.target;

import at.ainf.owlapi3.utils.creation.ontology.OAEI11AnatomyOntologyCreator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.08.12
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */
public class OAEI11AnatomyTargetProvider implements TargetProvider {

    private String file;

    public OAEI11AnatomyTargetProvider(String file) {
        this.file = file;
    }

    @Override
    public Set<OWLLogicalAxiom> getDiagnosisTarget() {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        Map<OWLLogicalAxiom, Double> axioms = new HashMap<OWLLogicalAxiom, Double>();
        Set<OWLLogicalAxiom> targetDiagnosis = new LinkedHashSet<OWLLogicalAxiom>();
        try {
            String path = ClassLoader.getSystemResource("oaei11").getPath() + "/";
            OAEI11AnatomyOntologyCreator.readDataOAEI(path + file + ".txt", axioms, targetDiagnosis, man);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return targetDiagnosis;
    }

}
