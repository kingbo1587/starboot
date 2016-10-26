package com.wh.starboot.web;

import com.wh.starboot.bean.MailInfo;
import com.wh.starboot.bean.MailTask;
import com.wh.starboot.dao.MailDao;
import com.wh.starboot.service.crawler.impl.Mail163CaptchaCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

/**
 * Created by shenjinbo on 2016/10/25.
 */
@Controller
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private Mail163CaptchaCrawler mail163CaptchaCrawler;
    @Autowired
    private MailDao mailDao;

    @RequestMapping(value = "/testCrawler")
    @ResponseBody
    String testCrawler(@RequestBody MailTask mailTask) {
        logger.info("testCrawler enter");
        mailTask.setTaskid(UUID.randomUUID().toString());
        mailDao.saveTask(mailTask);
        Object loginData = mail163CaptchaCrawler.loginMail(mailTask);
        List<MailInfo> mailInfos = mail163CaptchaCrawler.crawlMails(mailTask, loginData);
        mailDao.batchInsertMailInfo(mailInfos);
        logger.info("testCrawler quit");
        return "1";
    }
}
