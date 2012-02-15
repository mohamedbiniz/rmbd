package at.ainf.owlcontroller;

import at.ainf.owlapi3.model.OWLTheory;
import at.ainf.theory.model.InconsistentTheoryException;
import at.ainf.theory.model.SolverException;
import org.apache.log4j.Logger;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 06.05.11
 * Time: 00:19
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    private static ManchesterOWLSyntax[] keywords = {ManchesterOWLSyntax.SOME,
                ManchesterOWLSyntax.ONLY,
                ManchesterOWLSyntax.MIN,
                ManchesterOWLSyntax.MAX,
                ManchesterOWLSyntax.EXACTLY,
                ManchesterOWLSyntax.AND,
                ManchesterOWLSyntax.OR,
                ManchesterOWLSyntax.NOT,
                ManchesterOWLSyntax.VALUE,
                ManchesterOWLSyntax.INVERSE,
                ManchesterOWLSyntax.SUBCLASS_OF,
                ManchesterOWLSyntax.EQUIVALENT_TO,
                ManchesterOWLSyntax.DISJOINT_CLASSES,
                ManchesterOWLSyntax.DISJOINT_WITH,
                ManchesterOWLSyntax.FUNCTIONAL,
                ManchesterOWLSyntax.INVERSE_OF,
                ManchesterOWLSyntax.SUB_PROPERTY_OF,
                ManchesterOWLSyntax.SAME_AS,
                ManchesterOWLSyntax.DIFFERENT_FROM,
                ManchesterOWLSyntax.RANGE,
                ManchesterOWLSyntax.DOMAIN,
                ManchesterOWLSyntax.TYPE,
                ManchesterOWLSyntax.TRANSITIVE,
                ManchesterOWLSyntax.SYMMETRIC
        };

    public static HashMap<ManchesterOWLSyntax, Double> getProbabMap() {
        HashMap<ManchesterOWLSyntax, Double> map = new HashMap<ManchesterOWLSyntax, Double>();

        for (ManchesterOWLSyntax keyword : keywords) {
            map.put(keyword, 0.01);
        }

        map.put(ManchesterOWLSyntax.SOME, 0.05);
        map.put(ManchesterOWLSyntax.ONLY, 0.05);
        map.put(ManchesterOWLSyntax.AND, 0.001);
        map.put(ManchesterOWLSyntax.OR, 0.001);
        map.put(ManchesterOWLSyntax.NOT, 0.01);

        return map;
    }

    public static OWLTheory loadTheory(OWLOntologyManager manager, String path) throws SolverException, InconsistentTheoryException, OWLOntologyCreationException {
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

    public static OWLTheory createTheory(OWLOntology ontology) throws SolverException, InconsistentTheoryException {
        Set<OWLLogicalAxiom> bax = new HashSet<OWLLogicalAxiom>();
        for (OWLIndividual ind : ontology.getIndividualsInSignature()) {
            bax.addAll(ontology.getClassAssertionAxioms(ind));
            bax.addAll(ontology.getObjectPropertyAssertionAxioms(ind));
        }

        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLTheory theory = new OWLTheory(reasonerFactory, ontology, bax);
        assert (theory.verifyRequirements());

        return theory;
    }

    public static String getStringTime(long millis) {
        long timeInHours = TimeUnit.MILLISECONDS.toHours(millis);
        long timeInMinutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long timeInSec = TimeUnit.MILLISECONDS.toSeconds(millis);
        long timeInMillisec = TimeUnit.MILLISECONDS.toMillis(millis);

        long hours = timeInHours;
        long minutes = timeInMinutes - TimeUnit.HOURS.toMinutes(timeInHours);
        long seconds = timeInSec - TimeUnit.MINUTES.toSeconds(timeInMinutes);
        long milliseconds = timeInMillisec - TimeUnit.SECONDS.toMillis(timeInSec);
        
        return String.format("%d ms (%d h %d m, %d s %d ms)", millis, hours, minutes, seconds, milliseconds);
    }
    
    public static String renderManyAxioms(Collection<OWLLogicalAxiom> axioms) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        String result = "";

        for (OWLLogicalAxiom axiom : axioms) {
            result += renderer.render(axiom) + "\n";
        }
        result = (String) result.subSequence(0,result.length()-2);

        return result;
    }

    public static String renderAxioms(Set<OWLLogicalAxiom> axioms) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        String result = "";

        for (OWLLogicalAxiom axiom : axioms) {
            result += renderer.render(axiom) + ", ";
        }
        result = (String) result.subSequence(0,result.length()-2);

        return result;
    }
}
