用例图
@startuml
left to right direction
actor 学生 as Student
actor 教师 as Teacher
actor 管理员 as Admin

rectangle "题库管理系统" {
  usecase "注册/登录/退出" as UC_Auth
  usecase "加入班级/查看我的班级" as UC_JoinClass
  usecase "题库检索与练习" as UC_Practice
  usecase "开始作业并作答" as UC_AssignmentDo
  usecase "保存草稿/提交答案" as UC_Answer
  usecase "查看作答结果与记录" as UC_Result
  usecase "错题/掌握度/能力统计" as UC_Stats
  usecase "提交申诉/查看申诉" as UC_AppealStudent

  usecase "标签管理" as UC_Tag
  usecase "题目管理与发布" as UC_Question
  usecase "LLM 生成题目解析" as UC_QuestionLlm
  usecase "试卷管理与组卷" as UC_Paper
  usecase "作业管理与目标配置" as UC_AssignmentManage
  usecase "班级管理" as UC_ClassManage
  usecase "复核中心评分/LLM重试" as UC_Review
  usecase "处理申诉" as UC_AppealTeacher
  usecase "查看 LLM 调用记录" as UC_LlmLog

  usecase "用户管理" as UC_AdminUser
  usecase "审计日志/登录日志" as UC_AdminLog
}

Student --> UC_Auth
Student --> UC_JoinClass
Student --> UC_Practice
Student --> UC_AssignmentDo
Student --> UC_Answer
Student --> UC_Result
Student --> UC_Stats
Student --> UC_AppealStudent

Teacher --> UC_Auth
Teacher --> UC_Tag
Teacher --> UC_Question
Teacher --> UC_QuestionLlm
Teacher --> UC_Paper
Teacher --> UC_AssignmentManage
Teacher --> UC_ClassManage
Teacher --> UC_Review
Teacher --> UC_AppealTeacher
Teacher --> UC_LlmLog

Admin --> UC_Auth
Admin --> UC_Tag
Admin --> UC_Question
Admin --> UC_Paper
Admin --> UC_AssignmentManage
Admin --> UC_ClassManage
Admin --> UC_Review
Admin --> UC_AppealTeacher
Admin --> UC_LlmLog
Admin --> UC_AdminUser
Admin --> UC_AdminLog
@enduml


ER图
@startuml
hide circle
skinparam linetype ortho

entity "sys_user" as sys_user {
  *id : bigint
  --
  username
  password_hash
  status
}
entity "sys_role" as sys_role {
  *id : bigint
  --
  role_code
}
entity "sys_user_role" as sys_user_role {
  *user_id : bigint
  *role_id : bigint
}
entity "sys_audit_log" as sys_audit_log {
  *id : bigint
  user_id
  action
}
entity "sys_login_log" as sys_login_log {
  *id : bigint
  user_id
  username
}

entity "qb_tag" as qb_tag {
  *id : bigint
  tag_name
  parent_id
  tag_type
}
entity "qb_question" as qb_question {
  *id : bigint
  created_by
  analysis_llm_call_id
  question_type
  status
}
entity "qb_question_option" as qb_question_option {
  *id : bigint
  question_id
}
entity "qb_question_case" as qb_question_case {
  *id : bigint
  question_id
  case_no
}
entity "qb_question_tag" as qb_question_tag {
  *question_id : bigint
  *tag_id : bigint
}
entity "qb_llm_call" as qb_llm_call {
  *id : bigint
  biz_type
  biz_id
  call_status
}

entity "qb_paper" as qb_paper {
  *id : bigint
  creator_id
}
entity "qb_paper_question" as qb_paper_question {
  *id : bigint
  paper_id
  question_id
}
entity "qb_assignment" as qb_assignment {
  *id : bigint
  paper_id
  created_by
  publish_status
}
entity "qb_assignment_target" as qb_assignment_target {
  *assignment_id : bigint
  *user_id : bigint
}
entity "qb_assignment_target_class" as qb_assignment_target_class {
  *assignment_id : bigint
  *class_id : bigint
}
entity "qb_class" as qb_class {
  *id : bigint
  teacher_id
  class_code
}
entity "qb_class_member" as qb_class_member {
  *class_id : bigint
  *student_id : bigint
}

entity "qb_attempt" as qb_attempt {
  *id : bigint
  assignment_id
  paper_id
  user_id
  status
}
entity "qb_attempt_question" as qb_attempt_question {
  *id : bigint
  attempt_id
  question_id
}
entity "qb_answer" as qb_answer {
  *id : bigint
  attempt_id
  attempt_question_id
  question_id
  user_id
}
entity "qb_grading_record" as qb_grading_record {
  *id : bigint
  answer_id
  llm_call_id
  reviewer_id
}
entity "qb_appeal" as qb_appeal {
  *id : bigint
  answer_id
  user_id
  handled_by
}

