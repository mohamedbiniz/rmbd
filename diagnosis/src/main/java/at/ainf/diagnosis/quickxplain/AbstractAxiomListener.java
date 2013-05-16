package at.ainf.diagnosis.quickxplain;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 23.12.12
 * Time: 13:15
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAxiomListener<T> implements QXAxiomListener<T> {

    protected final ReentrantLock lock;
    protected final Condition addedAxiom;
    private boolean released = false;

    public AbstractAxiomListener(boolean fairLock) {
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
    public boolean isReleased() {
        return this.released;
    }
}
