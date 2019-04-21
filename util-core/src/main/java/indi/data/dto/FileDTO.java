package indi.data.dto;

import indi.constant.DiskEntryType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)// 使得转化为字符串时包含继承来的属性
public class FileDTO extends DirectoryEntryDTO {
    private Long size;// 文件大小
    private String storeType;// 存储方式（具体类型见 indi.constant.StoreType）
    private DiskEntryType type = DiskEntryType.FILE;
    private byte[] content;// 文件内容，字节数组格式
}
