package ru.scytech.documentsearchsystembackend.separators;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PDFSeparator implements FileSeparator {
    private PDDocument pdDocument;
    private PDDocumentInformation pdDocumentInformation;

    public PDFSeparator(byte[] bytes) throws IOException {
        pdDocument = PDDocument.load(bytes);
        pdDocumentInformation = pdDocument.getDocumentInformation();

    }

    public List<byte[]> getPagesBytes() throws IOException {
        List<byte[]> list = new ArrayList<>();
        int pageCount = pdDocument.getPages().getCount();
        PDFTextStripper stripper = new PDFTextStripper();
        for (int i = 1; i <= pageCount; i++) {
            stripper.setStartPage(i);
            stripper.setEndPage(i);
            byte[] bytes = stripper.getText(pdDocument).getBytes(StandardCharsets.UTF_8);
            list.add(bytes);
        }
        return list;
    }

    public String getTitle() {
        return Optional.ofNullable(pdDocumentInformation.getTitle()).orElseGet(() -> "");
    }

    public List<String> getKeywords() {
        return Arrays.stream(pdDocumentInformation.getKeywords().split(" ")).collect(Collectors.toList());
    }

    public byte[] getTitleImage() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
            BufferedImage bim = pdfRenderer.renderImage(0);
            ImageIO.write(bim, "JPEG", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public void close() throws IOException {
        pdDocument.close();
    }
}
