package com.wh.starboot.service.crawler;

import com.wh.starboot.bean.MailTask;
import com.wh.starboot.bean.TaskStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by  on 2016/7/26.
 */
@Component
public class CrawlerHelper {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerHelper.class);

    @Value("${crawler.subject.exclude}")
    private String exclude;
    @Value("${crawler.subject.include}")
    private String include;

    private static List<String> excludes = new ArrayList<>();
    private static String includeExpr = ".*信用卡.*账单.*";

    @PostConstruct
    private void init() {
        String include = this.include;
        String exclude = this.exclude;
        logger.info("crawler match subject rule,include:{},exclude:{}", include, exclude);
        if (StringUtils.isNotBlank(exclude)) {
            String[] excludeArray = exclude.split(",");
            for (String item : excludeArray) {
                this.excludes.add(item.toUpperCase());
            }
        }
        if (StringUtils.isNotBlank(include)) {
            this.includeExpr = include;
        }
    }

    /**
     * 匹配邮件标题
     *
     * @param subject 标题
     * @return 是否匹配
     */
    public static boolean matchSubject(String subject) {
        if (StringUtils.isBlank(subject)) {
            return false;
        }
        // 排除转发文件
        String subjectUpper = subject.toUpperCase();
        for (String item : excludes) {
            if (subjectUpper.contains(item)) {
                return false;
            }
        }
        // 匹配符合条件的
        return subject.matches(includeExpr);
    }

    public static boolean equalStatus(MailTask mailTask, TaskStatus taskStatus) {
        return taskStatus.key().equals(mailTask.getStatus());
    }

    public static String wrapHtml(String part) {
        return String.format("<html><head><meta charset=\"utf-8\"></head><body>%s</body></html>", part);
    }

    public void main(String[] args) throws IOException {
        CrawlerHelper helper = new CrawlerHelper();
        helper.exclude = "转发,FW,RE";
        helper.include = ".*银行信用卡.*消费明细.*|.*广发卡.*账单.*|.*信用卡.*[账帐]单.*|.*信用管家消费提醒.*|.*中国农业银行金穗贷记卡电子对账单.*|.*招行信用卡.*信用额度调升通知.*|.*招商银行.*账单.*|.*招商银行信用卡-《YOUNG卡笔记本》.*";
        helper.init();
        File file = new File("D:/temp/knife/unhandled.txt");
        String dataStr = FileCopyUtils.copyToString(new FileReader(file));
        String[] dataArray = dataStr.split(",");
        for (String data : dataArray) {
            String subject = data.split("=")[0].toUpperCase();
            if (matchSubject(subject)) {

            }
        }
    }


}
