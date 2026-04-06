用例图
@startuml
left to right direction
actor 学生 as Student
actor 教师 as Teacher
actor 管理员 as Admin

rectangle "题库管理系统" {
  usecase "登录/退出" as UC_Login

  usecase "浏览题库\n(标签/章节/难度检索)" as UC_Browse
  usecase "开始练习\n(随机/自适应)" as UC_Practice
  usecase "提交答案" as UC_Submit
  usecase "查看结果与解析" as UC_View
  usecase "错题集/练习记录" as UC_Wrong

  usecase "标签管理" as UC_Tag
  usecase "题目管理\n(CRUD)" as UC_Question
  usecase "组卷" as UC_Paper
  usecase "发布作业/测试" as UC_Publish
  usecase "查看成绩/批改记录" as UC_Grade

  usecase "LLM辅助评分\n(主观题)" as UC_LLM
  usecase "申诉复核" as UC_Appeal

  usecase "答疑区\n发帖/回帖" as UC_Forum
}

Student --> UC_Login
Student --> UC_Browse
Student --> UC_Practice
Student --> UC_Submit
Student --> UC_View
Student --> UC_Wrong
Student --> UC_Appeal
Student --> UC_Forum

Teacher --> UC_Login
Teacher --> UC_Tag
Teacher --> UC_Question
Teacher --> UC_Paper
Teacher --> UC_Publish
Teacher --> UC_Grade
Teacher --> UC_LLM
Teacher --> UC_Appeal
Teacher --> UC_Forum

Admin --> UC_Login
@enduml


四个流程图
1：

@startuml
|Teacher|
start
:Login;
:Open "Question Management";
|System|
:Verify JWT & Role(TEACHER);
|Teacher|
:Create Question (type/difficulty/chapter/stem/options/answer);
:Bind Tags;
:Save Draft;
if (Generate analysis by LLM?) then (Yes)
  |System|
  :Build prompt (stem + standard_answer + rubric for analysis);
  :Create LLMCall(biz_type=QUESTION_ANALYSIS);
  :Invoke LLM;
  if (Response OK?) then (Yes)
    :Store llm_response & analysis_draft;
  else (No)
    :Mark analysis_source = MANUAL_PENDING;
  endif
  |Teacher|
  :Review/Edit analysis;
endif
:Publish Question;
|System|
:Set question.status = PUBLISHED;
:Write audit log;
stop
@enduml

2：
@startuml
|Student|
start
:Login;
:Choose "Practice" or "Assignment";
|System|
:Verify JWT & Role(STUDENT);
if (Entry = Assignment?) then (Yes)
  :Check deadline & remaining attempts;
  if (Allowed?) then (Yes)
    :Create Attempt(type=ASSIGNMENT);
    :Generate AttemptQuestion list from Paper (snapshot/order/score);
  else (No)
    :Reject with reason (expired / no attempts left);
    stop
  endif
else (Practice)
  :Create Attempt(type=PRACTICE);
  :Generate AttemptQuestion list (Random/Adaptive);
endif

|Student|
repeat
  :Answer one question;
  :Save draft(optional);
repeat while (More questions?)

:Submit Attempt;

|System|
:Lock attempt (status=SUBMITTED);
:Auto-grade objective questions;
:Write AUTO GradingRecord(s);
:Compute total_score(objective part);

if (Has subjective questions?) then (Yes)
  :Mark attempt.status = GRADING_PENDING;
else (No)
  :Mark attempt.status = GRADED;
endif

:Update stats (wrong book / question stat / mastery / ability);
|Student|
:View result & analysis;
stop
@enduml

3：
@startuml
|System|
start
:Detect subjective answers in submitted attempt;
:For each subjective Answer -> build rubric & prompt;
:Create LLMCall(biz_type=SUBJECTIVE_GRADING);
:Invoke LLM;

if (LLM returns valid JSON?) then (Yes)
  :Parse {score, deductions, reason, confidence};
  if (confidence >= threshold AND consistency OK?) then (Yes)
    :Create GradingRecord(mode=LLM, needs_review=false);
    :Set Answer.final_score = llm_score;
  else (No)
    :Create GradingRecord(mode=LLM, needs_review=true);
    :Set Answer.status = NEED_REVIEW;
  endif
else (No)
  :Create GradingRecord(mode=LLM, needs_review=true, detail="parse failed");
  :Set Answer.status = NEED_REVIEW;
