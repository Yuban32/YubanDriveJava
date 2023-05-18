package com.yuban32.service;

import javax.mail.MessagingException;

/**
 * @author: Yuban32
 * @Date: 2023年05月19日
 */

public interface EmailService {
    boolean sendSimpleMail(String to, String subject,String content) throws MessagingException;
}
