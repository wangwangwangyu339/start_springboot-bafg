package com.example.webframework.application.service;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeBasicRequest;
import com.aliyun.ocr_api20210707.models.RecognizeBasicResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * @author luoyu
 * @date 2024/5/14
 **/
@Component
public class OcrService {

    @Autowired
    private Client ocrClient;

    public String recognizeBasic(String imageUrl) throws Exception {
        RecognizeBasicRequest request = new RecognizeBasicRequest().setUrl(imageUrl);
        RecognizeBasicResponse response = ocrClient.recognizeBasicWithOptions(request, new RuntimeOptions());
        return response.getBody().getData();
    }

    public String recognizeBasicByStream(InputStream imageStream) throws Exception {
        RecognizeBasicRequest request = new RecognizeBasicRequest().setBody(imageStream);
        RecognizeBasicResponse response = ocrClient.recognizeBasicWithOptions(request, new RuntimeOptions());
        return response.getBody().getData();
    }

}
