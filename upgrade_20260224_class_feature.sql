SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `qb_class` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `class_name` varchar(128) NOT NULL,
  `class_code` varchar(16) NOT NULL,
  `class_desc` text NULL,
  `teacher_id` bigint UNSIGNED NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qb_class_code` (`class_code`),
  KEY `idx_qb_class_teacher` (`teacher_id`),
  CONSTRAINT `fk_qb_class_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `qb_class_member` (
  `class_id` bigint UNSIGNED NOT NULL,
  `student_id` bigint UNSIGNED NOT NULL,
  `joined_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`class_id`, `student_id`),
  KEY `idx_qb_class_member_student` (`student_id`),
  CONSTRAINT `fk_qb_class_member_class` FOREIGN KEY (`class_id`) REFERENCES `qb_class` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_qb_class_member_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `qb_assignment_target_class` (
  `assignment_id` bigint UNSIGNED NOT NULL,
  `class_id` bigint UNSIGNED NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`assignment_id`, `class_id`),
  KEY `idx_qb_atc_class` (`class_id`),
  CONSTRAINT `fk_qb_atc_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `qb_assignment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_qb_atc_class` FOREIGN KEY (`class_id`) REFERENCES `qb_class` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
