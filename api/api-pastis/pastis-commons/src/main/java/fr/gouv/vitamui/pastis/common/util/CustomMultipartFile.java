package fr.gouv.vitamui.pastis.common.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomMultipartFile implements MultipartFile {
    private final byte[] fileByte;

    public CustomMultipartFile(byte[] fileByte) {
        this.fileByte = fileByte;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getOriginalFilename() {
        return "pa.rng";
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return this.fileByte == null || this.fileByte.length == 0;
    }

    @Override
    public long getSize() {
        return this.fileByte.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return this.fileByte;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.fileByte);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        new FileOutputStream(dest).write(this.fileByte);
    }
}