entity "qb_tag_mastery" as qb_tag_mastery {
  *user_id : bigint
  *tag_id : bigint
}
entity "qb_user_ability" as qb_user_ability {
  *user_id : bigint
}
entity "qb_question_user_stat" as qb_question_user_stat {
  *user_id : bigint
  *question_id : bigint
}
entity "qb_wrong_question" as qb_wrong_question {
  *user_id : bigint
  *question_id : bigint
}

sys_user ||--o{ sys_user_role
sys_role ||--o{ sys_user_role
sys_user ||--o{ sys_audit_log
sys_user ||--o{ sys_login_log

sys_user ||--o{ qb_question : created_by
qb_llm_call ||--o{ qb_question : analysis
qb_question ||--o{ qb_question_option
qb_question ||--o{ qb_question_case
qb_question ||--o{ qb_question_tag
qb_tag ||--o{ qb_question_tag

sys_user ||--o{ qb_paper : creator
qb_paper ||--o{ qb_paper_question
qb_question ||--o{ qb_paper_question
qb_paper ||--o{ qb_assignment
sys_user ||--o{ qb_assignment : creator
qb_assignment ||--o{ qb_assignment_target
sys_user ||--o{ qb_assignment_target

sys_user ||--o{ qb_class : teacher
qb_class ||--o{ qb_class_member
sys_user ||--o{ qb_class_member : student
qb_assignment ||--o{ qb_assignment_target_class
qb_class ||--o{ qb_assignment_target_class

qb_assignment ||--o{ qb_attempt
qb_paper ||--o{ qb_attempt
sys_user ||--o{ qb_attempt
qb_attempt ||--o{ qb_attempt_question
qb_question ||--o{ qb_attempt_question
qb_attempt ||--o{ qb_answer
qb_attempt_question ||--|| qb_answer
qb_question ||--o{ qb_answer
sys_user ||--o{ qb_answer

qb_answer ||--o{ qb_grading_record
qb_llm_call ||--o{ qb_grading_record
sys_user ||--o{ qb_grading_record : reviewer
qb_answer ||--o{ qb_appeal
sys_user ||--o{ qb_appeal : creator/handler

sys_user ||--o{ qb_tag_mastery
qb_tag ||--o{ qb_tag_mastery
sys_user ||--|| qb_user_ability
sys_user ||--o{ qb_question_user_stat
qb_question ||--o{ qb_question_user_stat
sys_user ||--o{ qb_wrong_question
qb_question ||--o{ qb_wrong_question
@enduml


类图（领域模型）
@startuml
skinparam classAttributeIconSize 0

class SysUser {
  +id: Long
  +username: String
  +status: Integer
}
class SysRole {
  +id: Long
  +roleCode: String
}
class QbTag {
  +id: Long
  +tagName: String
  +tagType: Integer
}
class QbQuestion {
  +id: Long
  +questionType: Integer
  +difficulty: Integer
  +status: Integer
}
class QbQuestionOption {
  +id: Long
  +questionId: Long
}
class QbQuestionCase {
  +id: Long
  +questionId: Long
}
class QbPaper {
  +id: Long
  +paperTitle: String
}
class QbPaperQuestion {
  +id: Long
  +paperId: Long
  +questionId: Long
  +score: Integer
}
class QbAssignment {
  +id: Long
  +paperId: Long
  +publishStatus: Integer
}
class QbClass {
  +id: Long
  +teacherId: Long
}
class QbAttempt {
  +id: Long
  +assignmentId: Long
  +userId: Long
  +status: Integer
}
class QbAttemptQuestion {
  +id: Long
  +attemptId: Long
  +questionId: Long
}
class QbAnswer {
  +id: Long
  +attemptId: Long
  +attemptQuestionId: Long
  +finalScore: Integer
}
class QbGradingRecord {
  +id: Long
  +answerId: Long
  +gradingMode: Integer
}
class QbAppeal {
  +id: Long
  +answerId: Long
  +appealStatus: Integer
}
class QbLlmCall {
  +id: Long
  +bizType: Integer
  +callStatus: Integer
}
class QbWrongQuestion {
  +userId: Long
  +questionId: Long
}
class QbTagMastery {
  +userId: Long
  +tagId: Long
  +masteryValue: Double
}
class QbUserAbility {
  +userId: Long
  +abilityScore: Integer
}

SysUser "many" -- "many" SysRole
QbQuestion "1" -- "many" QbQuestionOption
QbQuestion "1" -- "many" QbQuestionCase
QbQuestion "many" -- "many" QbTag
QbPaper "1" -- "many" QbPaperQuestion
QbPaperQuestion --> QbQuestion
QbAssignment --> QbPaper
QbClass --> SysUser : teacher
QbAttempt --> QbAssignment
QbAttempt --> SysUser
QbAttempt "1" -- "many" QbAttemptQuestion
QbAttemptQuestion --> QbQuestion
QbAnswer --> QbAttemptQuestion
QbAnswer --> QbAttempt
QbAnswer "1" -- "many" QbGradingRecord
QbGradingRecord --> QbLlmCall
QbAppeal --> QbAnswer
QbTagMastery --> QbTag
QbTagMastery --> SysUser
QbUserAbility --> SysUser
QbWrongQuestion --> SysUser
QbWrongQuestion --> QbQuestion
@enduml


类图（后端分层）
@startuml
skinparam classAttributeIconSize 0

package "controller" {
  class AuthController
  class TagController
  class QuestionController
  class PaperController
  class AssignmentController
  class ClassController
  class AttemptController
  class AnswerController
  class StatsController
  class AppealController
  class TeacherReviewController
  class LlmCallController
  class AdminUserController
  class AdminLogController
}

package "service" {
  interface AuthService
  interface TagService
  interface QuestionService
  interface PaperService
  interface AssignmentService
  interface ClassService
  interface AttemptService
  interface StatsService
  interface AppealService
  interface TeacherReviewService
  interface LlmCallQueryService
  interface AdminLogService
}

package "mapper" {
  class SysUserMapper
  class SysUserRoleMapper
  class QbTagMapper
  class QbQuestionMapper
  class QbQuestionOptionMapper
  class QbQuestionCaseMapper
  class QbQuestionTagMapper
  class QbPaperMapper
  class QbPaperQuestionMapper
  class QbAssignmentMapper
  class QbAssignmentTargetMapper
  class QbAssignmentTargetClassMapper
  class QbClassMapper
  class QbClassMemberMapper
  class QbAttemptMapper
  class QbAttemptQuestionMapper
  class QbAnswerMapper
  class QbGradingRecordMapper
  class QbAppealMapper
  class QbLlmCallMapper
  class QbWrongQuestionMapper
  class QbTagMasteryMapper
  class QbUserAbilityMapper
  class QbQuestionUserStatMapper
  class SysAuditLogMapper
  class SysLoginLogMapper
}

package "entity" {
  class SysUser
  class QbQuestion
  class QbPaper
  class QbAssignment
  class QbAttempt
  class QbAnswer
  class QbAppeal
  class QbLlmCall
}

AuthController --> AuthService
TagController --> TagService
QuestionController --> QuestionService
PaperController --> PaperService
AssignmentController --> AssignmentService
ClassController --> ClassService
AttemptController --> AttemptService
AnswerController --> AttemptService
StatsController --> StatsService
AppealController --> AppealService
TeacherReviewController --> TeacherReviewService
TeacherReviewController --> AppealService
LlmCallController --> LlmCallQueryService
AdminUserController --> AuthService
AdminLogController --> AdminLogService

AuthService ..> SysUserMapper
AuthService ..> SysUserRoleMapper
TagService ..> QbTagMapper
QuestionService ..> QbQuestionMapper
QuestionService ..> QbQuestionOptionMapper
QuestionService ..> QbQuestionCaseMapper
QuestionService ..> QbQuestionTagMapper
PaperService ..> QbPaperMapper
PaperService ..> QbPaperQuestionMapper
PaperService ..> QbQuestionMapper
AssignmentService ..> QbAssignmentMapper
AssignmentService ..> QbAssignmentTargetMapper
AssignmentService ..> QbAssignmentTargetClassMapper
ClassService ..> QbClassMapper
ClassService ..> QbClassMemberMapper
AttemptService ..> QbAttemptMapper
AttemptService ..> QbAttemptQuestionMapper
AttemptService ..> QbAnswerMapper
AttemptService ..> QbGradingRecordMapper
AttemptService ..> QbQuestionMapper
StatsService ..> QbWrongQuestionMapper
StatsService ..> QbTagMasteryMapper
StatsService ..> QbUserAbilityMapper
StatsService ..> QbQuestionUserStatMapper
AppealService ..> QbAppealMapper
AppealService ..> QbAnswerMapper
TeacherReviewService ..> QbGradingRecordMapper
TeacherReviewService ..> QbLlmCallMapper
LlmCallQueryService ..> QbLlmCallMapper
AdminLogService ..> SysAuditLogMapper
AdminLogService ..> SysLoginLogMapper

SysUserMapper --> SysUser
QbQuestionMapper --> QbQuestion
QbPaperMapper --> QbPaper
QbAssignmentMapper --> QbAssignment
QbAttemptMapper --> QbAttempt
QbAnswerMapper --> QbAnswer
QbAppealMapper --> QbAppeal
QbLlmCallMapper --> QbLlmCall
@enduml
