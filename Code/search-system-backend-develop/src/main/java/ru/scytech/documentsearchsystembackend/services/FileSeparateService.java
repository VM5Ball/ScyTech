package ru.scytech.documentsearchsystembackend.services;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.scytech.documentsearchsystembackend.model.PagedFile;
import ru.scytech.documentsearchsystembackend.model.filesystem.PageData;
import ru.scytech.documentsearchsystembackend.separators.DefaultSeparator;
import ru.scytech.documentsearchsystembackend.separators.FileSeparator;
import ru.scytech.documentsearchsystembackend.separators.PDFSeparator;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileSeparateService {
    public PagedFile separateFile(String filename, String domain, MediaType type, byte[] bytes) throws IOException {
        try (FileSeparator fileSeparator = createFileSeparator(type, bytes)) {
            List<byte[]> pageBytes = fileSeparator.getPagesBytes();
            List<PageData> pageDataList = new ArrayList<>();
            for (int i = 0; i < pageBytes.size(); i++) {
                PageData pageData = new PageData(
                        filename,
                        domain,
                        i + 1,
                        pageBytes.get(i),
                        type);
                pageDataList.add(pageData);
            }
            return new PagedFile(filename, domain, fileSeparator.getTitle(), pageDataList);
        }
    }

    public FileSeparator createFileSeparator(MediaType type, byte[] bytes) throws IOException {
        if (type.equals(MediaType.APPLICATION_PDF)) {
            return new PDFSeparator(bytes);
        } else {
            return new DefaultSeparator(bytes);
        }
    }

    public byte[] getTitleImage(String filename, String domain, MediaType type, byte[] bytes) throws IOException, OperationNotSupportedException {
        try (FileSeparator fileSeparator = createFileSeparator(type, bytes)) {
            return fileSeparator.getTitleImage();
        }
    }
}
