
package fit.lab.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

import fit.lab.IOCase;


public class NameFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = 8397947749814525798L;

    public static final Comparator<File> NAME_COMPARATOR = new NameFileComparator();

    public static final Comparator<File> NAME_REVERSE = new ReverseFileComparator(NAME_COMPARATOR);

    public static final Comparator<File> NAME_INSENSITIVE_COMPARATOR = new NameFileComparator(IOCase.INSENSITIVE);

    public static final Comparator<File> NAME_INSENSITIVE_REVERSE = new ReverseFileComparator(NAME_INSENSITIVE_COMPARATOR);

    public static final Comparator<File> NAME_SYSTEM_COMPARATOR = new NameFileComparator(IOCase.SYSTEM);

    public static final Comparator<File> NAME_SYSTEM_REVERSE = new ReverseFileComparator(NAME_SYSTEM_COMPARATOR);

    private final IOCase ioCase;

    public NameFileComparator() {
        this.ioCase = IOCase.SENSITIVE;
    }

   
    public NameFileComparator(final IOCase ioCase) {
        this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
    }

    @Override
    public int compare(final File file1, final File file2) {
        return ioCase.checkCompareTo(file1.getName(), file2.getName());
    }

   
    @Override
    public String toString() {
        return super.toString() + "[ioCase=" + ioCase + "]";
    }
}
