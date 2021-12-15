package fit.lab.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface IOBiConsumer<T, U> {
    void accept(T t, U u) throws IOException;

        default IOBiConsumer<T, U> andThen(IOBiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);

        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }
}
