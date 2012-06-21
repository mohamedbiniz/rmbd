package at.ainf.owlcontroller;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: pfleiss
 * Date: 27.05.12
 * Time: 13:35
 * To change this template use File | Settings | File Templates.
 */
public class ThreadTimeoutManager extends Thread {

    private long tout;

    private int maxThreads;

    List<Thread> threadList = new LinkedList<Thread>();

    private int numRunningThreads = 0;

    public ThreadTimeoutManager (long timeout, int maxNumThreads) {
        tout = timeout;
        maxThreads = maxNumThreads;
    }

    public void run() {
        while (!this.isInterrupted()) {
            if (numRunningThreads<maxThreads) {
                threadList.get(0).start();
            }
        }
    }

    public synchronized void submitThread (Thread t) {
        threadList.add(t);
    }


}
