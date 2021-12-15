
package fit.lab;

import java.io.IOException;
import java.io.Serializable;


@SuppressWarnings("deprecation") 
public class TaggedIOException extends IOException {


    private static final long serialVersionUID = -6994123481142850163L;

  
    public static boolean isTaggedWith(final Throwable throwable, final Object tag) {
        return tag != null
            && throwable instanceof TaggedIOException
            && tag.equals(((TaggedIOException) throwable).tag);
    }

    
    public static void throwCauseIfTaggedWith(final Throwable throwable, final Object tag)
            throws IOException {
        if (isTaggedWith(throwable, tag)) {
            throw ((TaggedIOException) throwable).getCause();
        }
    }

   
    private final Serializable tag;

   
    public TaggedIOException(final IOException original, final Serializable tag) {
        super(original.getMessage(), original);
        this.tag = tag;
    }

   
    @Override
    public synchronized IOException getCause() {
        return (IOException) super.getCause();
    }

   
    public Serializable getTag() {
        return tag;
    }

}
