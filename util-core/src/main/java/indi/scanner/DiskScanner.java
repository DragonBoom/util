package indi.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import indi.constant.DiskEntryType;
import indi.constant.StoreType;
import indi.data.dto.DirectoryDTO;
import indi.data.dto.DirectoryEntryDTO;
import indi.data.dto.FileDTO;
import indi.exception.WrapperException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
* Windows磁盘扫描器
* 
**/
@Slf4j
public class DiskScanner {
    
    public DiskScanner() {
        log.info("启用Windows系统磁盘扫描器");
    }
    
    /**
     * 设置绝对路径，若不为空，则创建对象时将隐藏该路径
     */
    @Getter
    @Setter
    private Path absolutePath = null;
    
    /**
     * 扫描指定文件，得到包含文件所有数据的结构化的Java对象，将获取文件内容（字节数组）
     * 
     * @param path
     * @return
     */
    public Optional<FileDTO> scanFile(Path path) {
        return scanFile(path, true);
    }

    /**
     * 扫描指定文件，得到包含文件所有数据的结构化的Java对象
     * 
     * @param isScanFileContent 是否读取文件内容
     */
    public Optional<FileDTO> scanFile(Path path, boolean isScanFileContent) {
        // 1. 校验
        // a. 校验路径是否存在
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("路径指向的文件不存在");
        }
        // b. 校验路径是指向目录还是文件
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("路径指向的是目录而不是文件");
        }
        /**
         * 值得注意的是，应该存在着某些特殊的“文件”，比如Java中用单词“SymbolicLink”描述的文件，这些文件可能存在着某些特殊的方面，
         * 可能需要特殊处理，但目前还没有实际碰到过，暂不作处理
         */
        // 读取文件基本属性
        BasicFileAttributeView fileAttributeView = 
                Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        final BasicFileAttributes attributes;
        try {
            attributes = fileAttributeView.readAttributes();
        } catch (IOException e) {
            throw new WrapperException(e);
        }
        
        // 2. 根据文件信息，构建FileDTO对象
        FileDTO fileDTO = scanDirectoryEntry(path, FileDTO.class).get();
        
        // 文件内容
        if (isScanFileContent) {
            byte[] bytes = null;
            try {
                bytes = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new WrapperException(e);
            }
            fileDTO.setContent(bytes);
        }
            
        fileDTO.setSize(attributes.size());// 文件大小
        // 创建时间
        Optional.ofNullable(attributes.creationTime())
                .map(FileTime::toMillis)
                .map(Date::new)
                .ifPresent(fileDTO::setCreateTime);
        // 最后访问时间
        Optional.ofNullable(attributes.lastAccessTime())
                .map(FileTime::toMillis)
                .map(Date::new)
                .ifPresent(fileDTO::setLastAccessTime);
        //最后修改时间
        Optional.ofNullable(attributes.lastModifiedTime())
                .map(FileTime::toMillis)
                .map(Date::new)
                .ifPresent(fileDTO::setLastModifiedTime);
        
        
        fileDTO.setStoreType(StoreType.DISK);
        
        return Optional.of(fileDTO);
    }
    
    /**
     * 扫描指定目录
     * 
     * @param path
     */
    public Optional<DirectoryDTO> scanDirectory(Path path) {
        return scanDirectory(path, true);
    }
    
    public Optional<DirectoryDTO> scanDirectory(Path path, boolean isScanFileContent) {
        return scanDirectory(path, isScanFileContent, null, null);
    }
    
    public Optional<DirectoryDTO> scanDirectory(Path path, boolean isScanFileContent,
            @Nullable Function<DirectoryEntryDTO, DirectoryEntryDTO> entryHandler) {
        return scanDirectory(path, isScanFileContent, entryHandler, dto -> (FileDTO) entryHandler.apply(dto));
    }

    /**
     * 
     * @param path
     * @param isScanFileContent 是否读取文件内容
     * @return
     */
    public Optional<DirectoryDTO> scanDirectory(Path path, boolean isScanFileContent, 
            @Nullable Function<DirectoryEntryDTO, DirectoryEntryDTO> directoryHandler,
            @Nullable Function<FileDTO, FileDTO> fileHandler) {
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("路径指向的目录不存在");
        }
        DirectoryDTO directoryDTO = new DirectoryDTO();
        // 填充子条目信息
        try {
            List<DirectoryEntryDTO> entries = Files.list(path)
                    .map(entryPath -> {
                        if (Files.isDirectory(entryPath, LinkOption.NOFOLLOW_LINKS)) {
                            DirectoryEntryDTO directoryEntryDTO = this.scanDirectoryEntry(entryPath).get();
                            // 后处理
                            return Optional.ofNullable(directoryHandler)
                                    .map(handler -> handler.apply(directoryEntryDTO)).orElse(directoryEntryDTO);
                        } else {
                            FileDTO fileDTO = this.scanFile(entryPath, isScanFileContent).get();
                            // 后处理
                            return Optional.ofNullable(fileHandler)
                                    .map(handler -> handler.apply(fileDTO)).orElse(fileDTO);
                        }
                    })
                    .collect(Collectors.toList());
            directoryDTO.setEntries(entries);
        } catch (IOException e) {
            throw new WrapperException(e);
        }
        // 填充目录本身的信息
        fillDirectoryEntryInfo(path, directoryDTO);
        return Optional.of(directoryDTO);
    }
    
    /**
     * 扫描目录条目，得到条目的通用数据
     */
    public Optional<DirectoryEntryDTO> scanDirectoryEntry(Path path) {
        return scanDirectoryEntry(path, DirectoryEntryDTO.class);
    }
    
    /**
     * 扫描目录条目（可能为文件/子目录），并将结果转换为给定DTO
     * 
     * @param path
     * @param targetClass
     * @return
     */
    public <T extends DirectoryEntryDTO> Optional<T> scanDirectoryEntry(Path path, Class<T> targetClass) {
        T entryDTO = null;
        try {
            entryDTO = targetClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new WrapperException(e);
        }
        fillDirectoryEntryInfo(path, entryDTO);
        
        return Optional.of(entryDTO);
    }
    
    private void fillDirectoryEntryInfo(Path path, DirectoryEntryDTO entryDTO) {
        entryDTO.setRealPath(path);
        // 读取文件基本属性
        BasicFileAttributeView fileAttributeView = 
                Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        BasicFileAttributes attributes = null;
        try {
            attributes = fileAttributeView.readAttributes();
        } catch (IOException e) {
            throw new WrapperException(e);
        }
        // 2. 根据文件信息，填充DTO
        // 设置路径，若有绝对路径则屏蔽掉绝对路径 TODO 这里其实还需要一层是否为绝对路径子路径的判断。。。
        setViewablePathPath(path, entryDTO);
        
        entryDTO.setName(path.getFileName().toString());
        
        FileTime creationTime = attributes.creationTime();
        Instant creationInstant = creationTime.toInstant();
        Date createionDate = Date.from(creationInstant);
        entryDTO.setCreateTime(createionDate);
        // 判断类型
        DiskEntryType type = null;
        if (attributes.isDirectory()) {
            type = DiskEntryType.DIRECTORY;
        } else if (attributes.isRegularFile()) {
            type = DiskEntryType.FILE;
        } else {
            type = DiskEntryType.UNKNOWN;
        }
        entryDTO.setType(type);
    }
    
    // 设置路径，若有绝对路径则屏蔽掉绝对路径 TODO 这里其实还需要一层是否为绝对路径子路径的判断。。。
    private void setViewablePathPath(Path path, DirectoryEntryDTO entryDTO) {
        Path viewablePath = null;
        if (absolutePath != null) {
            viewablePath = absolutePath.relativize(path);
        } else {
            viewablePath = path;
        }
        entryDTO.setPath(viewablePath.toString());
    }
}
