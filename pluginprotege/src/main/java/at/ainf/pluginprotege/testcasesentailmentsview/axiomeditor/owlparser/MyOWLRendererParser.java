package at.ainf.pluginprotege.testcasesentailmentsview.axiomeditor.owlparser;

import at.ainf.pluginprotege.testcasesentailmentsview.axiomeditor.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 18.04.11
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
public class MyOWLRendererParser {

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



}
