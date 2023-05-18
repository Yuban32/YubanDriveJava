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
        System.out.println(
                """
                        __    __  _   _   _____       ___   __   _   _____   _____    _   _     _   _____ \s
                        \\ \\  / / | | | | |  _  \\     /   | |  \\ | | |  _  \\ |  _  \\  | | | |   / / | ____|\s
                         \\ \\/ /  | | | | | |_| |    / /| | |   \\| | | | | | | |_| |  | | | |  / /  | |__  \s
                          \\  /   | | | | |  _  {   / /-| | | |\\   | | | | | |  _  /  | | | | / /   |  __| \s
                          / /    | |_| | | |_| |  / /--| | | | \\  | | |_| | | | \\ \\  | | | |/ /    | |___ \s
                         /_/     \\_____/ |_____/ /_/   |_| |_|  \\_| |_____/ |_|  \\_\\ |_| |___/     |_____|\s
                         
                                                                             Developed by Yuban32
                                                                             Github Repository\s
                                                                                 Web     https://bit.ly/45aOec3
                                                                                 Java    https://bit.ly/3MB1OhD
                        """
        );
    }

}
