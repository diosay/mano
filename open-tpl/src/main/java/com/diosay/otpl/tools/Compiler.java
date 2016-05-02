/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.otpl.tools;

import com.diosay.otpl.CompilationContext;
import com.diosay.otpl.runtime.Interpreter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author junhwong
 */
public class Compiler {
    static String map;
    static String otplTemplateSourceDirectory = "E:\\repositories\\clamp\\clamp-web\\views";
    static String otplTargetDirectory = "E:\\repositories\\clamp\\clamp-web\\bin\\otc";
    static void error(String s) {
        System.out.println(s);
    }

    static void error(Exception ex) {
        ex.printStackTrace();
    }

    public static void main(String[] args) {
        //E:\repositories\view-repo\clamp-webapp\views\admin
        //E:\repositories\view-repo\clamp-webapp\views\admin\admin\ad.html
        
        

        File targetDir = new File(otplTargetDirectory);
        File sourceDir = new File(otplTemplateSourceDirectory);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            error("模板根路径不存在");
            return;
        } else if (!sourceDir.canRead()) {
            error("模板根路径不能读");
            return;
        }
        try {
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                error("模板编译目标路径不存在，尝试创建失败:" + targetDir);
                return;
            } else if (!targetDir.canRead() || !targetDir.canWrite()) {
                error("模板根路径不能读或写");
                return;
            }
        } catch (Exception ex) {
            error(ex);
            return;
        }
        CompilationContext context = new CompilationContext() {

            @Override
            public String getSourcePath() {
                return otplTemplateSourceDirectory;
            }

            @Override
            public String getTargetPath() {
                return otplTargetDirectory;
            }

        };
        Interpreter interpreter = new Interpreter();
        sourceDir.list((dir, name) -> {
            try {
                v(interpreter, context, "~", dir.toString(), name);
            } catch (Exception ex) {
                error(ex);
            }
            return false;
        });
        
        File mapFile=new File(otplTargetDirectory+"\\out.map");
        if(mapFile.exists()){
            mapFile.delete();
        }
        try {
            mapFile.createNewFile();
            try (java.io.PrintStream ps = new java.io.PrintStream(mapFile.getPath(),"utf8")) {
                ps.append(map);
                ps.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
        }
        

    }

    static void v(Interpreter interpreter, CompilationContext context, String parent, String path, String name) throws Exception {
        File file = new File(path + "/" + name);
        if (!file.exists()) {
        } else if (file.isFile()) {
            error("compiling " + file);
            File target =interpreter.compileFile(context, parent + "/" + name);
            map+=(file.getPath().replace(otplTemplateSourceDirectory, ""))+" -> "+(target.getPath().replace(otplTargetDirectory, ""))+"\r\n";
        } else if (file.isDirectory()) {
            file.list((dir, name2) -> {
                try {
                    v(interpreter, context, parent + "/" + name, dir.toString(), name2);
                } catch (Exception ex) {
                    error(ex);
                }
                return false;
            });
        }
    }

}
