/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service.http;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import mano.net.http.HttpContext;
import mano.net.http.HttpException;
import mano.net.http.HttpModule;
import mano.net.http.HttpStatus;
import mano.util.ImageUtil;
import mano.util.NameValueCollection;
import mano.web.WebApplication;

/**
 *
 * @author jun
 */
public class ManoPlugsModule implements HttpModule {

    WebApplication app;
    private NameValueCollection<String> mappings;
    @Override
    public void init(WebApplication app, Properties params) {
        this.app = app;
        mappings = new NameValueCollection<>();
        mappings.put("jpg", "image/jpeg");
        mappings.put("jpeg", "image/jpeg");
        mappings.put("png", "image/png");
        mappings.put("gif", "image/gif");
        mappings.put("bmp", "image/bmp");
    }

    @Override
    public boolean handle(HttpContext context) throws Exception {
        return this.handle(context, context.getRequest().url().getPath());
    }

    @Override
    public boolean handle(HttpContext context, String tryPath) throws Exception {
        if ("/mano-thumb".equalsIgnoreCase(tryPath)) {
            return this.thumb(context);
        }
        //System.out.println("MT:" + tryPath);
        return false;
    }

    @Override
    public void dispose() {
    }

    boolean thumb(HttpContext context) throws HttpException, IOException {
        //System.out.println("fgghjgttgg");
        String path = context.getRequest().query().getOrDefault("p", null);
        if (path == null) {
            throw new HttpException(HttpStatus.NotFound, "丢失图片路径");
        }

        File src = new File(context.getServer().mapPath(path));
        if (!src.exists() || !src.isFile()) {
            throw new HttpException(HttpStatus.NotFound, "图片未找到");
        }

        String name = src.getName().toLowerCase();

        if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp"))) {
            throw new HttpException(HttpStatus.NotAcceptable, "文件格式不支持");
        }

        int width = 0;
        int height = 0;

        try {
            width = Integer.parseInt(context.getRequest().query().getOrDefault("w", "0"));
        } catch (Throwable t) {

        }
        try {
            height = Integer.parseInt(context.getRequest().query().getOrDefault("h", "0"));
        } catch (Throwable t) {

        }
        int lst = name.lastIndexOf(".");
        String ext = name.substring(lst+1);
        if (width == 0 && height == 0) {
            return StaticFileModule.process(context, src.toString(), mappings.get(ext));
        }
        name = name.substring(0, lst) + "-" + width + "x" + height +ImageUtil.decode("ffffff").getRGB();
        // "-" + Color
        //File.createTempFile(ext, ext)
        //context.getServer().mapPath(path)
        File target = new File(context.getServer().mapPath("WEB-INF/tmp/" + name + "." + ext));
        
        if (!target.exists() || !target.isFile()) {
            BufferedImage img = ImageUtil.resize(src, width, height, "ffffff");
            ImageUtil.saveJpeg(img, lst, target,ext);
        }

        return StaticFileModule.process(context, target.toString(), mappings.get(ext));
    }

}
