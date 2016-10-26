package com.wh.starboot.dao.impl;

import com.wh.starboot.bean.MailInfo;
import com.wh.starboot.bean.MailTask;
import com.wh.starboot.bean.TaskStatus;
import com.wh.starboot.dao.MailDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

;

/**
 * Created by  on 2016/7/16.
 */
@Repository
public class MailDaoImpl implements MailDao {

    private static final Logger logger = LoggerFactory.getLogger(MailDaoImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveTask(MailTask mailTask) {
        logger.debug("MailDaoImpl|saveTask : {}", mailTask.toString());
        mongoTemplate.save(mailTask);
    }

    @Override
    public MailTask getTask(String taskid) {
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("taskid").is(taskid));
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, MailTask.class);
    }

    @Override
    public void updateTask(MailTask mailTask, TaskStatus taskStatus) {
        logger.info("updateTask|Enter the method|mailTask:{},taskStatus:{}", mailTask, taskStatus);
        String status = taskStatus.key();
        mailTask.setStatus(status);
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("taskid").is(mailTask.getTaskid()));
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("status", status);
        try {
            mongoTemplate.findAndModify(query, update, MailTask.class);
        } catch (Exception e) {
            logger.error("updateTask|mongoTemplate.findAndModify() in MongoDaoImpl fail", e);
        }
        logger.info("updateTask|Quit the method.");
    }

    @Override
    public void updateTaskCrawlTime(MailTask mailTask) {
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("taskid").is(mailTask.getTaskid()));
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("crawlTime", mailTask.getCrawlTime());
        try {
            mongoTemplate.updateFirst(query, update, MailTask.class);
        } catch (Exception e) {
            logger.error("updateTaskCrawlTime|fail|mailTask:{}", mailTask, e);
        }
    }

    @Override
    public void updateTaskRecordAuthId(MailTask mailTask) {
        String recordAuthId = mailTask.getRecordAuthId();
        logger.info("updateTaskRecordAuthId|taskid:{},userid:{},recordAuthId:{}", mailTask.getTaskid(), mailTask.getUserid(), recordAuthId);
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("taskid").is(mailTask.getTaskid()));
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("recordAuthId", recordAuthId);
        try {
            mongoTemplate.updateFirst(query, update, MailTask.class);
        } catch (Exception e) {
            logger.error("updateTaskCrawlTime|fail|mailTask:{}", mailTask, e);
        }
    }

    @Override
    public void updateTask(MailTask mailTask, String captchaImage, String loginParam, TaskStatus taskStatus) {
        String taskid = mailTask.getTaskid();
        String status = taskStatus.key();
        logger.info("updateTask|taskid:{},userid:{},status:{}", taskid, mailTask.getUserid(), status);
        mailTask.setCaptchaImage(captchaImage);
        mailTask.setLoginParam(loginParam);
        mailTask.setStatus(status);
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("taskid").is(taskid));
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("captchaImage", captchaImage);
        update.set("loginParam", loginParam);
        update.set("status", status);
        try {
            mongoTemplate.updateFirst(query, update, MailTask.class);
        } catch (Exception e) {
            logger.error("updateTask|fail|mailTask:{}", mailTask, e);
        }
    }

    @Override
    public void updateTaskProvider(MailTask mailTask, String provider) {
        mailTask.setProvider(provider);
        logger.info("updateTaskProvider|taskid:{},userid:{},provider:{}", mailTask.getTaskid(), mailTask.getUserid(), provider);
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("taskid").is(mailTask.getTaskid()));
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("provider", provider);
        try {
            mongoTemplate.updateFirst(query, update, MailTask.class);
        } catch (Exception e) {
            logger.error("updateTaskProvider|fail|mailTask:{}", mailTask, e);
        }
    }

    @Override
    public long getLastCrawlTime(MailTask mailTask) {
        long lastCrawlTime = 0L;
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("userid").is(mailTask.getUserid()), Criteria.where("account").is(mailTask.getAccount()));
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "crawlTime"));
        try {
            MailTask last = mongoTemplate.findOne(query, MailTask.class);
            if (last != null) {
                lastCrawlTime = last.getCrawlTime();
            }
        } catch (Exception e) {
            logger.error("getLastCrawlTime|fail|mailTask:{}", mailTask, e);
        }
        return lastCrawlTime;
    }

    @Override
    public void batchInsertMailInfo(List<MailInfo> list) {
        mongoTemplate.insertAll(list);
    }

    @Override
    public List<MailInfo> getMailInfos(String taskid) {
        Query query = new Query(new Criteria().where("taskid").is(taskid));
        return mongoTemplate.find(query, MailInfo.class);
    }

    @Override
    public void updateTaskCrawlData(MailTask mailTask, String crawlData) {
        mailTask.setCrawlData(crawlData);
        logger.info("updateTaskCrawlData|taskid:{},userid:{}", mailTask.getTaskid(), mailTask.getUserid());
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("taskid").is(mailTask.getTaskid()));
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("crawlData", crawlData);
        try {
            mongoTemplate.updateFirst(query, update, MailTask.class);
        } catch (Exception e) {
            logger.error("updateTaskCrawlData|fail|mailTask:{}", mailTask, e);
        }
    }

}
