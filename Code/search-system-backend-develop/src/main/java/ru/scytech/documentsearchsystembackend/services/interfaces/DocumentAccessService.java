package ru.scytech.documentsearchsystembackend.services.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DocumentAccessService {
    void saveFile(String fileName, String domain, byte[] bytes) throws IOException;

    byte[] getFile(String fileName, String domain) throws IOException;

    boolean isFileExist(String fileName, String domain);

    Optional<String> getFileContentType(String filename, String domain) throws IOException;

    boolean deleteFile(String filename, String domain);

    Map<String, Set<String>> loadDocsRepository(boolean systemFiles) throws IOException;
}
