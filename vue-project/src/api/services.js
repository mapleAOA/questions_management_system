import { request } from './http'

function clean(params = {}) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}

export const authApi = {
  register(payload) {
    return request({
      url: '/api/register',
      method: 'post',
      data: payload,
    })
  },
  login(payload) {
    return request({
      url: '/api/login',
      method: 'post',
      data: payload,
    })
  },
  logout() {
    return request({
      url: '/api/logout',
      method: 'post',
    })
  },
  me() {
    return request({
      url: '/api/auth/me',
      method: 'get',
    })
  },
}

export const tagApi = {
  list(keyword) {
    return request({
      url: '/api/tags',
      method: 'get',
      params: clean({ keyword }),
    })
  },
  tree() {
    return request({
      url: '/api/tags/tree',
      method: 'get',
    })
  },
  create(payload) {
    return request({
      url: '/api/tags',
      method: 'post',
      data: payload,
    })
  },
  update(tagId, payload) {
    return request({
      url: `/api/tags/${tagId}`,
      method: 'put',
      data: payload,
    })
  },
  remove(tagId) {
    return request({
      url: `/api/tags/${tagId}`,
      method: 'delete',
    })
  },
}

export const questionApi = {
  search(params) {
    return request({
      url: '/api/questions',
      method: 'get',
      params: clean(params),
    })
  },
  detail(questionId) {
    return request({
      url: `/api/questions/${questionId}`,
      method: 'get',
    })
  },
  create(payload) {
    return request({
      url: '/api/questions',
      method: 'post',
      data: payload,
    })
  },
  update(questionId, payload) {
    return request({
      url: `/api/questions/${questionId}`,
      method: 'put',
      data: payload,
    })
  },
  remove(questionId) {
    return request({
      url: `/api/questions/${questionId}`,
      method: 'delete',
    })
  },
  publish(questionId) {
    return request({
      url: `/api/questions/${questionId}/publish`,
      method: 'post',
    })
  },
  generateLlmAnalysis(questionId) {
    return request({
      url: `/api/questions/${questionId}/analysis/llm`,
      method: 'post',
      timeout: 8000,
    })
  },
}

export const paperApi = {
  page(page = 1, size = 20) {
    return request({
      url: '/api/papers',
      method: 'get',
      params: { page, size },
    })
  },
  detail(paperId) {
    return request({
      url: `/api/papers/${paperId}`,
      method: 'get',
    })
  },
  create(payload) {
    return request({
      url: '/api/papers',
      method: 'post',
      data: payload,
    })
  },
  update(paperId, payload) {
    return request({
      url: `/api/papers/${paperId}`,
      method: 'put',
      data: payload,
    })
  },
  remove(paperId) {
    return request({
      url: `/api/papers/${paperId}`,
      method: 'delete',
    })
  },
  addQuestion(paperId, payload) {
    return request({
      url: `/api/papers/${paperId}/questions`,
      method: 'post',
      data: payload,
    })
  },
  batchUpdateQuestions(paperId, payload) {
    return request({
      url: `/api/papers/${paperId}/questions/batch`,
      method: 'put',
      data: payload,
    })
  },
  updatePaperQuestion(paperQuestionId, payload) {
    return request({
      url: `/api/papers/questions/${paperQuestionId}`,
      method: 'put',
      data: payload,
    })
  },
  removePaperQuestion(paperQuestionId) {
    return request({
      url: `/api/papers/questions/${paperQuestionId}`,
      method: 'delete',
    })
  },
  recalculate(paperId) {
    return request({
      url: `/api/papers/${paperId}/recalculate`,
      method: 'post',
    })
  },
}

export const assignmentApi = {
  page(page = 1, size = 20) {
    return request({
      url: '/api/assignments',
      method: 'get',
      params: { page, size },
    })
  },
  my(status, page = 1, size = 10) {
    return request({
      url: '/api/assignments/my',
      method: 'get',
      params: clean({ status, page, size }),
    })
  },
  detail(assignmentId) {
    return request({
      url: `/api/assignments/${assignmentId}`,
      method: 'get',
    })
  },
  create(payload) {
    return request({
      url: '/api/assignments',
      method: 'post',
      data: payload,
    })
  },
  update(assignmentId, payload) {
    return request({
      url: `/api/assignments/${assignmentId}`,
      method: 'put',
      data: payload,
    })
  },
  remove(assignmentId) {
    return request({
      url: `/api/assignments/${assignmentId}`,
      method: 'delete',
    })
  },
  publish(assignmentId) {
    return request({
      url: `/api/assignments/${assignmentId}/publish`,
      method: 'post',
    })
  },
  close(assignmentId) {
    return request({
      url: `/api/assignments/${assignmentId}/close`,
      method: 'post',
    })
  },
  setTargets(assignmentId, payload) {
    return request({
      url: `/api/assignments/${assignmentId}/targets`,
      method: 'put',
      data: payload,
    })
  },
}

