package eu.cloudbutton.dobj.utils;

import jdk.internal.vm.annotation.ForceInline;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Carrier {

    public static Method currentCarrierThread;

    static {
        try {
            currentCarrierThread = Thread.class.getDeclaredMethod("currentCarrierThread");
            currentCarrierThread.setAccessible(true);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @ForceInline
    public static final int carrierID() {
        try {
            long id = ((Thread) currentCarrierThread.invoke(null)).threadId();
            return (int) id;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Thread[] carrierThreads() {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parentGroup;
        while ((parentGroup = rootGroup.getParent()) != null) {
            rootGroup = parentGroup;
        }
        Thread[] threads = new Thread[rootGroup.activeCount()];
        while (rootGroup.enumerate(threads, true ) == threads.length) {
            threads = new Thread[threads.length * 2];
        }
        // assert threads.length == Runtime.getRuntime().availableProcessors();
        return threads;
    }

    private static ThreadGroup rootThreadGroup = null;

    public static ThreadGroup getRootThreadGroup() {
		if (rootThreadGroup != null)
			return rootThreadGroup;

		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		ThreadGroup ptg;
		while ((ptg = tg.getParent()) != null)
			tg = ptg;
		rootThreadGroup = tg;
		return tg;
	}

    public static ThreadGroup[] getAllThreadGroups() {
		final ThreadGroup root = getRootThreadGroup();
		int nAlloc = root.activeGroupCount();
		int n = 0;
		ThreadGroup[] groups = null;
		do {
			nAlloc *= 2;
			groups = new ThreadGroup[nAlloc];
			n = root.enumerate(groups, true);
		} while (n == nAlloc);
		ThreadGroup[] allGroups = new ThreadGroup[n + 1];
		allGroups[0] = root;
		System.arraycopy(groups, 0, allGroups, 1, n);
		return allGroups;
	}

}
