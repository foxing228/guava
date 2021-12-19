

package fit.lab;

import java.nio.charset.Charset;
import java.util.Objects;


public enum StandardLineSeparator {

    
    CR("\r"),

    CRLF("\r\n"),

    
    LF("\n");

    private final String lineSeparator;

    
    StandardLineSeparator(final String lineSeparator) {
        this.lineSeparator = Objects.requireNonNull(lineSeparator, "lineSeparator");
    }

  
    public byte[] getBytes(final Charset charset) {
        return lineSeparator.getBytes(charset);
    }

   
    public String getString() {
        return lineSeparator;
    }
}