export const classApi = {
  create(payload) {
    return request({
      url: '/api/classes',
      method: 'post',
      data: payload,
    })
  },
  mine() {
    return request({
      url: '/api/classes/mine',
      method: 'get',
    })
  },
  students(classId) {
    return request({
      url: `/api/classes/${classId}/students`,
      method: 'get',
    })
  },
  kickStudent(classId, studentId) {
    return request({
      url: `/api/classes/${classId}/students/${studentId}`,
      method: 'delete',
    })
  },
  join(classCode) {
    return request({
      url: '/api/classes/join',
      method: 'post',
      data: { classCode },
    })
  },
  my() {
    return request({
      url: '/api/classes/my',
      method: 'get',
    })
  },
}

export const attemptApi = {
  startAssignment(assignmentId) {
    return request({
      url: `/api/attempts/assignment/${assignmentId}/start`,
      method: 'post',
    })
  },
  startPractice(payload) {
    return request({
      url: '/api/attempts/practice/start',
      method: 'post',
      data: payload,
    })
  },
  questions(attemptId) {
    return request({
      url: `/api/attempts/${attemptId}/questions`,
      method: 'get',
    })
  },
  submit(attemptId) {
    return request({
      url: `/api/attempts/${attemptId}/submit`,
      method: 'post',
      timeout: 8000,
    })
  },
  result(attemptId) {
    return request({
      url: `/api/attempts/${attemptId}/result`,
      method: 'get',
    })
  },
  my(attemptType, page = 1, size = 20) {
    return request({
      url: '/api/attempts/my',
      method: 'get',
      params: clean({ attemptType, page, size }),
    })
  },
}

export const answerApi = {
  saveDraft(answerId, answerContent) {
    return request({
      url: `/api/answers/${answerId}/draft`,
      method: 'put',
      data: { answerContent },
    })
  },
  submit(answerId, answerContent) {
    return request({
      url: `/api/answers/${answerId}/submit`,
      method: 'put',
      data: { answerContent },
    })
  },
}

export const statsApi = {
  wrongQuestions(params) {
    return request({
      url: '/api/stats/wrong-questions',
      method: 'get',
      params: clean(params),
    })
  },
  resolveWrongQuestion(questionId) {
    return request({
      url: `/api/stats/wrong-questions/${questionId}/resolve`,
      method: 'post',
    })
  },
  mastery(tagType = 1) {
    return request({
      url: '/api/stats/mastery',
      method: 'get',
      params: { tagType },
    })
  },
  ability() {
    return request({
      url: '/api/stats/ability',
      method: 'get',
    })
  },
  questionStats(params) {
    return request({
      url: '/api/stats/question-stats',
      method: 'get',
      params: clean(params),
    })
  },
}

export const appealApi = {
  create(payload) {
    return request({
      url: '/api/appeals',
      method: 'post',
      data: payload,
    })
  },
  my(params) {
    return request({
      url: '/api/appeals/my',
      method: 'get',
      params: clean(params),
    })
  },
}

export const teacherApi = {
  reviewAnswers(params) {
    return request({
      url: '/api/teacher/review/answers',
      method: 'get',
      params: clean(params),
    })
  },
  answerEvidence(answerId) {
    return request({
      url: `/api/teacher/answers/${answerId}/evidence`,
      method: 'get',
    })
  },
  manualGrade(answerId, payload) {
    return request({
      url: `/api/teacher/answers/${answerId}/grade`,
      method: 'post',
      data: payload,
    })
  },
  llmRetry(answerId, payload) {
    return request({
      url: `/api/teacher/answers/${answerId}/llm-retry`,
      method: 'post',
      data: payload,
    })
  },
  assignmentScores(assignmentId, page = 1, size = 10) {
    return request({
      url: `/api/teacher/assignments/${assignmentId}/scores`,
      method: 'get',
      params: { page, size },
    })
  },
  appeals(params) {
    return request({
      url: '/api/teacher/appeals',
      method: 'get',
      params: clean(params),
    })
  },
  handleAppeal(appealId, payload) {
    return request({
      url: `/api/teacher/appeals/${appealId}/handle`,
      method: 'post',
      data: payload,
    })
  },
}

export const llmApi = {
  page(params) {
    return request({
      url: '/api/llm/calls',
      method: 'get',
      params: clean(params),
    })
  },
  detail(llmCallId) {
    return request({
      url: `/api/llm/calls/${llmCallId}`,
      method: 'get',
    })
  },
}

export const adminApi = {
  pageUsers(page = 1, size = 20) {
    return request({
      url: '/api/admin/users',
      method: 'get',
      params: { page, size },
    })
  },
  createUser(payload) {
    return request({
      url: '/api/admin/users',
      method: 'post',
      data: payload,
    })
  },
  updateUser(userId, payload) {
    return request({
      url: `/api/admin/users/${userId}`,
      method: 'put',
      data: payload,
    })
  },
  updateUserRole(userId, role) {
    return request({
      url: `/api/admin/users/${userId}/role`,
      method: 'put',
      data: { role },
    })
  },
  loginLogs(params) {
    return request({
      url: '/api/admin/login-logs',
      method: 'get',
      params: clean(params),
    })
  },
}
