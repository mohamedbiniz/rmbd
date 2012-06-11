package at.ainf.protegeview.basis;

import at.ainf.owlcontroller.OWLController;
import at.ainf.owlcontroller.OWLControllerImpl;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 22.05.12
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public class ControllerManager {

    private static Map<OWLEditorKit, Map<OWLOntology,OWLController>> map;

    public static OWLController getActiveOntologyController (OWLEditorKit editorKit) {
        return map.get(editorKit).get(editorKit.getModelManager().getActiveOntology());
    }

    public static void createControllerForActiveOntology(OWLEditorKit editorKit) {
        Map<OWLOntology,OWLController> controllers = map.get(editorKit);
        OWLOntology ontology = editorKit.getModelManager().getActiveOntology();
        OWLReasonerFactory factory = editorKit.getOWLModelManager().
                getOWLReasonerManager().getCurrentReasonerFactory().getReasonerFactory();

        if (controllers == null) {
            controllers = new HashMap<OWLOntology, OWLController>();
            controllers.put(ontology,new OWLControllerImpl(factory,ontology));
            map.put(editorKit,controllers);
        }
        else {
            if (controllers.get(ontology)==null)
                controllers.put(ontology,new OWLControllerImpl(factory,ontology));
        }
    }

    public static void removeControllers(OWLEditorKit editorKit) {
        map.remove(editorKit);
    }

}
