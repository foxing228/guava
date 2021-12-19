
package fit.lab.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;


public class BrokenOutputStream extends OutputStream {

   
    public static final BrokenOutputStream INSTANCE = new BrokenOutputStream();

    
    private final Supplier<IOException> exceptionSupplier;

    
    public BrokenOutputStream() {
        this(() -> new IOException("Broken output stream"));
    }

    
    public BrokenOutputStream(final IOException exception) {
        this(() -> exception);
    }

    
    public BrokenOutputStream(final Supplier<IOException> exceptionSupplier) {
        this.exceptionSupplier = exceptionSupplier;
    }

    
    @Override
    public void close() throws IOException {
        throw exceptionSupplier.get();
    }

    
    @Override
    public void flush() throws IOException {
        throw exceptionSupplier.get();
    }

   
    @Override
    public void write(final int b) throws IOException {
        throw exceptionSupplier.get();
    }

}
