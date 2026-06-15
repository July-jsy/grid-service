package com.grid.controller;

import com.grid.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Path UPLOAD_DIR = Path.of("uploads");

    @PostMapping("/images")
    public Result<Map<String, String>> image(@RequestParam MultipartFile file) throws Exception {
        if (file.isEmpty() || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、WebP 图片");
        }
        Files.createDirectories(UPLOAD_DIR);
        var extension = switch (file.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        var filename = UUID.randomUUID() + extension;
        file.transferTo(UPLOAD_DIR.resolve(filename));
        return Result.ok(Map.of("url", "/uploads/" + filename));
    }
}
