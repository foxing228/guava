
package main.java.fit.lab.file.spi;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Objects;

public class FileSystemProviders {

    private static final FileSystemProviders INSTALLED = new FileSystemProviders(FileSystemProvider.installedProviders());

        @SuppressWarnings("resource")    public static FileSystemProvider getFileSystemProvider(final Path path) {
        return Objects.requireNonNull(path, "path").getFileSystem().provider();
    }

        public static FileSystemProviders installed() {
        return INSTALLED;
    }

    private final List<FileSystemProvider> providers;

        private FileSystemProviders(final List<FileSystemProvider> providers) {
        this.providers = providers;
    }

        @SuppressWarnings("resource")    public FileSystemProvider getFileSystemProvider(final String scheme) {
        Objects.requireNonNull(scheme, "scheme");
        if (scheme.equalsIgnoreCase("file")) {
            return FileSystems.getDefault().provider();
        }
        if (providers != null) {
            for (final FileSystemProvider provider : providers) {
                if (provider.getScheme().equalsIgnoreCase(scheme)) {
                    return provider;
                }
            }
        }
        return null;
    }

        public FileSystemProvider getFileSystemProvider(final URI uri) {
        return getFileSystemProvider(Objects.requireNonNull(uri, "uri").getScheme());
    }

        public FileSystemProvider getFileSystemProvider(final URL url) {
        return getFileSystemProvider(Objects.requireNonNull(url, "url").getProtocol());
    }

}
