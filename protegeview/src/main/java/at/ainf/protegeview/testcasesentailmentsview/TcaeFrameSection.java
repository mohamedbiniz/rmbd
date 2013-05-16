package at.ainf.protegeview.testcasesentailmentsview;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.cls.AbstractOWLClassAxiomFrameSection;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.02.11
 * Time: 12:31
 * To change this template use File | Settings | File Templates.
 */
public class TcaeFrameSection extends AbstractOWLClassAxiomFrameSection<OWLLogicalAxiom, OWLLogicalAxiom> {

    //private static final String LABEL = "Add positive test case";

    //private Set<OWLLogicalAxiom> added = new TreeSet<OWLLogicalAxiom>();


    //public enum SectionType {PT, NT, NET, ET};
    SectionType type;


    public TcaeFrameSection(OWLEditorKit editorKit, OWLFrame<OWLClass> frame, SectionType type) {
        super(editorKit, "", "My Section", frame);

        this.type = type;
        setLabel(type.getLabel());

    }

    protected SectionType getSectionType() {
        return type;
    }

    protected void clear() {
        //added.clear();
    }

    public void setRootObject(OWLClassExpression rootObject) {

        /* System.out.println ("set root object was called");
        // getRows().clear();
        //clear();
        if (rootObject != null) {
            for (OWLOntology ontology : getOntologies()) {
                refill(ontology);
            }
            refillInferred();
        }
        getFrame().fireContentChanged(); */
    }


    /*protected void updateRow(int index, OWLLogicalAxiom ax) {
        actrows.set(index, new TcaeFrameSectionItem(getOWLEditorKit(), this, null, getRootObject(), ax));
    }*/

    protected void addAxiom(OWLLogicalAxiom ax, OWLOntology ont) {
        /*actrows.add(new TcaeFrameSectionItem(getOWLEditorKit(), this, ont, getRootObject(), ax)); */

        //getFrame().fireContentChanged();
    }

    protected Set<OWLLogicalAxiom> getClassAxioms(OWLClassExpression descr, OWLOntology ont) {
        if (!descr.isAnonymous()) {
            return null; //ont.getSubClassAxiomsForSubClass(descr.asOWLClass());
        } else {
            Set<OWLLogicalAxiom> axioms = new LinkedHashSet<OWLLogicalAxiom>();
            for (OWLLogicalAxiom ax : ont.getGeneralClassAxioms()) {
                if (ax.equals(descr)) {
                    axioms.add (ax);
                }
            }
            return axioms;
        }
    }

    /*public void setRootObject(OWLSubClassOfAxiom rootObject) {

        //added.add("1");
    }*/

    public void refill() {

    }

    protected void refillInferred() {
        /*getOWLModelManager().getReasonerPreferences().executeTask(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_SUPER_CLASSES, new Runnable() {
                public void runPostprocessor() {
                    if (getOWLModelManager().getReasoner().isSatisfiable(getRootObject())) {
                    	OWLClass thing = getOWLModelManager().getOWLDataFactory().getOWLThing();
                        for (SimpleNode<OWLClass> inferredSuperClasses : getOWLModelManager().getReasoner().getSuperClasses(getRootObject(), true)) {
                            for (OWLClassExpression inferredSuperClass : inferredSuperClasses) {
                                if (!added.contains(inferredSuperClass) && !thing.equals(inferredSuperClass)) {
                                    addRow(new OWLSubClassAxiomFrameSectionRow(getOWLEditorKit(),
                                                                               OWLSubClassAxiomFrameSection.this,
                                                                               null,
                                                                               getRootObject(),
                                                                               getOWLModelManager().getOWLDataFactory().getOWLSubClassOfAxiom(getRootObject(),
                                                                                                                                              inferredSuperClass)));
                                    added.add(inferredSuperClass);
                                }
                            }
                        }
                    }
                }
            });*/
    }


    protected OWLLogicalAxiom createAxiom(OWLLogicalAxiom object) {
        return object;
    }

    @Override
    public String toString() {
        return getSectionType().getLabel() + "  ";
    }

    public OWLObjectEditor<OWLLogicalAxiom> getObjectEditor() {   //public OWLObjectEditor<OWLClassExpression> getObjectEditor() {

        return null; //throw new NotImplementedException();
        //return getOWLEditorKit().getWorkspace().getOWLComponentFactory().getOWLClassDescriptionEditor(null, null);


        /*List<OWLClassExpressionEditorPlugin> descriptionEditorPlugins = null;
        if (descriptionEditorPlugins == null){
            OWLClassExpressionEditorPluginLoader loader = new OWLClassExpressionEditorPluginLoader(getOWLEditorKit());
            descriptionEditorPlugins = new ArrayList<OWLClassExpressionEditorPlugin>(loader.getPlugins());
            Comparator<OWLClassExpressionEditorPlugin> clsDescrPluginComparator = new Comparator<OWLClassExpressionEditorPlugin>(){
                public int compare(OWLClassExpressionEditorPlugin p1, OWLClassExpressionEditorPlugin p2) {
                    return p1.getIndex().compareTo(p2.getIndex());
                }
            };
            for (OWLClassExpressionEditorPlugin plugin : descriptionEditorPlugins)
                System.out.println(plugin.getId());

            Collections.sort(descriptionEditorPlugins, clsDescrPluginComparator);
        }*/


        /*for (OWLClassExpressionEditorPlugin plugin : descriptionEditorPlugins) {
            try {
                if (plugin.getId().equals("org.protege.axiomeditor.owl.OWLClassExpressionExpressionEditor")) {
                    OWLClassExpressionEditor editorPanel = plugin.newInstance();

                    editorPanel.initialise();
                    axiomeditor.addPanel(editorPanel);
                }
            }
            catch (Throwable e) { // be harsh if any problems with a plugin
                ProtegeApplication.getErrorLog().logError(e);
            }
        }*/

        /*OWLAxiomEditorPanel editorPan = new OWLAxiomEditorPanel();


        try {
            editorPan.setup("id","Class expression axiomeditor",getOWLEditorKit());
            editorPan.initialise();
        }
        catch(Exception exception) {
            System.err.println("Problem initialzing  ");
        }

        axiomeditor.addPanel(editorPan);*/

        /*  OWLAxiomEditor axiomeditor = new OWLAxiomEditor(getOWLEditorKit(), null);

        axiomeditor.addPanel();

        axiomeditor.selectPreferredEditor();

        return axiomeditor;*/

    }


    public boolean canAcceptDrop(List<OWLObject> objects) {
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLClassExpression)) {
                return false;
            }
        }
        return true;
    }


    private OWLObjectProperty prop;


    public boolean dropObjects(List<OWLObject> objects) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLObject obj : objects) {
            if (obj instanceof OWLClassExpression) {
                OWLClassExpression desc;
                if (prop != null) {
                    desc = getOWLDataFactory().getOWLObjectSomeValuesFrom(prop, (OWLClassExpression) obj);
                } else {
                    desc = (OWLClassExpression) obj;
                }
                OWLLogicalAxiom ax = getOWLDataFactory().getOWLSubClassOfAxiom(getRootObject(), desc);
                changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
            } else if (obj instanceof OWLObjectProperty) {
                // Prime
                prop = (OWLObjectProperty) obj;
            } else {
                return false;
            }
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }


    public void visit(OWLLogicalAxiom axiom) {
        if (axiom.equals(getRootObject())) {
            reset();
        }
    }


    public Comparator<OWLFrameSectionRow<OWLClassExpression, OWLLogicalAxiom, OWLLogicalAxiom>> getRowComparator() {
        return null;
    }




}
