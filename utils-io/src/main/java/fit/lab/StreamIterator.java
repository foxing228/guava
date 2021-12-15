

package fit.lab;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/
class StreamIterator<E> implements Iterator<E>, Closeable {

    @SuppressWarnings("resource") 
    public static <T> Iterator<T> iterator(final Stream<T> stream) {
        return new StreamIterator<>(stream).iterator;
    }

    private final Iterator<E> iterator;

    private final Stream<E> stream;
    private StreamIterator(final Stream<E> stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
        this.iterator = stream.iterator();
    }

   
    @Override
    public void close() {
        stream.close();

    }

    @Override
    public boolean hasNext() {
        final boolean hasNext = iterator.hasNext();
        if (!hasNext) {
            close();
        }
        return hasNext;
    }

    @Override
    public E next() {
        final E next = iterator.next();
        if (next == null) {
            close();
        }
        return next;
    }

}
