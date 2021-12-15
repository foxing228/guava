
package fit.lab.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

import fit.lab.FileUtils;


public class LastModifiedFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = 7372168004395734046L;

    public static final Comparator<File> LASTMODIFIED_COMPARATOR = new LastModifiedFileComparator();

    public static final Comparator<File> LASTMODIFIED_REVERSE = new ReverseFileComparator(LASTMODIFIED_COMPARATOR);

  
    @Override
    public int compare(final File file1, final File file2) {
        final long result = FileUtils.lastModifiedUnchecked(file1) - FileUtils.lastModifiedUnchecked(file2);
        if (result < 0) {
            return -1;
        }
        if (result > 0) {
            return 1;
        }
        return 0;
    }
}
