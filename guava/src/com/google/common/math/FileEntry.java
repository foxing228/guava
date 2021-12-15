
package fit.lab.monitor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import fit.lab.FileUtils;
import fit.lab.file.attribute.FileTimes;


public class FileEntry implements Serializable {

    private static final long serialVersionUID = -2505664948818681153L;

    static final FileEntry[] EMPTY_FILE_ENTRY_ARRAY = {};

    private final FileEntry parent;
    private FileEntry[] children;
    private final File file;
    private String name;
    private boolean exists;
    private boolean directory;
    private FileTime lastModified = FileTimes.EPOCH;
    private long length;

   
    public FileEntry(final File file) {
        this(null, file);
    }

 
    public FileEntry(final FileEntry parent, final File file) {
        if (file == null) {
            throw new IllegalArgumentException("File is null.");
        }
        this.file = file;
        this.parent = parent;
        this.name = file.getName();
    }


    public FileEntry[] getChildren() {
        return children != null ? children : EMPTY_FILE_ENTRY_ARRAY;
    }


    public File getFile() {
        return file;
    }

 
    public long getLastModified() {
        return lastModified.toMillis();
    }


    public FileTime getLastModifiedFileTime() {
        return lastModified;
    }


    public long getLength() {
        return length;
    }


    public int getLevel() {
        return parent == null ? 0 : parent.getLevel() + 1;
    }


    public String getName() {
        return name;
    }


    public FileEntry getParent() {
        return parent;
    }

 
    public boolean isDirectory() {
        return directory;
    }


    public boolean isExists() {
        return exists;
    }


    public FileEntry newChildInstance(final File file) {
        return new FileEntry(this, file);
    }

    public boolean refresh(final File file) {
        final boolean origExists = exists;
        final FileTime origLastModified = lastModified;
        final boolean origDirectory = directory;
        final long origLength = length;

        name = file.getName();
        exists = Files.exists(file.toPath());
        directory = exists && file.isDirectory();
        try {
            lastModified = exists ? FileUtils.lastModifiedFileTime(file) : FileTimes.EPOCH;
        } catch (final IOException e) {
            lastModified = FileTimes.EPOCH;
        }
        length = exists && !directory ? file.length() : 0;

        return exists != origExists || !lastModified.equals(origLastModified) || directory != origDirectory
            || length != origLength;
    }

  
    public void setChildren(final FileEntry... children) {
        this.children = children;
    }

   
    public void setDirectory(final boolean directory) {
        this.directory = directory;
    }

   
    public void setExists(final boolean exists) {
        this.exists = exists;
    }

  
    public void setLastModified(final FileTime lastModified) {
        this.lastModified = lastModified;
    }

   
    public void setLastModified(final long lastModified) {
        this.lastModified = FileTime.fromMillis(lastModified);
    }

    public void setLength(final long length) {
        this.length = length;
    }

   
    public void setName(final String name) {
        this.name = name;
    }
}
