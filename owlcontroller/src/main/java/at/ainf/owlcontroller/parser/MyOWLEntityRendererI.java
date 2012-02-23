package at.ainf.owlcontroller.parser;

/*import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;*/
//import org.protege.editor.owl.ui.renderer.AbstractOWLEntityRenderer;
//import org.protege.editor.owl.ui.renderer.RenderingEscapeUtils;
//import org.protege.editor.owl.ui.renderer.OWLEntityRendererListener;
//import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.semanticweb.owlapi.model.*;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.12.11
 * Time: 14:12
 * To change this template use File | Settings | File Templates.
 */
public class MyOWLEntityRendererI {
    public static String getEscapedRendering(String originalRendering) {
        if (originalRendering.indexOf(' ') != -1 || originalRendering.indexOf('(') != -1 || originalRendering.indexOf(
                ')') != -1) {
            return "'" + originalRendering + "'";
        }
        else {
            return originalRendering;
        }
    }


    public void initialise() {
        // do nothing
    }


    public String render(IRI iri) {
        try {
            String rendering = iri.getFragment();
            if (rendering == null) {
                // Get last bit of path
                String path = iri.toURI().getPath();
                if (path == null) {
                    return iri.toQuotedString();
                }
                return iri.toURI().getPath().substring(path.lastIndexOf("/") + 1);
            }
            return getEscapedRendering(rendering);
        }
        catch (Exception e) {
            return "<Error! " + e.getMessage() + ">";
        }
    }

    public boolean isConfigurable() {
    	return false;
    }

    /*public boolean configure(OWLEditorKit eKit) {
    	throw new IllegalStateException("This renderer is not configurable");
    }*/

    protected void disposeRenderer() {
        // do nothing
    }

    //private OWLModelManager mngr;

    private OWLOntologyChangeListener l = new OWLOntologyChangeListener(){
        public void ontologiesChanged(List<? extends OWLOntologyChange> owlOntologyChanges) throws OWLException {
            processChanges(owlOntologyChanges);
        }
    };

    /*public void setup(OWLModelManager owlModelManager){
        this.mngr = owlModelManager;
        mngr.addOntologyChangeListener(l);
    }*/



    // just wrap the render method
    public final String getShortForm(OWLEntity owlEntity) {
        return render(owlEntity);
    }

    public String render(OWLEntity owlEntity) {
        return render(owlEntity.getIRI());
    }


    protected void processChanges(List<? extends OWLOntologyChange> changes) {
    }


    /*protected void fireRenderingChanged(OWLEntity entity) {
        for (OWLEntityRendererListener listener : new ArrayList<OWLEntityRendererListener>(listeners)) {
            listener.renderingChanged(entity, this); }} */

    public void ontologiesChanged() {

    }


    final public void dispose() {
        //listeners.clear();
        //mngr.removeOntologyChangeListener(l);
        disposeRenderer();
    }



}
