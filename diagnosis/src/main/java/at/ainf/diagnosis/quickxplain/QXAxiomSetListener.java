package at.ainf.diagnosis.quickxplain;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 06.12.12
 * Time: 17:39
 *
 *  A listener that stores complete set of axiom from a found set. This listener must be used if
 *  the multi-threading searcher is searching for all unique sets depth-first in all branches of a tree.
 */
public class QXAxiomSetListener<Id> extends AbstractAxiomListener<Id> {
    private LinkedList<Id> axioms = new LinkedList<Id>();

    public QXAxiomSetListener(boolean fairLock) {
        super(fairLock);
    }

    public void setFoundAxiom(Id axiom) {
        this.lock.lock();
        try {
            this.axioms.add(axiom);
            addedAxiom.signal();
        } finally {
            this.lock.unlock();
        }
    }



    public Id getFoundAxiom() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (!isReleased() && !hasAxioms())
                addedAxiom.await();
            if (hasAxioms())
                return this.axioms.pop();
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean hasAxioms() {
        return !this.axioms.isEmpty();
    }

    @Override
    public QXAxiomListener<Id> newInstance() {
        return new QXAxiomSetListener<Id>(this.lock.isFair());
    }
}
