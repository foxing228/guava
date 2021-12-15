
package fit.lab.function;

import java.io.IOException;
import java.util.function.Supplier;

@FunctionalInterface
public interface IOSupplier<T> {

        T get() throws IOException;
}
