export const ROLE_OPTIONS = [
  { value: 'STUDENT', label: '学生' },
  { value: 'TEACHER', label: '教师' },
  { value: 'ADMIN', label: '管理员' },
]

export const QUESTION_TYPE_OPTIONS = [
  { value: 1, label: '单选题' },
  { value: 2, label: '多选题' },
  { value: 3, label: '判断题' },
  { value: 4, label: '填空题' },
  { value: 6, label: '编程题' },
]

export const QUESTION_STATUS_OPTIONS = [
  { value: 1, label: '草稿', type: 'info' },
  { value: 2, label: '已发布', type: 'success' },
  { value: 3, label: '已归档', type: 'warning' },
]

export const ASSIGNMENT_STATUS_OPTIONS = [
  { value: 1, label: '草稿', type: 'info' },
  { value: 2, label: '已发布', type: 'success' },
  { value: 3, label: '已关闭', type: 'danger' },
]

export const ATTEMPT_TYPE_OPTIONS = [
  { value: 1, label: '作业' },
  { value: 2, label: '练习' },
]

export const ATTEMPT_STATUS_OPTIONS = [
  { value: 1, label: '进行中', type: 'warning' },
  { value: 2, label: '已提交', type: 'info' },
  { value: 3, label: '批改中', type: 'warning' },
  { value: 4, label: '已完成', type: 'success' },
]

export const ANSWER_STATUS_OPTIONS = [
  { value: 1, label: '草稿', type: 'info' },
  { value: 2, label: '已提交', type: 'success' },
]

export const APPEAL_STATUS_OPTIONS = [
  { value: 1, label: '待处理', type: 'warning' },
  { value: 2, label: '已通过', type: 'success' },
  { value: 3, label: '已驳回', type: 'danger' },
  { value: 4, label: '已解决', type: 'success' },
]

export const GRADING_MODE_OPTIONS = [
  { value: 1, label: '自动' },
  { value: 2, label: 'LLM' },
  { value: 3, label: '人工' },
]

export const TAG_TYPE_OPTIONS = [
  { value: 1, label: '知识点' },
  { value: 2, label: '章节' },
  { value: 3, label: '自定义' },
]

export const PAPER_TYPE_OPTIONS = [
  { value: 1, label: '作业' },
  { value: 2, label: '考试' },
  { value: 3, label: '练习模板' },
]

export const CHAPTER_OPTIONS = [
  { value: '基础语法', label: '基础语法' },
  { value: '字符串处理', label: '字符串处理' },
  { value: '数组与矩阵', label: '数组与矩阵' },
  { value: '函数与递归', label: '函数与递归' },
  { value: '指针基础', label: '指针基础' },
  { value: '数据结构基础', label: '数据结构基础' },
  { value: '文件输入输出', label: '文件输入输出' },
  { value: '综合应用', label: '综合应用' },
]

export const BOOL_OPTIONS = [
  { value: 1, label: '是' },
  { value: 0, label: '否' },
]

export function labelBy(options, value, fallback = '-') {
  const item = options.find((opt) => opt.value === value)
  return item ? item.label : fallback
}

export function typeBy(options, value, fallback = 'info') {
  const item = options.find((opt) => opt.value === value)
  return item?.type || fallback
}
