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
public class QXSingleAxiomListener<Id> implements QXAxiomListener<Id> {
    Id axiom = null;
    boolean released = false;
    final ReentrantLock lock;
    private final Condition addedAxiom;

    public QXSingleAxiomListener(boolean fairLock) {
        this.lock = new ReentrantLock(fairLock);
        this.addedAxiom = lock.newCondition();
    }



    @Override
    public void release() {
        this.lock.lock();
        try {
            this.released = true;
            addedAxiom.signal();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
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

    @Override
    public Id getFoundAxiom() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (!released && this.axiom == null)
                addedAxiom.await();
        } finally {
            lock.unlock();
        }
        return this.axiom;
    }

    @Override
    public QXAxiomListener<Id> newInstance() {
        return new QXSingleAxiomListener<Id>(this.lock.isFair());
    }
}
