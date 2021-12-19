
package fit.lab;

import java.io.IOException;


public class IOIndexedException extends IOException {

    private static final long serialVersionUID = 1L;
   
    protected static String toMessage(final int index, final Throwable cause) {
        final String unspecified = "Null";
        final String name = cause == null ? unspecified : cause.getClass().getSimpleName();
        final String msg = cause == null ? unspecified : cause.getMessage();
        return String.format("%s #%,d: %s", name, index, msg);
    }

    private final int index;

   
    public IOIndexedException(final int index, final Throwable cause) {
        super(toMessage(index, cause), cause);
        this.index = index;
    }

   
    public int getIndex() {
        return index;
    }

}
