
package fit.lab.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class CompositeFileComparator extends AbstractFileComparator implements Serializable {

    private static final Comparator<?>[] EMPTY_COMPARATOR_ARRAY = {};
    private static final long serialVersionUID = -2224170307287243428L;

    private final Comparator<File>[] delegates;

  
    @SuppressWarnings("unchecked")
    public CompositeFileComparator(final Comparator<File>... delegates) {
        if (delegates == null) {
            this.delegates = (Comparator<File>[]) EMPTY_COMPARATOR_ARRAY;
        } else {
            this.delegates = delegates.clone();
        }
    }

  
    @SuppressWarnings("unchecked")
    public CompositeFileComparator(final Iterable<Comparator<File>> delegates) {
        if (delegates == null) {
            this.delegates = (Comparator<File>[]) EMPTY_COMPARATOR_ARRAY; 
        } else {
            final List<Comparator<File>> list = new ArrayList<>();
            for (final Comparator<File> comparator : delegates) {
                list.add(comparator);
            }
            this.delegates = (Comparator<File>[]) list.toArray(EMPTY_COMPARATOR_ARRAY); 
        }
    }

   
    @Override
    public int compare(final File file1, final File file2) {
        int result = 0;
        for (final Comparator<File> delegate : delegates) {
            result = delegate.compare(file1, file2);
            if (result != 0) {
                break;
            }
        }
        return result;
    }

  
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append('{');
        for (int i = 0; i < delegates.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(delegates[i]);
        }
        builder.append('}');
        return builder.toString();
    }
}
