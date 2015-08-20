/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.maven.manoserver.plugin;

import com.diosay.otpl.CompilationContext;
import com.diosay.otpl.runtime.Interpreter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @author junhwong
 */
public class OtpCompileMojo extends AbstractMojo {

    /**
     * @parameter expression=”${aSystemProperty}”
     * default-value=”${anExpression}”
     */
    private String otplTemplateSourceDirectory;
    /**
     * @parameter expression=”${aSystemProperty}”
     * default-value=”${anExpression}”
     */
    private String otplTargetDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        this.getLog().info("------------------------------------------------------------------------");
        this.getLog().info("OTPL Template Pakaging...");
        this.getLog().info("------------------------------------------------------------------------");
        File targetDir = new File(otplTargetDirectory);
        File sourceDir = new File(otplTemplateSourceDirectory);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            this.getLog().error("模板根路径不存在");
            return;
        } else if (!sourceDir.canRead()) {
            this.getLog().error("模板根路径不能读");
            return;
        }
        try {
            if (!targetDir.exists() || !targetDir.createNewFile()) {

                this.getLog().error("模板编译目标路径不存在，尝试创建失败！");
                return;
            } else if (!targetDir.canRead() || !targetDir.canWrite()) {
                this.getLog().error("模板根路径不能读或写");
                return;
            }
        } catch (Exception ex) {
            this.getLog().debug(ex);
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
                this.v(interpreter, context, "~/" + name, dir.toString(), name);
            } catch (Exception ex) {
                this.getLog().debug(ex);
            }
            return false;
        });

    }

    void v(Interpreter interpreter, CompilationContext context, String parent, String path, String name) throws Exception {
        File file = new File(path + "/" + name);
        if (!file.exists()) {
        } else if (file.isFile()) {
            interpreter.compileFile(context, parent + "/" + name);
        } else if (file.isDirectory()) {
            file.list((dir, name2) -> {
                try {
                    this.v(interpreter, context, parent + "/" + name, dir.toString(), name2);
                } catch (Exception ex) {
                    this.getLog().debug(ex);
                }
                return false;
            });
        }
    }
}
