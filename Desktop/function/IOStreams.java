
package fit.lab.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import fit.lab.IOExceptionList;

class IOStreams {

        @SafeVarargs // Creating a stream from an array is safe
    static <T> Stream<T> of(final T... values) {
        return values == null ? Stream.empty() : Stream.of(values);
    }

    static <T> void forEach(final Stream<T> stream, final IOConsumer<T> action) throws IOException {
        forEachIndexed(stream, action, (i, e) -> e);
    }

    static <T> void forEachIndexed(final Stream<T> stream, final IOConsumer<T> action, final BiFunction<Integer, IOException, IOException> exSupplier)
        throws IOExceptionList {
        final AtomicReference<List<IOException>> causeList = new AtomicReference<>();
        final AtomicInteger index = new AtomicInteger();
        stream.forEach(e -> {
            try {
                action.accept(e);
            } catch (final IOException ioex) {
                if (causeList.get() == null) {
                    causeList.set(new ArrayList<>());
                }
                causeList.get().add(exSupplier.apply(index.get(), ioex));
            }
            index.incrementAndGet();
        });
        IOExceptionList.checkEmpty(causeList.get(), null);
    }

}
