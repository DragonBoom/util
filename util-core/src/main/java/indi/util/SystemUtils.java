package indi.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author wzh
 * @since 2019.12.07
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemUtils {

    /**
     * 判断当前系统是否为windows系统
     * 
     */
    public static boolean isWin() {
        String osName = System.getProperty("os.name");// Windows 10
        return osName.startsWith("Win");
    }
}