endif

:If any Answer NEED_REVIEW -> Attempt.needs_review=true;

|Teacher|
:Open "Review Center";
:View evidence (student answer + rubric + LLM raw + parsed JSON);
:Manual adjust score (optional);
:Confirm final score & comment;
|System|
:Create GradingRecord(mode=MANUAL, is_final=true);
:Set Answer.final_score & graded_at;
:Attempt status -> GRADED (when all graded);

|Student|
:View final result;
if (Submit appeal?) then (Yes)
  :Fill appeal reason & attachments;
  |System|
  :Create Appeal(status=PENDING);
  |Teacher|
  :Handle appeal (approve/reject);
  |System|
  :Update Appeal(status=RESOLVED);
  :If score changed -> update Answer.final_score and record MANUAL grading;
endif
stop
@enduml

4：
@startuml
|Student|
start
:Login;
:Select scope (tags/chapters);
:Click "Adaptive Practice";

|System|
:Load candidate questions in scope (PUBLISHED);
:Load TagMastery (0~1) and UserAbility (0~100);
:Map ability -> target difficulty range (d-1..d+1);

:For each candidate question compute weight:
- weakness = 1 - mastery(tag)
- difficulty_match = |q.diff - target| small -> higher
- novelty = 1 / (1 + user_attempt_count)
- exclude recently done;

:Apply constraints:
- total_score = 100
- question type distribution (config)
- no duplicate question;

:Weighted random sampling by groups (type/score);
if (Not enough candidates?) then (Yes)
  :Relax difficulty range or report "insufficient coverage";
endif

:Create Attempt(type=PRACTICE);
:Create AttemptQuestion list with snapshot/order/score;
|Student|
:Enter answering page;
stop
@enduml

sql
-- ==========================================
-- Database: question_bank (example)
-- ==========================================

