/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.logging;

import mano.runtime.Intent;
import mano.runtime.IntentService;

/**
 * 日志服务。
 *
 * @author jun
 */
public class LogService extends IntentService {

    public static final String DEFAULT_SERVICE_NAME="mano.logging.LogService";
    

    @Override
    public void execute(Intent intent) throws Exception {
        if (intent.isCancelled()) {
            return;
        }
        if ("log".equals(intent.getAction())) {
            Entry entry = (Entry) intent.get("entry");
            if (entry == null) {
                throw new IllegalArgumentException("log entry is required");
            }
            entry.getHandler().doLog(entry);
        }else{
            throw new IllegalArgumentException("action is undefined.");
        }
    }

    @Override
    protected void onInit() throws Exception {
        this.setProperty(PROP_NAME, DEFAULT_SERVICE_NAME);
    }

}
