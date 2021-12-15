
package fit.lab.monitor;
import java.io.File;


public interface FileAlterationListener {


    void onDirectoryChange(final File directory);


    void onDirectoryCreate(final File directory);


    void onDirectoryDelete(final File directory);


    void onFileChange(final File file);

    void onFileCreate(final File file);

   
    void onFileDelete(final File file);

  
    void onStart(final FileAlterationObserver observer);

  
    void onStop(final FileAlterationObserver observer);
}
