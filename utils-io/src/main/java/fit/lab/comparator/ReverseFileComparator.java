
package fit.lab.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;


class ReverseFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = -4808255005272229056L;
    private final Comparator<File> delegate;

    public ReverseFileComparator(final Comparator<File> delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate comparator is missing");
        }
        this.delegate = delegate;
    }

   
    @Override
    public int compare(final File file1, final File file2) {
        return delegate.compare(file2, file1); 
    }

    @Override
    public String toString() {
        return super.toString() + "[" + delegate.toString() + "]";
    }

}
