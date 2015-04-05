/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import java.io.Serializable;
import mano.Action;
import mano.DateTime;
import mano.service.Intent;

/**
 *
 * @author johnwhang
 */
public class Log implements ILogger {

    static class LogSettings {

        boolean isDebugEnabled;
        boolean isInfoEnabled;
        boolean isWarnEnabled;
        boolean isErrorEnabled;
        boolean isFatalEnabled;
        boolean isTraceEnabled;
        Action<Intent> action = (intent) -> {
            if (intent.isFaulted()) {
                System.err.println("===Logger Error===");
                intent.getException().printStackTrace(System.err);
            }
        };
    }
    static final LogSettings settings;
    String category;

    public class Entry implements IEntry, Serializable {

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

    }

    static {
        settings = new LogSettings();
        try {
            settings.isDebugEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.debug", "true"));
        } catch (Throwable t) {
            settings.isDebugEnabled = true;
            System.err.println("===Logger Settings Error===");
            t.printStackTrace(System.err);
        }
        try {
            settings.isErrorEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.error", "true"));
        } catch (Throwable t) {
            settings.isErrorEnabled = true;
            System.err.println("===Logger Settings Error===");
            t.printStackTrace(System.err);
        }

        try {
            settings.isFatalEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.fatal", "true"));
        } catch (Throwable t) {
            settings.isFatalEnabled = true;
            System.err.println("===Logger Settings Error===");
            t.printStackTrace(System.err);
        }

        try {
            settings.isInfoEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.info", "true"));
        } catch (Throwable t) {
            settings.isInfoEnabled = true;
            System.err.println("===Logger Settings Error===");
            t.printStackTrace(System.err);
        }

        try {
            settings.isWarnEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.warn", "true"));
        } catch (Throwable t) {
            settings.isWarnEnabled = true;
            System.err.println("===Logger Settings Error===");
            t.printStackTrace(System.err);
        }
        try {
            settings.isTraceEnabled = Boolean.valueOf(System.getProperty("mano.logging.level.trace", "true"));
        } catch (Throwable t) {
            settings.isTraceEnabled = true;
            System.err.println("===Logger Settings Trace===");
            t.printStackTrace(System.err);
        }
    }

    public static ILogger get(String category){
        return new Log(category);
    }
    
    protected Log(String category){
        this.category=category;
    }
    
    protected Entry create(Thread thread, StackTraceElement[] traces, Level level, CharSequence message, Throwable... causes) {
        Entry entry = new Entry();
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

    protected void doLog(IEntry entry) {
        System.out.println("TODO-LOG:"+entry.getMessage());
        for(Throwable t:entry.getExceptions()){
            t.printStackTrace();
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return settings.isDebugEnabled;
    }

    @Override
    public boolean isInfoEnabled() {
        return settings.isInfoEnabled;
    }

    @Override
    public boolean isWarnEnabled() {
        return settings.isWarnEnabled;
    }

    @Override
    public boolean isErrorEnabled() {
        return settings.isErrorEnabled;
    }

    @Override
    public boolean isFatalEnabled() {
        return settings.isFatalEnabled;
    }
    
    @Override
    public boolean isTraceEnabled() {
        return settings.isTraceEnabled;
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
