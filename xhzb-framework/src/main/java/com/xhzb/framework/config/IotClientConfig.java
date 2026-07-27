package com.xhzb.framework.config;


import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.region.Region;
import com.huaweicloud.sdk.iotda.v5.IoTDAClient;
import com.xhzb.framework.config.properties.HuaWeiIotConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IotClientConfig {

    @Bean
    public IoTDAClient ioTDAClient(HuaWeiIotConfigProperties config){
        // 创建认证
        ICredential auth = new BasicCredentials()
                .withAk(config.getAk())
                .withSk(config.getSk())
                // 标准版/企业版需要使用衍生算法，基础版请删除配置"withDerivedPredicate"
                .withDerivedPredicate(BasicCredentials.DEFAULT_DERIVED_PREDICATE)
                .withProjectId(config.getProjectId());

        // 创建IoTDAClient实例并初始化
        IoTDAClient client = IoTDAClient.newBuilder()
                .withCredential(auth)
                // 标准版/企业版：需自行创建Region对象，基础版：请使用IoTDARegion的region对象，如"withRegion(IoTDARegion.CN_NORTH_4)"
                .withRegion(new Region(config.getRegionId(), config.getEndpoint()))
                // .withRegion(IoTDARegion.CN_NORTH_4)
                // 配置是否忽略SSL证书校验， 默认不忽略
                // .withHttpConfig(new HttpConfig().withIgnoreSSLVerification(true))
                .build();
        return client;
    }
}
