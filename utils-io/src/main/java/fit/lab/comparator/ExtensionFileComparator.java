
package fit.lab.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

import fit.lab.IOCase;

/
public class ExtensionFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = 1928235200184222815L;

   
    public static final Comparator<File> EXTENSION_COMPARATOR = new ExtensionFileComparator();

    public static final Comparator<File> EXTENSION_REVERSE = new ReverseFileComparator(EXTENSION_COMPARATOR);

    
    public static final Comparator<File> EXTENSION_INSENSITIVE_COMPARATOR
                                                = new ExtensionFileComparator(IOCase.INSENSITIVE);
   
    public static final Comparator<File> EXTENSION_INSENSITIVE_REVERSE
                                                = new ReverseFileComparator(EXTENSION_INSENSITIVE_COMPARATOR);

    public static final Comparator<File> EXTENSION_SYSTEM_COMPARATOR = new ExtensionFileComparator(IOCase.SYSTEM);

   
    public static final Comparator<File> EXTENSION_SYSTEM_REVERSE = new ReverseFileComparator(EXTENSION_SYSTEM_COMPARATOR);

    
    private final IOCase ioCase;

   
    public ExtensionFileComparator() {
        this.ioCase = IOCase.SENSITIVE;
    }

  
    public ExtensionFileComparator(final IOCase ioCase) {
        this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
    }

  
    @Override
    public int compare(final File file1, final File file2) {
        final String suffix1 = FilenameUtils.getExtension(file1.getName());
        final String suffix2 = FilenameUtils.getExtension(file2.getName());
        return ioCase.checkCompareTo(suffix1, suffix2);
    }

    @Override
    public String toString() {
        return super.toString() + "[ioCase=" + ioCase + "]";
    }
}
