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

    // 公共上传方法：保存文件到 uploads 目录并返回可访问的相对 URL
    private Map<String, Object> saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) return auth.resp(400, "请选择文件");
        String uploadDirPath = getUploadDir();
        Path uploadDir = Paths.get(uploadDirPath);
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
        // 始终使用 UUID 文件名防止同名覆盖和浏览器缓存，保留原始扩展名
        String ext = ".jpg";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID() + ext;
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());
        String url = "/uploads/" + filename;
        Map<String, Object> result = auth.ok();
        result.put("url", url);
        result.put("msg", "上传成功");
        return result;
    }

    // 需认证的通用文件上传接口
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            return saveFile(file);
        } catch (IOException e) {
            return auth.resp(500, "上传失败: " + e.getMessage());
        }
    }

    // 无需认证的头像上传接口（注册时使用，归属 /api/front/** permitAll 路径）
    @PostMapping("/front/upload-avatar")
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            return saveFile(file);
        } catch (IOException e) {
            return auth.resp(500, "上传失败: " + e.getMessage());
        }
    }
}
