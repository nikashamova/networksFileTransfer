package nsu.fit.shamova;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class AttachmentInfo {

    public ByteBuffer nameSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
    public ByteBuffer nameBuffer = null;


    public ByteBuffer fileSizeBuffer = ByteBuffer.allocate(Long.BYTES);

    public ByteBuffer fileBuffer = null;
    public FileChannel fileChannel = null;


    public ByteBuffer inputBuffer;

    private int nameSize;
    private String name;
    private long fileSize;


    public Integer getNameSize() {
        return nameSize;
    }

    public void setNameSize(Integer nameSize) {
        this.nameSize = nameSize;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
