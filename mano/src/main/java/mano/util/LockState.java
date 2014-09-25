/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.util;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface LockState {
    void notifyDone();
     boolean isDone();
     void waitDone() throws InterruptedException;
}
