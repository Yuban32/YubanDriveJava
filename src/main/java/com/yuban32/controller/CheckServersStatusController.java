package com.yuban32.controller;

import com.yuban32.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yuban32
 * @ClassName CheckServersStatusController
 * @Description 检测后端状况
 * @Date 2023年05月21日
 */
@Slf4j
@RestController
@RequestMapping("/check-servers-status")
public class CheckServersStatusController {
    @GetMapping
    public void checkServersStatus(){
    }
}
