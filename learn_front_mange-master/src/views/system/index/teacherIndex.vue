<template>
  <div class="teacher-workspace">
    <header class="workspace-heading">
      <div><span class="eyebrow">{{ $t("教师工作台") }}</span><h1>{{ $t("把时间留给教学") }}</h1><p>{{ $t("管理课程、安排考试，从今天要做的事开始。") }}</p></div>
      <el-button @click="toUserInfo">{{ $t("个人资料") }}</el-button>
    </header>
    <nav class="teaching-actions" :aria-label="$t('教学常用操作')">
      <button @click="toTask"><i class="el-icon-reading" aria-hidden="true"></i><strong>{{ $t("管理课程") }}</strong><span>{{ $t("维护课程、章节与课程作业") }}</span></button>
      <button @click="openTeachingPage('/test', '考试管理')"><i class="el-icon-edit-outline" aria-hidden="true"></i><strong>{{ $t("安排考试") }}</strong><span>{{ $t("管理试卷与考试安排") }}</span></button>
      <button @click="openTeachingPage('/aiData', 'AI数据整理')"><i class="el-icon-document" aria-hidden="true"></i><strong>{{ $t("整理教学资料") }}</strong><span>{{ $t("使用 AI 辅助整理内容") }}</span></button>
    </nav>
    <section class="course-panel">
      <div class="panel-title"><div><h2>{{ $t("课程概览") }}</h2><p>{{ $t("查看课程及选课人数；作业管理请进入课程章节。") }}</p></div><el-button type="primary" @click="toTask">{{ $t("进入课程管理") }}</el-button></div>
          <el-table
            :data="taskList"
            :header-cell-style="{
              color: '#3f3f3c',
              'background-color': '#f1f1ee',
              'border-bottom': '1px solid #e3e3e0',
            }"
            :row-style="{
              color: '#585854',
              'font-size': '15px',
              'font-family': '黑体',
            }"
            height="238"
            stripe
            style="width: 100%; background: transparent"
          >
            <el-table-column prop="name" :label="$t('课程名称')"> </el-table-column>
            <el-table-column prop="teacherName" :label="$t('教师')" />
            <el-table-column prop="major" :label="$t('专业')">
              <template #default="scope">
                <el-tag size="small" type="info">{{ scope.row.major }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="classification" :label="$t('课程分类')">
              <template #default="scope">
                <el-tag size="small" type="info">{{
                  scope.row.classification
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="num" :label="$t('学生数量')"> </el-table-column>
            <el-table-column prop="createTime" :label="$t('创建时间')">
            </el-table-column>
          </el-table>
    </section>
    <details class="supplement" @toggle="resizeOverview">
      <summary>{{ $t("查看补充统计与日历") }}</summary>
      <p class="overview-note">{{ $t("以下为系统提供的教师视角统计，不代表待完成任务。") }}</p>
      <div class="overview-counts"><span>{{ $t("用户") }}<strong>{{ top.userNum }}</strong></span><span>{{ $t("学生") }}<strong>{{ top.studentNum }}</strong></span><span>{{ $t("教师") }}<strong>{{ top.teacherNum }}</strong></span></div>
      <div class="overview-charts"><div ref="chartRef" id="chart"></div><div ref="pieChartRef" id="pie-chart"></div></div>
      <el-calendar v-model="today" />
    </details>
  </div>
</template>
<script>
import { t } from '@/i18n'

import {
  getIndexData,
  getIndexSexData,
  getTaskChart,
  getTaskIndexList,
  getUser,
} from "../../../api/api";
import * as echarts from "echarts";
import { markRaw } from "vue";
// 教师维度常量（区别于管理员维度 type:0）
const DASHBOARD_TYPE = 1;
export default {
  data() {
    return {
      top: {
        userNum: 0,
        studentNum: 0,
        teacherNum: 0,
      },
      sex: [],
      tasks: [],
      nums: [],
      taskList: [],
      taskCard: [],
      today: new Date(),
      myChart: null,
      pieChart: null,
      resizeHandler: null,
      userInfo: {},
    };
  },
  watch: { '$locale'() { this.$nextTick(() => { this.renderBarLineChart(); this.renderPieChart(); }) } },
  methods: {
    resizeOverview(event) {
      if (event.target.open) this.$nextTick(() => { this.initCharts(); this.renderBarLineChart(); this.renderPieChart(); this.handleResize(); });
    },
    openTeachingPage(path, name) {
      this.$menus.addActiveMenu({ name, url: path });
      this.$router.push(path);
      this.$menus.setActiveMenu(path);
    },
    initCharts() {
      if (this.$refs.chartRef && !this.myChart) {
        this.myChart = markRaw(echarts.init(this.$refs.chartRef));
      }
      if (this.$refs.pieChartRef && !this.pieChart) {
        this.pieChart = markRaw(echarts.init(this.$refs.pieChartRef));
      }
    },
    renderBarLineChart() {
      if (!this.myChart) return;
      var option = {
        title: {
          get text() { return t("课程学生数量统计") },
          left: "center",
          top: 10,
          textStyle: {
            color: "#3f3f3c",
            fontSize: 16,
            fontWeight: 600,
          },
        },
        tooltip: {
          trigger: "axis",
        },
        legend: {
          data: [t("学生数量(折线)"), t("学生数量(柱状)")],
          top: 40,
          textStyle: {
            color: "#585854",
          },
        },
        grid: {
          top: 80,
          left: "3%",
          right: "4%",
          bottom: "3%",
          containLabel: true,
        },
        backgroundColor: "transparent",
        xAxis: [
          {
            type: "category",
            color: "#585854",
            data: this.tasks,
            axisLine: {
              lineStyle: {
                color: "#dedfd9",
              },
            },
            axisTick: {
              show: true,
              lineStyle: {
                color: "#dedfd9",
              },
            },
            axisLabel: {
              color: "#8f8f8a",
            },
          },
        ],
        yAxis: [
          {
            axisLine: {
              lineStyle: {
                color: "#dedfd9",
              },
            },
            axisTick: {
              show: true,
              lineStyle: {
                color: "#dedfd9",
              },
            },
            axisLabel: {
              color: "#8f8f8a",
            },
            splitLine: {
              lineStyle: {
                color: "#f0f0ed",
                type: "solid",
              },
            },
          },
        ],
        series: [
          {
            data: this.nums,
            type: "line",
            name: t("学生数量(折线)"),
            symbol: "none",
            areaStyle: {
              color: "rgba(176, 168, 220, 0.15)",
            },
            lineStyle: {
              color: "#7a6dbe",
              width: 2,
            },
            zlevel: 1,
          },
          {
            type: "bar",
            data: this.nums,
            barWidth: "15px",
            name: t("学生数量(柱状)"),
            itemStyle: {
              normal: {
                color: function (params) {
                  let colorArr =
                    params.value > 0
                      ? ["#2f3437", "#3c4246"]
                      : ["rgba(0,0,0,0)", "rgba(0,0,0,0)"];
                  return new echarts.graphic.LinearGradient(
                    0,
                    0,
                    0,
                    1,
                    [
                      {
                        offset: 0,
                        color: colorArr[0],
                      },
                      {
                        offset: 1,
                        color: colorArr[1],
                      },
                    ],
                    false,
                  );
                },
                barBorderRadius: [8, 8, 0, 0],
              },
            },
            label: {
              normal: {
                show: true,
                fontSize: 13,
                color: "#585854",
                position: "top",
              },
            },
            zlevel: 2,
          },
        ],
      };
      this.myChart.setOption(option, true);
    },
    renderPieChart() {
      if (!this.pieChart) return;
      var optionPie = {
        title: {
          get text() { return t("学生性别占比") },
          left: "center",
          top: 10,
          textStyle: {
            color: "#3f3f3c",
            fontSize: 16,
            fontWeight: 600,
          },
        },
        tooltip: {
          trigger: "item",
        },
        legend: {
          center: "center",
          textStyle: {
            color: "#585854",
          },
          bottom: 10,
        },
        series: [
          {
            name: t("学生性别分析"),
            type: "pie",
            radius: "50%",
            data: this.sex.map(item => ({ ...item, name: t(item.name) })),
            label: {
              color: "#585854",
              formatter: "{b}: {d}%",
            },
            itemStyle: {
              borderColor: "#fcfcfb",
              borderWidth: 2,
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: "rgba(0, 0, 0, 0.2)",
              },
            },
          },
        ],
      };
      this.pieChart.setOption(optionPie, true);
    },
    handleResize() {
      if (this.myChart && typeof this.myChart.resize === "function") {
        this.myChart.resize();
      }
      if (this.pieChart && typeof this.pieChart.resize === "function") {
        this.pieChart.resize();
      }
    },
    toTask() {
      var param = {
        name: "教师课程管理",
        url: "/teacherTask",
      };
      this.$menus.addActiveMenu(param);
      this.$router.push("/teacherTask");
      this.$menus.setActiveMenu("/teacherTask");
    },
    toStudent() {
      var param = {
        name: "学生管理",
        url: "/student",
      };
      this.$menus.addActiveMenu(param);
      this.$router.push("/student");
      this.$menus.setActiveMenu("/student");
    },
    toTeacher() {
      var param = {
        name: "教师管理",
        url: "/teacher",
      };
      this.$menus.addActiveMenu(param);
      this.$router.push("/teacher");
      this.$menus.setActiveMenu("/teacher");
    },
    toUserInfo() {
      var param = {
        name: "个人中心",
        url: "/center",
      };
      this.$menus.addActiveMenu(param);
      this.$router.push("/center");
      this.$menus.setActiveMenu("/center");
    },
    toUser() {
      var param = {
        name: "用户管理",
        url: "/user",
      };
      this.$menus.addActiveMenu(param);
      this.$router.push("/user");
      this.$menus.setActiveMenu("/user");
    },
  },
  created() {},
  mounted() {
    // Supplemental charts are initialized after their panel is expanded.

    this.resizeHandler = this.handleResize.bind(this);
    window.addEventListener("resize", this.resizeHandler);

    getUser().then((res) => {
      if (res.code == 1000) {
        this.userInfo = res.data;
      }
    }).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false });

    getIndexData({ type: DASHBOARD_TYPE })
      .then((res) => {
        if (res.code !== 1000) {
          this.$message.warning(t("统计数据加载失败，请刷新重试"));
          return;
        }
        this.top = res.data || { userNum: 0, studentNum: 0, teacherNum: 0 };
      })
      .catch((err) => {
        this.$message.error(
          t("获取统计数据异常：") + (err.message || "网络请求失败"),
        );
        console.error("getIndexData error:", err);
      });
    getIndexSexData({ type: DASHBOARD_TYPE })
      .then((res) => {
        if (res.code !== 1000) {
          this.$message.warning(t("性别分析数据加载失败"));
          this.sex = [];
          return;
        }
        this.sex = Array.isArray(res.data) ? res.data : [];
        this.$nextTick(() => {
          this.renderPieChart();
        });
      })
      .catch((err) => {
        this.$message.error(
          t("获取性别分析异常：") + (err.message || "网络请求失败"),
        );
        this.sex = [];
        console.error("getIndexSexData error:", err);
      });
    getTaskChart({ type: DASHBOARD_TYPE })
      .then((res) => {
        if (res.code !== 1000) {
          this.$message.warning(t("任务统计数据加载失败"));
          this.tasks = [];
          this.nums = [];
          return;
        }
        this.tasks =
          res.data && Array.isArray(res.data.tasks) ? res.data.tasks : [];
        this.nums =
          res.data && Array.isArray(res.data.nums) ? res.data.nums : [];
        this.$nextTick(() => {
          this.renderBarLineChart();
        });
      })
      .catch((err) => {
        this.$message.error(
          t("获取任务统计异常：") + (err.message || "网络请求失败"),
        );
        this.tasks = [];
        this.nums = [];
        console.error("getTaskChart error:", err);
      });
    getTaskIndexList({ type: DASHBOARD_TYPE })
      .then((res) => {
        if (res.code !== 1000) {
          this.$message.warning(t("任务列表数据加载失败"));
          this.taskList = [];
          this.taskCard = [];
          return;
        }
        const dataArray = Array.isArray(res.data) ? res.data : [];
        this.taskList = dataArray.slice(0, 5);
        this.taskCard = dataArray.slice(0, 3);
      })
      .catch((err) => {
        this.$message.error(
          t("获取任务列表异常：") + (err.message || "网络请求失败"),
        );
        this.taskList = [];
        this.taskCard = [];
        console.error("getTaskIndexList error:", err);
      });
  },
  beforeUnmount() {
    if (this.resizeHandler) {
      window.removeEventListener("resize", this.resizeHandler);
    }
    if (this.myChart) {
      this.myChart.dispose();
      this.myChart = null;
    }
    if (this.pieChart) {
      this.pieChart.dispose();
      this.pieChart = null;
    }
  },
};
</script>

