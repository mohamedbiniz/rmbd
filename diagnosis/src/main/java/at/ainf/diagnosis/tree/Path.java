package at.ainf.diagnosis.tree;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chicco
 * Date: 27.03.13
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public class Path<E> {
    private Set<E> positivePath = new LinkedHashSet<E>();
    private Set<E> negativePath = new LinkedHashSet<E>();
    private boolean isExtended = false;

    public Set<E>  getPositivePath(){
        return positivePath;
    }

    public void setPositivePath(Set<E> positivePath){
        this.positivePath=positivePath;

    }

    public Set<E>  getNegativePath(){
        return negativePath;
    }

    public void setNegativePath(Set<E> negativePath){
        this.negativePath=negativePath;

    }

    public boolean isExtended(){
        return isExtended;
    }

    public void setExtended(boolean bool){
        this.isExtended=bool;
    }

}