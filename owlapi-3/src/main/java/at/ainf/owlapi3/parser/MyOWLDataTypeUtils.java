package at.ainf.owlapi3.parser;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pfleiss
 * Date: 12.12.11
 * Time: 13:53
 * To change this template use File | Settings | File Templates.
 */
public class MyOWLDataTypeUtils {
    private OWLOntologyManager mngr;


    public MyOWLDataTypeUtils(OWLOntologyManager mngr) {
        this.mngr = mngr;
    }


    public Set<OWLDatatype> getBuiltinDatatypes(){
        Set<OWLDatatype> datatypes = new HashSet<OWLDatatype>();
        final OWLDataFactory df = mngr.getOWLDataFactory();

        datatypes.add(df.getTopDatatype());
        for (OWL2Datatype dt : OWL2Datatype.values()) {
            datatypes.add(df.getOWLDatatype(dt.getIRI()));
        }
        return datatypes;
    }


    public Set<OWLDatatype> getReferencedDatatypes(Set<OWLOntology> onts){
        Set<OWLDatatype> referencedTypes = new HashSet<OWLDatatype>();
        for (OWLOntology ont : onts){
            referencedTypes.addAll(ont.getDatatypesInSignature());
        }
        return referencedTypes;
    }


    public Set<OWLDatatype> getKnownDatatypes(Set<OWLOntology> onts){
        Set<OWLDatatype> knownTypes = getBuiltinDatatypes();
        for (OWLOntology ont : onts){
            knownTypes.addAll(ont.getDatatypesInSignature());
        }
        return knownTypes;
    }
}
