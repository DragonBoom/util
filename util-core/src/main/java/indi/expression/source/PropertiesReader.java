package indi.expression.source;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PropertiesReader {

    /**
     * 简单地从配置文件流中读取键值对
     */
    public Map<String, String> read(FileInputStream inStream) throws IOException {
        HashMap<String, String> result = new HashMap<>();
        // 使用BufferedReader的readLine功能
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
        
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();// 可优化
            if (line.startsWith("#")) {
                continue;
            }
            int i = line.indexOf("=");
            int len = line.length();
            if (i == -1 || i == len - 1) {
                continue;
            }
            String k = line.substring(0, i);
            String v = line.substring(i + 1, len);
            result.put(k, v);
        }
        
        return result;
    }
}
