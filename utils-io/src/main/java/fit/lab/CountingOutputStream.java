package main.java.fit.lab;

import java.io.OutputStream;

public class CountingOutputStream extends ProxyOutputStream {

        private long count = 0;

        public CountingOutputStream( OutputStream out ) {
        super(out);
    }


        @Override
    protected synchronized void beforeWrite(int n) {
        count += n;
    }

        public int getCount() {
        long result = getByteCount();
        if (result > Integer.MAX_VALUE) {
            throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
        }
        return (int) result;
    }

        public int resetCount() {
        long result = resetByteCount();
        if (result > Integer.MAX_VALUE) {
            throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
        }
        return (int) result;
    }

        public synchronized long getByteCount() {
        return this.count;
    }

        public synchronized long resetByteCount() {
        long tmp = this.count;
        this.count = 0;
        return tmp;
    }

}
