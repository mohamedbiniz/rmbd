package at.ainf.owlapi3.performance;

import at.ainf.diagnosis.model.InconsistentTheoryException;
import at.ainf.diagnosis.model.SolverException;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 25.06.13
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */
public class OAEI12ConferenceTests extends OAEI11ConferenceTests {

    @Test
    public void doTestsOAEI12Conference()
            throws SolverException, InconsistentTheoryException, IOException, OWLOntologyCreationException {


        String matchingsDir = "oaei12conference/matcheralignments/";
        String ontologyDir = "oaei12conference/ontology";

        File[] f = getMappingFiles(matchingsDir, "incoherent", "incoherent.txt");
        File[] f2 = getMappingFiles(matchingsDir, "inconsistent", "inconsistent.txt");

        Set<File> files = new LinkedHashSet<File>();
        Map<File, String> map = new HashMap<File, String>();
        for (File file : f) {
            files.add(file);
            map.put(file, "incoherent");
        }
        for (File file : f2) {
            files.add(file);
            map.put(file, "inconsistent");
        }

        runOaeiConfereneTests(matchingsDir, ontologyDir, files, map);
    }

    @Override
    protected String getMatchingsDir() {
        return "/matcheralignments/";
    }

    @Override
    protected String getConferenceYear() {
        return "oaei12conference";
    }

}
