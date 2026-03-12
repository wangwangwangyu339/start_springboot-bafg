package com.example.webframework.infrastructure.ocr;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.teaopenapi.models.Config;
import com.example.webframework.domain.STSCredentials;
import com.example.webframework.infrastructure.utils.EnvironmentUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author luoyu
 * @date 2024/5/14
 **/
@Component
public class OcrBeans {

    @Bean
    public Client ocrClient() throws Exception {
        STSCredentials credentials = EnvironmentUtils.getSTSCredentials();
        String regionId = EnvironmentUtils.getCurrentRegion();
        Config config = new Config()
                .setAccessKeyId(credentials.accessKeyId)
                .setAccessKeySecret(credentials.accessKeySecret)
                .setSecurityToken(credentials.securityToken)
                // https://help.aliyun.com/zh/ocr/developer-reference/api-ocr-api-2021-07-07-recognizebasic
                .setEndpoint("ocr-api." + regionId + ".aliyuncs.com");
        return new Client(config);
    }

}
