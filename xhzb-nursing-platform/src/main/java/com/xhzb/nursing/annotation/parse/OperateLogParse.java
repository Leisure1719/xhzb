package com.xhzb.nursing.annotation.parse;

import com.xhzb.nursing.annotation.OperateLog;

import java.lang.reflect.Method;

public class OperateLogParse {

    public static void parseOperateLog(Class<?> clazz) {
        // 1. 获取类中所有方法
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            OperateLog logAnno = method.getAnnotation(OperateLog.class);
            if(logAnno != null){
                System.out.println("=====================");
                System.out.println("方法名：" + method.getName());
                // 4. 读取注解内自定义参数
                System.out.println("操作描述：" + logAnno.value());
                System.out.println("所属模块：" + logAnno.module());
                System.out.println("是否记录参数：" + logAnno.recordParam());
            }else {
                System.out.println("方法【" + method.getName() + "】无@OperateLog注解");
            }
        }

    }
}
