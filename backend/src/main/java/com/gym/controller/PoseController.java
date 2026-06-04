package com.gym.controller;

import com.gym.service.PoseService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/* 体态分析控制器，接收上传图片并调用 Python 脚本进行 AI 姿态识别 */
@RestController
@RequestMapping("/api/pose")
public class PoseController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final PoseService poseService;

    public PoseController(PoseService poseService) {
        this.poseService = poseService;
    }

    // 体态分析接口：接收图片文件，委托 PoseService 调用 Python 分析脚本
    @PostMapping("/analyze")
    public Map<String, Object> analyze(@RequestParam("file") MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            return Map.of("code", 400, "msg", "图片不能超过 10MB");
        }
        return poseService.analyze(file);
    }
}
