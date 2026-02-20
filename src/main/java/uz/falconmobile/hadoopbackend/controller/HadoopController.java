package uz.falconmobile.hadoopbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.falconmobile.hadoopbackend.model.FileInfo;
import uz.falconmobile.hadoopbackend.model.JobResult;
import uz.falconmobile.hadoopbackend.service.HdfsService;
import uz.falconmobile.hadoopbackend.service.WordCountService;

import java.util.*;

@RestController
@RequestMapping("/api/hadoop")
@CrossOrigin(origins = "*")
public class HadoopController {

    @Autowired
    private HdfsService hdfsService;

    @Autowired
    private WordCountService wordCountService;

    // Health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Hadoop Backend ishlayapti");
        response.put("timestamp", new Date().toString());
        return ResponseEntity.ok(response);
    }

    // Fayl yuklash
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String hdfsPath = hdfsService.uploadFile(file, "/user/data");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fayl muvaffaqiyatli yuklandi");
            response.put("fileName", file.getOriginalFilename());
            response.put("hdfsPath", hdfsPath);
            response.put("size", file.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Fayllar ro'yxati
    @GetMapping("/files")
    public ResponseEntity<?> listFiles(
            @RequestParam(defaultValue = "/user/data") String path) {

        try {
            List<FileInfo> files = hdfsService.listFiles(path);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("path", path);
            response.put("count", files.size());
            response.put("files", files);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Fayl ma'lumotlari
    @GetMapping("/file/info")
    public ResponseEntity<?> getFileInfo(@RequestParam String path) {
        try {
            FileInfo fileInfo = hdfsService.getFileInfo(path);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileInfo", fileInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        }
    }

    // Faylni o'qish
    @GetMapping("/file/read")
    public ResponseEntity<?> readFile(@RequestParam String path) {
        try {
            String content = hdfsService.readFile(path);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("path", path);
            response.put("content", content);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        }
    }

    // Faylni o'chirish
    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(@RequestParam String path) {
        try {
            boolean deleted = hdfsService.deleteFile(path);

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Fayl o'chirildi" : "Faylni o'chirishda xatolik");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // WordCount ishini boshlash
    @PostMapping("/wordcount")
    public ResponseEntity<?> runWordCount(@RequestBody Map<String, String> request) {
        try {
            String inputPath = request.get("inputPath");
            String outputPath = "/user/output/wordcount-" + System.currentTimeMillis();

            JobResult result = wordCountService.runWordCount(inputPath, outputPath);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobResult", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
