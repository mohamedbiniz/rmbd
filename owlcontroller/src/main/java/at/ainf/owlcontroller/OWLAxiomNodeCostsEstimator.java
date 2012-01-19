package at.ainf.owlcontroller;

import at.ainf.diagnosis.tree.NodeCostsEstimator;
import at.ainf.theory.model.ITheory;
import at.ainf.theory.storage.HittingSet;
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
public class OWLAxiomNodeCostsEstimator implements NodeCostsEstimator<OWLLogicalAxiom> {

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


    public OWLAxiomNodeCostsEstimator(ITheory<OWLLogicalAxiom> t) {
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

    public double getNodeSetCosts(Set<OWLLogicalAxiom> labelSet) {
            double probability = 1.0;
            for (OWLLogicalAxiom axiom : labelSet) {
                probability *= getNodeCosts(axiom);
            }
            Collection<OWLLogicalAxiom> activeFormulas = new ArrayList<OWLLogicalAxiom>(theory.getActiveFormulas());
            activeFormulas.removeAll(labelSet);
            for (OWLLogicalAxiom axiom : activeFormulas) {
                if (probability * (1 - getNodeCosts(axiom)) == 0)
                    probability = Double.MIN_VALUE;
                else
                    probability *= (1 - getNodeCosts(axiom));
            }
            return probability;
        }

        public double getNodeCosts(OWLLogicalAxiom axiom) {
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
                                            Set<HittingSet<OWLLogicalAxiom>> hittingSets) {


            this.keywordProbabilities = keywordProbabilities;
            updateAxiomProbabilities();
            updateDiagnosisProbabilities(hittingSets);

        }

        private void updateDiagnosisProbabilities(Set<HittingSet<OWLLogicalAxiom>> hittingSets) {

            if (hittingSets == null)
                return;
            if (!hittingSets.isEmpty()) {
                for (HittingSet<OWLLogicalAxiom> hittingSet : hittingSets) {
                    double probability = getNodeSetCosts(hittingSet);

                    hittingSet.setMeasure(probability);
                    //hittingSet.setUserAssignedProbability(probability);
                }
                double sum = 0;

                for (HittingSet<OWLLogicalAxiom> hittingSet : hittingSets) {
                    sum += hittingSet.getMeasure();
                }
                for (HittingSet<OWLLogicalAxiom> hittingSet : hittingSets) {
                    hittingSet.setMeasure(hittingSet.getMeasure() / sum);
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
