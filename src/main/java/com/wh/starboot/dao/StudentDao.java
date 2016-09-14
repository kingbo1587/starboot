package com.wh.starboot.dao;

import com.wh.starboot.model.StudentBean;

public interface StudentDao {

	int add(StudentBean bean);

	int modify(StudentBean bean);

	StudentBean get(String studentId);

	int delete(String studentId);

}
