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
public class QXSingleAxiomListener<Id> extends AbstractAxiomListener<Id> {
    private Id axiom = null;

    public QXSingleAxiomListener(boolean fairLock) {
        super(fairLock);
    }

    @Override
    public Id getFoundAxiom() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();

        try {
            while (!isReleased() && this.axiom == null)
                addedAxiom.await();
        } finally {
            lock.unlock();
        }
        release();
        Id localAxiom = this.axiom;
        this.axiom = null;
        return localAxiom;
    }

    @Override
    public void setFoundAxiom(Id axiom) {
        if (isReleased() || this.axiom != null)
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
    public QXAxiomListener<Id> newInstance() {
        return new QXSingleAxiomListener<Id>(this.lock.isFair());
    }

    @Override
    public boolean hasAxioms() {
        return this.axiom != null;
    }
}
