
package fit.lab.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import fit.lab.IOExceptionList;
import fit.lab.IOIndexedException;

@FunctionalInterface
public interface IOConsumer<T> {

        IOConsumer<?> NOOP_IO_CONSUMER = t -> {/* noop */};

        static <T> void forEach(final T[] array, final IOConsumer<T> action) throws IOException {
        IOStreams.forEach(IOStreams.of(array), action);
    }

        static <T> void forEachIndexed(final Stream<T> stream, final IOConsumer<T> action) throws IOExceptionList {
        IOStreams.forEachIndexed(stream, action, IOIndexedException::new);
    }    @SuppressWarnings("unchecked")
    static <T> IOConsumer<T> noop() {
        return (IOConsumer<T>) NOOP_IO_CONSUMER;
    }

        void accept(T t) throws IOException;

        default IOConsumer<T> andThen(final IOConsumer<? super T> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