<style scoped>
.teacher-workspace { max-width: 1320px; margin: 0 auto; color: #263647; }
.workspace-heading,.panel-title { display:flex; justify-content:space-between; align-items:center; gap:20px; }
.workspace-heading { padding:12px 0 24px; }
.eyebrow { color:#456c91;font-size:13px;font-weight:600; }
h1 { font-size:28px; margin:8px 0; } h2 { font-size:18px; margin:0; }
p { color:#687889; line-height:1.7; margin:6px 0 0; font-size:14px; }
.teaching-actions { display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px;margin-bottom:24px; }
.teaching-actions button { text-align:left;display:grid;gap:9px;border:1px solid #dfe6ed;border-radius:12px;background:white;padding:22px;color:inherit;cursor:pointer;font:inherit; }
.teaching-actions button:hover { border-color:#7698b9;background:#f7faff; }
.teaching-actions button:focus-visible { outline:3px solid #87b6e6;outline-offset:3px; }
.teaching-actions i { font-size:23px;color:#436c94; }.teaching-actions span { font-size:13px;color:#687889; }
.course-panel,.supplement { border:1px solid #e1e7ed;background:white;border-radius:12px;padding:22px; }
.panel-title { margin-bottom:20px; }.supplement { margin-top:20px; }
summary { cursor:pointer; padding:8px 0;font-weight:600; } .overview-counts { display:flex;gap:32px;margin:20px 0; }.overview-counts strong { margin-left:8px; }
.overview-charts { display:grid;grid-template-columns:2fr 1fr; } #chart,#pie-chart { width:100%;height:360px; }
@media(max-width:760px) { .teaching-actions,.overview-charts { grid-template-columns:1fr; }.workspace-heading,.panel-title { align-items:flex-start;flex-direction:column; }.course-panel { padding:16px; } }
</style>
