/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.data;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author junhwong
 */
public class DbRecord extends HashMap<String,Serializable> implements Record {

    public Serializable get(String name) {
        
        Queryable query=null;
        query.where();//Conditions.and(Conditions.eq(),Conditions.eq())
        //query(pj.prop(abc),pj.all(),pj.count(j))
        //addgroup(pj.count(j))
        //query.select(pj.gg,'')
        
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Record set(String name, Serializable value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
