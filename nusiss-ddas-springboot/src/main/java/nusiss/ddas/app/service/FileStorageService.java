package nusiss.ddas.app.service;

import nusiss.ddas.app.exception.InvalidFileException;
import nusiss.ddas.app.util.FileHashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Map<String, Set<String>> ALLOWED_CONTENT_TYPES_BY_EXT = Map.of(
        "pdf",  Set.of("application/pdf"),
        "doc",  Set.of("application/msword"),
        "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        "jpg",  Set.of("image/jpeg"),
        "jpeg", Set.of("image/jpeg"),
        "png",  Set.of("image/png")
    );

    @Value("${app.storage.upload-dir}")
    private String uploadDir;

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Please choose a file to upload.");
        }

        String originalFilename = StringUtils.cleanPath(
            file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = extractExtension(originalFilename);

        Set<String> allowedContentTypes = ALLOWED_CONTENT_TYPES_BY_EXT.get(extension);
        if (allowedContentTypes == null) {
            throw new InvalidFileException(
                "Unsupported file type. Only PDF, DOC, DOCX, JPG and PNG files are allowed.");
        }

        String declaredContentType = file.getContentType();
        if (declaredContentType == null
                || !allowedContentTypes.contains(declaredContentType.toLowerCase())) {
            throw new InvalidFileException(
                "The file's declared type does not match its extension (." + extension
                + "). Only PDF, DOC, DOCX, JPG and PNG files are allowed.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidFileException("Could not read the uploaded file. Please try again.");
        }

        if (!matchesMagicBytes(extension, bytes)) {
            throw new InvalidFileException(
                "The file's content does not match its extension. Only genuine PDF, DOC, DOCX, "
                + "JPG and PNG files are allowed.");
        }

        try {
            Path dir = Path.of(uploadDir);
            Files.createDirectories(dir);
            String storedFilename = UUID.randomUUID() + "." + extension;
            Path destination = dir.resolve(storedFilename);
            Files.write(destination, bytes);

            String fileHash = FileHashUtil.sha256Hex(bytes);
            return new StoredFile(destination.toString(), fileHash, bytes.length, declaredContentType);
        } catch (IOException e) {
            throw new InvalidFileException("Could not save the uploaded file. Please try again.");
        }
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new InvalidFileException("The file must have a valid extension.");
        }
        return filename.substring(dot + 1).toLowerCase();
    }

    private boolean matchesMagicBytes(String extension, byte[] bytes) {
        return switch (extension) {
            case "pdf"  -> startsWith(bytes, 0x25, 0x50, 0x44, 0x46);
            case "png"  -> startsWith(bytes, 0x89, 0x50, 0x4E, 0x47);
            case "jpg", "jpeg" -> startsWith(bytes, 0xFF, 0xD8, 0xFF);
            case "docx" -> startsWith(bytes, 0x50, 0x4B, 0x03, 0x04);
            case "doc"  -> startsWith(bytes, 0xD0, 0xCF, 0x11, 0xE0);
            default -> false;
        };
    }

    private boolean startsWith(byte[] data, int... signature) {
        if (data.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if ((data[i] & 0xFF) != signature[i]) return false;
        }
        return true;
    }

    public record StoredFile(String filePath, String fileHash, long size, String contentType) {}
}
