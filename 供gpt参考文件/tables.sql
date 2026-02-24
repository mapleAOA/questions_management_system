/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : question_bank

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 30/01/2026 01:42:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for qb_answer
-- ----------------------------
DROP TABLE IF EXISTS `qb_answer`;
CREATE TABLE `qb_answer`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `attempt_id` bigint UNSIGNED NOT NULL,
  `attempt_question_id` bigint UNSIGNED NOT NULL,
  `question_id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `answer_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `answer_format` tinyint NOT NULL DEFAULT 1,
  `answer_status` tinyint NOT NULL DEFAULT 1 COMMENT '1=draft,2=submitted',
  `auto_score` int NOT NULL DEFAULT 0,
  `final_score` int NOT NULL DEFAULT 0,
  `is_correct` tinyint NOT NULL DEFAULT 0,
  `answered_at` datetime(3) NULL DEFAULT NULL,
  `graded_at` datetime(3) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_answer_attempt_question`(`attempt_question_id`) USING BTREE,
  INDEX `idx_answer_attempt_question`(`attempt_id`, `question_id`) USING BTREE,
  INDEX `fk_answer_question`(`question_id`) USING BTREE,
  INDEX `fk_answer_user`(`user_id`) USING BTREE,
  CONSTRAINT `fk_answer_attempt` FOREIGN KEY (`attempt_id`) REFERENCES `qb_attempt` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_answer_attempt_question` FOREIGN KEY (`attempt_question_id`) REFERENCES `qb_attempt_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `qb_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_answer_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_answer
-- ----------------------------

-- ----------------------------
-- Table structure for qb_appeal
-- ----------------------------
DROP TABLE IF EXISTS `qb_appeal`;
CREATE TABLE `qb_appeal`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `answer_id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `reason_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `appeal_status` tinyint NOT NULL DEFAULT 1 COMMENT '1=pending,2=approved,3=rejected,4=resolved',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `handled_by` bigint UNSIGNED NULL DEFAULT NULL,
  `handled_at` datetime(3) NULL DEFAULT NULL,
  `decision_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `final_score` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_appeal_answer`(`answer_id`) USING BTREE,
  INDEX `idx_appeal_user`(`user_id`) USING BTREE,
  INDEX `fk_appeal_handler`(`handled_by`) USING BTREE,
  CONSTRAINT `fk_appeal_answer` FOREIGN KEY (`answer_id`) REFERENCES `qb_answer` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_appeal_handler` FOREIGN KEY (`handled_by`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_appeal_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_appeal
-- ----------------------------

-- ----------------------------
-- Table structure for qb_assignment
-- ----------------------------
DROP TABLE IF EXISTS `qb_assignment`;
CREATE TABLE `qb_assignment`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `paper_id` bigint UNSIGNED NOT NULL,
  `assignment_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `assignment_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `start_time` datetime(3) NULL DEFAULT NULL,
  `end_time` datetime(3) NOT NULL,
  `time_limit_min` int NOT NULL DEFAULT 0,
  `max_attempts` int NOT NULL DEFAULT 1,
  `shuffle_questions` tinyint NOT NULL DEFAULT 0,
  `shuffle_options` tinyint NOT NULL DEFAULT 0,
  `publish_status` tinyint NOT NULL DEFAULT 1 COMMENT '1=draft,2=published,3=closed',
  `created_by` bigint UNSIGNED NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_paper`(`paper_id`) USING BTREE,
  INDEX `idx_assignment_deadline`(`end_time`) USING BTREE,
  INDEX `idx_assignment_creator`(`created_by`) USING BTREE,
  CONSTRAINT `fk_assignment_creator` FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_assignment_paper` FOREIGN KEY (`paper_id`) REFERENCES `qb_paper` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_assignment
-- ----------------------------

-- ----------------------------
-- Table structure for qb_assignment_target
-- ----------------------------
DROP TABLE IF EXISTS `qb_assignment_target`;
CREATE TABLE `qb_assignment_target`  (
  `assignment_id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`assignment_id`, `user_id`) USING BTREE,
  INDEX `idx_assignment_target_user`(`user_id`) USING BTREE,
  CONSTRAINT `fk_at_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `qb_assignment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_at_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_assignment_target
-- ----------------------------

