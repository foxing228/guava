
package fit.lab.output;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;


public class AppendableWriter <T extends Appendable> extends Writer {

    private final T appendable;

    
    public AppendableWriter(final T appendable) {
        this.appendable = appendable;
    }

    
    @Override
    public Writer append(final char c) throws IOException {
        appendable.append(c);
        return this;
    }

    
    @Override
    public Writer append(final CharSequence csq) throws IOException {
        appendable.append(csq);
        return this;
    }

   
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        appendable.append(csq, start, end);
        return this;
    }

    
    @Override
    public void close() throws IOException {
        
    }

    
    @Override
    public void flush() throws IOException {
        
    }

    
    public T getAppendable() {
        return appendable;
    }

    
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        Objects.requireNonNull(cbuf, "Character array is missing");
        if (len < 0 || (off + len) > cbuf.length) {
            throw new IndexOutOfBoundsException("Array Size=" + cbuf.length +
                    ", offset=" + off + ", length=" + len);
        }
        for (int i = 0; i < len; i++) {
            appendable.append(cbuf[off + i]);
        }
    }

    
    @Override
    public void write(final int c) throws IOException {
        appendable.append((char)c);
    }

    
    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        // appendable.append will add "null" for a null String; add an explicit null check
        Objects.requireNonNull(str, "String is missing");
        appendable.append(str, off, off + len);
    }

}
