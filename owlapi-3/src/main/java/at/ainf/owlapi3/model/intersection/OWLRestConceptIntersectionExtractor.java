package at.ainf.owlapi3.model.intersection;

import at.ainf.owlapi3.model.OWLModuleExtractor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static at.ainf.owlapi3.util.OWLUtils.calculateSignature;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 16.05.13
 * Time: 11:48
 * To change this template use File | Settings | File Templates.
 */
public class OWLRestConceptIntersectionExtractor extends AbstractOWLIntersectionExtractor {

    private static Logger logger = LoggerFactory.getLogger(OWLRestConceptIntersectionExtractor.class.getName());

    protected int size;

    public OWLRestConceptIntersectionExtractor(int size) {
        this.size = size;
    }

    @Override
    protected List<Set<OWLLogicalAxiom>> calculateModules (Set<OWLLogicalAxiom> axioms) {
        List<OWLClass> signature = new LinkedList<OWLClass>(calculateSignature(axioms));
        OWLModuleExtractor extractor = new OWLModuleExtractor(axioms);

        List<Set<OWLLogicalAxiom>> submodules = new LinkedList<Set<OWLLogicalAxiom>>();
        while (!signature.isEmpty()) {
            List<OWLClass> signatureForExtraction = calculateSignatureForExtraction (signature);

            Set<OWLLogicalAxiom> module = extractor.calculateModule(signatureForExtraction);
            if (module.isEmpty()) {
                break;
            }
            Set<OWLClass> moduleSignature = calculateSignature(module);
            signature.removeAll(moduleSignature);
            logger.info("signature for extraction / module signature / rest signature / module size: " +
                    signatureForExtraction.size() + ", " + moduleSignature.size() +
                    ", " + signature.size() + ", " + module.size());
            submodules.add(module);

        }

        return submodules;
    }

    protected List<OWLClass> calculateSignatureForExtraction (List<OWLClass> signature) {
        if (signature.size() > size)
            return new LinkedList<OWLClass>(signature.subList(0, size));
        else
            return new LinkedList<OWLClass>(signature);
    }

}
