package at.ainf.diagnosis.quickxplain;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: kostya
 * Date: 06.12.12
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
class QXAxiomListener<Id> {
    Id axiom = null;
    boolean released = false;
    final ReentrantLock lock;
    private final Condition addedAxiom;

    QXAxiomListener(boolean fairLock) {
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
        if (this.axiom != null)
            return;
        this.lock.lock();
        try {
            this.axiom = axiom;
            addedAxiom.signal();
        } finally {
            this.lock.unlock();
        }
    }

    public Id getFoundAxiom() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (!released && this.axiom == null)
                addedAxiom.await();
            return this.axiom;
        } finally {
            lock.unlock();
        }
    }
}
