package indi.data.dto;

import java.util.Date;

import indi.constant.DiskEntryType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用于描述磁盘上的一个【条目】，主要包括:
 * <ul>
 *  <li>文件</li>
 *  <li>文件夹</li>
 *  <li>快捷方式/链接</li>
 * </ul>
 * 
 * @author DragonBoom
 *
 */
@Getter
@Setter
@ToString(callSuper = true)// 使得转化为字符串时包含继承来的属性
public class DirectoryEntryDTO {
    private String name;
    private String path;// 路径
    private Date createTime;
    private Date deletedTime;
    private Date updateTime;
    private Boolean isDeleted = false;
    private DiskEntryType type;
    private String description;// 描述
    private String category;// 标签
}
