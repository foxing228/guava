
package fit.lab.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;


public class DefaultFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = 3260141861365313518L;

    
    public static final Comparator<File> DEFAULT_COMPARATOR = new DefaultFileComparator();

    public static final Comparator<File> DEFAULT_REVERSE = new ReverseFileComparator(DEFAULT_COMPARATOR);

  
    @Override
    public int compare(final File file1, final File file2) {
        return file1.compareTo(file2);
    }
}
