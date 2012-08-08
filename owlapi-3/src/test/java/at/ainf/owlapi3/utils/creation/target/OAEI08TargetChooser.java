package at.ainf.owlapi3.utils.creation.target;

import at.ainf.diagnosis.storage.AxiomSet;
import at.ainf.diagnosis.tree.CostsEstimator;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.08.12
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class OAEI08TargetChooser {

    private Set<AxiomSet<OWLLogicalAxiom>> diagnoses;
    private CostsEstimator<OWLLogicalAxiom> estimator;

    public OAEI08TargetChooser(Set<AxiomSet<OWLLogicalAxiom>> diagnoses, final CostsEstimator<OWLLogicalAxiom> e) {
        this.diagnoses = diagnoses;
        this.estimator = e;
    }

    public AxiomSet<OWLLogicalAxiom> getDgTarget() {
        Comparator<AxiomSet<OWLLogicalAxiom>> c = new Comparator<AxiomSet<OWLLogicalAxiom>>() {
            public int compare(AxiomSet<OWLLogicalAxiom> o1, AxiomSet<OWLLogicalAxiom> o2) {
                int numOfOntologyAxiomsO1 = 0;
                int numOfMatchingAxiomO1 = 0;
                for (OWLLogicalAxiom axiom : o1) {
                    if (estimator.getAxiomCosts(axiom).compareTo(new BigDecimal("0.001")) != 0)
                        numOfMatchingAxiomO1++;
                    else
                        numOfOntologyAxiomsO1++;
                }
                double percAxiomFromOntO1 = (double) numOfOntologyAxiomsO1;// / (numOfOntologyAxiomsO1 + numOfMatchingAxiomO1);

                int numOfOntologyAxiomsO2 = 0;
                int numOfMatchingAxiomO2 = 0;
                for (OWLLogicalAxiom axiom : o2) {
                    if (estimator.getAxiomCosts(axiom).compareTo(new BigDecimal("0.001")) != 0)
                        numOfMatchingAxiomO2++;
                    else
                        numOfOntologyAxiomsO2++;
                }
                double percAxiomFromOntO2 = (double) numOfOntologyAxiomsO2;// / (numOfOntologyAxiomsO2 + numOfMatchingAxiomO2);


                if (percAxiomFromOntO1 < percAxiomFromOntO2)
                    return -1;
                else if (percAxiomFromOntO1 == percAxiomFromOntO2)
                    return 0;
                else
                    return 1;
            }
        };

        return Collections.max(diagnoses, c);
    }
}
