package com.orca.backend.server;


import java.util.HashMap;
/**
 * It's a HashMap, but the Key is always a String, and 'get' and 'getOrDefault' 
 * trim, toLowerCase, and replace all '\' with '/' for each key
 * @param <T> 
 */
public class LCHashMap<T> extends HashMap<String, T>{
    @Override
        public T get(Object key) {
            if (!(key instanceof String)) {
                return null;
            }
            return super.get(((String) key).replaceAll("\\\\", "/").toLowerCase().trim());
        }
    @Override
        public T getOrDefault(Object key, T def) {
            if (!(key instanceof String)) {
                return def;
            }
            return super.getOrDefault(((String) key).replaceAll("\\\\", "/").toLowerCase().trim(),def);
        }

    @Override
    public boolean containsKey(Object key) {
        if(key instanceof String){
            return super.containsKey(((String)key).toLowerCase().trim());
        }
        return super.containsKey(key);
    }
        
}