-- --------------------------
-- 0) RBAC
-- --------------------------
CREATE TABLE sys_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(64) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1=active,0=disabled',
  last_login_at DATETIME(3) DEFAULT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username),
  KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_code VARCHAR(64) NOT NULL COMMENT 'e.g. STUDENT/TEACHER/ADMIN',
  role_name VARCHAR(64) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  perm_code VARCHAR(128) NOT NULL,
  perm_name VARCHAR(128) NOT NULL,
  resource VARCHAR(255) DEFAULT NULL COMMENT 'api/resource identifier',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role_permission (
  role_id BIGINT UNSIGNED NOT NULL,
  perm_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (role_id, perm_id),
  CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
  CONSTRAINT fk_role_perm_perm FOREIGN KEY (perm_id) REFERENCES sys_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------
-- 1) Tag / Question
-- --------------------------
CREATE TABLE qb_tag (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tag_name VARCHAR(128) NOT NULL,
  tag_code VARCHAR(128) DEFAULT NULL,
  parent_id BIGINT UNSIGNED DEFAULT NULL,
  tag_level INT NOT NULL DEFAULT 1,
  tag_type TINYINT NOT NULL DEFAULT 1 COMMENT '1=knowledge,2=chapter,3=custom',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_qb_tag_name (tag_name),
  KEY idx_qb_tag_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_llm_call (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  biz_type TINYINT NOT NULL COMMENT '1=QUESTION_ANALYSIS,2=SUBJECTIVE_GRADING,3=OTHER',
  biz_id BIGINT UNSIGNED DEFAULT NULL,
  model_name VARCHAR(64) NOT NULL,
  prompt_text LONGTEXT NOT NULL,
  response_text LONGTEXT DEFAULT NULL,
  response_json JSON DEFAULT NULL,
  call_status TINYINT NOT NULL DEFAULT 0 COMMENT '0=pending,1=success,2=failed',
  latency_ms INT DEFAULT NULL,
  tokens_prompt INT DEFAULT NULL,
  tokens_completion INT DEFAULT NULL,
  cost_amount DECIMAL(10,4) DEFAULT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_llm_call_biz (biz_type, biz_id),
  KEY idx_llm_call_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_question (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(255) DEFAULT NULL,
  question_type TINYINT NOT NULL COMMENT '1=single,2=multiple,3=true_false,4=blank,5=short,6=code,7=code_reading',
  difficulty TINYINT NOT NULL DEFAULT 1 COMMENT '1~5',
  chapter VARCHAR(64) DEFAULT NULL,
  stem LONGTEXT NOT NULL,
  standard_answer LONGTEXT DEFAULT NULL,
  answer_format TINYINT NOT NULL DEFAULT 1 COMMENT '1=text,2=json',
  analysis_text LONGTEXT DEFAULT NULL,
  analysis_source TINYINT NOT NULL DEFAULT 1 COMMENT '1=manual,2=llm_draft,3=llm_final',
  analysis_llm_call_id BIGINT UNSIGNED DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1=draft,2=published,3=archived',
  created_by BIGINT UNSIGNED DEFAULT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_qb_question_type_diff (question_type, difficulty),
  KEY idx_qb_question_chapter (chapter),
  KEY idx_qb_question_status (status),
  CONSTRAINT fk_question_analysis_llm FOREIGN KEY (analysis_llm_call_id) REFERENCES qb_llm_call(id),
  CONSTRAINT fk_question_creator FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_question_option (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  question_id BIGINT UNSIGNED NOT NULL,
  option_label VARCHAR(8) NOT NULL COMMENT 'A/B/C/D...',
  option_content TEXT NOT NULL,
  is_correct TINYINT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_qb_option_question (question_id),
  CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES qb_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_question_tag (
  question_id BIGINT UNSIGNED NOT NULL,
  tag_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (question_id, tag_id),
  KEY idx_qb_question_tag_tag (tag_id, question_id),
  CONSTRAINT fk_qt_question FOREIGN KEY (question_id) REFERENCES qb_question(id),
  CONSTRAINT fk_qt_tag FOREIGN KEY (tag_id) REFERENCES qb_tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_question_case (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  question_id BIGINT UNSIGNED NOT NULL,
  case_no INT NOT NULL DEFAULT 1,
  input_data LONGTEXT NOT NULL,
  expected_output LONGTEXT NOT NULL,
  case_score INT NOT NULL DEFAULT 0,
  is_sample TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_qb_case_q_no (question_id, case_no),
  CONSTRAINT fk_case_question FOREIGN KEY (question_id) REFERENCES qb_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------
-- 2) Paper / Assignment
-- --------------------------
CREATE TABLE qb_paper (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  paper_title VARCHAR(255) NOT NULL,
  paper_desc TEXT DEFAULT NULL,
  paper_type TINYINT NOT NULL DEFAULT 1 COMMENT '1=manual,2=rule_generated',
  total_score INT NOT NULL DEFAULT 0,
  rule_json JSON DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1=draft,2=published,3=archived',
  creator_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_qb_paper_creator (creator_id),
  CONSTRAINT fk_paper_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_paper_question (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  paper_id BIGINT UNSIGNED NOT NULL,
  question_id BIGINT UNSIGNED NOT NULL,
  order_no INT NOT NULL DEFAULT 1,
  score INT NOT NULL DEFAULT 0,
  snapshot_json JSON DEFAULT NULL COMMENT 'question snapshot for traceability',
  snapshot_hash CHAR(64) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pq_paper_order (paper_id, order_no),
  KEY idx_pq_paper (paper_id),
  KEY idx_pq_question (question_id),
  CONSTRAINT fk_pq_paper FOREIGN KEY (paper_id) REFERENCES qb_paper(id),
  CONSTRAINT fk_pq_question FOREIGN KEY (question_id) REFERENCES qb_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_assignment (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  paper_id BIGINT UNSIGNED NOT NULL,
  assignment_title VARCHAR(255) NOT NULL,
  assignment_desc TEXT DEFAULT NULL,
  start_time DATETIME(3) DEFAULT NULL,
  end_time DATETIME(3) NOT NULL,
  time_limit_min INT NOT NULL DEFAULT 0,
  max_attempts INT NOT NULL DEFAULT 1,
  shuffle_questions TINYINT NOT NULL DEFAULT 0,
  shuffle_options TINYINT NOT NULL DEFAULT 0,
  publish_status TINYINT NOT NULL DEFAULT 1 COMMENT '1=draft,2=published,3=closed',
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_assignment_paper (paper_id),
  KEY idx_assignment_deadline (end_time),
  KEY idx_assignment_creator (created_by),
  CONSTRAINT fk_assignment_paper FOREIGN KEY (paper_id) REFERENCES qb_paper(id),
  CONSTRAINT fk_assignment_creator FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_assignment_target (
  assignment_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (assignment_id, user_id),
  KEY idx_assignment_target_user (user_id),
  CONSTRAINT fk_at_assignment FOREIGN KEY (assignment_id) REFERENCES qb_assignment(id),
  CONSTRAINT fk_at_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------
-- 3) Attempt / Answer / Grading
-- --------------------------
CREATE TABLE qb_attempt (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  assignment_id BIGINT UNSIGNED DEFAULT NULL,
  paper_id BIGINT UNSIGNED DEFAULT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  attempt_type TINYINT NOT NULL COMMENT '1=assignment,2=practice',
  attempt_no INT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1=in_progress,2=submitted,3=grading,4=graded',
  started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  submitted_at DATETIME(3) DEFAULT NULL,
  duration_sec INT DEFAULT NULL,
  total_score INT NOT NULL DEFAULT 0,
  objective_score INT NOT NULL DEFAULT 0,
  subjective_score INT NOT NULL DEFAULT 0,
  needs_review TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_attempt_user_created (user_id, created_at),
  KEY idx_attempt_assignment (assignment_id),
  CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_attempt_assignment FOREIGN KEY (assignment_id) REFERENCES qb_assignment(id),
  CONSTRAINT fk_attempt_paper FOREIGN KEY (paper_id) REFERENCES qb_paper(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_attempt_question (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  attempt_id BIGINT UNSIGNED NOT NULL,
  question_id BIGINT UNSIGNED NOT NULL,
  order_no INT NOT NULL DEFAULT 1,
  score INT NOT NULL DEFAULT 0,
  snapshot_json JSON DEFAULT NULL,
  snapshot_hash CHAR(64) DEFAULT NULL,
  question_type TINYINT NOT NULL,
  difficulty TINYINT NOT NULL,
  tag_ids_json JSON DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_aq_attempt_order (attempt_id, order_no),
  KEY idx_aq_attempt (attempt_id),
  KEY idx_aq_question (question_id),
  CONSTRAINT fk_aq_attempt FOREIGN KEY (attempt_id) REFERENCES qb_attempt(id),
  CONSTRAINT fk_aq_question FOREIGN KEY (question_id) REFERENCES qb_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_answer (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  attempt_id BIGINT UNSIGNED NOT NULL,
  attempt_question_id BIGINT UNSIGNED NOT NULL,
  question_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  answer_content LONGTEXT DEFAULT NULL,
  answer_format TINYINT NOT NULL DEFAULT 1,
  answer_status TINYINT NOT NULL DEFAULT 1 COMMENT '1=draft,2=submitted',
  auto_score INT NOT NULL DEFAULT 0,
  final_score INT NOT NULL DEFAULT 0,
  is_correct TINYINT NOT NULL DEFAULT 0,
  answered_at DATETIME(3) DEFAULT NULL,
  graded_at DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_answer_attempt_question (attempt_question_id),
  KEY idx_answer_attempt_question (attempt_id, question_id),
  CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id) REFERENCES qb_attempt(id),
  CONSTRAINT fk_answer_attempt_question FOREIGN KEY (attempt_question_id) REFERENCES qb_attempt_question(id),
  CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES qb_question(id),
  CONSTRAINT fk_answer_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_grading_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  answer_id BIGINT UNSIGNED NOT NULL,
  grading_mode TINYINT NOT NULL COMMENT '1=auto,2=llm,3=manual',
  score INT NOT NULL DEFAULT 0,
  detail_json JSON DEFAULT NULL,
  llm_call_id BIGINT UNSIGNED DEFAULT NULL,
  confidence DECIMAL(6,4) DEFAULT NULL,
  needs_review TINYINT NOT NULL DEFAULT 0,
  reviewer_id BIGINT UNSIGNED DEFAULT NULL,
  review_comment TEXT DEFAULT NULL,
  is_final TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_gr_answer (answer_id),
  KEY idx_gr_llm (llm_call_id),
  KEY idx_gr_created (created_at),
  CONSTRAINT fk_gr_answer FOREIGN KEY (answer_id) REFERENCES qb_answer(id),
  CONSTRAINT fk_gr_llm FOREIGN KEY (llm_call_id) REFERENCES qb_llm_call(id),
  CONSTRAINT fk_gr_reviewer FOREIGN KEY (reviewer_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_appeal (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  answer_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  reason_text TEXT NOT NULL,
  appeal_status TINYINT NOT NULL DEFAULT 1 COMMENT '1=pending,2=approved,3=rejected,4=resolved',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  handled_by BIGINT UNSIGNED DEFAULT NULL,
  handled_at DATETIME(3) DEFAULT NULL,
  decision_comment TEXT DEFAULT NULL,
  final_score INT DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_appeal_answer (answer_id),
  KEY idx_appeal_user (user_id),
  CONSTRAINT fk_appeal_answer FOREIGN KEY (answer_id) REFERENCES qb_answer(id),
  CONSTRAINT fk_appeal_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_appeal_handler FOREIGN KEY (handled_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------
-- 4) Statistics / Adaptive
-- --------------------------
CREATE TABLE qb_tag_mastery (
  user_id BIGINT UNSIGNED NOT NULL,
  tag_id BIGINT UNSIGNED NOT NULL,
  mastery_value DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
  correct_count INT NOT NULL DEFAULT 0,
  attempt_count INT NOT NULL DEFAULT 0,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (user_id, tag_id),
  KEY idx_mastery_tag (tag_id),
  CONSTRAINT fk_mastery_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_mastery_tag FOREIGN KEY (tag_id) REFERENCES qb_tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_user_ability (
  user_id BIGINT UNSIGNED NOT NULL,
  ability_score INT NOT NULL DEFAULT 0,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (user_id),
  CONSTRAINT fk_ability_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_question_user_stat (
  user_id BIGINT UNSIGNED NOT NULL,
  question_id BIGINT UNSIGNED NOT NULL,
  attempt_count INT NOT NULL DEFAULT 0,
  correct_count INT NOT NULL DEFAULT 0,
  last_attempt_at DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (user_id, question_id),
  KEY idx_qus_question (question_id),
  CONSTRAINT fk_qus_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_qus_question FOREIGN KEY (question_id) REFERENCES qb_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_wrong_question (
  user_id BIGINT UNSIGNED NOT NULL,
  question_id BIGINT UNSIGNED NOT NULL,
  wrong_count INT NOT NULL DEFAULT 0,
  first_wrong_at DATETIME(3) DEFAULT NULL,
  last_wrong_at DATETIME(3) DEFAULT NULL,
  is_resolved TINYINT NOT NULL DEFAULT 0,
  resolved_at DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (user_id, question_id),
  KEY idx_wq_question (question_id),
  CONSTRAINT fk_wq_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_wq_question FOREIGN KEY (question_id) REFERENCES qb_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_practice_rule (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  rule_name VARCHAR(128) NOT NULL,
  rule_json JSON NOT NULL,
  is_default TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_practice_generation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  attempt_id BIGINT UNSIGNED NOT NULL,
  rule_id BIGINT UNSIGNED DEFAULT NULL,
  scope_json JSON NOT NULL,
  target_diff_min TINYINT NOT NULL,
  target_diff_max TINYINT NOT NULL,
  debug_json JSON DEFAULT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_pg_user_created (user_id, created_at),
  CONSTRAINT fk_pg_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_pg_attempt FOREIGN KEY (attempt_id) REFERENCES qb_attempt(id),
  CONSTRAINT fk_pg_rule FOREIGN KEY (rule_id) REFERENCES qb_practice_rule(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------
-- 5) Forum
-- --------------------------
CREATE TABLE qb_post (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  author_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(255) NOT NULL,
  content LONGTEXT NOT NULL,
  related_question_id BIGINT UNSIGNED DEFAULT NULL,
  post_status TINYINT NOT NULL DEFAULT 1 COMMENT '1=normal,2=closed',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_post_author_created (author_id, created_at),
  KEY idx_post_related_question (related_question_id),
  CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES sys_user(id),
  CONSTRAINT fk_post_related_question FOREIGN KEY (related_question_id) REFERENCES qb_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_reply (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  author_id BIGINT UNSIGNED NOT NULL,
  parent_reply_id BIGINT UNSIGNED DEFAULT NULL,
  content LONGTEXT NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_reply_post_created (post_id, created_at),
  KEY idx_reply_parent (parent_reply_id),
  CONSTRAINT fk_reply_post FOREIGN KEY (post_id) REFERENCES qb_post(id),
  CONSTRAINT fk_reply_author FOREIGN KEY (author_id) REFERENCES sys_user(id),
  CONSTRAINT fk_reply_parent FOREIGN KEY (parent_reply_id) REFERENCES qb_reply(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qb_post_tag (
  post_id BIGINT UNSIGNED NOT NULL,
  tag_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (post_id, tag_id),
  KEY idx_post_tag_tag (tag_id, post_id),
  CONSTRAINT fk_pt_post FOREIGN KEY (post_id) REFERENCES qb_post(id),
  CONSTRAINT fk_pt_tag FOREIGN KEY (tag_id) REFERENCES qb_tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------
-- 6) Logs
-- --------------------------
CREATE TABLE sys_audit_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED DEFAULT NULL,
  action VARCHAR(64) NOT NULL,
  entity_type VARCHAR(64) NOT NULL,
  entity_id BIGINT UNSIGNED DEFAULT NULL,
  before_json JSON DEFAULT NULL,
  after_json JSON DEFAULT NULL,
  ip_addr VARCHAR(64) DEFAULT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_audit_user_created (user_id, created_at),
  KEY idx_audit_entity (entity_type, entity_id),
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_login_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED DEFAULT NULL,
  username VARCHAR(64) DEFAULT NULL,
  success_flag TINYINT NOT NULL DEFAULT 1,
  fail_reason VARCHAR(255) DEFAULT NULL,
  ip_addr VARCHAR(64) DEFAULT NULL,
  user_agent VARCHAR(255) DEFAULT NULL,
  login_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_login_user_time (user_id, login_at),
  KEY idx_login_username_time (username, login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

类图
@startuml
skinparam classAttributeIconSize 0

package "domain" {

  class User {
    +id: Long
    +username: String
    +passwordHash: String
    +displayName: String
    +email: String
    +status: int
  }

  class Role {
    +id: Long
    +roleCode: String
    +roleName: String
  }

  class Permission {
    +id: Long
    +permCode: String
    +permName: String
    +resource: String
  }

  class Tag {
    +id: Long
    +tagName: String
    +tagCode: String
    +parentId: Long
    +tagLevel: int
    +tagType: int
  }

  class Question {
    +id: Long
    +title: String
    +questionType: int
    +difficulty: int
    +chapter: String
    +stem: String
    +standardAnswer: String
    +analysisText: String
    +status: int
  }

  class QuestionOption {
    +id: Long
    +questionId: Long
    +optionLabel: String
    +optionContent: String
    +isCorrect: boolean
  }

  class Paper {
    +id: Long
    +paperTitle: String
    +paperType: int
    +totalScore: int
    +creatorId: Long
  }

  class PaperQuestion {
    +id: Long
    +paperId: Long
    +questionId: Long
    +orderNo: int
    +score: int
    +snapshotJson: String
  }

  class Assignment {
    +id: Long
    +paperId: Long
    +assignmentTitle: String
    +startTime: DateTime
    +endTime: DateTime
    +timeLimitMin: int
    +maxAttempts: int
    +publishStatus: int
  }

  class Attempt {
    +id: Long
    +assignmentId: Long
    +paperId: Long
    +userId: Long
    +attemptType: int
    +attemptNo: int
    +status: int
    +startedAt: DateTime
    +submittedAt: DateTime
    +totalScore: int
    +needsReview: boolean
  }

  class AttemptQuestion {
    +id: Long
    +attemptId: Long
    +questionId: Long
    +orderNo: int
    +score: int
    +snapshotJson: String
    +questionType: int
    +difficulty: int
  }

  class Answer {
    +id: Long
    +attemptId: Long
    +attemptQuestionId: Long
    +questionId: Long
    +userId: Long
    +answerContent: String
    +answerStatus: int
    +autoScore: int
    +finalScore: int
    +isCorrect: boolean
  }

  class GradingRecord {
    +id: Long
    +answerId: Long
    +gradingMode: int
    +score: int
    +detailJson: String
    +llmCallId: Long
    +confidence: double
    +needsReview: boolean
    +reviewerId: Long
    +isFinal: boolean
  }

  class LlmCall {
    +id: Long
    +bizType: int
    +bizId: Long
    +modelName: String
    +promptText: String
    +responseText: String
    +responseJson: String
    +callStatus: int
  }

  class Appeal {
    +id: Long
    +answerId: Long
    +userId: Long
    +reasonText: String
    +appealStatus: int
    +handledBy: Long
    +finalScore: int
  }

  class TagMastery {
    +userId: Long
    +tagId: Long
    +masteryValue: double
    +correctCount: int
    +attemptCount: int
  }

  class UserAbility {
    +userId: Long
    +abilityScore: int
    +updatedAt: DateTime
  }

  class QuestionUserStat {
    +userId: Long
    +questionId: Long
    +attemptCount: int
    +correctCount: int
    +lastAttemptAt: DateTime
  }

  class WrongQuestion {
    +userId: Long
    +questionId: Long
    +wrongCount: int
    +lastWrongAt: DateTime
    +isResolved: boolean
  }

  class Post {
    +id: Long
    +authorId: Long
    +title: String
    +content: String
    +relatedQuestionId: Long
    +postStatus: int
  }

  class Reply {
    +id: Long
    +postId: Long
    +authorId: Long
    +parentReplyId: Long
    +content: String
  }
}

package "service" {

  interface AuthService {
    +login(username:String, password:String): String
    +parseToken(token:String): User
    +checkPermission(userId:Long, perm:String): boolean
  }

  interface TagService {
    +createTag(tag:Tag): Long
    +updateTag(tag:Tag): void
    +deleteTag(tagId:Long): void
    +listTags(): List<Tag>
  }

  interface QuestionService {
    +createQuestion(q:Question, options:List<QuestionOption>, tagIds:List<Long>): Long
    +updateQuestion(q:Question, options:List<QuestionOption>, tagIds:List<Long>): void
    +publishQuestion(questionId:Long): void
    +searchQuestions(criteriaJson:String, page:int, size:int): Page<Question>
    +generateAnalysisByLlm(questionId:Long): LlmCall
  }

  interface PaperService {
    +createPaper(p:Paper): Long
    +addQuestion(paperId:Long, questionId:Long, score:int, orderNo:int): void
    +removeQuestion(paperQuestionId:Long): void
    +calculateTotalScore(paperId:Long): int
  }

  interface AssignmentService {
    +publishAssignment(a:Assignment): Long
    +closeAssignment(assignmentId:Long): void
    +checkStartAllowed(assignmentId:Long, userId:Long): boolean
  }

  interface AttemptService {
    +startAssignmentAttempt(assignmentId:Long, userId:Long): Attempt
    +startPracticeAttempt(scopeJson:String, userId:Long): Attempt
    +saveAnswerDraft(answer:Answer): void
    +submitAttempt(attemptId:Long): void
  }

  interface GradingService {
    +autoGradeAttempt(attemptId:Long): void
    +requestLlmGrade(answerId:Long): LlmCall
    +finalizeManualGrade(answerId:Long, score:int, comment:String): void
    +markNeedReview(answerId:Long, reason:String): void
  }

  interface AdaptiveService {
    +generateAdaptiveAttempt(scopeJson:String, userId:Long): Attempt
    +updateMasteryAndAbility(attemptId:Long): void
    +computeQuestionWeight(userId:Long, questionId:Long): double
  }

  interface AppealService {
    +submitAppeal(answerId:Long, userId:Long, reason:String): Long
    +handleAppeal(appealId:Long, teacherId:Long, finalScore:int, comment:String): void
  }

  interface ForumService {
    +createPost(p:Post, tagIds:List<Long>): Long
    +reply(postId:Long, r:Reply): Long
    +listPosts(tagId:Long, page:int, size:int): Page<Post>
  }
}

' -----------------------
' relationships
' -----------------------
User "many" -- "many" Role
Role "many" -- "many" Permission

Question "1" -- "many" QuestionOption
Question "many" -- "many" Tag
Paper "1" -- "many" PaperQuestion
PaperQuestion --> Question

Assignment --> Paper
Attempt --> Assignment
Attempt "1" -- "many" AttemptQuestion
AttemptQuestion --> Question
Attempt "1" -- "many" Answer
Answer --> AttemptQuestion
Answer "1" -- "many" GradingRecord
GradingRecord --> LlmCall
Answer "0..1" -- "many" Appeal

TagMastery --> User
TagMastery --> Tag
UserAbility --> User
QuestionUserStat --> User
QuestionUserStat --> Question
WrongQuestion --> User
WrongQuestion --> Question

Post --> User
Post "1" -- "many" Reply
Reply --> User

' service dependencies (simplified)
AuthService ..> User
QuestionService ..> Question
PaperService ..> Paper
AttemptService ..> Attempt
GradingService ..> GradingRecord
AdaptiveService ..> TagMastery
ForumService ..> Post
AppealService ..> Appeal

@enduml