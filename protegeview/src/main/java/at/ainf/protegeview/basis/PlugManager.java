package at.ainf.protegeview.basis;

import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.owl.OWLEditorKit;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.11.11
 * Time: 08:19
 * To change this template use File | Settings | File Templates.
 */
public class PlugManager {

    private static Map<OWLEditorKit,PlugManager> map = new HashMap<OWLEditorKit, PlugManager>();

    private static PlugManager actualPlugManager;

    OWLEditorKit editorKit;

    public static PlugManager getActualInstance () {
        return actualPlugManager;
    }

    public static void updateActualEditorKit(EditorKit ekit) {
        OWLEditorKit editorKit = (OWLEditorKit) ekit;
        if (map.get(editorKit) != null)
            actualPlugManager = map.get(editorKit);
         else
            actualPlugManager = map.put(editorKit,new PlugManager(editorKit));
    }

    private PlugManager(OWLEditorKit ek) {
        editorKit = ek;
    }



}
