package com.gym.controller;

import com.gym.config.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/* 文件上传控制器，处理图片等文件的上传并返回访问 URL */
@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private AuthHelper auth;

    // 获取上传目录路径（项目根目录下的 uploads 文件夹）
    private String getUploadDir() {
        return System.getProperty("user.dir") + "/uploads/";
    }

    // 文件上传接口：校验文件、安全化文件名后保存至 uploads 目录并返回可访问的相对 URL
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return auth.resp(400, "请选择文件");
        try {
            String uploadDirPath = getUploadDir();
            Path uploadDir = Paths.get(uploadDirPath);
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

            // 安全化文件名：去除路径成分，仅保留字母数字和常见符号，防止路径遍历
            String originalName = file.getOriginalFilename();
            String filename;
            if (originalName != null && !originalName.isBlank()) {
                String safe = Paths.get(originalName).getFileName().toString();
                safe = safe.replaceAll("[^a-zA-Z0-9._\\-]", "_");
                filename = safe.isEmpty() ? UUID.randomUUID() + ".jpg" : safe;
            } else {
                filename = UUID.randomUUID() + ".jpg";
            }
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath.toFile());

            // 返回相对 URL，部署到不同域名/Nginx 反代后仍然有效
            String url = "/uploads/" + filename;
            Map<String, Object> result = auth.ok();
            result.put("url", url);
            result.put("msg", "上传成功");
            return result;
        } catch (IOException e) {
            return auth.resp(500, "上传失败: " + e.getMessage());
        }
    }
}
