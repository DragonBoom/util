package indi.command;

import java.util.HashMap;
import java.util.Map;

public class CommandContext {
    private Map<String, Object> data;

    public CommandContext() {
        init();
    }
    
    protected void init() {
        data = new HashMap<>();
    }
    
    public Object get(String key) {
        return data.get(key);
    }
    
    public void put(String key, Object value) {
        data.put(key, value);
    }
    
    
}
