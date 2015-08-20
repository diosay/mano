/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.otpl;

import java.nio.file.Path;
import mano.util.Utility;

/**
 * 编译时上下文。
 *
 * @author junhwong
 */
public interface CompilationContext {

    /**
     *
     * @return 获取目标生成文件的临时目录。
     */
    String getSourcePath();

    /**
     *
     * @return 获取源文件的根目录。
     */
    String getTargetPath();

    default String getCanonicalRelativeFile(String file, String currentPath, String basePath,boolean baseIsFile) {
        file = (file == null ? "" : file.trim()).toLowerCase().replace("\\", "/");
        if (file.startsWith("~/")) {
            return file.substring(1);
        } else if (file.startsWith("./")) {
//            if(currentPath==null || "".equals(currentPath)){
//                currentPath="";
//            }
//            if(basePath==null || "".equals(basePath)){
//                basePath="";
//            }
            if (currentPath != null && basePath != null) {
                if(currentPath.toLowerCase().startsWith(basePath.toLowerCase())){
                    currentPath=currentPath.substring(basePath.length());
                }
                if(baseIsFile){
                    return Utility.toPath(Utility.toPath(currentPath).getParent().toString(),file.substring(1)).toString();
                }else{
                    Utility.toPath(currentPath, file.substring(1));
                }
//                Path path=Utility.toPath(currentPath, file.substring(1));
//                return baseIsFile?path.getParent().toString():path.toString();
            }
            
            return file.substring(1);
        }
        return file;
    }

    default String getCanonicalTargetFile(String relativeSourceFile,String suffix) {
        return Utility.toPath(getTargetPath(),Integer.toHexString(getCanonicalRelativeFile(relativeSourceFile, null, null,false).hashCode())).toString()+suffix;
    }

    default String getCanonicalSourceFile(String relativeSourceFile){
        return Utility.toPath(getSourcePath(),getCanonicalRelativeFile(relativeSourceFile, null, null,false)).toString();
    }
}
