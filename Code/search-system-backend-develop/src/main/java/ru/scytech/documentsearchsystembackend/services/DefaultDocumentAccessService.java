package ru.scytech.documentsearchsystembackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.scytech.documentsearchsystembackend.services.interfaces.DocumentAccessService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DefaultDocumentAccessService implements DocumentAccessService {
    private String rootDir;

    private String buildPath(String... point) {
        return Arrays.stream(point).reduce((a, b) -> a + "/" + b).get();
    }

    public DefaultDocumentAccessService(@Value("${application.filesystem.root}") String rootDir)
            throws IOException {
        Path path = Paths.get(rootDir);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        this.rootDir = rootDir;
    }

    @Override
    public void saveFile(String fileName, String domain, byte[] bytes) throws IOException {
        createDirectoryIfNotExists(buildPath(rootDir, domain));
        try (FileOutputStream fileOutputStream = new FileOutputStream(buildPath(rootDir, domain, fileName))) {
            fileOutputStream.write(bytes);
        }
    }

    @Override
    public byte[] getFile(String fileName, String domain) throws IOException {
        try (var fileInputStream = new FileInputStream(buildPath(rootDir, domain, fileName))) {
            return fileInputStream.readAllBytes();
        }
    }

    @Override
    public boolean isFileExist(String fileName, String domain) {
        Path path = Paths.get(buildPath(rootDir, domain, fileName));
        return Files.exists(path);
    }

    @Override
    public Optional<String> getFileContentType(String filename, String domain) throws IOException {
        createDirectoryIfNotExists(buildPath(rootDir, domain));
        Path path = Paths.get(buildPath(rootDir, domain, filename));
        return Optional.ofNullable(Files.probeContentType(path));
    }

    private void createDirectoryIfNotExists(String pathString) throws IOException {
        Path path = Paths.get(pathString);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
    }

    public boolean deleteFile(String filename, String domain) {
        File file = new File(buildPath(rootDir, domain, filename));
        return file.delete();
    }

    @Override
    public Map<String, Set<String>> loadDocsRepository(boolean systemFiles) throws IOException {
        return Files.list(Paths.get(rootDir))
                .filter(Files::isDirectory)
                .filter(it -> !it.getFileName().toString().equals(rootDir))
                .collect(Collectors.toMap(it -> it.getFileName().toString(),
                        it -> {
                            try {
                                return Files.list(it)
                                        .filter(Files::isRegularFile)
                                        .filter(file -> systemFiles || !file.getFileName().toString().startsWith("."))
                                        .map(file -> file.getFileName().toString())
                                        .collect(Collectors.toSet());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }));
    }
}
