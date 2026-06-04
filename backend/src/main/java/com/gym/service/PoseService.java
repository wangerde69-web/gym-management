package com.gym.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/* 体态分析服务：调用 Python YOLOv8-pose 脚本进行人体姿态检测，可选接入 LLM 增强分析 */
@Service
public class PoseService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.api-url:http://localhost:1234/v1}")
    private String llmApiUrl;

    @Value("${llm.model:auto}")
    private String llmModel;

    @Value("${llm.enabled:true}")
    private boolean llmEnabled;

    // 体态分析主方法：校验文件、保存临时文件、调用 Python 脚本、解析返回结果
    public Map<String, Object> analyze(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("msg", "文件为空");
            return result;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            result.put("code", 400);
            result.put("msg", "仅支持图片格式");
            return result;
        }

        Path tmpFile = null;
        try {
            // 保存上传文件到临时目录
            String ext = ".jpg";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            tmpFile = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "pose_", ext);
            file.transferTo(tmpFile.toFile());

            // 定位 Python 脚本目录
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            Path pythonDir = projectDir.resolve("python");
            if (!Files.exists(pythonDir)) {
                pythonDir = projectDir.resolve("../python").normalize();
            }

            // 构建 Python 命令
            List<String> cmd = new ArrayList<>();
            cmd.add("python");
            cmd.add(pythonDir.resolve("pose_analyzer.py").toString());
            cmd.add(tmpFile.toAbsolutePath().toString());
            if (llmEnabled && llmApiUrl != null && !llmApiUrl.isBlank()) {
                cmd.add("--llm-url"); cmd.add(llmApiUrl);
                cmd.add("--llm-model"); cmd.add(llmModel);
            }

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(pythonDir.toFile());
            pb.redirectErrorStream(false);
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            Process process = pb.start();

            // 读取 stdout
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) output.append(line);
            }

            // 读取 stderr
            StringBuilder errOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) errOutput.append(line);
            }

            // 等待进程结束，超时 90 秒
            boolean finished = process.waitFor(90, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                result.put("code", 500);
                result.put("msg", "分析超时，请重试");
                return result;
            }

            if (process.exitValue() != 0) {
                result.put("code", 500);
                String msg = output.length() > 0 ? output.toString() : errOutput.toString();
                result.put("msg", "分析失败: " + msg);
                return result;
            }

            // 解析 Python 返回的 JSON
            Map<String, Object> pythonResult = objectMapper.readValue(output.toString(), Map.class);
            if (pythonResult.containsKey("error")) {
                result.put("code", 500);
                result.put("msg", pythonResult.get("error"));
                return result;
            }

            result.put("code", 200);
            result.put("data", pythonResult);
            result.put("msg", "分析完成");

        } catch (IOException e) {
            result.put("code", 500);
            result.put("msg", "文件处理失败: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("code", 500);
            result.put("msg", "分析被中断");
        } finally {
            if (tmpFile != null) {
                try { Files.deleteIfExists(tmpFile); } catch (IOException ignored) {}
            }
        }

        return result;
    }
}
