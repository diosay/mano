/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.logging;

import java.io.Serializable;
import mano.Action;
import mano.DateTime;
import mano.runtime.Intent;

/**
 *
 * @author johnwhang
 */
public class Log implements Logger {

    /**
     * 获取只能用于跟踪的日志器。
     * <p>{@code isTraceEnabled()}为{@code true},其它为{@code false}。
     */
    static public final Logger TRACE;
    static{
        Log l=new Log("default");//Trace
        l.setDebugEnabled(true);
        l.setErrorEnabled(false);
        l.setFatalEnabled(false);
        l.setInfoEnabled(false);
        l.setWarnEnabled(false);
        l.setTraceEnabled(true);
        TRACE=l;
    }
    
    
//    static class LogSettings {
//
//        boolean isDebugEnabled;
//        boolean isInfoEnabled;
//        boolean isWarnEnabled;
//        boolean isErrorEnabled;
//        boolean isFatalEnabled;
//        boolean isTraceEnabled;
//        Action<Intent> action = (intent) -> {
//            if (intent.isFaulted()) {
//                System.err.println("===Logger Error===");
//                intent.getException().printStackTrace(System.err);
//            }
//        };
//    }
//    static final LogSettings settings;
    protected class EntryImpl implements Entry, Serializable {

        /**
         * @serial The Throwable (if any) associated with log message
         */
        private Throwable[] thrown;
        /**
         * @serial Logging message level
         */
        private Level level;
        /**
         * @serial Name of the source Logger.
         */
        private String category;
        /**
         * @serial Class that issued logging call
         */
        private String sourceClassName;

        /**
         * @serial Method that issued logging call
         */
        private String sourceMethodName;

        /**
         * @serial Non-localized raw message text
         */
        private CharSequence message;

        /**
         * @serial Thread ID for thread that issued logging call.
         */
        private long threadId;

        private long lineNumber;

        private DateTime time;

        private Handler handler;

        private Logger logger;

        @Override
        public String getCategory() {
            return this.category;
        }

        @Override
        public Level getLevel() {
            return this.level;
        }

        @Override
        public DateTime getTime() {
            return this.time;
        }

        @Override
        public CharSequence getMessage() {
            return this.message;
        }

        @Override
        public Throwable[] getExceptions() {
            return this.thrown;
        }

        @Override
        public String getSourceClassName() {
            return this.sourceClassName;
        }

        @Override
        public String getSourceMethodName() {
            return this.sourceMethodName;
        }

        @Override
        public long getSourceLineNumber() {
            return this.lineNumber;
        }

        @Override
        public long getSourceThreadId() {
            return this.threadId;
        }

        @Override
        public Handler getHandler() {
            return handler;
        }

        @Override
        public Logger getLogger() {
            return logger;
        }

    }

//    static {
//        settings = new LogSettings();
//        try {
//            settings.isDebugEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.debug", "true"));
//        } catch (Throwable t) {
//            settings.isDebugEnabled = true;
//            System.err.println("===Logger Settings Error===");
//            t.printStackTrace(System.err);
//        }
//        try {
//            settings.isErrorEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.error", "true"));
//        } catch (Throwable t) {
//            settings.isErrorEnabled = true;
//            System.err.println("===Logger Settings Error===");
//            t.printStackTrace(System.err);
//        }
//
//        try {
//            settings.isFatalEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.fatal", "true"));
//        } catch (Throwable t) {
//            settings.isFatalEnabled = true;
//            System.err.println("===Logger Settings Error===");
//            t.printStackTrace(System.err);
//        }
//
//        try {
//            settings.isInfoEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.info", "true"));
//        } catch (Throwable t) {
//            settings.isInfoEnabled = true;
//            System.err.println("===Logger Settings Error===");
//            t.printStackTrace(System.err);
//        }
//
//        try {
//            settings.isWarnEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.warn", "true"));
//        } catch (Throwable t) {
//            settings.isWarnEnabled = true;
//            System.err.println("===Logger Settings Error===");
//            t.printStackTrace(System.err);
//        }
//        try {
//            settings.isTraceEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.trace", "true"));
//        } catch (Throwable t) {
//            settings.isTraceEnabled = true;
//            System.err.println("===Logger Settings Trace===");
//            t.printStackTrace(System.err);
//        }
//    }
    private String category;
    private boolean isDebugEnabled = true;
    private boolean isInfoEnabled = true;
    private boolean isWarnEnabled = true;
    private boolean isErrorEnabled = true;
    private boolean isFatalEnabled = true;
    private boolean isTraceEnabled = false;
    private Handler handler = ConsoleHandler.getDefault();
    private String serviceName = LogService.DEFAULT_SERVICE_NAME;
    private String serviceAction = "log";
    private Action<Intent> action = (intent) -> {
        if (intent.isFaulted()) {
            System.err.println("===Logger Error===");
            intent.getException().printStackTrace(System.err);
        }
    };

