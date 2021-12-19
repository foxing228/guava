
package main.java.fit.lab.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fit.lab.FileUtils;
import fit.lab.IOUtils;
import fit.lab.UncheckedIOExceptions;
import fit.lab.filefilter.IOFileFilter;
import fit.lab.function.IOFunction;
import fit.lab.file.Counters.PathCounters;

public final class PathUtils {

        private static class RelativeSortedPaths {

        final boolean equals;
        final List<Path> relativeFileList1;
        final List<Path> relativeFileList2;

                private RelativeSortedPaths(final Path dir1, final Path dir2, final int maxDepth, final LinkOption[] linkOptions,
                final FileVisitOption[] fileVisitOptions) throws IOException {
            final List<Path> tmpRelativeDirList1;
            final List<Path> tmpRelativeDirList2;
            List<Path> tmpRelativeFileList1 = null;
            List<Path> tmpRelativeFileList2 = null;
            if (dir1 == null && dir2 == null) {
                equals = true;
            } else if (dir1 == null ^ dir2 == null) {
                equals = false;
            } else {
                final boolean parentDirNotExists1 = Files.notExists(dir1, linkOptions);
                final boolean parentDirNotExists2 = Files.notExists(dir2, linkOptions);
                if (parentDirNotExists1 || parentDirNotExists2) {
                    equals = parentDirNotExists1 && parentDirNotExists2;
                } else {
                    final AccumulatorPathVisitor visitor1 = accumulate(dir1, maxDepth, fileVisitOptions);
                    final AccumulatorPathVisitor visitor2 = accumulate(dir2, maxDepth, fileVisitOptions);
                    if (visitor1.getDirList().size() != visitor2.getDirList().size() || visitor1.getFileList().size() != visitor2.getFileList().size()) {
                        equals = false;
                    } else {
                        tmpRelativeDirList1 = visitor1.relativizeDirectories(dir1, true, null);
                        tmpRelativeDirList2 = visitor2.relativizeDirectories(dir2, true, null);
                        if (!tmpRelativeDirList1.equals(tmpRelativeDirList2)) {
                            equals = false;
                        } else {
                            tmpRelativeFileList1 = visitor1.relativizeFiles(dir1, true, null);
                            tmpRelativeFileList2 = visitor2.relativizeFiles(dir2, true, null);
                            equals = tmpRelativeFileList1.equals(tmpRelativeFileList2);
                        }
                    }
                }
            }
            relativeFileList1 = tmpRelativeFileList1;
            relativeFileList2 = tmpRelativeFileList2;
        }
    }

    private static final OpenOption[] OPEN_OPTIONS_TRUNCATE = { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };

    private static final OpenOption[] OPEN_OPTIONS_APPEND = { StandardOpenOption.CREATE, StandardOpenOption.APPEND };

        public static final CopyOption[] EMPTY_COPY_OPTIONS = {};

        public static final DeleteOption[] EMPTY_DELETE_OPTION_ARRAY = {};

        public static final FileVisitOption[] EMPTY_FILE_VISIT_OPTION_ARRAY = {};

        public static final LinkOption[] EMPTY_LINK_OPTION_ARRAY = {};

        public static final LinkOption[] NOFOLLOW_LINK_OPTION_ARRAY = { LinkOption.NOFOLLOW_LINKS };

        public static final OpenOption[] EMPTY_OPEN_OPTION_ARRAY = {};

        public static final Path[] EMPTY_PATH_ARRAY = {};

        private static AccumulatorPathVisitor accumulate(final Path directory, final int maxDepth, final FileVisitOption[] fileVisitOptions) throws IOException {
        return visitFileTree(AccumulatorPathVisitor.withLongCounters(), directory, toFileVisitOptionSet(fileVisitOptions), maxDepth);
    }

        public static PathCounters cleanDirectory(final Path directory) throws IOException {
        return cleanDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
    }

        public static PathCounters cleanDirectory(final Path directory, final DeleteOption... deleteOptions) throws IOException {
        return visitFileTree(new CleaningPathVisitor(Counters.longPathCounters(), deleteOptions), directory).getPathCounters();
    }

        private static int compareLastModifiedTimeTo(final Path file, final FileTime fileTime, final LinkOption... options) throws IOException {
        return getLastModifiedTime(file, options).compareTo(fileTime);
    }

