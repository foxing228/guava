
package fit.lab.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

import fit.lab.IOCase;


public class PathFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = 6527501707585768673L;

    public static final Comparator<File> PATH_COMPARATOR = new PathFileComparator();

    public static final Comparator<File> PATH_REVERSE = new ReverseFileComparator(PATH_COMPARATOR);

    public static final Comparator<File> PATH_INSENSITIVE_COMPARATOR = new PathFileComparator(IOCase.INSENSITIVE);

    public static final Comparator<File> PATH_INSENSITIVE_REVERSE = new ReverseFileComparator(PATH_INSENSITIVE_COMPARATOR);

    public static final Comparator<File> PATH_SYSTEM_COMPARATOR = new PathFileComparator(IOCase.SYSTEM);

    public static final Comparator<File> PATH_SYSTEM_REVERSE = new ReverseFileComparator(PATH_SYSTEM_COMPARATOR);

    private final IOCase ioCase;

    public PathFileComparator() {
        this.ioCase = IOCase.SENSITIVE;
    }

    
    public PathFileComparator(final IOCase ioCase) {
        this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
    }

    @Override
    public int compare(final File file1, final File file2) {
        return ioCase.checkCompareTo(file1.getPath(), file2.getPath());
    }

   
    @Override
    public String toString() {
        return super.toString() + "[ioCase=" + ioCase + "]";
    }
}
