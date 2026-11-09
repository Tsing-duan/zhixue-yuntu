import request, { post, get } from "@/utils/request";

//-------------------------------登陆---------------------------------------
// 学生入口登录；教师及管理员只能从管理端进入
export const login = (params) => post("/login/student",params)
//登出
export const logout = () => get("/login/logout")
//用户注册
export const register = (params) => post("/login/register",params)
//获取登陆用户信息
export const getUser = () => get("/user/getUserInfo")
//获取学校列表
export const getApeSchoolList = () => get("/school/getApeSchoolList")
//获取专业列表
export const getApeMajorList = () => get("/major/getApeMajorList")
//主页数据
export const getIndexAchievement = () => get("/index/getIndexAchievement")
//主页获取笔记
export const getIndexArticleList = () => get("/article/getIndexArticleList")
//分页获取课程
export const getApeTaskPage = (params) => post("/task/getApeTaskPage",params)
//获取分类列表 
export const getApeClassificationList = () => get("/classification/getApeClassificationList")
//获取教师分页
export const getTeacherPage = (params) => post("/portal/teachers",params)
//分页获取笔记
export const getApeArticlePage = (params) => post("/article/getApeArticlePage",params)
//分页获取公告
export const getNoticePage = (params) => post("/portal/notices",params)
//分页获取留言
export const getApeMessagePage = (params) => post("/message/getApeMessagePage",params)
//保存留言
export const saveApeMessage = (params) => post("/message/saveApeMessage",params)
//获取个人信息
export const getUserInfo = () => get("/user/getUserInfo")
//保存个人信息
export const setUserInfo = (params) => post("/user/setUserInfo",params)
//保存头像
export const setUserAvatar = (params) => post("/user/setUserAvatar",params)
//修改密码
export const changePassword = (params) => post("/user/changePassword",params)
//获取课程信息
export const getApeTaskById = (params) => get("/task/getApeTaskById",params)
//保存课程报名
export const saveApeTaskStudent = (params) => post("/student/saveApeTaskStudent",params)
//获取用户报名情况
export const getTaskStudent = (params) => get("/student/getTaskStudent",params)
//根据课程id获取章节
export const getApeChapterByTaskId = (params) => get("/chapter/getApeChapterByTaskId",params)
//保存课程评论
export const saveApeTaskComment = (params) => post("/comment/saveApeTaskComment",params)
//获取课程评论
export const getApeTaskCommentListByTaskId = (params) => get("/comment/getApeTaskCommentListByTaskId",params)
//获取教师课程列表
export const getApeTaskByTeacher = (params) => get("/task/getApeTaskByTeacher",params)
//获取教师详情
export const getTeacherById = (params) => get("/portal/teachers/" + encodeURIComponent(params.id))
//获取笔记详情
export const getApeArticleById = (params) => get("/article/getApeArticleById",params)
//保存笔记评论
export const saveApeArticleComment = (params) => post("/articleComment/saveApeArticleComment",params)
//获取笔记评论
export const getApeArticleCommentByArticleId = (params) => get("/articleComment/getApeArticleCommentByArticleId",params)
//查询笔记收藏
export const getApeArticleFavorPage = (params) => post("/favor/getApeArticleFavorPage",params)
//根据id查询笔记收藏
export const getApeArticleFavorById = (params) => get("/favor/getApeArticleFavorById",params)
//保存笔记收藏
export const saveApeArticleFavor = (params) => post("/favor/saveApeArticleFavor",params)
//更新笔记收藏
export const editApeArticleFavor = (params) => post("/favor/editApeArticleFavor",params)
//删除ApeArticleFavor
export const removeApeArticleFavor = (params) => post("/favor/removeApeArticleFavor",params)
//我的课程
export const getApeMyTaskPage = (params) => post("/student/getApeMyTaskPage",params)
//我的课程列表 
export const getApeMyTaskList = (params) => post("/student/getApeMyTaskList",params)
//保存笔记
export const saveApeArticle = (params) => post("/article/saveApeArticle",params)
//更新笔记
export const updateApeArticle = (params) => post("/article/editApeArticle",params)
//根据id获取章节
export const getApeChapterById = (params) => get("/chapter/getApeChapterById",params)
//查询学生作业
export const getApeHomeworkStudentPage = (params) => post("/student/getApeHomeworkStudentPage",params)
//根据id查询学生作业
export const getApeHomeworkStudentById = (params) => get("/student/getApeHomeworkStudentById",params)
//保存学生作业
export const saveApeHomeworkStudent = (params) => post("/student/saveApeHomeworkStudent",params)
//更新学生作业
export const editApeHomeworkStudent = (params) => post("/student/editApeHomeworkStudent",params)
//删除ApeHomeworkStudent
export const removeApeHomeworkStudent = (params) => get("/student/removeApeHomeworkStudent",params)
//获取作业题目
export const getApeHomeworkStudentList = (params) => post("/student/getApeHomeworkStudentList",params)
//获取章节作业数
export const getApeHomeworkByChapterId = (params) => get("/homework/getApeHomeworkByChapterId",params)
//获取作业完成标志
export const getApeHomeworkStudentFlag = (params) => get("/student/getApeHomeworkStudentFlag",params)
//获取我的作业
export const getMyApeHomework = (params) => get("/student/getMyApeHomework",params)
//获取错题
export const getWrongWork = (params) => get("/student/getWrongWork",params)
//保存章节视频是否观看
export const saveApeChapterVideo = (params) => post("/chapterVideo/saveApeChapterVideo",params)
//后去考试列表
export const getTestListByUser = (params) => get("/test/getTestListByUser",params)
//查询用户考试题目
export const getApeTestStudentPage = (params) => post("/student/getApeTestStudentPage",params)
//根据id查询用户考试题目
export const getApeTestStudentById = (params) => get("/student/getApeTestStudentById",params)
//更新用户考试题目
export const editApeTestStudent = (params) => post("/student/editApeTestStudent",params)
//删除ApeTestStudent
export const removeApeTestStudent = (params) => get("/student/removeApeTestStudent",params)
//获取考试题目
export const getApeTestItemByTestId = (params) => get("/item/getApeTestItemByTestId",params)
//保存考试内容
export const saveApeTestStudent = (params) => post("/student/saveApeTestStudent",params)
//获取考试状态
export const getTestUserState = (params) => get("/student/getTestUserState",params)
export const getTaskChapterStudy = (params) => get("/chapter/getTaskChapterStudy",params)

