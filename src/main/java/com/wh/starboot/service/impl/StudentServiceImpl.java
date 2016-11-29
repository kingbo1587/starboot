package com.wh.starboot.service.impl;

import com.wh.starboot.config.annotation.PrintHa;
import com.wh.starboot.dao.StudentDao;
import com.wh.starboot.model.QueueMessage;
import com.wh.starboot.model.StudentBean;
import com.wh.starboot.service.StudentService;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Autowired
    private StudentDao studentDao;
    @Autowired
    private HttpClient httpClient;
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${mq.exchange.test}")
    private String mqExchangeTest;
    @Value("${mq.key.test}")
    private String mqKeyTest;

    @Override
    public StudentBean get(String studentId) {
        BoundValueOperations<String, Integer> valueOpers = redisTemplate.boundValueOps(studentId);
        Integer count = valueOpers.get();
        if (count == null) {
            count = 0;
        }
        count++;
        logger.info("get|count|count:{}", count);
        valueOpers.set(count);
        return studentDao.get(studentId);
    }

    @Override
    public int save(StudentBean bean) {
        int rows = studentDao.add(bean);
        // mongoTemplate.save(bean);
        return rows;
    }

    @Override
    public void getBaidu() {
        String url = "http://www.baidu.com";
        HttpGet httpGet = new HttpGet();
        httpGet.setURI(URI.create(url));
        try {
            httpClient.execute(httpGet);
        } catch (Exception e) {
            logger.error("getBaidu|excetpion", e);
        }
    }

    @Override
    public void sendMq(String message) {
        logger.debug("sendMq|message:{}", message);
        try {
            final QueueMessage queueMessage = new QueueMessage(message);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    rabbitTemplate.send(mqExchangeTest, mqKeyTest, queueMessage.toAmqpMessage());
                }
            }).start();
        } catch (Exception e) {
            logger.error("sendMq|exception", e);
            throw e;
        }
    }

     @PrintHa("lalallalaallfjlfdasjfljalf!!!!!!!!!!!")
    private String haha;

    @Override
    public void sayHi(String str) {
        logger.info("sayHi:{}", haha);
    }

}
