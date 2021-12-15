
package fit.lab.monitor;

import java.io.File;


public class FileAlterationListenerAdaptor implements FileAlterationListener {

   
    @Override
    public void onDirectoryChange(final File directory) {
    }

   
    @Override
    public void onDirectoryCreate(final File directory) {
    }

    
    @Override
    public void onDirectoryDelete(final File directory) {
   
    }

  
    @Override
    public void onFileChange(final File file) {
       
    }

  
    @Override
    public void onFileCreate(final File file) {
      
    }

  
    @Override
    public void onFileDelete(final File file) {
     
    }


    @Override
    public void onStart(final FileAlterationObserver observer) {
        
    }

 
    @Override
    public void onStop(final FileAlterationObserver observer) {
        
    }

}