//-------------------------------论坛---------------------------------------
//查询论坛
export const getApeForumPage = (params) => post("/forum/getApeForumPage",params)
//根据id查询论坛
export const getApeForumById = (params) => get("/forum/getApeForumById",params)
//保存论坛
export const saveApeForum = (params) => post("/forum/saveApeForum",params)
//更新论坛
export const editApeForum = (params) => post("/forum/editApeForum",params)
//删除ApeForum
export const removeApeForum = (params) => get("/forum/removeApeForum",params)

//-------------------------------论坛讨论---------------------------------------
//查询论坛讨论
export const getApeForumItemPage = (params) => post("/item/getApeForumItemPage",params)
export const getApeForumItemList = (params) => post("/item/getApeForumItemList",params)
//根据id查询论坛讨论
export const getApeForumItemById = (params) => get("/item/getApeForumItemById",params)
//保存论坛讨论
export const saveApeForumItem = (params) => post("/item/saveApeForumItem",params)
//更新论坛讨论
export const editApeForumItem = (params) => post("/item/editApeForumItem",params)
//删除ApeForumItem
export const removeApeForumItem = (params) => get("/item/removeApeForumItem",params)

//-------------------------------答疑---------------------------------------
//查询答疑
export const getApeQuestionPage = (params) => post("/question/getApeQuestionPage",params)
//根据id查询答疑
export const getApeQuestionById = (params) => get("/question/getApeQuestionById",params)
//保存答疑
export const saveApeQuestion = (params) => post("/question/saveApeQuestion",params)
//更新答疑
export const editApeQuestion = (params) => post("/question/editApeQuestion",params)
//删除ApeQuestion
export const removeApeQuestion = (params) => get("/question/removeApeQuestion",params)

export const getApeTaskFavorById = (params) => get("/favor/getApeTaskFavorById",params)

export const removeApeTaskFavor = (params) => get("/favor/removeApeTaskFavor",params)

export const saveApeTaskFavor = (params) => post("/favor/saveApeTaskFavor",params)

export const getApeTaskFavorPage = (params) => post("/favor/getApeTaskFavorPage",params)
// AI 聊天
export const aiChat = (params) => post("/ai/chat", params)
// AI 自动学习规划
export const aiStudyPlan = (params) => post("/ai/study-plan", params)
// AI 错题 Agent
export const aiWrongAgent = (params) => post("/ai/wrong-agent", params)
export const getWrongList = () => get("/ai/wrong-list")
// AI 教师备课助手
export const aiLessonPrepare = (params) => post("/ai/lesson-prepare", params)
// AI 自动总结课程
export const aiCourseSummary = (params) => post("/ai/course-summary", params)
// AI 历史查询
export const getAiHistory = (params) => post("/ai/history", params)
// AI 历史删除
export const deleteAiHistory = (params) => post("/ai/history/delete", params)
// 获取当前教师课程列表
export const getApeTaskByTeacherId = () => get("/task/getApeTaskByTeacherId")
