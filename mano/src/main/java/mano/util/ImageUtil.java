/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author jun
 */
public class ImageUtil {

    public static Color decode(String nm) {
        try {
            return Color.decode(nm);
        } catch (NumberFormatException ex) {
            return Color.white;
        }
    }

    public static BufferedImage resize(File src, int width, int height, String fullColor) throws IOException {
        ImageIcon ii = new ImageIcon(src.getCanonicalPath());
        Image i = ii.getImage();

        int iWidth = i.getWidth(null);
        int iHeight = i.getHeight(null);
        double scale;
        if (width == height || width > height) {
            scale = (double) width / (double) iWidth;

            if (iWidth < iHeight) {
                scale = (double) height / (double) iHeight;//比例
            }

        } else {
            scale = (double) height / (double) iHeight;//比例
        }

        //double scaleWidth = (double) width / (double) iWidth;//比例
        //double scaleHeight = (double) height / (double) iHeight;//比例
        //scale = Math.max(scaleWidth, scaleHeight);
            /* 调整后的图片的宽度和高度 */
        int toWidth = (int) (iWidth * scale);
        int toHeight = (int) (iHeight * scale);

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = result.getGraphics();

        g.setColor(decode(fullColor));
        g.fillRect(0, 0, width, height);//Color.

        Image tmp = i.getScaledInstance(toWidth, toHeight, Image.SCALE_SMOOTH);
        g.drawImage(new ImageIcon(tmp).getImage(), (width - toWidth) / 2, (height - toHeight) / 2, null);// 

        g.dispose();
        return result;

    }

    public static BufferedImage crop(File src, int width, int height, int left, int top) throws IOException {
        ImageIcon ii = new ImageIcon(src.getCanonicalPath());
        Image i = ii.getImage();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = result.getGraphics();
        Image tmp = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(i.getSource(), new CropImageFilter(left, top, width, height)));
        //Image tmp = i.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        g.drawImage(new ImageIcon(tmp).getImage(), 0, 0, null);// 

        g.dispose();
        return result;

    }

    public static void saveJpeg(BufferedImage image, float quality, File file,String type) throws FileNotFoundException, IOException {
        file.delete();
        file.createNewFile();
        ImageIO.write(image, type, file);//thumbs/{path}?
        //try (FileOutputStream out = new FileOutputStream(file)) {
        //http://www.thinksaas.cn/group/topic/237566/
        //http://blog.chinaunix.net/uid-570310-id-2734859.html
        //http://bbs.csdn.net/topics/120029100
        //com.sun.image.codec.jpeg.JPEGImageEncoder encoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(out);
        //com.sun.image.codec.jpeg.JPEGEncodeParam param = com.sun.image.codec.jpeg.JPEGCodec.getDefaultJPEGEncodeParam(image);
        //param.setQuality(quality, true);
        //encoder.setJPEGEncodeParam(param);
        //encoder.encode(image);
        //}
    }
}
