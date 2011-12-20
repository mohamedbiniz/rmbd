package at.ainf.protegeview.testcasesentailmentsview;

import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditorHandler;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.02.11
 * Time: 13:00
 * To change this template use File | Settings | File Templates.
 */
public class TcaeFrameSectionItem implements OWLFrameSectionRow<OWLClassExpression, OWLLogicalAxiom, Collection<OWLLogicalAxiom>>, OWLObjectEditorHandler<Collection<OWLLogicalAxiom>> {

    private SectionType type;

    private Set<OWLLogicalAxiom> axioms;

    public static final String DEFAULT_DELIMETER = ", ";

    public static final String DEFAULT_PREFIX = "";

    public static final String DEFAULT_SUFFIX = "";

    private OWLEditorKit owlEditorKit;

    private OWLOntology ontology;

    private OWLClassExpression rootObject;

    protected OWLLogicalAxiom axiom;

    private Object userObject;

    private OWLFrameSection section;

    //  OWLClassExpression = R extends Object // OWLLogicalAxiom = A extends OWLAxiom // Collection<OWLLogicalAxiom> = E


    public TcaeFrameSectionItem(OWLEditorKit owlEditorKit, OWLFrameSection section, OWLOntology ontology,
                                OWLClassExpression rootObject, Set<OWLLogicalAxiom> axiom) {
        this.owlEditorKit = owlEditorKit;
        this.section = section;
        this.ontology = ontology;
        this.rootObject = rootObject;
        this.axioms = axiom;
        type = ((TcaeFrameSection) section).getSectionType();
    }

    public TcaeFrameSectionItem(OWLEditorKit owlEditorKit, OWLFrameSection section, OWLOntology ontology,
                                OWLClassExpression rootObject, OWLLogicalAxiom axiom) {
        this.owlEditorKit = owlEditorKit;
        this.section = section;
        this.ontology = ontology;
        this.rootObject = rootObject;
        this.axioms = new TreeSet<OWLLogicalAxiom>();
        this.axioms.add(axiom);

        type = ((TcaeFrameSection) section).getSectionType();
    }

    public SectionType getSectionType() {
        return type;
    }


    public Set<OWLLogicalAxiom> getAxioms() {
        return axioms;
    }

    public void setAxiom(Set<OWLLogicalAxiom> a) {
        this.axioms = a;
    }

    protected OWLObjectEditor<Collection<OWLLogicalAxiom>> getObjectEditor() {
        return null; //throw new NotImplementedException();

        //getOWLEditorKit().getWorkspace().getOWLComponentFactory().getOWLClassDescriptionEditor(getAxiom().getSuperClass(), AxiomType.SUBCLASS_OF);
    }


    protected Collection<OWLLogicalAxiom> createAxiom(Collection<OWLLogicalAxiom> editedObject) {
        return editedObject;
    }


    /**
     * Gets a list of objects contained in this row.
     */
    public List<OWLLogicalAxiom> getManipulatableObjects() {
        return new ArrayList<OWLLogicalAxiom>(axioms);
    }

    public boolean isDeleteable() {
        return true;
    }

    public String getTooltip() {
        switch (type) {
            case PT:
                return "Positive test case";
            case NT:
                return "Negative test case";
            case NET:
                return "Not entailed";
            case ET:
                return "entailed";
            default:
                return null;
        }
    }


    public boolean isEditable() {
        return true;
    }

    public OWLFrameSection getFrameSection() {
        return section;
    }


    public OWLClassExpression getRootObject() {
        return rootObject;
    }


    /**
     * Default implementation which returns <code>null</code>
     */
    public Object getUserObject() {
        return userObject;
    }


    public void setUserObject(Object object) {
        this.userObject = object;
    }


    public boolean isFixedHeight() {
        return false;
    }

