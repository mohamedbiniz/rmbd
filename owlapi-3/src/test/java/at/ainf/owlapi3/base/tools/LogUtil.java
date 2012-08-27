package at.ainf.owlapi3.base.tools;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: kostya
 * Date: 06.05.11
 * Time: 00:19
 * To change this template use File | Settings | File Templates.
 */
public class LogUtil {

    public static String getStringTime(long millis) {
        long timeInHours = TimeUnit.MILLISECONDS.toHours(millis);
        long timeInMinutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long timeInSec = TimeUnit.MILLISECONDS.toSeconds(millis);
        long timeInMillisec = TimeUnit.MILLISECONDS.toMillis(millis);

        long hours = timeInHours;
        long minutes = timeInMinutes - TimeUnit.HOURS.toMinutes(timeInHours);
        long seconds = timeInSec - TimeUnit.MINUTES.toSeconds(timeInMinutes);
        long milliseconds = timeInMillisec - TimeUnit.SECONDS.toMillis(timeInSec);
        
        return String.format("%d , (%d h %d m %d s %d ms)", millis, hours, minutes, seconds, milliseconds);
    }
    
    /*public static String renderManyAxioms(Collection<OWLLogicalAxiom> axioms) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        String result = "";

        for (OWLLogicalAxiom axiom : axioms) {
            result += renderer.render(axiom) + "\n";
        }
        result = (String) result.subSequence(0,result.length()-2);

        return result; }*/

    public static String renderAxioms(Collection<OWLLogicalAxiom> axioms) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        String result = "";

        for (OWLLogicalAxiom axiom : axioms) {
            result += renderer.render(axiom) + ", ";
        }
        result = (String) result.subSequence(0,result.length()-2);

        return result;
    }

}
