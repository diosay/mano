package mano.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author junhwong
 */


public class DataRecord extends HashMap<String,Serializable>{
    
    public interface Entry extends Map.Entry<String,Serializable>{
        
    }
    
//    public Serializable put(String key,Serializable value){
//        super.
//        return this.get(key);
//    }
    
    
    
    
//    
//    
//    
//    String key;
//    Serializable value;
//    public DataRecord(String key){
//        if(key==null || "".equals(key)){
//            throw new java.lang.NullPointerException("key");
//        }
//        this.key=key;
//    }
//    
//    @Override
//    public String getKey() {
//        return this.key;
//    }
//
//    @Override
//    public Serializable getValue() {
//        return this.value;
//    }
//
//    @Override
//    public Serializable setValue(Serializable value) {
//        this.value=value;
//        return this.getValue();
//    }
    
}
