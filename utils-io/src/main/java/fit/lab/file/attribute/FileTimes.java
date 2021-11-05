
package main.java.fit.lab.file.attribute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class FileTimes {

        public static final FileTime EPOCH = FileTime.from(Instant.EPOCH);

        public static FileTime minusMillis(final FileTime fileTime, final long millisToSubtract) {
        return FileTime.from(fileTime.toInstant().minusMillis(millisToSubtract));
    }

        public static FileTime minusNanos(final FileTime fileTime, final long nanosToSubtract) {
        return FileTime.from(fileTime.toInstant().minusNanos(nanosToSubtract));
    }

        public static FileTime minusSeconds(final FileTime fileTime, final long secondsToSubtract) {
        return FileTime.from(fileTime.toInstant().minusSeconds(secondsToSubtract));
    }

        public static FileTime now() {
        return FileTime.from(Instant.now());
    }

        public static FileTime plusMillis(final FileTime fileTime, final long millisToAdd) {
        return FileTime.from(fileTime.toInstant().plusMillis(millisToAdd));
    }

        public static FileTime plusNanos(final FileTime fileTime, final long nanosToSubtract) {
        return FileTime.from(fileTime.toInstant().plusNanos(nanosToSubtract));
    }

        public static FileTime plusSeconds(final FileTime fileTime, final long secondsToAdd) {
        return FileTime.from(fileTime.toInstant().plusSeconds(secondsToAdd));
    }

        public static void setLastModifiedTime(final Path path) throws IOException {
        Files.setLastModifiedTime(path, now());
    }

    private FileTimes() {
    }
}
