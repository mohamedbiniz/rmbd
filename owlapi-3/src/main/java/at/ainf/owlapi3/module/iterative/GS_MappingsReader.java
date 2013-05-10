package at.ainf.owlapi3.module.iterative;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 11.03.13
 * Time: 10:29
 * To change this template use File | Settings | File Templates.
 */
public class GS_MappingsReader {

    public Set<OWLLogicalAxiom> loadGSmappings(String filename) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
            String line = reader.readLine();
            Set<OWLLogicalAxiom> mappings = new HashSet<OWLLogicalAxiom>();
            while (line != null) {
                String[] tok = line.split("\\|");
                OWLClass class1 = factory.getOWLClass(IRI.create(tok[0]));
                OWLClass class2 = factory.getOWLClass(IRI.create(tok[1]));
                mappings.add(factory.getOWLEquivalentClassesAxiom(class1,class2));
                line = reader.readLine();
            }
            reader.close();

            return mappings;
        }
        catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

}
