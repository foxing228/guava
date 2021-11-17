
package fit.lab.monitor;
import java.io.File;


public interface FileAlterationListener {


    void onDirectoryCreate(final File directory);

  
     @param directory 
   
    void onDirectoryDelete(final File directory);


      @param file 
    
    void onFileChange(final File file);

   
      @param file 
     
    void onFileCreate(final File file);

   
      @param file 
    void onFileDelete(final File file);

   
      @param observer 
     
    void onStart(final FileAlterationObserver observer);

    
      @param observer 
    
    void onStop(final FileAlterationObserver observer);
}
