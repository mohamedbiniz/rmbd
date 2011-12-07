package at.ainf.pluginprotege.testcasesentailmentsview;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.AbstractOWLFrame;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 15.02.11
 * Time: 08:54
 * To change this template use File | Settings | File Templates.
 */
public class TcaeFrame extends AbstractOWLFrame<OWLClass> {

    public TcaeFrame(OWLEditorKit editorKit) {
        super(editorKit.getModelManager().getOWLOntologyManager());

        addSection(new TcaeFrameSection(editorKit, this, SectionType.PT));
        addSection(new TcaeFrameSection(editorKit, this, SectionType.NT));
        addSection(new TcaeFrameSection(editorKit, this, SectionType.ET));
        addSection(new TcaeFrameSection(editorKit, this, SectionType.NET));
    }
}
