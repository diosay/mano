/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface ServiceContainer {
    AbstractService getService(String serviceName);
}
