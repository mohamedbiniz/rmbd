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
class QXAxiomSetListener<Id> {
    private LinkedList<Id> axioms = new LinkedList<Id>();
    private boolean released = false;
    private final ReentrantLock lock;
    private final Condition addedAxiom;

    QXAxiomSetListener(boolean fairLock) {
        this.lock = new ReentrantLock(fairLock);
        this.addedAxiom = lock.newCondition();
    }

    public void release() {
        this.lock.lock();
        try {
            this.released = true;
            addedAxiom.signal();
        } finally {
            this.lock.unlock();
        }
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
        Id axiom = null;
        try {
            while (!released && this.axioms.isEmpty())
                addedAxiom.await();
            if (!this.axioms.isEmpty())
                axiom = this.axioms.pop();
        } finally {
            lock.unlock();
        }
        return axiom;
    }
}
