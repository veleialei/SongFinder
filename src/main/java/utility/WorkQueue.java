package utility;

import java.util.LinkedList;

public class WorkQueue {
	private final PoolWorker[] threads;
	private final LinkedList<Runnable> queue;
	private volatile boolean isClosed;

	public WorkQueue(int nThreads) {
		queue = new LinkedList<Runnable>();
		threads = new PoolWorker[nThreads];
		isClosed = false;
		for (int i = 0; i < nThreads; i++) {
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	/**
	 * Execute the task
	 * 
	 * @param r
	 */
	public void execute(Runnable r) {
		if (!isClosed) {
			synchronized (queue) {
				queue.addLast(r);
				queue.notify();
			}
		}
	}

	/**
	 * tell the work queue, no more new task
	 */
	public void shutdown() {
		isClosed = true;
	}

	/**
	 * wait until all the tasks finish
	 */
	public void awaitTermination() {
		synchronized (queue) {
			queue.notifyAll();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class PoolWorker extends Thread {
		public void run() {
			Runnable r;
			while (true) {
				synchronized (queue) {
					// if queue is empty but not closed, wait for new task enter queue
					while (queue.isEmpty() && isClosed == false) {
						try {
							queue.wait();
						} catch (InterruptedException ignored) {
						}
					}
					// if queue is closed and no more new work, get out of the loop
					if (isClosed && queue.isEmpty())
						break;
					r = (Runnable) queue.removeFirst();
				}
				// If we don't catch RuntimeException,
				// the pool could leak threads
				try {
					r.run();
				} catch (RuntimeException e) {
					// You might want to log something here
				}
			}
		}
	}
}