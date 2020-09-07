package indi.command;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

/**
 * 用于控制台的工具类
 * 
 * @author wzh
 * @since 2019.12.15
 */
public class ConsoleUtils {

    /**
     * 以表格的形式打印内容到控制台
     * 
     * TODO: 2019.12.16 实现中，先把分块传输实现了先。。。
     * 
     * @param table
     */
    public static void printTable(Table<?, ?, ?> table) {
        // 计算每一列的宽度
        Object[] columnKeys = table.columnKeySet().stream().sorted().toArray();
        Object[] rowKeys = table.rowKeySet().stream().sorted().toArray();
        // 获取table的每个值
        // 获取值的长度，从而计算宽度
        // 
        for (Object obj : rowKeys) {
            String colV = obj.toString();
        }
        
//        System.out.println(rowKeySet);
//        System.out.println(columnKeySet);
    }
    
    @Test
    void go() {
        HashBasedTable<Object, Object, Object> table = HashBasedTable.create();
        table.put("1", "2", "v1");
        table.put("2", "2", "v1");
        table.put("4", "2", "v1");
        printTable(table);
    }
}
