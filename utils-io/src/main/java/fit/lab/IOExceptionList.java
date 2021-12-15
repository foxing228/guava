

package fit.lab;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class IOExceptionList extends IOException {

    private static final long serialVersionUID = 1L;

  
    public static void checkEmpty(final List<? extends Throwable> causeList, final Object message) throws IOExceptionList {
        if (!isEmpty(causeList)) {
            throw new IOExceptionList(Objects.toString(message, null), causeList);
        }
    }

    private static boolean isEmpty(final List<? extends Throwable> causeList) {
        return causeList == null || causeList.isEmpty();
    }

    private static String toMessage(final List<? extends Throwable> causeList) {
        return String.format("%,d exception(s): %s", causeList == null ? 0 : causeList.size(), causeList);
    }

    private final List<? extends Throwable> causeList;

   
    public IOExceptionList(final List<? extends Throwable> causeList) {
        this(toMessage(causeList), causeList);
    }

   
    public IOExceptionList(final String message, final List<? extends Throwable> causeList) {
        super(message != null ? message : toMessage(causeList), isEmpty(causeList) ? null : causeList.get(0));
        this.causeList = causeList == null ? Collections.emptyList() : causeList;
    }

    public <T extends Throwable> T getCause(final int index) {
        return (T) causeList.get(index);
    }

    public <T extends Throwable> T getCause(final int index, final Class<T> clazz) {
        return clazz.cast(getCause(index));
    }

   
    public <T extends Throwable> List<T> getCauseList() {
        return (List<T>) causeList;
    }

    
    public <T extends Throwable> List<T> getCauseList(final Class<T> clazz) {
        return (List<T>) causeList;
    }

}