        public static PathCounters copyDirectory(final Path sourceDirectory, final Path targetDirectory, final CopyOption... copyOptions) throws IOException {
        final Path absoluteSource = sourceDirectory.toAbsolutePath();
        return visitFileTree(new CopyDirectoryVisitor(Counters.longPathCounters(), absoluteSource, targetDirectory, copyOptions), absoluteSource)
                .getPathCounters();
    }

        public static Path copyFile(final URL sourceFile, final Path targetFile, final CopyOption... copyOptions) throws IOException {
        try (final InputStream inputStream = sourceFile.openStream()) {
            Files.copy(inputStream, targetFile, copyOptions);
            return targetFile;
        }
    }

        public static Path copyFileToDirectory(final Path sourceFile, final Path targetDirectory, final CopyOption... copyOptions) throws IOException {
        return Files.copy(sourceFile, targetDirectory.resolve(sourceFile.getFileName()), copyOptions);
    }

        public static Path copyFileToDirectory(final URL sourceFile, final Path targetDirectory, final CopyOption... copyOptions) throws IOException {
        try (final InputStream inputStream = sourceFile.openStream()) {
            Files.copy(inputStream, targetDirectory.resolve(sourceFile.getFile()), copyOptions);
            return targetDirectory;
        }
    }

        public static PathCounters countDirectory(final Path directory) throws IOException {
        return visitFileTree(CountingPathVisitor.withLongCounters(), directory).getPathCounters();
    }

        public static PathCounters countDirectoryAsBigInteger(final Path directory) throws IOException {
        return visitFileTree(CountingPathVisitor.withBigIntegerCounters(), directory).getPathCounters();
    }

        public static Path createParentDirectories(final Path path, final FileAttribute<?>... attrs) throws IOException {
        final Path parent = getParent(path);
        return parent == null ? null : Files.createDirectories(parent, attrs);
    }

        public static Path current() {
        return Paths.get(".");
    }

        public static PathCounters delete(final Path path) throws IOException {
        return delete(path, EMPTY_DELETE_OPTION_ARRAY);
    }

