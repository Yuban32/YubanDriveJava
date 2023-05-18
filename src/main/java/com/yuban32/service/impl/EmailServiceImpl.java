package com.yuban32.service.impl;

import com.yuban32.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author Yuban32
 * @ClassName EmailServiceImpl
 * @Description
 * @Date 2023年05月19日
 */
@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MailProperties mailProperties;
    @Override
    public boolean sendSimpleMail(String to, String subject, String content) throws MessagingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
            //发送者
            helper.setFrom(mailProperties.getUsername());
            //接收人
            helper.setTo(to);
            //主题
            helper.setSubject(subject);
            //内容
            helper.setText(content,true);
            //发送
            mailSender.send(mimeMessage);
            return true;
        } catch (MessagingException e) {
            // 处理邮件发送异常
            e.printStackTrace();
            return false;
        }

    }
}
