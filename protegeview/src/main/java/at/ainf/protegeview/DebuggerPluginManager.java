package at.ainf.protegeview;

import at.ainf.protegeview.basis.OntologyDiagnosisSearcher;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.08.12
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */
public class DebuggerPluginManager {

    private Map<OWLEditorKit,Map<OWLOntology,OntologyDiagnosisSearcher>> owlEditorKitMapperMap = new LinkedHashMap<OWLEditorKit, Map<OWLOntology,OntologyDiagnosisSearcher>>();

    public Map<OWLEditorKit,Map<OWLOntology,OntologyDiagnosisSearcher>> getOwlEditorKitMapperMap() {
        return owlEditorKitMapperMap;
    }

    private static DebuggerPluginManager instance;

    public static DebuggerPluginManager getInstance() {
        if (instance == null)
            instance = new DebuggerPluginManager();

        return instance;
    }

}
