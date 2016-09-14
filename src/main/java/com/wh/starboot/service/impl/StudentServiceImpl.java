package com.wh.starboot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.wh.starboot.dao.StudentDao;
import com.wh.starboot.model.StudentBean;
import com.wh.starboot.service.StudentService;

@Service
public class StudentServiceImpl implements StudentService {

	@Autowired
	private StudentDao studentDao;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public StudentBean get(String studentId) {
		return studentDao.get(studentId);
	}

	@Override
	public int save(StudentBean bean) {
		int rows = studentDao.add(bean);
		mongoTemplate.save(bean);
		return rows;
	}

}
