package at.ainf.protegeview.debugmanager;

import at.ainf.theory.storage.Partition;
import at.ainf.theory.storage.HittingSet;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 14.10.11
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
public class LogToolsImp {

    ManchesterOWLSyntaxOWLObjectRendererImpl rend = new ManchesterOWLSyntaxOWLObjectRendererImpl();

    public String getRendering(Object o) {
        String result = rend.render((OWLObject) o);
        if (result.contains("\n")) {
            result=result.replaceAll("\n","");

        }
        return result;
    }

    public String getRenderingSet(Set set) {
        String result = "[";

        for (Object axiom : set)
            result += getRendering(axiom) + ", ";

        result = result.substring(0,result.length()-2);
        result +="]";
        return result;
    }

    public String renderQuery(Object queryObject) {
        String result = "";
        Partition<OWLLogicalAxiom> query =  (Partition<OWLLogicalAxiom>) queryObject  ;

        result += "Query: score = " + query.score + " " + getRenderingSet(query.partition) + " ";
        result += "DX: size=" + query.dx.size();
        double sum = 0.0;
        for (HittingSet hs : query.dx) {
            result += getHsRendering(hs) + " ";
            sum += hs.getMeasure();
        }
        result += "sum(p)=" + sum + " ";
        result += "DNX: size=" + query.dnx.size();
        sum = 0.0;
        for (HittingSet hs : query.dnx) {
            result += getHsRendering(hs) + " ";
            sum += hs.getMeasure();
        }
        result += "sum(p)=" + sum + " ";
        result += "D0: size=" + query.dz.size();
        sum = 0.0;
        for (HittingSet hs : query.dz) {
            result += getHsRendering(hs) + " ";
            sum += hs.getMeasure();
        }
        result += "sum(p)=" + sum + " ";

        return result;
    }

    public String getHsRendering(Object o) {
        String result = "";

        HittingSet<OWLLogicalAxiom> hs = (HittingSet<OWLLogicalAxiom>) o;
        result += "[";
        for (OWLLogicalAxiom axiom : hs)
            result += rend.render(axiom) + ", ";

        result += "p = " + hs.getMeasure() + "] ";
        if (result.contains("\n")) {
            result=result.replaceAll("\n","");
        }

        return result;
    }

}
