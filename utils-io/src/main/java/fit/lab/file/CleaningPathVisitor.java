
package main.java.fit.lab.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;

public class CleaningPathVisitor extends CountingPathVisitor {

        public static CountingPathVisitor withBigIntegerCounters() {
        return new CleaningPathVisitor(Counters.bigIntegerPathCounters());
    }

        public static CountingPathVisitor withLongCounters() {
        return new CleaningPathVisitor(Counters.longPathCounters());
    }

    private final String[] skip;
    private final boolean overrideReadOnly;

        public CleaningPathVisitor(final Counters.PathCounters pathCounter, final DeleteOption[] deleteOption, final String... skip) {
        super(pathCounter);
        final String[] temp = skip != null ? skip.clone() : EMPTY_STRING_ARRAY;
        Arrays.sort(temp);
        this.skip = temp;
        this.overrideReadOnly = StandardDeleteOption.overrideReadOnly(deleteOption);
    }

        public CleaningPathVisitor(final Counters.PathCounters pathCounter, final String... skip) {
        this(pathCounter, PathUtils.EMPTY_DELETE_OPTION_ARRAY, skip);
    }

        private boolean accept(final Path path) {
        return Arrays.binarySearch(skip, Objects.toString(path.getFileName(), null)) < 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CleaningPathVisitor other = (CleaningPathVisitor) obj;
        return overrideReadOnly == other.overrideReadOnly && Arrays.equals(skip, other.skip);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(skip);
        result = prime * result + Objects.hash(overrideReadOnly);
        return result;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attributes) throws IOException {
        super.preVisitDirectory(dir, attributes);
        return accept(dir) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
        if (accept(file) && Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            if (overrideReadOnly) {
                PathUtils.setReadOnly(file, false, LinkOption.NOFOLLOW_LINKS);
            }
            Files.deleteIfExists(file);
        }
        updateFileCounters(file, attributes);
        return FileVisitResult.CONTINUE;
    }
}