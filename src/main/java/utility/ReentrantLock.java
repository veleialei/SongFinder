package utility;

import java.util.HashMap;
import java.util.Map;

/**
 * A read/write lock that allows multiple readers, disallows multiple writers,
 * and allows a writer to acquire a read lock while holding the write lock.
 * 
 * A writer may also aquire a second write lock.
 * 
 * A reader may not upgrade to a write lock.
 * 
 */

public class ReentrantLock {
	private Map<Thread, Integer> readThreads;
	private Thread writeThread;
	private int writers;

	/**
	 * Construct a new ReentrantLock.
	 */
	public ReentrantLock() {
		readThreads = new HashMap<Thread, Integer>();
		writers = 0;
		writeThread = null;
	}

	/**
	 * Returns true if the invoking thread holds a read lock.
	 * 
	 * @return
	 */
	public synchronized boolean hasRead() {
		Thread cur = Thread.currentThread();
		// check if current thread inside read threads
		if (readThreads.containsKey(cur)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the invoking thread holds a write lock.
	 * 
	 * @return
	 */
	public synchronized boolean hasWrite() {
		Thread cur = Thread.currentThread();
		// check if current thread inside read threads or is a write thread
		if (writeThread == cur) {
			return true;
		}
		return false;
	}

	/**
	 * Non-blocking method that attempts to acquire the read lock. Returns true if
	 * successful.
	 * 
	 * @return
	 */
	public synchronized boolean tryLockRead() {
		// check if write is hold
		Thread cur = Thread.currentThread();
		if (writers > 0 && cur != writeThread) {
			return false;
		}
		// add read thread into the map
		if (readThreads.containsKey(cur)) {
			readThreads.put(cur, readThreads.get(cur) + 1);
		} else {
			readThreads.put(cur, 1);
		}
		return true;
	}

	/**
	 * Non-blocking method that attempts to acquire the write lock. Returns true if
	 * successful.
	 * 
	 * @return
	 */
	public synchronized boolean tryLockWrite() {
		// TODO: Replace with your code.
		// check if write or read is hold
		Thread cur = Thread.currentThread();
		if (cur != writeThread && (!readThreads.isEmpty() || writers > 0)) {
			return false;
		}
		// add write thread;
		writers++;
		writeThread = cur;
		return true;
	}

	/**
	 * Blocking method that will return only when the read lock has been acquired.
	 */
	public synchronized void lockRead() {
		// TODO: Replace with your code.
		while (!tryLockRead()) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Releases the read lock held by the calling thread. Other threads may continue
	 * to hold a read lock.
	 */
	public synchronized void unlockRead() {
		// TODO: Replace with your code.
		// throw error if no read lock
		if (readThreads.isEmpty()) {
			throw new IllegalMonitorStateException();
		}
		Thread cur = Thread.currentThread();
		// remove current thread
		if (readThreads.get(cur) == 1) {
			readThreads.remove(cur);
		} else {
			readThreads.put(cur, readThreads.get(cur) - 1);
		}
		this.notifyAll();
	}

	/**
	 * Blocking method that will return only when the write lock has been acquired.
	 */
	public synchronized void lockWrite() {
		while (!tryLockWrite()) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Releases the write lock held by the calling thread. The calling thread may
	 * continue to hold a read lock.
	 */
	public synchronized void unlockWrite() {
		// throw error if no write lock
		if (writers <= 0) {
			throw new IllegalMonitorStateException();
		}
		writers--;
		// writers = 0, then remove the write lock
		if (writers == 0) {
			writeThread = null;
		}
		this.notifyAll();
	}
}