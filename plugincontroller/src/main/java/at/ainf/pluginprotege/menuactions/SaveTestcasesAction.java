package at.ainf.pluginprotege.menuactions;

import at.ainf.pluginprotege.WorkspaceTab;
import at.ainf.pluginprotege.testcasesentailmentsview.SectionType;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.03.11
 * Time: 09:39
 * To change this template use File | Settings | File Templates.
 */
public class SaveTestcasesAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {
        getWS().saveTestcasesAction();
    }

    public void saveTestcasesAction() {
        try {


            HashMap<SectionType, Collection<Set<OWLLogicalAxiom>>> items = new HashMap<SectionType, Collection<Set<OWLLogicalAxiom>>>();
            for (SectionType type : SectionType.values()) {
                items.put(type, getWS().getOWLLogicalAxioms(type));
            }

            if (getWS().getOwlTheory() == null) {

                getWS().createOWLTheory();
            }
            OWLOntologyManager manager = getWS().getOwlTheory().getOntology().getOWLOntologyManager();
            OWLOntology on = null;

            try {
                on = manager.createOntology();
            } catch (OWLOntologyCreationException ex) {
                throw new OWLRuntimeException(ex);
            }

            for (SectionType type : SectionType.values()) {
                Collection<Set<OWLLogicalAxiom>> itemslist = items.get(type);
                int c = 0;
                for (Collection<OWLLogicalAxiom> axiomCollection : itemslist) {
                    for (OWLLogicalAxiom axiom : axiomCollection) {
                        OWLDataFactory factory = manager.getOWLDataFactory();
                        OWLAnnotation annot = factory.getOWLAnnotation(
                                factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
                                factory.getOWLLiteral(type.toString() + c, "en"));
                        TreeSet<OWLAnnotation> set = new TreeSet<OWLAnnotation>();
                        set.add(annot);
                        axiom = (OWLLogicalAxiom) axiom.getAnnotatedAxiom(set);
                        AddAxiom addAxiom = new AddAxiom(on, axiom);
                        manager.applyChange(addAxiom);
                    }
                    c++;
                }
            }

            /* File file = null;
            JFileChooser fc = workspacetab.fc;

            switch(fc.showSaveDialog(workspacetab)) {
                case JFileChooser.APPROVE_OPTION:
                    file = fc.getSelectedFile();
                    break;
                default:
                    return;
            } */


            RDFXMLOntologyFormat rdfxmlOntologyFormat = new RDFXMLOntologyFormat();
            OWLOntologyDocumentTarget documentTarget = new StringDocumentTarget();

            // manager.saveOntology(on, rdfxmlFormat, IRI.create(file.toURI()));
            manager.saveOntology(on, rdfxmlOntologyFormat, documentTarget);

            manager.removeOntology(on);
            String result = "Testcases " + ((StringDocumentTarget) documentTarget).toString();

            for (Iterator<OWLAnnotation> iter = getWS().getOwlTheory()
                    .getOriginalOntology().getAnnotations().iterator(); iter.hasNext();) {
                OWLAnnotation ann = iter.next();
                if (ann.getValue() instanceof OWLLiteral) {
                    String s = ((OWLLiteral) ann.getValue()).getLiteral();
                    if (s.startsWith("Testcases ")) {
                        iter.remove();
                    }

                }
            }

            OWLLiteral lit = manager.getOWLDataFactory().getOWLLiteral(result);
            IRI iri = OWLRDFVocabulary.RDFS_COMMENT.getIRI();   //  IRI.create("http://www.ainf.at/isbi#comment");
            OWLAnnotation anno = manager.getOWLDataFactory().getOWLAnnotation(manager.getOWLDataFactory().getOWLAnnotationProperty(iri), lit);
            manager.applyChange(new AddOntologyAnnotation(getWS().getOwlTheory().getOriginalOntology(), anno));


        } catch (OWLOntologyStorageException ex) {
            System.out.println("Could not save ontology: " + ex.getMessage());
        }
    }


}