    public static Logger get(String category) {
        return new Log(category);
    }

    public Log(String category) {
        this.category = category;
    }

    protected EntryImpl create(Thread thread, StackTraceElement[] traces, Level level, CharSequence message, Throwable... causes) {
        EntryImpl entry = new EntryImpl();
        entry.logger=this;
        entry.handler = handler;
        entry.category = this.category;
        entry.level = level;
        entry.time = DateTime.now();
        entry.threadId = thread.getId();
        entry.message = message;
        entry.thrown = causes;
        if (traces != null && traces.length > 2) {
            entry.sourceClassName = traces[2].getClassName();
            entry.sourceMethodName = traces[2].getMethodName();
            entry.lineNumber = traces[2].getLineNumber();
        }
        return entry;
    }

    protected void doLog(Entry entry) {

//        for (Throwable t : entry.getExceptions()) {
//            t.printStackTrace();
//        }
        Intent bag = new Intent(this.serviceName, this.serviceAction);
        bag.set("entry", entry);
        try {
            bag.submit(action);
        } catch (Throwable t) {
            System.out.println("Failed to logging:" + t.getMessage());
            try {
                entry.getHandler().doLog(entry);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void setIntentService(String serviceName, String action) {
        if (serviceName == null || "".equals(serviceName)) {
            throw new java.lang.NullPointerException("serviceName");
        }
        if (action == null || "".equals(action)) {
            throw new java.lang.NullPointerException("action");
        }
        this.serviceName = serviceName;
        this.serviceAction = action;
    }

    public void setHandler(Handler handler) {
        if (handler == null) {
            throw new java.lang.NullPointerException("handler");
        }
        this.handler = handler;
    }

    /**
     * 设置一个值，以判断 DEBUG 级别是否启用。
     *
     * @param b
     */
    public void setDebugEnabled(boolean b) {
        isDebugEnabled = b;
    }

    /**
     * 设置一个值，以判断 INFO 级别是否启用。
     *
     * @param b
     */
    public void setInfoEnabled(boolean b) {
        isInfoEnabled = b;
    }

    /**
     * @param b
     * @设置一个值，以判断 WARN 级别是否启用。
     */
    public void setWarnEnabled(boolean b) {
        isWarnEnabled = b;
    }

    /**
     * 设置一个值，以判断 ERROR 级别是否启用。
     *
     * @param b
     */
    public void setErrorEnabled(boolean b) {
        isErrorEnabled = b;
    }

    /**
     * 设置一个值，以判断 INFO 级别是否启用。
     *
     * @param b
     */
    public void setFatalEnabled(boolean b) {
        isFatalEnabled = b;
    }

    /**
     * 设置一个值，以判断 TRACE 级别是否启用。
     *
     * @param b
     */
    public void setTraceEnabled(boolean b) {
        isTraceEnabled = b;
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override
    public boolean isInfoEnabled() {
        return isInfoEnabled;
    }

    @Override
    public boolean isWarnEnabled() {
        return isWarnEnabled;
    }

    @Override
    public boolean isErrorEnabled() {
        return isErrorEnabled;
    }

    @Override
    public boolean isFatalEnabled() {
        return isFatalEnabled;
    }

    @Override
    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }

    @Override
    public void debug(CharSequence message, Throwable... causes) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.DEBUG, message, causes));
    }

    @Override
    public void debug(Throwable cause) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.DEBUG, null, new Throwable[]{cause}));
    }

    @Override
    public void info(CharSequence message, Throwable... causes) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.INFO, message, causes));
    }

    @Override
    public void info(Throwable cause) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.INFO, null, new Throwable[]{cause}));
    }

    @Override
    public void warn(CharSequence message, Throwable... causes) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.WARNING, message, causes));
    }

    @Override
    public void warn(Throwable cause) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.WARNING, null, new Throwable[]{cause}));
    }

    @Override
    public void error(CharSequence message, Throwable... causes) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.ERROR, message, causes));
    }

    @Override
    public void error(Throwable cause) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.ERROR, null, new Throwable[]{cause}));
    }

    @Override
    public void fatal(CharSequence message, Throwable... causes) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.FATAL, message, causes));
    }

    @Override
    public void fatal(Throwable cause) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.FATAL, null, new Throwable[]{cause}));
    }

    @Override
    public void trace(CharSequence message, Throwable... causes) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.TRACE, message, causes));
    }

    @Override
    public void trace(Throwable cause) {
        doLog(create(Thread.currentThread(), Thread.currentThread().getStackTrace(), Level.TRACE, null, new Throwable[]{cause}));
    }
}
