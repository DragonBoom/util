package indi.constant;

/**
 * 磁盘上【条目】的类型，主要包括:
 * <ul>
 * <li>文件</li>
 * <li>文件夹</li>
 * <li>快捷方式/链接</li>
 * </ul>
 * 
 * 
 * @author DragonBoom
 *
 */
public enum DiskEntryType {
    FILE, 
    DIRECTORY,
    UNKNOWN
}
