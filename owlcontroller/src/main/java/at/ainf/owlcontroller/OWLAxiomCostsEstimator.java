package at.ainf.owlcontroller;

import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.storage.AxiomSet;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.11.11
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class OWLAxiomCostsEstimator implements CostsEstimator<OWLLogicalAxiom> {

    private ITheory<OWLLogicalAxiom> theory;

    private ManchesterOWLSyntax[] keywords = {ManchesterOWLSyntax.SOME,
                ManchesterOWLSyntax.ONLY,
                ManchesterOWLSyntax.MIN,
                ManchesterOWLSyntax.MAX,
                ManchesterOWLSyntax.EXACTLY,
                ManchesterOWLSyntax.AND,
                ManchesterOWLSyntax.OR,
                ManchesterOWLSyntax.NOT,
                ManchesterOWLSyntax.VALUE,
                ManchesterOWLSyntax.INVERSE,
                ManchesterOWLSyntax.SUBCLASS_OF,
                ManchesterOWLSyntax.EQUIVALENT_TO,
                ManchesterOWLSyntax.DISJOINT_CLASSES,
                ManchesterOWLSyntax.DISJOINT_WITH,
                ManchesterOWLSyntax.FUNCTIONAL,
                ManchesterOWLSyntax.INVERSE_OF,
                ManchesterOWLSyntax.SUB_PROPERTY_OF,
                ManchesterOWLSyntax.SAME_AS,
                ManchesterOWLSyntax.DIFFERENT_FROM,
                ManchesterOWLSyntax.RANGE,
                ManchesterOWLSyntax.DOMAIN,
                ManchesterOWLSyntax.TYPE,
                ManchesterOWLSyntax.TRANSITIVE,
                ManchesterOWLSyntax.SYMMETRIC
        };


    public OWLAxiomCostsEstimator(ITheory<OWLLogicalAxiom> t) {
        this.keywordProbabilities = createKeywordProbs();
        this.theory = t;
        updateAxiomProbabilities();
    }

    public void updateKeywordProb(Map<ManchesterOWLSyntax, Double> keywordProbabilities) {
        this.keywordProbabilities = keywordProbabilities;
        updateAxiomProbabilities();
    }

    private Map<ManchesterOWLSyntax, Double> createKeywordProbs() {

        Map<ManchesterOWLSyntax, Double> map = new HashMap<ManchesterOWLSyntax, Double>();

        for (ManchesterOWLSyntax keyword : keywords)
            map.put(keyword, 0.01);
        map.put(ManchesterOWLSyntax.SOME, 0.05);
        map.put(ManchesterOWLSyntax.ONLY, 0.05);
        map.put(ManchesterOWLSyntax.AND, 0.001);
        map.put(ManchesterOWLSyntax.OR, 0.001);
        map.put(ManchesterOWLSyntax.NOT, 0.01);
        return map;
    }

    public double getAxiomSetCosts(Set<OWLLogicalAxiom> labelSet) {
            double probability = 1.0;
            for (OWLLogicalAxiom axiom : labelSet) {
                probability *= getAxiomCosts(axiom);
            }
            Collection<OWLLogicalAxiom> activeFormulas = new ArrayList<OWLLogicalAxiom>(theory.getActiveFormulas());
            activeFormulas.removeAll(labelSet);
            for (OWLLogicalAxiom axiom : activeFormulas) {
                if (probability * (1 - getAxiomCosts(axiom)) == 0)
                    probability = Double.MIN_VALUE;
                else
                    probability *= (1 - getAxiomCosts(axiom));
            }
            return probability;
        }

        public double getAxiomCosts(OWLLogicalAxiom axiom) {
            Double p = axiomsProbabilities.get(axiom);
            if (p != null)
                return p;

            ManchesterOWLSyntaxOWLObjectRendererImpl impl = new ManchesterOWLSyntaxOWLObjectRendererImpl();
            String renderedAxiom = impl.render(axiom); // String renderedAxiom = modelManager.getRendering(axiom);
            double result = 1.0;

            for (ManchesterOWLSyntax keyword : this.keywordProbabilities.keySet()) {
                int occurrence = getNumOccurrences(keyword, renderedAxiom);
                double probability = getProbability(keyword);

                result = result * Math.pow(1.0 - probability, occurrence);
            }

            return 1 - result;
        }

        private Map<OWLLogicalAxiom, Double> axiomsProbabilities = null;
        private Map<ManchesterOWLSyntax, Double> keywordProbabilities;

        public void setKeywordProbabilities(Map<ManchesterOWLSyntax, Double> keywordProbabilities,
                                            Set<AxiomSet<OWLLogicalAxiom>> axiomSets) {


            this.keywordProbabilities = keywordProbabilities;
            updateAxiomProbabilities();
            updateDiagnosisProbabilities(axiomSets);

        }

        private void updateDiagnosisProbabilities(Set<AxiomSet<OWLLogicalAxiom>> axiomSets) {

            if (axiomSets == null)
                return;
            if (!axiomSets.isEmpty()) {
                for (AxiomSet<OWLLogicalAxiom> axiomSet : axiomSets) {
                    double probability = getAxiomSetCosts(axiomSet);

                    axiomSet.setMeasure(probability);
                    //axiomSet.setUserAssignedProbability(probability);
                }
                double sum = 0;

                for (AxiomSet<OWLLogicalAxiom> axiomSet : axiomSets) {
                    sum += axiomSet.getMeasure();
                }
                for (AxiomSet<OWLLogicalAxiom> axiomSet : axiomSets) {
                    axiomSet.setMeasure(axiomSet.getMeasure() / sum);
                }
            }
        }

        private void updateAxiomProbabilities() {
            Map<OWLLogicalAxiom, Double> axiomsProbs = new HashMap<OWLLogicalAxiom, Double>();
            ManchesterOWLSyntaxOWLObjectRendererImpl impl = new ManchesterOWLSyntaxOWLObjectRendererImpl();
            Collection<OWLLogicalAxiom> activeFormulas = theory.getActiveFormulas();
            double sum = 0;
            for (OWLLogicalAxiom axiom : activeFormulas) {
                String renderedAxiom = impl.render(axiom); // String renderedAxiom = modelManager.getRendering(axiom);
                double result = 1.0;

                for (ManchesterOWLSyntax keyword : this.keywordProbabilities.keySet()) {
                    int occurrence = getNumOccurrences(keyword, renderedAxiom);
                    double probability = getProbability(keyword);

                    result = result * Math.pow(1.0 - probability, occurrence);
                }
                axiomsProbs.put(axiom, 1 - result);
                sum += 1 - result;
            }
            /*if (normalize_axioms) {
                for (Id axiom : axiomsProbs.keySet())
                    axiomsProbs.put(axiom, axiomsProbs.get(axiom) / sum);
            }*/


            this.axiomsProbabilities = Collections.unmodifiableMap(axiomsProbs);
        }

        private double getProbability(ManchesterOWLSyntax keyword) {
            return keywordProbabilities.get(keyword);
        }

        private int getNumOccurrences(ManchesterOWLSyntax keyword, String str) {
            int cnt = 0;
            int last = 0;

            if (keyword == null) {
                System.out.println();
            }
            last = str.indexOf(keyword.toString());
            while (last > -1) {
                cnt++;
                last = str.indexOf(keyword.toString(), last + 1);
            }

            return cnt;

        }

}
