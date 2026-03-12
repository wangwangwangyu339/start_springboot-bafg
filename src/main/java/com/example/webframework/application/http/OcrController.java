package com.example.webframework.application.http;

import com.example.webframework.application.service.OcrService;
import com.example.webframework.domain.OcrResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

/**
 * @author luoyu
 * @date 2024/5/14
 **/
@RestController
@RequestMapping("/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @PostMapping("/recognizeBasic")
    public OcrResult recognizeBasic(@RequestParam("url") String url) throws Exception {
        validateUrl(url);
        String content = ocrService.recognizeBasic(url);
        return new OcrResult(content);
    }

    @PostMapping("/recognizeBasicFile")
    public OcrResult recognizeBasicFile(@RequestParam("file") MultipartFile file) throws Exception {
        String content = ocrService.recognizeBasicByStream(file.getInputStream());
        return new OcrResult(content);
    }

    private void validateUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL scheme: only http and https are allowed");
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL format");
        }
    }

}
