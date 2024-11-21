package com.renomad.minum.queue;

import com.renomad.minum.queue.model.IActionQueue;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class tracks the overall state of the {@link ActionQueue}s that
 * are in use throughout the system.  We need one central place to
 * track these, so that at system shutdown we can close them all cleanly.
 * <br>
 * As each ActionQueue gets created, it registers itself here.
 */
public class ActionQueueState {

    private final Queue<IActionQueue> aqQueue;

    public ActionQueueState() {
        aqQueue = new LinkedBlockingQueue<>();
    }

    public String aqQueueAsString() {
        return aqQueue.toString();
    }

    public void offerToQueue(IActionQueue actionQueue) {
        aqQueue.offer(actionQueue);
    }

    public IActionQueue pollFromQueue() {
        return aqQueue.poll();
    }

    public boolean isAqQueueEmpty() {
        return aqQueue.isEmpty();
    }

}
