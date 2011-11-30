package at.ainf.pluginprotege.debugmanager;

import at.ainf.diagnosis.storage.HittingSet;
import at.ainf.pluginprotege.views.ResultsListSection;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import javax.swing.event.EventListenerList;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 08.08.11
 * Time: 13:23
 * To change this template use File | Settings | File Templates.
 */
public class DebugManager {

    private static DebugManager manager = null;

    protected EventListenerList listenerList = new EventListenerList();





    private Set<? extends HittingSet<OWLLogicalAxiom>> validHittingSets;

    private Set<Set<OWLLogicalAxiom>> conflictSets = null;

    private OWLLogicalAxiom axiom;

    private HittingSet<OWLLogicalAxiom> treeNode;

    private Set<ResultsListSection> entSets = null;

    private int conflictSetSelected;

    public void reset() {
        setValidHittingSets(null);
        notifyHittingSetsChanged();
        setConflictSets(null);
        notifyConflictSetsChanged();
        setAxiom(null);
        notifyAxiomChanged();
    }

    public Set<? extends HittingSet<OWLLogicalAxiom>> getValidHittingSets() {
        return validHittingSets;
    }

    public <E extends HittingSet<OWLLogicalAxiom>> void setValidHittingSets(Set<E> validHittingSets) {
        this.validHittingSets = validHittingSets;
    }

    public Set<Set<OWLLogicalAxiom>> getConflictSets() {
        return conflictSets;
    }

    public void setConflictSets(Set<Set<OWLLogicalAxiom>> conflictSets) {
        this.conflictSets = conflictSets;
    }

    public OWLLogicalAxiom getAx() {
       return axiom;
    }

    public void setAxiom(OWLLogicalAxiom a) {
        this.axiom = a;
    }

    public Set<ResultsListSection> getEntSets() {
        return entSets;
    }

    public void setEntSets(Set<ResultsListSection> entSets) {
        this.entSets = entSets;
    }

    public HittingSet<OWLLogicalAxiom> getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(HittingSet<OWLLogicalAxiom> treeNode) {
        this.treeNode = treeNode;
    }

    public int getConflictSetSelected() {
        return conflictSetSelected;
    }

    public void setConflictSetSelected(int conflictSetSelected) {
        this.conflictSetSelected = conflictSetSelected;
    }

    public static DebugManager getInstance() {
        if (manager == null) {
            manager = new DebugManager();
        }

        return manager;
    }


    public void addHittingSetsChangedListener(HittingSetsChangedListener l) {
        listenerList.add(HittingSetsChangedListener.class, l);
    }

    public void addConflictSetsChangedListener(ConflictSetsChangedListener l) {
        listenerList.add(ConflictSetsChangedListener.class, l);
    }

    public void addAxiomChangedListener(AxiomChangedListener l) {
        listenerList.add(AxiomChangedListener.class, l);
    }

    public void addTreeNodeChangedListener(TreeNodeChangedListener l) {
        listenerList.add(TreeNodeChangedListener.class, l);
    }

    public void addConflictSetSelectedListener(ConflictSetSelectedListener l) {
        listenerList.add(ConflictSetSelectedListener.class, l);
    }

    public void addResetReqListener(ResetReqListener l) {
        listenerList.add(ResetReqListener.class, l);
    }

    public void addEntailmentsShowListener(EntailmentsShowListener l) {
        listenerList.add(EntailmentsShowListener.class, l);
    }

   public void removeHittingSetsChangedListener(HittingSetsChangedListener l) {
       listenerList.remove(HittingSetsChangedListener.class, l);
   }

   public void removeConflictSetsChangedListener(ConflictSetsChangedListener l) {
       listenerList.remove(ConflictSetsChangedListener.class, l);
   }

    public void removeAxiomChangedListener(AxiomChangedListener l) {
        listenerList.remove(AxiomChangedListener.class, l);
    }

    public void removeTreeNodeChangedListener(TreeNodeChangedListener l) {
       listenerList.remove(TreeNodeChangedListener.class, l);
    }

    public void removeConflictSetSelectedListener(ConflictSetSelectedListener l) {
       listenerList.remove(ConflictSetSelectedListener.class, l);
    }

    public void removeResetReqListener(ResetReqListener l) {
       listenerList.remove(ResetReqListener.class, l);
    }

    public void removeEntailmentsShowListener(EntailmentsShowListener l) {
        listenerList.remove(EntailmentsShowListener.class,l);
    }


