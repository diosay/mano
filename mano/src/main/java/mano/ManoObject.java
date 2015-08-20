/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano;

import java.util.Map;
import java.util.Objects;
import mano.DateTime;
//http://www.ccidnet.com/2006/1013/920905.shtml

/**
 *
 * @author junhwong
 */
public class ManoObject {

    public static final int TYPE_NULL = 0;
    public static final int TYPE_OBJECT = 1;
    public static final int TYPE_INT = 2;
    public static final int TYPE_FLOAT = 3;
    public static final int TYPE_DATETIME = 4;
    public static final int TYPE_STRING = 5;
    public static final int TYPE_BOOLEAN = 6;

    private final Object realValue;
    private final int type;

    public static int exploreType(Object value) {
        if (value == null) {
            return TYPE_NULL;
        }

        Class clazz = value.getClass();
        //System.out.println(clazz.getName());

        switch (clazz.getName()) {
            case "java.lang.Boolean":
                return TYPE_BOOLEAN;//
            case "java.lang.Character":
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Integer":
            case "java.lang.Long":
                return TYPE_INT;
            case "java.lang.Float":
            case "java.lang.Double":
                return TYPE_FLOAT;
            case "java.lang.Void":
                return TYPE_NULL;
            case "mano.DateTime":
                return TYPE_DATETIME;
            case "java.lang.String":
                return TYPE_STRING;
        }

        return TYPE_OBJECT;
    }

    public ManoObject(Object value) {
        this.realValue = value;
        this.type = exploreType(value);
    }

    private ManoObject(int type, Object val) {
        this.type = type;
        this.realValue = val;
    }

    /**
     * @return the realValue
     */
    public Object getRealValue() {
        return realValue;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    public boolean isNumber() {
        return type == TYPE_INT || type == TYPE_FLOAT;
    }

    public boolean isNull() {
        return type == TYPE_NULL;
    }

    public boolean isString() {
        return type == TYPE_STRING;
    }

    public long intValue() {
        return (long) Math.floor(floatValue());
    }

    public double floatValue() {
        if (this.isNull()) {
            return 0;
        }
        try {
            return Double.parseDouble(this.realValue.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String stringValue() {
        if (this.isNull()) {
            return emptyString;
        }
        return this.realValue.toString();
    }

    public boolean eq(Object obj) {
        ManoObject right = get(obj);

        if (type == TYPE_STRING || right.type == TYPE_STRING) {
            //System.out.println("bbb");
            return this.stringValue().equals(right.stringValue());
        }
        else if (this.isNull()) {
            return right.isNull();
        } else if (right.isNull()) {
            return this.isNull();
        } else if (this.isNumber() || right.isNumber()) {
            return this.floatValue() == right.floatValue();
        } else if (type == TYPE_BOOLEAN || right.type == TYPE_BOOLEAN) {
            return toBoolean(this.realValue, true) == toBoolean(right.realValue, false)
                    && toBoolean(this.realValue, false) == toBoolean(right.realValue, true);
        }

        return Objects.equals(this.realValue, right.realValue);
    }

    public boolean ne(Object obj) {
        return !this.eq(obj);
    }

    public static final ManoObject NULL = new ManoObject(0, null);
    public static final String emptyString = "";

    public static long toLong(Object val, long def) {
        try {
            return Long.parseLong(val.toString());
        } catch (Throwable t) {
            return def;
        }
    }

    public static int toInt(Object val, int def) {
        try {
            return Integer.parseInt(val.toString());
        } catch (Throwable t) {
            return def;
        }
    }

    public static boolean toBoolean(Object val, boolean def) {
        try {
            return Boolean.parseBoolean(val.toString());
        } catch (Throwable t) {
            return def;
        }
    }

    public static float toFloat(Object val, float def) {
        try {
            return Float.parseFloat(val.toString());
        } catch (Throwable t) {
            return def;
        }
    }

    public static DateTime toDateTime(Object val, DateTime def) {
        try {
            return DateTime.parse(val.toString(), DateTime.FORMAT_ISO);
        } catch (Throwable t) {
            return def;
        }
    }

    public static ManoObject get(Object obj) {
        if (obj == null) {
            return NULL;
        }
        Class clazz = obj.getClass();
        if (clazz.getName().equals("com.diosay.otpl.runtime.OTPLObject")) {
            return (ManoObject) obj;
        }
        return new ManoObject(exploreType(obj), obj);
    }

    public static ManoObject get(Object obj, Map<Object, ManoObject> cache) {
        if (obj == null) {
            return NULL;
        }
        if (cache == null) {
            throw new java.lang.NullPointerException("cache");
        }
        ManoObject result = null;
        if (cache.containsKey(obj)) {
            result = cache.get(obj);
        }
        if (result == null) {
            result = get(obj);
            cache.put(obj, result);
        }
        return result;
    }

    public static void mainx(String[] args) {
        //Float x = 1d;
        //ManoObject obj = ManoObject.get(3.5f);
        System.out.println("val:" + (ManoObject.get("f").ne(null)));
    }

}
