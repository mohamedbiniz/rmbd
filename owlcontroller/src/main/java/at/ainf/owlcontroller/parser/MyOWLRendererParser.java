package at.ainf.owlcontroller.parser;

import at.ainf.theory.storage.AxiomRenderer;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.04.11
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
public class MyOWLRendererParser implements AxiomRenderer<OWLLogicalAxiom> {

    private static ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

    public static String render (OWLLogicalAxiom axiom) {
        return renderer.render (axiom);
    }

    private OWLOntology ontology;

    public MyOWLRendererParser (OWLOntology ontology) {
        this.ontology = ontology;
    }

    public OWLLogicalAxiom parse (String axiom) {
        try {
            ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(ontology.getOWLOntologyManager().getOWLDataFactory(), axiom);
            parser.setOWLEntityChecker(new MyOWLEntFnd(ontology));

            return (OWLLogicalAxiom) parser.parseAxiom();
        } catch (ParserException e) {
            return null;
        }

    }

    public String renderAxioms(Collection<OWLLogicalAxiom> axioms) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        String result = "";

        if (axioms == null)
            return "null";

        for (OWLLogicalAxiom axiom : axioms) {
            result += renderer.render(axiom) + ", ";
        }
        if (result.length()>2)
            result = (String) result.subSequence(0,result.length()-2);
        return result;
        /*if(axioms==null) return "";
        String r = "[";
        for(OWLLogicalAxiom a : axioms)
            r += renderer.render (a) + ", ";
        r = r.substring(0,r.length()-2)+ " ]";
        return r;*/

    }

    public String renderAxiom(OWLLogicalAxiom axiom) {
        if(axiom==null)  return "";
        return renderer.render (axiom);
    }
}
