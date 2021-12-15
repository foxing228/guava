
package fit.lab.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;


public class DirectoryFileComparator extends AbstractFileComparator implements Serializable {

    private static final int TYPE_FILE = 2;

    private static final int TYPE_DIRECTORY = 1;

    private static final long serialVersionUID = 296132640160964395L;

   
    public static final Comparator<File> DIRECTORY_COMPARATOR = new DirectoryFileComparator();

    
    public static final Comparator<File> DIRECTORY_REVERSE = new ReverseFileComparator(DIRECTORY_COMPARATOR);

    
    @Override
    public int compare(final File file1, final File file2) {
        return getType(file1) - getType(file2);
    }

   
    private int getType(final File file) {
        return file.isDirectory() ? TYPE_DIRECTORY : TYPE_FILE;
    }
}
