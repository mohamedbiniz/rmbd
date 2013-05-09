package at.ainf.owlapi3.model;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 26.01.13
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */
public class OWLJustificationIncoherencyExtractor extends OWLIncoherencyExtractor {

    public OWLJustificationIncoherencyExtractor(OWLReasonerFactory reasonerFactory) {
        super(reasonerFactory);
    }

    private Set<OWLAxiom> getAllJust(OWLOntology ontology, OWLReasonerFactory factory) {
        Set<OWLAxiom> allJust = new LinkedHashSet<OWLAxiom>();
        OWLReasoner reasoner = factory.createReasoner(ontology);
        BlackBoxExplanation blackBox = new BlackBoxExplanation(ontology,factory,reasoner);
        HSTExplanationGenerator generator = new HSTExplanationGenerator(blackBox);
        for (OWLClass entity : reasoner.getUnsatisfiableClasses().getEntities()) {
            for(Set<OWLAxiom> just : generator.getExplanations(entity))
                allJust.addAll(just);
        }
        return allJust;

    }

    protected List<Integer> calcUniqueAxioms(List<OWLOntology> ontologies) {
        List<Integer> res = new LinkedList<Integer>();
        for (int i = 0; i < ontologies.size(); i++) {

            OWLOntology toCheck = ontologies.get(i);
            Set<OWLLogicalAxiom> axiomsToCheck = new HashSet<OWLLogicalAxiom>(toCheck.getLogicalAxioms());

            Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>();
            for (int j = 0; j < ontologies.size(); j++)
                if (i!=j)
                    axioms.addAll(ontologies.get(j).getLogicalAxioms());
            axiomsToCheck.removeAll(axioms);
            res.add(axiomsToCheck.size());

        }

        return res;
    }

    protected Set<OWLOntology> extract(OWLOntology ont, boolean multiple, boolean useMultiple) {

        Set<OWLEntity> signature = new LinkedHashSet<OWLEntity>();
        OWLOntology ontology = createCopyForExtraction(ont);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        Set<OWLAxiom> aBoxAxioms = null;
        boolean consistent = reasoner.isConsistent();
        if (!consistent) {
            //OWLOntologyManager man = OWLManager.createOWLOntologyManager(); //ontology.getOWLOntologyManager();
            aBoxAxioms = ontology.getABoxAxioms(false);      //true
            ontology.getOWLOntologyManager().removeAxioms(ontology, aBoxAxioms);
            reasoner.flush();
            if (!reasoner.isConsistent())
                throw new RuntimeException("The ontology without ABox is not consistent! Reasoner Flush Problem? ");
            /*for (OWLAxiom aBoxAxiom : aBoxAxioms) {
                // if contains negation
                signature.addAll(aBoxAxiom.getClassesInSignature());
            }*/
        }

        for (OWLClass entity : reasoner.getUnsatisfiableClasses().getEntities())
            signature.add(entity);

        signature.remove(OWLManager.getOWLDataFactory().getOWLNothing());


        Set<OWLOntology> result;

        SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ontology, ModuleType.STAR);


        String iriString = "http://ainf.at/IncoherencyModule";
        try {
            if (!signature.isEmpty()) {
                if (multiple) {
                    result = new LinkedHashSet<OWLOntology>();
                    List<OWLOntology> resList = new LinkedList<OWLOntology>();
                    int cnt = 0;
                    for (OWLEntity i : signature) {
                        OWLOntology on = sme.extractAsOntology(Collections.singleton(i), IRI.create(iriString + "_" + cnt));

                        resList.add(on);
                        cnt++;
                    }

                    Collections.sort(resList,new Comparator<OWLOntology>() {
                        @Override
                        public int compare(OWLOntology o1, OWLOntology  o2 ) {
                            return  -((Integer)o1.getLogicalAxioms().size()).compareTo(o2.getLogicalAxioms().size());
                        }
                    });
                    List<Integer> uniqueAxioms = calcUniqueAxioms(resList);
                    for (int i = 0; i < resList.size(); i++) {
                        OWLOntology onto1 = resList.get(i);
                        if (uniqueAxioms.get(i)>8) {
                            Set<OWLLogicalAxiom> justAxioms = new HashSet<OWLLogicalAxiom>();
                            for (OWLAxiom axiom : getAllJust(onto1,reasonerFactory))
                                justAxioms.add((OWLLogicalAxiom)axiom);
                            onto1.getOWLOntologyManager().removeAxioms(onto1,onto1.getLogicalAxioms());
                            onto1.getOWLOntologyManager().addAxioms(onto1,justAxioms);
                        }
                    }
                    result.addAll(resList);

                }
                else {
                    OWLOntology onto = sme.extractAsOntology(signature, IRI.create(iriString));

                    Set<OWLLogicalAxiom> justAxioms = new HashSet<OWLLogicalAxiom>();
                    for (OWLAxiom axiom : getAllJust(onto,reasonerFactory))
                        justAxioms.add((OWLLogicalAxiom)axiom);
                    onto.getOWLOntologyManager().removeAxioms(onto,onto.getLogicalAxioms());
                    onto.getOWLOntologyManager().addAxioms(onto,justAxioms);
                    result = Collections.singleton(onto );

                }

            } else
                result = Collections.singleton(OWLManager.createOWLOntologyManager().createOntology(IRI.create(iriString)));
        } catch (OWLOntologyCreationException e) {
            result = null;
        }

        if (!consistent)
            ontology.getOWLOntologyManager().addAxioms(ontology, aBoxAxioms);

        return result;

    }
}
