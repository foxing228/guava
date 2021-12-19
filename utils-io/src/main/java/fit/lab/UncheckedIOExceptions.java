

package fit.lab;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public class UncheckedIOExceptions {


    public static UncheckedIOException create(final Object message) {
        final String string = Objects.toString(message);
        return new UncheckedIOException(string, new IOException(string));
    }

   
    public static UncheckedIOException create(final Object message, final IOException e) {
        return new UncheckedIOException(Objects.toString(message), e);
    }

}
