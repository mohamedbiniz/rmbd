package at.ainf.protegeview.basis;

import at.ainf.protegeview.DebuggerPluginManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLEditorKitHook;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.11.11
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
public class EditorKitHook extends OWLEditorKitHook implements OWLModelManagerListener {

    private Logger logger = LoggerFactory.getLogger(EditorKitHook.class.getName());

    private int id = 0;

    private static int cnt = 0;

    boolean initialized = false;

    private Map<OWLEditorKit,Map<OWLOntology,OntologyDiagnosisSearcher>> map = DebuggerPluginManager.getInstance().getOwlEditorKitMapperMap();

    public void initialise() throws Exception {
        if (!initialized) {
            DebuggerPluginManager pluginMan = DebuggerPluginManager.getInstance();

            pluginMan.getOwlEditorKitMapperMap().put(getEditorKit(), new LinkedHashMap<OWLOntology, OntologyDiagnosisSearcher>());
            getEditorKit().getModelManager().addListener(this);
            id = cnt;
            cnt++;
            logger.info("initialised editorKitHook " + id);
            initialized = true;
        }
        else
            logger.info("editorKitHook is already initialized " + id);

    }

    // Every (OWL)EditorKit has a Model (get ModelManager) and a UI (get Workspace)

    public void dispose() throws Exception {
        getEditorKit().getModelManager().removeListener(this);
        map.remove(getEditorKit());
        logger.info("disposed editorKitHook " + id);
    }

    @Override
    public void handleChange(OWLModelManagerChangeEvent event) {
        if (event.getType().equals(EventType.ACTIVE_ONTOLOGY_CHANGED)) {
            OWLOntology activeOntology = event.getSource().getActiveOntology();
            if (!map.get(getEditorKit()).containsKey(activeOntology)) {
                map.get(getEditorKit()).put(activeOntology,new OntologyDiagnosisSearcher(event.getSource()));
            }
            logger.info("ontology changed");
        }
    }

}
