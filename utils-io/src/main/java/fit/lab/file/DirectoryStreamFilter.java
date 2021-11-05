
package main.java.fit.lab.file;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.Objects;

public class DirectoryStreamFilter implements DirectoryStream.Filter<Path> {

    private final PathFilter pathFilter;

        public DirectoryStreamFilter(final PathFilter pathFilter) {
        this.pathFilter = Objects.requireNonNull(pathFilter, "pathFilter");
    }

    @Override
    public boolean accept(final Path path) throws IOException {
        return pathFilter.accept(path, PathUtils.readBasicFileAttributes(path)) == FileVisitResult.CONTINUE;
    }

        public PathFilter getPathFilter() {
        return pathFilter;
    }

}
