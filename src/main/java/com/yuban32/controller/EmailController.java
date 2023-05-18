package com.yuban32.controller;

import com.yuban32.response.Result;
import com.yuban32.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

/**
 * @author Yuban32
 * @ClassName EmailController
 * @Description 邮件控制类 此功能暂时废弃
 * @Date 2023年05月19日
 */
@Slf4j
//@RestController
//@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;
    @RequiresAuthentication
//    @PostMapping("/send")
    public Result sendEmail() throws MessagingException {
        boolean simpleMail = emailService.sendSimpleMail("965963765@qq.com", "测试邮件", "测试邮件");
        if(simpleMail){
            return Result.success("邮件发送成功,请注意查收",null);
        }else {
            return Result.error("邮件发送失败,请检查邮件地址是否正确",null);
        }
    }
}
