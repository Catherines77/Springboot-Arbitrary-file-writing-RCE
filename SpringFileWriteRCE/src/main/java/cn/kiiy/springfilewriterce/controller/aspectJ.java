package cn.kiiy.springfilewriterce.controller;

import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
public class aspectJ {
    @PostMapping("/deserialize")
    public String deserialize(HttpServletRequest request) throws Exception {
        String raw = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        byte[] b = Base64.getDecoder().decode(raw.trim());

        // 2. 反序列化触发漏洞
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            ois.readObject();
        }
        return "Done";
    }
}