package com.xhzb;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class HttpTest {


    @Test
    public void testGet() {
        String result = HttpUtil.get("https://www.baidu.com");
        System.out.println(result);
    }

    @Test
    public void testGetParam() {
        // 访问地址
        String url = "http://localhost:8080/nursing/project/list";
        //参数构建
        Map<String, Object> param = new HashMap<>();
        param.put("pageNum", 1);
        param.put("pageSize", 10);


        //分页查询护理项目
        String result = HttpUtil.get(url, param);
        System.out.println(result);

    }

    @Test
    public void testGetByRequest() {
        // 访问地址
        String url = "http://localhost:8080/nursing/project/list";
        //参数构建
        Map<String, Object> param = new HashMap<>();
        param.put("pageNum", 1);
        param.put("pageSize", 10);


        //分页查询护理项目
        HttpResponse response = HttpUtil.createRequest(Method.GET, url)
                .header("authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImxvZ2luX3VzZXJfa2V5IjoiMzcwOWM5OTMtNDhjMS00OTVhLWFkNzgtNDc5MWY4MTFjOTBiIn0.DYxKbIifO5duvnqaUPgzUMQcRXDjSfHSUBqDT5kH5Pk8UxdJxnjE6jWFmAZvgsPqx0JHmNqjtSuHplkZaUpvvA")
                .form(param).execute();
        if (response.isOk()) {
            System.out.println(response.body());
        }
    }


    @Test
    public void testPost() {
        String url = "http://localhost:8080/nursing/project";
        HashMap map = new HashMap();
        map.put("name", "至尊SPA");
        map.put("orderNo", 1);
        map.put("unit", "1次");
        map.put("price", 888888);
        map.put("image", "https://tse4-mm.cn.bing.net/th/id/OIP-C.E6UrxPS0mD2-iulNSnFxEAHaEK?w=186&h=104&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3");
        map.put("nursingRequirement", "蚌埠住了！家人们谁懂啊，迪哥这波操作真是典中典！花888888个大不溜就为了洗个脚，这什么含金量啊？这波啊，这波是直接把洗脚城干成‘迪哥私人疗养院’了！\n" +
                "\n" +
                "老板收钱时手都在抖，连夜给迪哥立了个长生牌位。按洗个脚均价188来算，迪哥这888888够把全城的技师轮番点个几十遍，脚底板都给搓成阿房宫了！估计迪哥往那一坐，整个洗脚城都得拉横幅：‘热烈庆祝迪总消费突破88万大关");
        HttpResponse authorization = HttpUtil.createPost(url)
                .body(JSONUtil.toJsonStr(map)).header("authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImxvZ2luX3VzZXJfa2V5IjoiZjAxN2ZhN2MtODVhOC00NmIwLWIwOWYtMmY0OTcwNWNmMDU5In0.dJXOfESs9U96tBgqmZ1QhIR6OFWYghSJNrFmEyvY6bLRWOwkmLV-2gqKNitBkMTvYRaqSwb-ejYJD0YB5e56hA")
                .execute();
        String body = authorization.body();
        System.out.println(body);
    }


}