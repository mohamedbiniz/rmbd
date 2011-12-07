package at.ainf.pluginprotege.testcasesentailmentsview.axiomeditor;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.02.11
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */
public class OWLAxiomChecker implements OWLExpressionChecker<Collection<OWLLogicalAxiom>> {
    private OWLModelManager mngr;


    public OWLAxiomChecker(OWLModelManager mngr) {
        this.mngr = mngr;
    }


    public void check(String text) throws OWLExpressionParserException {
        createObject(text);

    }


    public Collection<OWLLogicalAxiom> createObject(String text) throws OWLExpressionParserException {

        Collection<OWLLogicalAxiom> axioms = new TreeSet<OWLLogicalAxiom>();

        String[] splitted = text.split(",");
        for (String a : splitted) {
            ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(mngr.getOWLDataFactory(), a);
            parser.setOWLEntityChecker(new ProtegeOWLEntityChecker(mngr.getOWLEntityFinder()));
            try {
                OWLAxiom ax = parser.parseAxiom();
                axioms.add((OWLLogicalAxiom) ax);
                /*if (ax instanceof OWLAxiom) {
                       return (OWLLogicalAxiom) ax;
               }
               return null;*/
            } catch (ParserException e) {
                throw ParserUtil.convertException(e);
            }
        }
        return axioms;
    }
}



