package com.example.webframework.domain;

/**
 * @author luoyu
 * @date 2024/5/14
 **/
public class OcrResult {

    public String content;

    public OcrResult(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "OcrResult{content='" + content + "'}";
    }

}
