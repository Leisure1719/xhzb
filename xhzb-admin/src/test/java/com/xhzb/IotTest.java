package com.xhzb;

import com.huaweicloud.sdk.iotda.v5.IoTDAClient;
import com.huaweicloud.sdk.iotda.v5.model.ListDevicesRequest;
import com.huaweicloud.sdk.iotda.v5.model.ListDevicesResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IotTest {

    @Autowired
    private IoTDAClient ioTDAClient;
    @Test
    void test(){
        // 实例化请求对象
        ListDevicesRequest request = new ListDevicesRequest();
        try {
            // 调用查询设备列表接口
            ListDevicesResponse response = ioTDAClient.listDevices(request);
            System.out.println(response.toString());
        }catch (Exception e){

        }
    }
}
