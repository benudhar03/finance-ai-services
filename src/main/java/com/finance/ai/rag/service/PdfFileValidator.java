package com.finance.ai.rag.service;

import com.finance.ai.exception.InvalidFileException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfFileValidator {

    private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F', '-'};
    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024; // 20 MB

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File exceeds maximum allowed size of 20MB.");
        }
        if (!hasPdfMagicBytes(file)) {
            throw new InvalidFileException("File is not a valid PDF.");
        }
    }

    private boolean hasPdfMagicBytes(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] header = in.readNBytes(PDF_MAGIC.length);
            for (int i = 0; i < PDF_MAGIC.length; i++) {
                if (header.length <= i || header[i] != PDF_MAGIC[i]) return false;
            }
            return true;
        } catch (IOException e) {
            throw new InvalidFileException("Could not read uploaded file.");
        }
    }
}