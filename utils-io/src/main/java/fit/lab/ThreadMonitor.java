
package fit.lab;

import java.time.Duration;
import java.time.Instant;


class ThreadMonitor implements Runnable {

    private static int getNanosOfMiili(final Duration duration) {
        return duration.getNano() % 1_000_000;
    }
    
    static void sleep(final Duration duration) throws InterruptedException {
        final Instant finishInstant = Instant.now().plus(duration);
        Duration remainingDuration = duration;
        do {
            Thread.sleep(remainingDuration.toMillis(), getNanosOfMiili(remainingDuration));
            remainingDuration = Duration.between(Instant.now(), finishInstant);
        } while (!remainingDuration.isNegative());
    }

   
    static Thread start(final Duration timeout) {
        return start(Thread.currentThread(), timeout);
    }

   
    static Thread start(final Thread thread, final Duration timeout) {
        if (timeout.isZero() || timeout.isNegative()) {
            return null;
        }
        final Thread monitor = new Thread(new ThreadMonitor(thread, timeout), ThreadMonitor.class.getSimpleName());
        monitor.setDaemon(true);
        monitor.start();
        return monitor;
    }

   
    static void stop(final Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }

    private final Thread thread;

    private final Duration timeout;

    
    private ThreadMonitor(final Thread thread, final Duration timeout) {
        this.thread = thread;
        this.timeout = timeout;
    }

   
    @Override
    public void run() {
        try {
            sleep(timeout);
            thread.interrupt();
        } catch (final InterruptedException e) {
           
        }
    }
}
