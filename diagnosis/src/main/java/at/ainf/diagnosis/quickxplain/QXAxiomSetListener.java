package at.ainf.diagnosis.quickxplain;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 06.12.12
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
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
            while (!isReleased() && this.axioms.isEmpty())
                addedAxiom.await();
            if (!this.axioms.isEmpty())
                return this.axioms.pop();
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public QXAxiomListener<Id> newInstance() {
        return new QXAxiomSetListener<Id>(this.lock.isFair());
    }
}
