package com.wh.starboot.service;

import com.wh.starboot.model.StudentBean;

public interface StudentService {

	StudentBean get(String studentId);

	int save(StudentBean bean);

	void getBaidu();

	void sendMq(String message);

}
