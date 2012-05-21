package at.ainf.owlcontroller;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 21.05.12
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class SearchConfiguration {

    public static enum SearchType {UNIFORM_COST, BREATHFIRST};

    public static enum TreeType {REITER, DUAL};

    public static enum QSS {MINSCORE, SPLIT, DYNAMIC};


    public Boolean aBoxInBG;
    public Boolean tBoxInBG;
    public SearchType searchType;
    public TreeType treeType;
    public Integer numOfLeadingDiags;
    public QSS qss;
    public Boolean reduceIncoherency;
    public Boolean minimizeQuery;
    public Boolean calcAllDiags;

    public Boolean inclEntSubClass;
    public Boolean incEntClassAssert;
    public Boolean incEntEquivClass;
    public Boolean incEntDisjClasses;
    public Boolean incEntPropAssert;
    public Boolean incOntolAxioms;
    public Boolean incAxiomsRefThing;

    public Double entailmentCalThres;




}
