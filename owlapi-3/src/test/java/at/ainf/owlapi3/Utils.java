package at.ainf.owlapi3;

import at.ainf.diagnosis.model.SolverException;
import at.ainf.diagnosis.model.UnsatisfiableFormulasException;
import at.ainf.owlapi3.model.OWLTheory;
import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 06.05.11
 * Time: 00:19
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    public static OWLTheory loadTheory(OWLOntologyManager manager, String path) throws SolverException, UnsatisfiableFormulasException, OWLOntologyCreationException {
        InputStream st = ClassLoader.getSystemResourceAsStream(path);
        return createTheory(manager.loadOntologyFromOntologyDocument(st));
    }

    public static <E extends Set<OWLLogicalAxiom>> String logCollection(Logger logger, String name, Set<E> col) {
        StringBuilder buf = new StringBuilder();
        //TreeSet<E> col  = new TreeSet<E>();
        //col.addAll(col1);
        buf.append(name).append(" {");
        for (Iterator<? extends Set<OWLLogicalAxiom>> sub = col.iterator(); sub.hasNext(); ) {
            buf.append(" {");
            buf.append(logCollection(sub.next()));
            if (sub.hasNext())
                buf.append(",");

        }
        buf.append(" }");
        String message = buf.toString();
        logger.info(message);
        return message;
    }

    public static String logCollection(Set<OWLLogicalAxiom> sub) {
        //TreeSet<OWLLogicalAxiom> sub  = new TreeSet<OWLLogicalAxiom>();
        //sub.addAll(sub1);
        StringBuilder buf = new StringBuilder();
        for (Iterator<OWLLogicalAxiom> iter = sub.iterator(); iter.hasNext(); ) {
            OWLLogicalAxiom ax = iter.next();
            OWLClass cls;
            switch (ax.getAxiomType().getIndex()) {
                case 1:
                    OWLClass cl = ((OWLEquivalentClassesAxiom) ax).getNamedClasses().iterator().next();
                    buf.append(cl.asOWLClass().getIRI().getFragment());
                    break;
                case 2:
                    OWLClassExpression cle = ((OWLSubClassOfAxiom) ax).getSubClass();
                    buf.append(cle.asOWLClass().getIRI().getFragment());
                    break;
                case 3:
                    buf.append("D[ ");
                    Set<OWLClass> dja = ax.getClassesInSignature();
                    for (OWLClass ocl : dja)
                        buf.append(ocl.getIRI().getFragment()).append(" ");
                    buf.append("]");
                    break;
                case 5:
                    cls = ax.getClassesInSignature().iterator().next();
                    OWLIndividual ind = ((OWLClassAssertionAxiom) ax).getIndividual();
                    buf.append(cls.getIRI().getFragment()).append("(").append(ind.asOWLNamedIndividual()
                            .getIRI().getFragment()).append(")");
                    break;
                default:
                    buf.append(ax.getAxiomType());
                    for (Iterator<OWLEntity> iterator = ax.getSignature().iterator(); iterator.hasNext(); ) {
                        OWLEntity next = iterator.next();
                        buf.append(" [").append(next.getIRI().getFragment()).append("] ");
                    }
                    //throw new RuntimeException(ax.getAxiomType() + " has unknown index " + ax.getAxiomType().getIndex() + " !");
            }
            if (iter.hasNext())
                buf.append(",");
        }
        buf.append("}");
        return buf.toString();
    }


    public static OWLTheory createTheory(OWLOntology ontology) throws SolverException, UnsatisfiableFormulasException {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        assert (theory.isConsistent());

        return theory;
    }
}
