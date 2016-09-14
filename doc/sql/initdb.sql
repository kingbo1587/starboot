CREATE TABLE `student` (
  `student_id` int(11) NOT NULL,
  `name` varchar(32) NOT NULL,
  `age` int(11) DEFAULT NULL,
  `grade` int(11) DEFAULT NULL,
  PRIMARY KEY (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
