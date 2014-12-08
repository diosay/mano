/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.util;

import java.io.File;

/**
 * 文件或文件夹变化监听。
 * @author junhwong
 */
public class FileWatcher implements Runnable{
    private File file;
    public FileWatcher(File file){
        if(file==null){
            
        }
        this.file=file;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }
    
    @Override
    public final void run(){
        
        FileWatcherHandler handler=null;
        file.lastModified();
        file.getAbsolutePath();
        
        handler.handle(this);
        
        if(!file.exists()){
            //delete
        }
    }
}
