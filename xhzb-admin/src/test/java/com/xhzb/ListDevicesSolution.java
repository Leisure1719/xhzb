package com.xhzb;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.core.region.Region;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.iotda.v5.*;
import com.huaweicloud.sdk.iotda.v5.model.*;

public class ListDevicesSolution {

    // REGION_ID：如果是上海一，请填写"cn-east-3"；如果是北京四，请填写"cn-north-4";如果是华南广州，请填写"cn-south-1"
    private static final String REGION_ID = "cn-east-3";
    // ENDPOINT：请在控制台的"总览"界面的"平台接入地址"中查看“应用侧”的https接入地址。
    private static final String ENDPOINT = "31b1e6863a.st1.iotda-app.cn-east-3.myhuaweicloud.com";

    public static void main(String[] args) {
        // 认证用的ak和sk直接写到代码中有很大的安全风险，建议在配置文件或者环境变量中密文存放，使用时解密，确保安全；
        // 本示例以ak和sk保存在环境变量中为例，运行本示例前请先在本地环境中设置环境变量HUAWEICLOUD_SDK_AK和HUAWEICLOUD_SDK_SK。
        String ak = "HPUATKE9RMHA85MCOZM7";
        String sk = "g1zmbcAeXQxJ5pFbMsmAZc0kH6NTQWjXUFUuiQQS";
        String projectId = "801a3d22cdc34c09841fbe711b26db07";

        // 创建认证
        ICredential auth = new BasicCredentials()
               .withAk(ak)
               .withSk(sk)
               // 标准版/企业版需要使用衍生算法，基础版请删除配置"withDerivedPredicate"
               .withDerivedPredicate(BasicCredentials.DEFAULT_DERIVED_PREDICATE)
               .withProjectId(projectId);

        // 创建IoTDAClient实例并初始化
        IoTDAClient client = IoTDAClient.newBuilder()
                .withCredential(auth)
                // 标准版/企业版：需自行创建Region对象，基础版：请使用IoTDARegion的region对象，如"withRegion(IoTDARegion.CN_NORTH_4)"
                .withRegion(new Region(REGION_ID, ENDPOINT))
               // .withRegion(IoTDARegion.CN_NORTH_4)
                // 配置是否忽略SSL证书校验， 默认不忽略
                // .withHttpConfig(new HttpConfig().withIgnoreSSLVerification(true))
                .build();

        // 实例化请求对象
        ListDevicesRequest request = new ListDevicesRequest();
        try {
            // 调用查询设备列表接口
            ListDevicesResponse response = client.listDevices(request);
            System.out.println(response.toString());
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (RequestTimeoutException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            System.out.println(e.getHttpStatusCode());
            System.out.println(e.getErrorCode());
            System.out.println(e.getErrorMsg());
        }
    }
}