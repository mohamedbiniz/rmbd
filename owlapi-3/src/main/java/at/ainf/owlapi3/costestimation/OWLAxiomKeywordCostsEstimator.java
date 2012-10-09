package at.ainf.owlapi3.costestimation;

import static at.ainf.diagnosis.tree.Rounding.*;
import at.ainf.diagnosis.partitioning.BigFunctions;
import at.ainf.diagnosis.tree.CostsEstimator;
import at.ainf.diagnosis.model.ITheory;
import at.ainf.diagnosis.storage.AxiomSet;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.11.11
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class OWLAxiomKeywordCostsEstimator implements CostsEstimator<OWLLogicalAxiom> {

    private ITheory<OWLLogicalAxiom> theory;

    public final static ManchesterOWLSyntax[] keywords = {ManchesterOWLSyntax.SOME,
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


    public static int getMaxLengthKeyword() {
        int max = 0;
        for (ManchesterOWLSyntax keyword : keywords)
            if (keyword.toString().length() > max)
                max = keyword.toString().length();

        return max;
    }

    public OWLAxiomKeywordCostsEstimator(ITheory<OWLLogicalAxiom> t) {
        this.keywordProbabilities = createKeywordProbs();
        this.theory = t;
        updateAxiomProbabilities();
    }

    public void updateKeywordProb(Map<ManchesterOWLSyntax, BigDecimal> keywordProbabilities) {
        this.keywordProbabilities = keywordProbabilities;
        updateAxiomProbabilities();
    }

    private Map<ManchesterOWLSyntax, BigDecimal> createKeywordProbs() {

        Map<ManchesterOWLSyntax, BigDecimal> map = new HashMap<ManchesterOWLSyntax, BigDecimal>();

        for (ManchesterOWLSyntax keyword : keywords)
            map.put(keyword, new BigDecimal("0.01"));
        map.put(ManchesterOWLSyntax.SOME, new BigDecimal("0.05"));
        map.put(ManchesterOWLSyntax.ONLY, new BigDecimal("0.05"));
        map.put(ManchesterOWLSyntax.AND, new BigDecimal("0.001"));
        map.put(ManchesterOWLSyntax.OR, new BigDecimal("0.001"));
        map.put(ManchesterOWLSyntax.NOT, new BigDecimal("0.01"));
        return map;
    }

    public BigDecimal getAxiomSetCosts(Set<OWLLogicalAxiom> labelSet) {
            BigDecimal probability = BigDecimal.ONE;
            for (OWLLogicalAxiom axiom : labelSet) {
                probability = probability.multiply(getAxiomCosts(axiom));
            }
            Collection<OWLLogicalAxiom> activeFormulas = new ArrayList<OWLLogicalAxiom>(theory.getFaultyFormulas());
            activeFormulas.removeAll(labelSet);
            for (OWLLogicalAxiom axiom : activeFormulas) {
                probability = probability.multiply(BigDecimal.ONE.subtract(getAxiomCosts(axiom)));
            }
            return probability;
        }

        public BigDecimal getAxiomCosts(OWLLogicalAxiom axiom) {
            BigDecimal p = axiomsProbabilities.get(axiom);
            if (p != null)
                return p;

            ManchesterOWLSyntaxOWLObjectRendererImpl impl = new ManchesterOWLSyntaxOWLObjectRendererImpl();
            String renderedAxiom = impl.render(axiom); // String renderedAxiom = modelManager.getRendering(axiom);
            BigDecimal result = BigDecimal.ONE;

            for (ManchesterOWLSyntax keyword : this.keywordProbabilities.keySet()) {
                int occurrence = getNumOccurrences(keyword, renderedAxiom);
                BigDecimal probability = getProbability(keyword);

                BigDecimal temp = BigDecimal.ONE.subtract(probability);
                temp = BigFunctions.intPower(temp,occurrence,temp.scale());
                result = result.multiply(temp);
            }
            result = BigDecimal.ONE.subtract(result);
            // no keyword is known
            if(result.compareTo(new BigDecimal("0.0"))==0)
                result = new BigDecimal("0.000000000000000000000000000000000000000000001");

            return result;
        }

        private Map<OWLLogicalAxiom, BigDecimal> axiomsProbabilities = null;
        private Map<ManchesterOWLSyntax, BigDecimal> keywordProbabilities;

        public void setKeywordProbabilities(Map<ManchesterOWLSyntax, BigDecimal> keywordProbabilities,
                                            Set<AxiomSet<OWLLogicalAxiom>> axiomSets) {


            this.keywordProbabilities = keywordProbabilities;
            updateAxiomProbabilities();
            updateDiagnosisProbabilities(axiomSets);

        }

    public Map<ManchesterOWLSyntax, BigDecimal> getKeywordProbabilities() {
        return keywordProbabilities;
    }

    private void updateDiagnosisProbabilities(Set<AxiomSet<OWLLogicalAxiom>> axiomSets) {

            if (axiomSets == null)
                return;
            if (!axiomSets.isEmpty()) {
                for (AxiomSet<OWLLogicalAxiom> axiomSet : axiomSets) {
                    BigDecimal probability = getAxiomSetCosts(axiomSet);

                    axiomSet.setMeasure(probability);
                    //axiomSet.setUserAssignedProbability(probability);
                }
                BigDecimal sum = BigDecimal.ZERO;

                for (AxiomSet<OWLLogicalAxiom> axiomSet : axiomSets) {
                    sum = sum.add(axiomSet.getMeasure());
                }
                for (AxiomSet<OWLLogicalAxiom> axiomSet : axiomSets) {
                    axiomSet.setMeasure(axiomSet.getMeasure().divide(sum,PRECISION,ROUNDING_MODE));
                }
            }
        }

        private void updateAxiomProbabilities() {
            Map<OWLLogicalAxiom, BigDecimal> axiomsProbs = new HashMap<OWLLogicalAxiom, BigDecimal>();
            ManchesterOWLSyntaxOWLObjectRendererImpl impl = new ManchesterOWLSyntaxOWLObjectRendererImpl();
            Collection<OWLLogicalAxiom> activeFormulas = theory.getFaultyFormulas();
            BigDecimal sum = BigDecimal.ZERO;
            for (OWLLogicalAxiom axiom : activeFormulas) {
                String renderedAxiom = impl.render(axiom); // String renderedAxiom = modelManager.getRendering(axiom);
                BigDecimal result = BigDecimal.ONE;

                for (ManchesterOWLSyntax keyword : this.keywordProbabilities.keySet()) {
                    int occurrence = getNumOccurrences(keyword, renderedAxiom);
                    BigDecimal probability = getProbability(keyword);

                    BigDecimal t = BigDecimal.ONE.subtract(probability);
                    t = BigFunctions.intPower(t,occurrence,t.scale());
                    result = result.multiply(t);
                }
                result = BigDecimal.ONE.subtract(result);
                // no keyword is known
                if(result.compareTo(new BigDecimal("0.0"))==0)
                    result = new BigDecimal("0.000000000000000000000000000000000000000000001");
                axiomsProbs.put(axiom, result);
                sum = sum.add(BigDecimal.ONE.subtract(result));
            }
            /*if (normalize_axioms) {
                for (Id axiom : axiomsProbs.keySet())
                    axiomsProbs.put(axiom, axiomsProbs.get(axiom) / sum);
            }*/


            this.axiomsProbabilities = Collections.unmodifiableMap(axiomsProbs);
        }

        private BigDecimal getProbability(ManchesterOWLSyntax keyword) {
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
