
package fit.lab.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;


public final class FileAlterationMonitor implements Runnable {

    private static final FileAlterationObserver[] EMPTY_ARRAY = {};

    private final long intervalMillis;
    private final List<FileAlterationObserver> observers = new CopyOnWriteArrayList<>();
    private Thread thread;
    private ThreadFactory threadFactory;
    private volatile boolean running;

    
    public FileAlterationMonitor() {
        this(10_000);
    }

    
      @param intervalMillis 
    
    public FileAlterationMonitor(final long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    
    public FileAlterationMonitor(final long interval, final Collection<FileAlterationObserver> observers) {
        
        this(interval,
            Optional
                .ofNullable(observers)
                .orElse(Collections.emptyList())
                .toArray(EMPTY_ARRAY)
        );
        
    }

   
      @param interval The amount of time in milliseconds to wait between
      checks of the file system.
      @param observers The set of observers to add to the monitor.
     
    public FileAlterationMonitor(final long interval, final FileAlterationObserver... observers) {
        this(interval);
        if (observers != null) {
            for (final FileAlterationObserver observer : observers) {
                addObserver(observer);
            }
        }
    }

    
      @param observer The file system observer to add
     
    public void addObserver(final FileAlterationObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    
      @return the interval
     
    public long getInterval() {
        return intervalMillis;
    }

    
    public Iterable<FileAlterationObserver> getObservers() {
        return observers;
    }

    
      @param observer The file system observer to remove
     
    public void removeObserver(final FileAlterationObserver observer) {
        if (observer != null) {
            while (observers.remove(observer)) {
                
            }
        }
    }

    
    @Override
    public void run() {
        while (running) {
            for (final FileAlterationObserver observer : observers) {
                observer.checkAndNotify();
            }
            if (!running) {
                break;
            }
            try {
                Thread.sleep(intervalMillis);
            } catch (final InterruptedException ignored) {
            }
        }
    }

  
     @param threadFactory the thread factory
     
    public synchronized void setThreadFactory(final ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    
    public synchronized void start() throws Exception {
        if (running) {
            throw new IllegalStateException("Monitor is already running");
        }
        for (final FileAlterationObserver observer : observers) {
            observer.initialize();
        }
        running = true;
        if (threadFactory != null) {
            thread = threadFactory.newThread(this);
        } else {
            thread = new Thread(this);
        }
        thread.start();
    }

    /
    public synchronized void stop() throws Exception {
        stop(intervalMillis);
    }

   
    public synchronized void stop(final long stopInterval) throws Exception {
        if (!running) {
            throw new IllegalStateException("Monitor is not running");
        }
        running = false;
        try {
            thread.interrupt();
            thread.join(stopInterval);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (final FileAlterationObserver observer : observers) {
            observer.destroy();
        }
    }
}