        public static PathCounters delete(final Path path, final DeleteOption... deleteOptions) throws IOException {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) ? deleteDirectory(path, deleteOptions) : deleteFile(path, deleteOptions);
    }

        public static PathCounters delete(final Path path, final LinkOption[] linkOptions, final DeleteOption... deleteOptions) throws IOException {
        return Files.isDirectory(path, linkOptions) ? deleteDirectory(path, linkOptions, deleteOptions) : deleteFile(path, linkOptions, deleteOptions);
    }

        public static PathCounters deleteDirectory(final Path directory) throws IOException {
        return deleteDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
    }

        public static PathCounters deleteDirectory(final Path directory, final DeleteOption... deleteOptions) throws IOException {
        final LinkOption[] linkOptions = PathUtils.NOFOLLOW_LINK_OPTION_ARRAY;
        return withPosixFileAttributes(getParent(directory), linkOptions, overrideReadOnly(deleteOptions),
                pfa -> visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions), directory).getPathCounters());
    }

        public static PathCounters deleteDirectory(final Path directory, final LinkOption[] linkOptions, final DeleteOption... deleteOptions) throws IOException {
        return visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions), directory).getPathCounters();
    }

        public static PathCounters deleteFile(final Path file) throws IOException {
        return deleteFile(file, EMPTY_DELETE_OPTION_ARRAY);
    }

        public static PathCounters deleteFile(final Path file, final DeleteOption... deleteOptions) throws IOException {
        return deleteFile(file, NOFOLLOW_LINK_OPTION_ARRAY, deleteOptions);
    }

        public static PathCounters deleteFile(final Path file, final LinkOption[] linkOptions, final DeleteOption... deleteOptions)
        throws NoSuchFileException, IOException {
        if (Files.isDirectory(file, linkOptions)) {
            throw new NoSuchFileException(file.toString());
        }
        final PathCounters pathCounts = Counters.longPathCounters();
        boolean exists = exists(file, linkOptions);
        long size = exists && !Files.isSymbolicLink(file) ? Files.size(file) : 0;
        try {
            if (Files.deleteIfExists(file)) {
                pathCounts.getFileCounter().increment();
                pathCounts.getByteCounter().add(size);
                return pathCounts;
            }
        } catch (final AccessDeniedException e) {
        }
        final Path parent = getParent(file);
        PosixFileAttributes posixFileAttributes = null;
        try {
            if (overrideReadOnly(deleteOptions)) {
                posixFileAttributes = readPosixFileAttributes(parent, linkOptions);
                setReadOnly(file, false, linkOptions);
            }
            exists = exists(file, linkOptions);
            size = exists && !Files.isSymbolicLink(file) ? Files.size(file) : 0;
            if (Files.deleteIfExists(file)) {
                pathCounts.getFileCounter().increment();
                pathCounts.getByteCounter().add(size);
            }
        } finally {
            if (posixFileAttributes != null) {
                Files.setPosixFilePermissions(parent, posixFileAttributes.permissions());
            }
        }
        return pathCounts;
    }

        public static boolean directoryAndFileContentEquals(final Path path1, final Path path2) throws IOException {
        return directoryAndFileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
    }

        public static boolean directoryAndFileContentEquals(final Path path1, final Path path2, final LinkOption[] linkOptions, final OpenOption[] openOptions,
            final FileVisitOption[] fileVisitOption) throws IOException {
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null || path2 == null) {
            return false;
        }
        if (notExists(path1) && notExists(path2)) {
            return true;
        }
        final RelativeSortedPaths relativeSortedPaths = new RelativeSortedPaths(path1, path2, Integer.MAX_VALUE, linkOptions, fileVisitOption);
        if (!relativeSortedPaths.equals) {
            return false;
        }
        final List<Path> fileList1 = relativeSortedPaths.relativeFileList1;
        final List<Path> fileList2 = relativeSortedPaths.relativeFileList2;
        for (final Path path : fileList1) {
            final int binarySearch = Collections.binarySearch(fileList2, path);
            if (binarySearch <= -1) {
                throw new IllegalStateException("Unexpected mismatch.");
            }
            if (!fileContentEquals(path1.resolve(path), path2.resolve(path), linkOptions, openOptions)) {
                return false;
            }
        }
        return true;
    }

        public static boolean directoryContentEquals(final Path path1, final Path path2) throws IOException {
        return directoryContentEquals(path1, path2, Integer.MAX_VALUE, EMPTY_LINK_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
    }

        public static boolean directoryContentEquals(final Path path1, final Path path2, final int maxDepth, final LinkOption[] linkOptions,
            final FileVisitOption[] fileVisitOptions) throws IOException {
        return new RelativeSortedPaths(path1, path2, maxDepth, linkOptions, fileVisitOptions).equals;
    }

    private static boolean exists(final Path path, final LinkOption... options) {
        return Files.exists(Objects.requireNonNull(path, "path"), options);
    }

        public static boolean fileContentEquals(final Path path1, final Path path2) throws IOException {
        return fileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY);
    }

        public static boolean fileContentEquals(final Path path1, final Path path2, final LinkOption[] linkOptions, final OpenOption[] openOptions)
            throws IOException {
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null || path2 == null) {
            return false;
        }
        final Path nPath1 = path1.normalize();
        final Path nPath2 = path2.normalize();
        final boolean path1Exists = exists(nPath1, linkOptions);
        if (path1Exists != exists(nPath2, linkOptions)) {
            return false;
        }
        if (!path1Exists) {
            return true;
        }
        if (Files.isDirectory(nPath1, linkOptions)) {
            throw new IOException("Can't compare directories, only files: " + nPath1);
        }
        if (Files.isDirectory(nPath2, linkOptions)) {
            throw new IOException("Can't compare directories, only files: " + nPath2);
        }
        if (Files.size(nPath1) != Files.size(nPath2)) {
            return false;
        }
        if (path1.equals(path2)) {
            return true;
        }
        try (final InputStream inputStream1 = Files.newInputStream(nPath1, openOptions);
                final InputStream inputStream2 = Files.newInputStream(nPath2, openOptions)) {
            return IOUtils.contentEquals(inputStream1, inputStream2);
        }
    }

        public static Path[] filter(final PathFilter filter, final Path... paths) {
        Objects.requireNonNull(filter, "filter");
        if (paths == null) {
            return EMPTY_PATH_ARRAY;
        }
        return filterPaths(filter, Stream.of(paths), Collectors.toList()).toArray(EMPTY_PATH_ARRAY);
    }

    private static <R, A> R filterPaths(final PathFilter filter, final Stream<Path> stream, final Collector<? super Path, A, R> collector) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(collector, "collector");
        if (stream == null) {
            return Stream.<Path>empty().collect(collector);
        }
        return stream.filter(p -> {
            try {
                return p != null && filter.accept(p, readBasicFileAttributes(p)) == FileVisitResult.CONTINUE;
            } catch (final IOException e) {
                return false;
            }
        }).collect(collector);
    }

        public static List<AclEntry> getAclEntryList(final Path sourcePath) throws IOException {
        final AclFileAttributeView fileAttributeView = getAclFileAttributeView(sourcePath);
        return fileAttributeView == null ? null : fileAttributeView.getAcl();
    }

        public static AclFileAttributeView getAclFileAttributeView(final Path path, final LinkOption... options) {
        return Files.getFileAttributeView(path, AclFileAttributeView.class, options);
    }

        public static DosFileAttributeView getDosFileAttributeView(final Path path, final LinkOption... options) {
        return Files.getFileAttributeView(path, DosFileAttributeView.class, options);
    }

    private static FileTime getLastModifiedTime(final Path path, final LinkOption... options) throws IOException {
        return Files.getLastModifiedTime(Objects.requireNonNull(path, "path"), options);
    }

    private static Path getParent(final Path path) {
        return path == null ? null : path.getParent();
    }

        public static PosixFileAttributeView getPosixFileAttributeView(final Path path, final LinkOption... options) {
        return Files.getFileAttributeView(path, PosixFileAttributeView.class, options);
    }

        public static Path getTempDirectory() {
        return Paths.get(FileUtils.getTempDirectoryPath());
    }

        public static boolean isDirectory(final Path path, final LinkOption... options) {
        return path != null && Files.isDirectory(path, options);
    }

        public static boolean isEmpty(final Path path) throws IOException {
        return Files.isDirectory(path) ? isEmptyDirectory(path) : isEmptyFile(path);
    }

        public static boolean isEmptyDirectory(final Path directory) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            return !directoryStream.iterator().hasNext();
        }
    }

        public static boolean isEmptyFile(final Path file) throws IOException {
        return Files.size(file) <= 0;
    }

        public static boolean isNewer(final Path file, final ChronoZonedDateTime<?> czdt, final LinkOption... options) throws IOException {
        Objects.requireNonNull(czdt, "czdt");
        return isNewer(file, czdt.toInstant(), options);
    }

        public static boolean isNewer(final Path file, final FileTime fileTime, final LinkOption... options) throws IOException {
        if (notExists(file)) {
            return false;
        }
        return compareLastModifiedTimeTo(file, fileTime, options) > 0;
    }

        public static boolean isNewer(final Path file, final Instant instant, final LinkOption... options) throws IOException {
        return isNewer(file, FileTime.from(instant), options);
    }

        public static boolean isNewer(final Path file, final long timeMillis, final LinkOption... options) throws IOException {
        return isNewer(file, FileTime.fromMillis(timeMillis), options);
    }

        public static boolean isNewer(final Path file, final Path reference) throws IOException {
        return isNewer(file, getLastModifiedTime(reference));
    }

        public static boolean isOlder(final Path file, final FileTime fileTime, final LinkOption... options) throws IOException {
        if (notExists(file)) {
            return false;
        }
        return compareLastModifiedTimeTo(file, fileTime, options) < 0;
    }

        public static boolean isOlder(final Path file, final Instant instant, final LinkOption... options) throws IOException {
        return isOlder(file, FileTime.from(instant), options);
    }

        public static boolean isOlder(final Path file, final long timeMillis, final LinkOption... options) throws IOException {
        return isOlder(file, FileTime.fromMillis(timeMillis), options);
    }

        public static boolean isOlder(final Path file, final Path reference) throws IOException {
        return isOlder(file, getLastModifiedTime(reference));
    }

        public static boolean isPosix(final Path test, final LinkOption... options) {
        return exists(test, options) && readPosixFileAttributes(test, options) != null;
    }

        public static boolean isRegularFile(final Path path, final LinkOption... options) {
        return path != null && Files.isRegularFile(path, options);
    }

        public static DirectoryStream<Path> newDirectoryStream(final Path dir, final PathFilter pathFilter) throws IOException {
        return Files.newDirectoryStream(dir, new DirectoryStreamFilter(pathFilter));
    }

        public static OutputStream newOutputStream(final Path path, final boolean append) throws IOException {
        Objects.requireNonNull(path, "path");
        if (exists(path)) {
        } else {
            createParentDirectories(path);
        }
        return Files.newOutputStream(path, append ? OPEN_OPTIONS_APPEND : OPEN_OPTIONS_TRUNCATE);
    }

    private static boolean notExists(final Path path, final LinkOption... options) {
        return Files.notExists(Objects.requireNonNull(path, "path"), options);
    }

        private static boolean overrideReadOnly(final DeleteOption... deleteOptions) {
        if (deleteOptions == null) {
            return false;
        }
        return Stream.of(deleteOptions).anyMatch(e -> e == StandardDeleteOption.OVERRIDE_READ_ONLY);
    }

        public static <A extends BasicFileAttributes> A readAttributes(final Path path, final Class<A> type, final LinkOption... options) {
        try {
            return path == null ? null : Files.readAttributes(path, type, options);
        } catch (final UnsupportedOperationException e) {
            return null;
        } catch (final IOException e) {
            throw UncheckedIOExceptions.create(path, e);
        }
    }

        @Deprecated
    public static BasicFileAttributes readBasicFileAttributes(final Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class);
    }

        public static BasicFileAttributes readBasicFileAttributes(final Path path, final LinkOption... options) {
        return readAttributes(path, BasicFileAttributes.class, options);
    }

        @Deprecated
    public static BasicFileAttributes readBasicFileAttributesUnchecked(final Path path) {
        return readBasicFileAttributes(path, EMPTY_LINK_OPTION_ARRAY);
    }

        public static DosFileAttributes readDosFileAttributes(final Path path, final LinkOption... options) {
        return readAttributes(path, DosFileAttributes.class, options);
    }

        public static BasicFileAttributes readOsFileAttributes(final Path path, final LinkOption... options) {
        final PosixFileAttributes fileAttributes = readPosixFileAttributes(path, options);
        return fileAttributes != null ? fileAttributes : readDosFileAttributes(path, options);
    }

        public static PosixFileAttributes readPosixFileAttributes(final Path path, final LinkOption... options) {
        return readAttributes(path, PosixFileAttributes.class, options);
    }

        public static String readString(final Path path, final Charset charset) throws IOException {
        return new String(Files.readAllBytes(path), Charsets.toCharset(charset));
    }

        static List<Path> relativize(final Collection<Path> collection, final Path parent, final boolean sort, final Comparator<? super Path> comparator) {
        Stream<Path> stream = collection.stream().map(parent::relativize);
        if (sort) {
            stream = comparator == null ? stream.sorted() : stream.sorted(comparator);
        }
        return stream.collect(Collectors.toList());
    }

        private static void requireCanWrite(final Path file, final String name) {
        Objects.requireNonNull(file, "file");
        if (!Files.isWritable(file)) {
            throw new IllegalArgumentException("File parameter '" + name + " is not writable: '" + file + "'");
        }
    }

        private static Path requireExists(final Path file, final String fileParamName, final LinkOption... options) {
        Objects.requireNonNull(file, fileParamName);
        if (!exists(file, options)) {
            throw new IllegalArgumentException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

        private static Path requireFile(final Path file, final String name) {
        Objects.requireNonNull(file, name);
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a regular file: " + file);
        }
        return file;
    }

    private static boolean setDosReadOnly(final Path path, final boolean readOnly, final LinkOption... linkOptions) throws IOException {
        final DosFileAttributeView dosFileAttributeView = getDosFileAttributeView(path, linkOptions);
        if (dosFileAttributeView != null) {
            dosFileAttributeView.setReadOnly(readOnly);
            return true;
        }
        return false;
    }

        public static void setLastModifiedTime(final Path sourceFile, final Path targetFile) throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        Files.setLastModifiedTime(targetFile, getLastModifiedTime(sourceFile));
    }

        private static boolean setPosixDeletePermissions(final Path parent, final boolean enableDeleteChildren, final LinkOption... linkOptions)
            throws IOException {
        return setPosixPermissions(parent, enableDeleteChildren, Arrays.asList(
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE
            ), linkOptions);
    }

        private static boolean setPosixPermissions(final Path path, final boolean addPermissions, final List<PosixFilePermission> updatePermissions,
            final LinkOption... linkOptions) throws IOException {
        if (path != null) {
            final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, linkOptions);
            if (addPermissions) {
                permissions.addAll(updatePermissions);
            } else {
                permissions.removeAll(updatePermissions);
            }
            Files.setPosixFilePermissions(path, permissions);
            return true;
        }
        return false;
    }

    private static void setPosixReadOnlyFile(final Path path, final boolean readOnly, final LinkOption... linkOptions) throws IOException {
        final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, linkOptions);
        final List<PosixFilePermission> readPermissions = Arrays.asList(
                PosixFilePermission.OWNER_READ
            );
        final List<PosixFilePermission> writePermissions = Arrays.asList(
                PosixFilePermission.OWNER_WRITE
            );
        if (readOnly) {
            permissions.addAll(readPermissions);
            permissions.removeAll(writePermissions);
        } else {
            permissions.addAll(readPermissions);
            permissions.addAll(writePermissions);
        }
        Files.setPosixFilePermissions(path, permissions);
    }

        public static Path setReadOnly(final Path path, final boolean readOnly, final LinkOption... linkOptions) throws IOException {
        try {
            if (setDosReadOnly(path, readOnly, linkOptions)) {
                return path;
            }
        } catch (final IOException e) {
        }
        final Path parent = getParent(path);
        if (!isPosix(parent, linkOptions)) {            throw new IOException(String.format("DOS or POSIX file operations not available for '%s' %s", path, Arrays.toString(linkOptions)));
        }
        if (readOnly) {
            setPosixReadOnlyFile(path, readOnly, linkOptions);
            setPosixDeletePermissions(parent, false, linkOptions);
        } else {
            setPosixDeletePermissions(parent, true, linkOptions);
        }
        return path;
    }

        public static long sizeOf(final Path path) throws IOException {
        requireExists(path, "path");
        return Files.isDirectory(path) ? sizeOfDirectory(path) : Files.size(path);
    }

        public static BigInteger sizeOfAsBigInteger(final Path path) throws IOException {
        requireExists(path, "path");
        return Files.isDirectory(path) ? sizeOfDirectoryAsBigInteger(path) : BigInteger.valueOf(Files.size(path));
    }

        public static long sizeOfDirectory(final Path directory) throws IOException {
        return countDirectory(directory).getByteCounter().getLong();
    }

        public static BigInteger sizeOfDirectoryAsBigInteger(final Path directory) throws IOException {
        return countDirectoryAsBigInteger(directory).getByteCounter().getBigInteger();
    }

        static Set<FileVisitOption> toFileVisitOptionSet(final FileVisitOption... fileVisitOptions) {
        return fileVisitOptions == null ? EnumSet.noneOf(FileVisitOption.class) : Stream.of(fileVisitOptions).collect(Collectors.toSet());
    }

        public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final Path directory) throws IOException {
        requireExists(directory, "directory");
        Files.walkFileTree(directory, visitor);
        return visitor;
    }

        public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final Path start, final Set<FileVisitOption> options,
            final int maxDepth) throws IOException {
        Files.walkFileTree(start, options, maxDepth, visitor);
        return visitor;
    }

        public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final String first, final String... more) throws IOException {
        return visitFileTree(visitor, Paths.get(first, more));
    }

        public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final URI uri) throws IOException {
        return visitFileTree(visitor, Paths.get(uri));
    }

        public static boolean waitFor(final Path file, final Duration timeout, final LinkOption... options) {
        Objects.requireNonNull(file, "file");
        final Instant finishInstant = Instant.now().plus(timeout);
        boolean interrupted = false;
        final long minSleepMillis = 100;
        try {
            while (!exists(file, options)) {
                final Instant now = Instant.now();
                if (now.isAfter(finishInstant)) {
                    return false;
                }
                try {
                    Thread.sleep(Math.min(minSleepMillis, finishInstant.minusMillis(now.toEpochMilli()).toEpochMilli()));
                } catch (final InterruptedException ignore) {
                    interrupted = true;
                } catch (final Exception ex) {
                    break;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return exists(file, options);
    }

        public static Stream<Path> walk(final Path start, final PathFilter pathFilter, final int maxDepth, final boolean readAttributes,
            final FileVisitOption... options) throws IOException {
        return Files.walk(start, maxDepth, options)
                .filter(path -> pathFilter.accept(path, readAttributes ? readBasicFileAttributesUnchecked(path) : null) == FileVisitResult.CONTINUE);
    }

    private static <R> R withPosixFileAttributes(final Path path, final LinkOption[] linkOptions, final boolean overrideReadOnly,
            final IOFunction<PosixFileAttributes, R> function) throws IOException {
        final PosixFileAttributes posixFileAttributes = overrideReadOnly ? readPosixFileAttributes(path, linkOptions) : null;
        try {
            return function.apply(posixFileAttributes);
        } finally {
            if (posixFileAttributes != null && path != null && Files.exists(path, linkOptions)) {
                Files.setPosixFilePermissions(path, posixFileAttributes.permissions());
            }
        }
    }

        public static Path writeString(final Path path, final CharSequence charSequence, final Charset charset, final OpenOption... openOptions)
            throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(charSequence, "charSequence");
        Files.write(path, String.valueOf(charSequence).getBytes(Charsets.toCharset(charset)), openOptions);
        return path;
    }

        private PathUtils() {
    }

}
