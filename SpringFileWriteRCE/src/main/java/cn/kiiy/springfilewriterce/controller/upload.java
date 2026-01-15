package cn.kiiy.springfilewriterce.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class upload {
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 漏洞点：直接使用了用户可控的 filename，未过滤 ../
        String fileName = file.getOriginalFilename();
        File dest = new File("D:/idea/project/SpringFileWriteRCE/uploads/" + fileName);
        if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
        file.transferTo(dest);
        return "Upload Success to: " + dest.getAbsolutePath();
    }
}