    final public OWLObjectEditor<Collection<OWLLogicalAxiom>> getEditor() {
        OWLObjectEditor<Collection<OWLLogicalAxiom>> editor = getObjectEditor();
        if (editor != null) {
            editor.setHandler(this);
        }
        return editor;
    }


    public boolean checkEditorResults(OWLObjectEditor<Collection<OWLLogicalAxiom>> editor) {
        return true;
    }

    public void handleEditingFinished(Set<Collection<OWLLogicalAxiom>> editedObjects) {
        if (editedObjects.isEmpty()) {
            return;
        }
        Collection<OWLLogicalAxiom> axioms = editedObjects.iterator().next();
        for (OWLLogicalAxiom axiom : axioms) {
            OWLAxiom newAxiom = axiom;
            if (newAxiom != null) { // the editor should protect from this, but just in case
                OWLLogicalAxiom oldAxiom = getAxiom();
                Set<OWLAnnotation> axiomAnnotations = oldAxiom.getAnnotations();
                if (axiomAnnotations != null && !axiomAnnotations.isEmpty()) {
                    newAxiom = newAxiom.getAnnotatedAxiom(axiomAnnotations);
                }
                List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
                changes.add(new RemoveAxiom(getOntology(), oldAxiom));
                changes.add(new AddAxiom(getOntology(), newAxiom));
                getOWLModelManager().applyChanges(changes);
            }
        }

    }


    /**
     * This row represents an assertion in a particular ontology.
     * This gets the ontology that the assertion belongs to.
     */
    public OWLOntology getOntology() {
        return ontology;
    }


    public OWLModelManager getOWLModelManager() {
        return owlEditorKit.getModelManager();
    }


    public OWLDataFactory getOWLDataFactory() {
        return getOWLModelManager().getOWLDataFactory();
    }


    public OWLOntologyManager getOWLOntologyManager() {
        return getOWLModelManager().getOWLOntologyManager();
    }


    public OWLEditorKit getOWLEditorKit() {
        return owlEditorKit;
    }


    /**
     * Gets the root object of the frame that this row belongs to.
     */
    public OWLClassExpression getRoot() {
        return rootObject;
    }


    /**
     * Gets the object that the row holds.
     */
    public OWLLogicalAxiom getAxiom() {

        return axioms.iterator().next();

    }


    public boolean canAcceptDrop(List<OWLObject> objects) {
        return false;
    }


    public boolean dropObjects(List<OWLObject> objects) {
        return false;
    }


    public String toString() {
        return getRendering();
    }


    /**
     * Deletes this row.  This will alter the underlying model of which
     * this row is a representation.  This method should not be called
     * if the <code>isEditable</code> method returns <code>false</code>.
     */
    public List<? extends OWLOntologyChange> getDeletionChanges() {
        if (isDeleteable()) {
            return Arrays.asList(new RemoveAxiom(getOntology(), getAxiom()));
        } else {
            return Collections.emptyList();
        }
    }


    public String getPrefix() {
        return DEFAULT_PREFIX;
    }


    public String getDelimeter() {
        return DEFAULT_DELIMETER;
    }


    public String getSuffix() {
        return DEFAULT_SUFFIX;
    }


    protected Object getObjectRendering(OWLObject ob) {
        return getOWLModelManager().getRendering(ob);
    }


    public boolean isInferred() {
        return ontology == null;
    }


    /**
     * Gets the rendering of the value of a particular column.
     *
     * @return The <code>String</code> representation of the column
     *         value.
     */
    public String getRendering() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPrefix());
        for (Iterator<? extends OWLObject> it = getManipulatableObjects().iterator(); it.hasNext();) {
            OWLObject obj = it.next();
            sb.append(getObjectRendering(obj));
            if (it.hasNext()) {
                sb.append(getDelimeter());
            }
        }
        sb.append(getSuffix());
        return sb.toString();
    }


    public void handleEdit() {

    }


    public boolean handleDelete() {
        return false;
    }

    public List<MListButton> getAdditionalButtons() {
        return Collections.emptyList();
    }

}
