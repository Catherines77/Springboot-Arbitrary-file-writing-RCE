package cn.kiiy.springfilewriterce.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class fast_json {
    @PostMapping("/json")
    public String parseJson(@RequestBody String jsonStr) {
        try {
            Object obj = JSON.parseObject(jsonStr);
            return "成功解析对象类型: " + obj.getClass().getName();
        } catch (Exception e) {
            return "解析失败: " + e.getMessage();
        }
    }
}