    public void notifyAxiomChanged() {

       AxiomChangedEvent axiomChangedEvent = null;
       Object[] listeners = listenerList.getListenerList();
       for (int i = listeners.length-2; i>=0; i-=2) {
           if (listeners[i]==AxiomChangedListener.class) {
               // Lazily create the event:
               if (axiomChangedEvent == null)
                   axiomChangedEvent = new AxiomChangedEvent(this,axiom);
               ((AxiomChangedListener)listeners[i+1]).axiomChanged(axiomChangedEvent);
           }
       }
       axiomChangedEvent=null;
   }

    public void notifyConflictSetsChanged() {

       ConflictSetsChangedEvent conflictSetsChangedEvent = null;
       Object[] listeners = listenerList.getListenerList();
       for (int i = listeners.length-2; i>=0; i-=2) {
           if (listeners[i]==ConflictSetsChangedListener.class) {
               // Lazily create the event:
               if (conflictSetsChangedEvent == null)
                   conflictSetsChangedEvent = new ConflictSetsChangedEvent(this,getConflictSets());
               ((ConflictSetsChangedListener)listeners[i+1]).conflictSetsChanged(conflictSetsChangedEvent);
           }
       }
       conflictSetsChangedEvent=null;
    }

    public void notifyHittingSetsChanged() {

       HittingSetsChangedEvent hittingSetsChangedEvent = null;
       Object[] listeners = listenerList.getListenerList();
       for (int i = listeners.length-2; i>=0; i-=2) {
           if (listeners[i]==HittingSetsChangedListener.class) {
               // Lazily create the event:
               if (hittingSetsChangedEvent == null)
                   hittingSetsChangedEvent = new HittingSetsChangedEvent(this,getValidHittingSets());
               ((HittingSetsChangedListener)listeners[i+1]).hittingSetsChanged(hittingSetsChangedEvent);
           }
       }
       hittingSetsChangedEvent=null;
    }

    public void notifyTreeNodeChanged() {

       TreeNodeChangedEvent treeNodeChangedEvent = null;
       Object[] listeners = listenerList.getListenerList();
       for (int i = listeners.length-2; i>=0; i-=2) {
           if (listeners[i]==TreeNodeChangedListener.class) {
               // Lazily create the event:
               if (treeNodeChangedEvent == null)
                   treeNodeChangedEvent = new TreeNodeChangedEvent(this, getTreeNode());
               ((TreeNodeChangedListener)listeners[i+1]).treeNodeChanged(treeNodeChangedEvent);
           }
       }
       treeNodeChangedEvent =null;
    }

    public void notifyResetReq() {

       ResetReqEvent resetReqEvent = null;
       Object[] listeners = listenerList.getListenerList();
       for (int i = listeners.length-2; i>=0; i-=2) {
           if (listeners[i]==ResetReqListener.class) {
               // Lazily create the event:
               if (resetReqEvent == null)
                   resetReqEvent = new ResetReqEvent(this);
               ((ResetReqListener)listeners[i+1]).processResetReq(resetReqEvent);
           }
       }
       resetReqEvent =null;
    }

    public void notifyConflictSetSelected() {

       ConflictSetSelectedEvent conflictSetSelectedEvent = null;
       Object[] listeners = listenerList.getListenerList();
       for (int i = listeners.length-2; i>=0; i-=2) {
           if (listeners[i]==ConflictSetSelectedListener.class) {
               // Lazily create the event:
               if (conflictSetSelectedEvent == null)
                   conflictSetSelectedEvent = new ConflictSetSelectedEvent(this, getConflictSetSelected());
               ((ConflictSetSelectedListener)listeners[i+1]).conflictSetSelected(conflictSetSelectedEvent);
           }
       }
       conflictSetSelectedEvent =null;
    }

    public void notifyEntSetSelected() {

       EntailmentsShowEvent entailmentsShowEvent = null;
       Object[] listeners = listenerList.getListenerList();
       for (int i = listeners.length-2; i>=0; i-=2) {
           if (listeners[i]==EntailmentsShowListener.class) {
               // Lazily create the event:
               if (entailmentsShowEvent == null)
                   entailmentsShowEvent = new EntailmentsShowEvent(this, entSets);
               ((EntailmentsShowListener)listeners[i+1]).entailmentSetChanged(entailmentsShowEvent);
           }
       }
       entailmentsShowEvent = null;
    }
}
