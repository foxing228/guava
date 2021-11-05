
package main.java.fit.lab.file;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import fit.lab.filefilter.SymbolicLinkFileFilter;
import fit.lab.filefilter.TrueFileFilter;

public class CountingPathVisitor extends SimplePathVisitor {

    static final String[] EMPTY_STRING_ARRAY = {};

        public static CountingPathVisitor withBigIntegerCounters() {
        return new CountingPathVisitor(Counters.bigIntegerPathCounters());
    }

        public static CountingPathVisitor withLongCounters() {
        return new CountingPathVisitor(Counters.longPathCounters());
    }

    private final Counters.PathCounters pathCounters;
    private final PathFilter fileFilter;
    private final PathFilter dirFilter;

        public CountingPathVisitor(final Counters.PathCounters pathCounter) {
        this(pathCounter, new SymbolicLinkFileFilter(FileVisitResult.TERMINATE, FileVisitResult.CONTINUE), TrueFileFilter.INSTANCE);
    }

        public CountingPathVisitor(final Counters.PathCounters pathCounter, final PathFilter fileFilter, final PathFilter dirFilter) {
        this.pathCounters = Objects.requireNonNull(pathCounter, "pathCounter");
        this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
        this.dirFilter = Objects.requireNonNull(dirFilter, "dirFilter");
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CountingPathVisitor)) {
            return false;
        }
        final CountingPathVisitor other = (CountingPathVisitor) obj;
        return Objects.equals(pathCounters, other.pathCounters);
    }

        public Counters.PathCounters getPathCounters() {
        return pathCounters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathCounters);
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
        updateDirCounter(dir, exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attributes) throws IOException {
        final FileVisitResult accept = dirFilter.accept(dir, attributes);
        return accept != FileVisitResult.CONTINUE ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    @Override
    public String toString() {
        return pathCounters.toString();
    }

        protected void updateDirCounter(final Path dir, final IOException exc) {
        pathCounters.getDirectoryCounter().increment();
    }

        protected void updateFileCounters(final Path file, final BasicFileAttributes attributes) {
        pathCounters.getFileCounter().increment();
        pathCounters.getByteCounter().add(attributes.size());
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
        if (Files.exists(file) && fileFilter.accept(file, attributes) == FileVisitResult.CONTINUE) {
            updateFileCounters(file, attributes);
        }
        return FileVisitResult.CONTINUE;
    }

}