-- ----------------------------
-- Table structure for qb_attempt
-- ----------------------------
DROP TABLE IF EXISTS `qb_attempt`;
CREATE TABLE `qb_attempt`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint UNSIGNED NULL DEFAULT NULL,
  `paper_id` bigint UNSIGNED NULL DEFAULT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `attempt_type` tinyint NOT NULL COMMENT '1=assignment,2=practice',
  `attempt_no` int NOT NULL DEFAULT 1,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=in_progress,2=submitted,3=grading,4=graded',
  `started_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `submitted_at` datetime(3) NULL DEFAULT NULL,
  `duration_sec` int NULL DEFAULT NULL,
  `total_score` int NOT NULL DEFAULT 0,
  `objective_score` int NOT NULL DEFAULT 0,
  `subjective_score` int NOT NULL DEFAULT 0,
  `needs_review` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_attempt_user_created`(`user_id`, `created_at`) USING BTREE,
  INDEX `idx_attempt_assignment`(`assignment_id`) USING BTREE,
  INDEX `fk_attempt_paper`(`paper_id`) USING BTREE,
  CONSTRAINT `fk_attempt_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `qb_assignment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_attempt_paper` FOREIGN KEY (`paper_id`) REFERENCES `qb_paper` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_attempt_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_attempt
-- ----------------------------

-- ----------------------------
-- Table structure for qb_attempt_question
-- ----------------------------
DROP TABLE IF EXISTS `qb_attempt_question`;
CREATE TABLE `qb_attempt_question`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `attempt_id` bigint UNSIGNED NOT NULL,
  `question_id` bigint UNSIGNED NOT NULL,
  `order_no` int NOT NULL DEFAULT 1,
  `score` int NOT NULL DEFAULT 0,
  `snapshot_json` json NULL,
  `snapshot_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `question_type` tinyint NOT NULL,
  `difficulty` tinyint NOT NULL,
  `tag_ids_json` json NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_aq_attempt_order`(`attempt_id`, `order_no`) USING BTREE,
  INDEX `idx_aq_attempt`(`attempt_id`) USING BTREE,
  INDEX `idx_aq_question`(`question_id`) USING BTREE,
  CONSTRAINT `fk_aq_attempt` FOREIGN KEY (`attempt_id`) REFERENCES `qb_attempt` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_aq_question` FOREIGN KEY (`question_id`) REFERENCES `qb_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_attempt_question
-- ----------------------------

-- ----------------------------
-- Table structure for qb_grading_record
-- ----------------------------
DROP TABLE IF EXISTS `qb_grading_record`;
CREATE TABLE `qb_grading_record`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `answer_id` bigint UNSIGNED NOT NULL,
  `grading_mode` tinyint NOT NULL COMMENT '1=auto,2=llm,3=manual',
  `score` int NOT NULL DEFAULT 0,
  `detail_json` json NULL,
  `llm_call_id` bigint UNSIGNED NULL DEFAULT NULL,
  `confidence` decimal(6, 4) NULL DEFAULT NULL,
  `needs_review` tinyint NOT NULL DEFAULT 0,
  `reviewer_id` bigint UNSIGNED NULL DEFAULT NULL,
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `is_final` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_gr_answer`(`answer_id`) USING BTREE,
  INDEX `idx_gr_llm`(`llm_call_id`) USING BTREE,
  INDEX `idx_gr_created`(`created_at`) USING BTREE,
  INDEX `fk_gr_reviewer`(`reviewer_id`) USING BTREE,
  CONSTRAINT `fk_gr_answer` FOREIGN KEY (`answer_id`) REFERENCES `qb_answer` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_gr_llm` FOREIGN KEY (`llm_call_id`) REFERENCES `qb_llm_call` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_gr_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_grading_record
-- ----------------------------

-- ----------------------------
-- Table structure for qb_llm_call
-- ----------------------------
DROP TABLE IF EXISTS `qb_llm_call`;
CREATE TABLE `qb_llm_call`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `biz_type` tinyint NOT NULL COMMENT '1=QUESTION_ANALYSIS,2=SUBJECTIVE_GRADING,3=OTHER',
  `biz_id` bigint UNSIGNED NULL DEFAULT NULL,
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `prompt_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `response_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `response_json` json NULL,
  `call_status` tinyint NOT NULL DEFAULT 0 COMMENT '0=pending,1=success,2=failed',
  `latency_ms` int NULL DEFAULT NULL,
  `tokens_prompt` int NULL DEFAULT NULL,
  `tokens_completion` int NULL DEFAULT NULL,
  `cost_amount` decimal(10, 4) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_llm_call_biz`(`biz_type`, `biz_id`) USING BTREE,
  INDEX `idx_llm_call_created`(`created_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_llm_call
-- ----------------------------

-- ----------------------------
-- Table structure for qb_paper
-- ----------------------------
DROP TABLE IF EXISTS `qb_paper`;
CREATE TABLE `qb_paper`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `paper_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `paper_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `paper_type` tinyint NOT NULL DEFAULT 1 COMMENT '1=manual,2=rule_generated',
  `total_score` int NOT NULL DEFAULT 0,
  `rule_json` json NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=draft,2=published,3=archived',
  `creator_id` bigint UNSIGNED NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qb_paper_creator`(`creator_id`) USING BTREE,
  CONSTRAINT `fk_paper_creator` FOREIGN KEY (`creator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_paper
-- ----------------------------

-- ----------------------------
-- Table structure for qb_paper_question
-- ----------------------------
DROP TABLE IF EXISTS `qb_paper_question`;
CREATE TABLE `qb_paper_question`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `paper_id` bigint UNSIGNED NOT NULL,
  `question_id` bigint UNSIGNED NOT NULL,
  `order_no` int NOT NULL DEFAULT 1,
  `score` int NOT NULL DEFAULT 0,
  `snapshot_json` json NULL COMMENT 'question snapshot for traceability',
  `snapshot_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pq_paper_order`(`paper_id`, `order_no`) USING BTREE,
  INDEX `idx_pq_paper`(`paper_id`) USING BTREE,
  INDEX `idx_pq_question`(`question_id`) USING BTREE,
  CONSTRAINT `fk_pq_paper` FOREIGN KEY (`paper_id`) REFERENCES `qb_paper` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_pq_question` FOREIGN KEY (`question_id`) REFERENCES `qb_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_paper_question
-- ----------------------------

-- ----------------------------
-- Table structure for qb_post
-- ----------------------------
DROP TABLE IF EXISTS `qb_post`;
CREATE TABLE `qb_post`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `author_id` bigint UNSIGNED NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `related_question_id` bigint UNSIGNED NULL DEFAULT NULL,
  `post_status` tinyint NOT NULL DEFAULT 1 COMMENT '1=normal,2=closed',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_author_created`(`author_id`, `created_at`) USING BTREE,
  INDEX `idx_post_related_question`(`related_question_id`) USING BTREE,
  CONSTRAINT `fk_post_author` FOREIGN KEY (`author_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_post_related_question` FOREIGN KEY (`related_question_id`) REFERENCES `qb_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_post
-- ----------------------------

-- ----------------------------
-- Table structure for qb_post_tag
-- ----------------------------
DROP TABLE IF EXISTS `qb_post_tag`;
CREATE TABLE `qb_post_tag`  (
  `post_id` bigint UNSIGNED NOT NULL,
  `tag_id` bigint UNSIGNED NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`post_id`, `tag_id`) USING BTREE,
  INDEX `idx_post_tag_tag`(`tag_id`, `post_id`) USING BTREE,
  CONSTRAINT `fk_pt_post` FOREIGN KEY (`post_id`) REFERENCES `qb_post` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_pt_tag` FOREIGN KEY (`tag_id`) REFERENCES `qb_tag` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_post_tag
-- ----------------------------

-- ----------------------------
-- Table structure for qb_practice_generation
-- ----------------------------
DROP TABLE IF EXISTS `qb_practice_generation`;
CREATE TABLE `qb_practice_generation`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NOT NULL,
  `attempt_id` bigint UNSIGNED NOT NULL,
  `rule_id` bigint UNSIGNED NULL DEFAULT NULL,
  `scope_json` json NOT NULL,
  `target_diff_min` tinyint NOT NULL,
  `target_diff_max` tinyint NOT NULL,
  `debug_json` json NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pg_user_created`(`user_id`, `created_at`) USING BTREE,
  INDEX `fk_pg_attempt`(`attempt_id`) USING BTREE,
  INDEX `fk_pg_rule`(`rule_id`) USING BTREE,
  CONSTRAINT `fk_pg_attempt` FOREIGN KEY (`attempt_id`) REFERENCES `qb_attempt` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_pg_rule` FOREIGN KEY (`rule_id`) REFERENCES `qb_practice_rule` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_pg_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_practice_generation
-- ----------------------------

-- ----------------------------
-- Table structure for qb_practice_rule
-- ----------------------------
DROP TABLE IF EXISTS `qb_practice_rule`;
CREATE TABLE `qb_practice_rule`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rule_json` json NOT NULL,
  `is_default` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_practice_rule
-- ----------------------------

-- ----------------------------
-- Table structure for qb_question
-- ----------------------------
DROP TABLE IF EXISTS `qb_question`;
CREATE TABLE `qb_question`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `question_type` tinyint NOT NULL COMMENT '1=single,2=multiple,3=true_false,4=blank,5=short,6=code,7=code_reading',
  `difficulty` tinyint NOT NULL DEFAULT 1 COMMENT '1~5',
  `chapter` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `stem` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `standard_answer` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `answer_format` tinyint NOT NULL DEFAULT 1 COMMENT '1=text,2=json',
  `analysis_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `analysis_source` tinyint NOT NULL DEFAULT 1 COMMENT '1=manual,2=llm_draft,3=llm_final',
  `analysis_llm_call_id` bigint UNSIGNED NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=draft,2=published,3=archived',
  `created_by` bigint UNSIGNED NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qb_question_type_diff`(`question_type`, `difficulty`) USING BTREE,
  INDEX `idx_qb_question_chapter`(`chapter`) USING BTREE,
  INDEX `idx_qb_question_status`(`status`) USING BTREE,
  INDEX `fk_question_analysis_llm`(`analysis_llm_call_id`) USING BTREE,
  INDEX `fk_question_creator`(`created_by`) USING BTREE,
  CONSTRAINT `fk_question_analysis_llm` FOREIGN KEY (`analysis_llm_call_id`) REFERENCES `qb_llm_call` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_question_creator` FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_question
-- ----------------------------

-- ----------------------------
-- Table structure for qb_question_case
-- ----------------------------
DROP TABLE IF EXISTS `qb_question_case`;
CREATE TABLE `qb_question_case`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `question_id` bigint UNSIGNED NOT NULL,
  `case_no` int NOT NULL DEFAULT 1,
  `input_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `expected_output` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `case_score` int NOT NULL DEFAULT 0,
  `is_sample` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_qb_case_q_no`(`question_id`, `case_no`) USING BTREE,
  CONSTRAINT `fk_case_question` FOREIGN KEY (`question_id`) REFERENCES `qb_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_question_case
-- ----------------------------

-- ----------------------------
-- Table structure for qb_question_option
-- ----------------------------
DROP TABLE IF EXISTS `qb_question_option`;
CREATE TABLE `qb_question_option`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `question_id` bigint UNSIGNED NOT NULL,
  `option_label` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'A/B/C/D...',
  `option_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_correct` tinyint NOT NULL DEFAULT 0,
  `sort_order` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qb_option_question`(`question_id`) USING BTREE,
  CONSTRAINT `fk_option_question` FOREIGN KEY (`question_id`) REFERENCES `qb_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_question_option
-- ----------------------------

-- ----------------------------
-- Table structure for qb_question_tag
-- ----------------------------
DROP TABLE IF EXISTS `qb_question_tag`;
CREATE TABLE `qb_question_tag`  (
  `question_id` bigint UNSIGNED NOT NULL,
  `tag_id` bigint UNSIGNED NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`question_id`, `tag_id`) USING BTREE,
  INDEX `idx_qb_question_tag_tag`(`tag_id`, `question_id`) USING BTREE,
  CONSTRAINT `fk_qt_question` FOREIGN KEY (`question_id`) REFERENCES `qb_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_qt_tag` FOREIGN KEY (`tag_id`) REFERENCES `qb_tag` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_question_tag
-- ----------------------------

-- ----------------------------
-- Table structure for qb_question_user_stat
-- ----------------------------
DROP TABLE IF EXISTS `qb_question_user_stat`;
CREATE TABLE `qb_question_user_stat`  (
  `user_id` bigint UNSIGNED NOT NULL,
  `question_id` bigint UNSIGNED NOT NULL,
  `attempt_count` int NOT NULL DEFAULT 0,
  `correct_count` int NOT NULL DEFAULT 0,
  `last_attempt_at` datetime(3) NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`, `question_id`) USING BTREE,
  INDEX `idx_qus_question`(`question_id`) USING BTREE,
  CONSTRAINT `fk_qus_question` FOREIGN KEY (`question_id`) REFERENCES `qb_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_qus_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_question_user_stat
-- ----------------------------

-- ----------------------------
-- Table structure for qb_reply
-- ----------------------------
DROP TABLE IF EXISTS `qb_reply`;
CREATE TABLE `qb_reply`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` bigint UNSIGNED NOT NULL,
  `author_id` bigint UNSIGNED NOT NULL,
  `parent_reply_id` bigint UNSIGNED NULL DEFAULT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reply_post_created`(`post_id`, `created_at`) USING BTREE,
  INDEX `idx_reply_parent`(`parent_reply_id`) USING BTREE,
  INDEX `fk_reply_author`(`author_id`) USING BTREE,
  CONSTRAINT `fk_reply_author` FOREIGN KEY (`author_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reply_parent` FOREIGN KEY (`parent_reply_id`) REFERENCES `qb_reply` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reply_post` FOREIGN KEY (`post_id`) REFERENCES `qb_post` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_reply
-- ----------------------------

-- ----------------------------
-- Table structure for qb_tag
-- ----------------------------
DROP TABLE IF EXISTS `qb_tag`;
CREATE TABLE `qb_tag`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tag_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL,
  `tag_level` int NOT NULL DEFAULT 1,
  `tag_type` tinyint NOT NULL DEFAULT 1 COMMENT '1=knowledge,2=chapter,3=custom',
  `sort_order` int NOT NULL DEFAULT 0,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_qb_tag_name`(`tag_name`) USING BTREE,
  INDEX `idx_qb_tag_parent`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_tag
-- ----------------------------

-- ----------------------------
-- Table structure for qb_tag_mastery
-- ----------------------------
DROP TABLE IF EXISTS `qb_tag_mastery`;
CREATE TABLE `qb_tag_mastery`  (
  `user_id` bigint UNSIGNED NOT NULL,
  `tag_id` bigint UNSIGNED NOT NULL,
  `mastery_value` decimal(6, 4) NOT NULL DEFAULT 0.0000,
  `correct_count` int NOT NULL DEFAULT 0,
  `attempt_count` int NOT NULL DEFAULT 0,
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`user_id`, `tag_id`) USING BTREE,
  INDEX `idx_mastery_tag`(`tag_id`) USING BTREE,
  CONSTRAINT `fk_mastery_tag` FOREIGN KEY (`tag_id`) REFERENCES `qb_tag` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_mastery_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_tag_mastery
-- ----------------------------

-- ----------------------------
-- Table structure for qb_user_ability
-- ----------------------------
DROP TABLE IF EXISTS `qb_user_ability`;
CREATE TABLE `qb_user_ability`  (
  `user_id` bigint UNSIGNED NOT NULL,
  `ability_score` int NOT NULL DEFAULT 0,
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`user_id`) USING BTREE,
  CONSTRAINT `fk_ability_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_user_ability
-- ----------------------------

-- ----------------------------
-- Table structure for qb_wrong_question
-- ----------------------------
DROP TABLE IF EXISTS `qb_wrong_question`;
CREATE TABLE `qb_wrong_question`  (
  `user_id` bigint UNSIGNED NOT NULL,
  `question_id` bigint UNSIGNED NOT NULL,
  `wrong_count` int NOT NULL DEFAULT 0,
  `first_wrong_at` datetime(3) NULL DEFAULT NULL,
  `last_wrong_at` datetime(3) NULL DEFAULT NULL,
  `is_resolved` tinyint NOT NULL DEFAULT 0,
  `resolved_at` datetime(3) NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`, `question_id`) USING BTREE,
  INDEX `idx_wq_question`(`question_id`) USING BTREE,
  CONSTRAINT `fk_wq_question` FOREIGN KEY (`question_id`) REFERENCES `qb_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_wq_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qb_wrong_question
-- ----------------------------

-- ----------------------------
-- Table structure for sys_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NULL DEFAULT NULL,
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `entity_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `entity_id` bigint UNSIGNED NULL DEFAULT NULL,
  `before_json` json NULL,
  `after_json` json NULL,
  `ip_addr` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_audit_user_created`(`user_id`, `created_at`) USING BTREE,
  INDEX `idx_audit_entity`(`entity_type`, `entity_id`) USING BTREE,
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_audit_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NULL DEFAULT NULL,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `success_flag` tinyint NOT NULL DEFAULT 1,
  `fail_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ip_addr` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `login_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_login_user_time`(`user_id`, `login_at`) USING BTREE,
  INDEX `idx_login_username_time`(`username`, `login_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `perm_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `perm_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `resource` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api/resource identifier',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_perm_code`(`perm_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'e.g. STUDENT/TEACHER/ADMIN',
  `role_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `role_id` bigint UNSIGNED NOT NULL,
  `perm_id` bigint UNSIGNED NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`role_id`, `perm_id`) USING BTREE,
  INDEX `fk_role_perm_perm`(`perm_id`) USING BTREE,
  CONSTRAINT `fk_role_perm_perm` FOREIGN KEY (`perm_id`) REFERENCES `sys_permission` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_perm_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `display_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1=active,0=disabled',
  `last_login_at` datetime(3) NULL DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_username`(`username`) USING BTREE,
  INDEX `idx_sys_user_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint UNSIGNED NOT NULL,
  `role_id` bigint UNSIGNED NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `fk_user_role_role`(`role_id`) USING BTREE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
