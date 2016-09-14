package com.wh.starboot.dao.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.wh.starboot.dao.StudentDao;
import com.wh.starboot.dao.mapper.StudentMapper;
import com.wh.starboot.model.StudentBean;
import com.wh.starboot.model.database.Student;
import com.wh.starboot.util.MyUtil;

@Repository
public class StudentDaoImpl implements StudentDao{
	
	@Autowired
	private StudentMapper studentMapper;
	
	@Override
	public int add(StudentBean bean){
		String studentId = MyUtil.generateUuid();
		bean.setStudentId(studentId);
		Student record = new Student();
		BeanUtils.copyProperties(bean, record);
		record.setStudentId(studentId);
		return studentMapper.insert(record);
	}
	
	@Override
	public int modify(StudentBean bean){
		Student record = new Student();
		BeanUtils.copyProperties(bean, record);
		return studentMapper.updateByPrimaryKeySelective(record);
	}
	
	@Override
	public StudentBean get(String studentId){
		StudentBean bean = null;
		Student record = studentMapper.selectByPrimaryKey(studentId);
		if(record != null){
			bean = new StudentBean();
			BeanUtils.copyProperties(record, bean);
		}
		return bean;
	}
	
	@Override
	public int delete(String studentId){
		return studentMapper.deleteByPrimaryKey(studentId);
	}
	
}
