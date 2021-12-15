

package fit.lab.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface IOFunction<T, R> {

        static <T> IOFunction<T, T> identity() {
        return t -> t;
    }

        default IOConsumer<T> andThen(final Consumer<? super R> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> after.accept(apply(t));
    }

        default <V> IOFunction<T, V> andThen(final Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> after.apply(apply(t));
    }

        default IOConsumer<T> andThen(final IOConsumer<? super R> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> after.accept(apply(t));
    }

        default <V> IOFunction<T, V> andThen(final IOFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> after.apply(apply(t));
    }

        R apply(final T t) throws IOException;    default <V> IOFunction<V, R> compose(final Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before, "before");
        return (final V v) -> apply(before.apply(v));
    }

        default <V> IOFunction<V, R> compose(final IOFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before, "before");
        return (final V v) -> apply(before.apply(v));
    }

        default IOSupplier<R> compose(final IOSupplier<? extends T> before) {
        Objects.requireNonNull(before, "before");
        return () -> apply(before.get());
    }

    
    default IOSupplier<R> compose(final Supplier<? extends T> before) {
        Objects.requireNonNull(before, "before");
        return () -> apply(before.get());
    }
}
