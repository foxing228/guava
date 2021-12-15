
package fit.lab.monitor;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fit.lab.FileUtils;
import fit.lab.IOCase;
import fit.lab.comparator.NameFileComparator;


public class FileAlterationObserver implements Serializable {

    private static final long serialVersionUID = 1185122225658782848L;
    private final List<FileAlterationListener> listeners = new CopyOnWriteArrayList<>();
    private final FileEntry rootEntry;
    private final FileFilter fileFilter;
    private final Comparator<File> comparator;


    public FileAlterationObserver(final File directory) {
        this(directory, null);
    }


    public FileAlterationObserver(final File directory, final FileFilter fileFilter) {
        this(directory, fileFilter, null);
    }

 
    public FileAlterationObserver(final File directory, final FileFilter fileFilter, final IOCase ioCase) {
        this(new FileEntry(directory), fileFilter, ioCase);
    }


    protected FileAlterationObserver(final FileEntry rootEntry, final FileFilter fileFilter, final IOCase ioCase) {
        if (rootEntry == null) {
            throw new IllegalArgumentException("Root entry is missing");
        }
        if (rootEntry.getFile() == null) {
            throw new IllegalArgumentException("Root directory is missing");
        }
        this.rootEntry = rootEntry;
        this.fileFilter = fileFilter;
        switch (IOCase.value(ioCase, IOCase.SYSTEM)) {
        case SYSTEM:
            this.comparator = NameFileComparator.NAME_SYSTEM_COMPARATOR;
            break;
        case INSENSITIVE:
            this.comparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
            break;
        default:
            this.comparator = NameFileComparator.NAME_COMPARATOR;
        }
    }

    public FileAlterationObserver(final String directoryName) {
        this(new File(directoryName));
    }

    public FileAlterationObserver(final String directoryName, final FileFilter fileFilter) {
        this(new File(directoryName), fileFilter);
    }

 
    public FileAlterationObserver(final String directoryName, final FileFilter fileFilter, final IOCase ioCase) {
        this(new File(directoryName), fileFilter, ioCase);
    }

    public void addListener(final FileAlterationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }


    public void checkAndNotify() {

        for (final FileAlterationListener listener : listeners) {
            listener.onStart(this);
        }

        final File rootFile = rootEntry.getFile();
        if (rootFile.exists()) {
            checkAndNotify(rootEntry, rootEntry.getChildren(), listFiles(rootFile));
        } else if (rootEntry.isExists()) {
            checkAndNotify(rootEntry, rootEntry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
        } else {
        }

        for (final FileAlterationListener listener : listeners) {
            listener.onStop(this);
        }
    }

    private void checkAndNotify(final FileEntry parent, final FileEntry[] previous, final File[] files) {
        int c = 0;
        final FileEntry[] current = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_FILE_ENTRY_ARRAY;
        for (final FileEntry entry : previous) {
            while (c < files.length && comparator.compare(entry.getFile(), files[c]) > 0) {
                current[c] = createFileEntry(parent, files[c]);
                doCreate(current[c]);
                c++;
            }
            if (c < files.length && comparator.compare(entry.getFile(), files[c]) == 0) {
                doMatch(entry, files[c]);
                checkAndNotify(entry, entry.getChildren(), listFiles(files[c]));
                current[c] = entry;
                c++;
            } else {
                checkAndNotify(entry, entry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
                doDelete(entry);
            }
        }
        for (; c < files.length; c++) {
            current[c] = createFileEntry(parent, files[c]);
            doCreate(current[c]);
        }
        parent.setChildren(current);
    }


    private FileEntry createFileEntry(final FileEntry parent, final File file) {
        final FileEntry entry = parent.newChildInstance(file);
        entry.refresh(file);
        entry.setChildren(doListFiles(file, entry));
        return entry;
    }


    @SuppressWarnings("unused") 
    public void destroy() throws Exception {
    }

    private void doCreate(final FileEntry entry) {
        for (final FileAlterationListener listener : listeners) {
            if (entry.isDirectory()) {
                listener.onDirectoryCreate(entry.getFile());
            } else {
                listener.onFileCreate(entry.getFile());
            }
        }
        final FileEntry[] children = entry.getChildren();
        for (final FileEntry aChildren : children) {
            doCreate(aChildren);
        }
    }


    private void doDelete(final FileEntry entry) {
        for (final FileAlterationListener listener : listeners) {
            if (entry.isDirectory()) {
                listener.onDirectoryDelete(entry.getFile());
            } else {
                listener.onFileDelete(entry.getFile());
            }
        }
    }


    private FileEntry[] doListFiles(final File file, final FileEntry entry) {
        final File[] files = listFiles(file);
        final FileEntry[] children = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_FILE_ENTRY_ARRAY;
        for (int i = 0; i < files.length; i++) {
            children[i] = createFileEntry(entry, files[i]);
        }
        return children;
    }


    private void doMatch(final FileEntry entry, final File file) {
        if (entry.refresh(file)) {
            for (final FileAlterationListener listener : listeners) {
                if (entry.isDirectory()) {
                    listener.onDirectoryChange(file);
                } else {
                    listener.onFileChange(file);
                }
            }
        }
    }

    public File getDirectory() {
        return rootEntry.getFile();
    }

    

    public FileFilter getFileFilter() {
        return fileFilter;
    }


    public Iterable<FileAlterationListener> getListeners() {
        return listeners;
    }

  
    @SuppressWarnings("unused") 
    public void initialize() throws Exception {
        rootEntry.refresh(rootEntry.getFile());
        rootEntry.setChildren(doListFiles(rootEntry.getFile(), rootEntry));
    }


    private File[] listFiles(final File file) {
        File[] children = null;
        if (file.isDirectory()) {
            children = fileFilter == null ? file.listFiles() : file.listFiles(fileFilter);
        }
        if (children == null) {
            children = FileUtils.EMPTY_FILE_ARRAY;
        }
        if (comparator != null && children.length > 1) {
            Arrays.sort(children, comparator);
        }
        return children;
    }

 
    public void removeListener(final FileAlterationListener listener) {
        if (listener != null) {
            while (listeners.remove(listener)) {
            }
        }
    }


    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[file='");
        builder.append(getDirectory().getPath());
        builder.append('\'');
        if (fileFilter != null) {
            builder.append(", ");
            builder.append(fileFilter.toString());
        }
        builder.append(", listeners=");
        builder.append(listeners.size());
        builder.append("]");
        return builder.toString();
    }

}
