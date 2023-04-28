package com.yuban32;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages =  {"com.yuban32.*"})
public class YubanDriveApplication {

    public static void main(String[] args) {
        SpringApplication.run(YubanDriveApplication.class, args);
        System.out.println("项目启动成功啦~");
    }

}
