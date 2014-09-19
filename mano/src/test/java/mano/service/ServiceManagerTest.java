/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import java.lang.reflect.Field;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class ServiceManagerTest {

    public ServiceManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * 测试是否可以获取私有字段
     */
    @Test
    public void testGetPrivateMembers() {
        System.out.println("GetPrivateMembers");

        ServiceManager instance = ServiceManager.getInstance();
        HashMap map = null;
        try {
            Field field=instance.getClass().getDeclaredField("services");
            field.setAccessible(true);
            map = (HashMap) field.get(instance);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace(System.err);
        }
        //assertFalse("assert that private filed[serviceis] is a null.",map!=null);
    }

}
