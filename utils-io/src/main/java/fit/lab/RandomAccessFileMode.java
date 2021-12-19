
package fit.lab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public enum RandomAccessFileMode {

    READ_ONLY("r"),

  
    READ_WRITE("rw"),

    READ_WRITE_SYNC_ALL("rws"),

  
    READ_WRITE_SYNC_CONTENT("rwd");

    private final String mode;

    RandomAccessFileMode(final String mode) {
        this.mode = mode;
    }

    
    public RandomAccessFile create(final File file) throws FileNotFoundException {
        return new RandomAccessFile(file, mode);
    }

   
    public RandomAccessFile create(final Path file) throws FileNotFoundException {
        return create(file.toFile());
    }

   
    public RandomAccessFile create(final String file) throws FileNotFoundException {
        return new RandomAccessFile(file, mode);
    }

    @Override
    public String toString() {
        return mode;
    }

}
