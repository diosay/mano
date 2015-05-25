/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import mano.DateTime;

/**
 *
 * @author jun
 */
public class FileHandler extends Handler {

    private long maxFileSize = 10485760L;
    private String root;

    public FileHandler(String path, long maxFileSize) throws FileNotFoundException {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new FileNotFoundException("不是一个有效的目录");
        }
        if (maxFileSize <= 0) {
            throw new IllegalArgumentException("maxFileSize 必须大于0.");
        }
        root = dir.toString();
        this.maxFileSize = maxFileSize;
    }

    public FileHandler(String path) throws FileNotFoundException {
        this(path, 10485760L);
    }

    public String getLogPath() {
        return root;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    protected File getLogFile(Entry entry) throws FileNotFoundException, IOException {
        File dir = mano.util.Utility.toPath(getLogPath(), entry.getCategory()).toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            if (!dir.mkdirs()) {
                throw new FileNotFoundException("创建日志目录失败。");
            }
        }
        File file = mano.util.Utility.toPath(dir.toString(), entry.getLevel().name.toLowerCase() + "-" + DateTime.now().toString("yyyyMMdd") + ".log").toFile();
        if (file.exists() && file.isFile()) {
            if (file.length() < getMaxFileSize()) {
                return file;
            }
        } else if (file.createNewFile()) {
            return file;
        }

        for (int i = 1; i < 100; i++) {

            file = mano.util.Utility.toPath(dir.toString(), entry.getLevel().name.toLowerCase() + "-" + DateTime.now().toString("yyyyMMdd") + "-" + i + ".log").toFile();
            if (file.exists() && file.isFile()) {
                if (file.length() < getMaxFileSize()) {
                    return file;
                }
            } else if (file.createNewFile()) {
                return file;
            }

        }

        throw new FileNotFoundException("创建日志文件失败。");
    }

    @Override
    public boolean doLog(Entry entry) throws Exception {
//        try (PrintStream ps = new PrintStream(getLogFile(entry), mano.io.CharsetUtil.UTF8.name())) {
//            ps.append(this.getFormatter().format(entry));
//            ps.flush();
//            ps.close();
//        }
        try (FileWriter writer = new FileWriter(getLogFile(entry),true)) {
            writer.append(this.getFormatter().format(entry));
            writer.flush();
        }
        return false;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

}
