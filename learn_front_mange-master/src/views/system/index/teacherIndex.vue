<template>
  <div>
    <el-row :gutter="20" class="index-top">
      <!-- 第一行 -->
      <el-col :span="6">
        <div class="grid-content bg-purple">
          <el-card shadow="always" class="item-01">
            <!-- 用户数量 -->
            <div class="item-01-top">
              <img
                src="../../../assets/image/index-01.png"
                style="width: 110px"
              />
              <div class="item-01-top-center">
                <div style="color: #3f3f3c; font-size: 20px; font-weight: bold">
                  用户数量
                </div>
                <div style="color: #2f2f2c; font-size: 30px; font-weight: bold">
                  {{ top.userNum }}人
                </div>
              </div>
            </div>
            <!-- <div class="item-01-under">
            </div> -->
          </el-card>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple">
          <el-card shadow="always" class="item-03">
            <div class="item-01-top">
              <img
                src="../../../assets/image/index-02.png"
                style="width: 110px"
              />
              <div class="item-01-top-center">
                <div style="color: #000000; font-size: 20px; font-weight: bold">
                  学生数量
                </div>
                <div style="color: #000000; font-size: 30px; font-weight: bold">
                  {{ top.studentNum }}人
                </div>
              </div>
            </div>
            <!-- <div class="item-01-under">
            </div> -->
          </el-card>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple">
          <el-card shadow="always" class="item-05">
            <div class="item-01-top">
              <img
                src="../../../assets/image/avator.png"
                style="width: 110px"
              />
              <div class="item-01-top-center">
                <div style="color: #3f3f3c; font-size: 20px; font-weight: bold">
                  教师数量
                </div>
                <div style="color: #2f2f2c; font-size: 30px; font-weight: bold">
                  {{ top.teacherNum }}人
                </div>
              </div>
            </div>
            <!-- <div class="item-01-under" ></div> -->
          </el-card>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple">
          <el-card shadow="always" class="item-06">
            <div class="item-01-top">
              <img
                v-if="userInfo.avatar"
                :src="$resolveAvatar(userInfo.avatar, '/img/avatars/zhixue-teacher-default.png')"
                @error="$avatarError($event, '/img/avatars/zhixue-teacher-default.png')"
                style="
                  width: 80px;
                  height: 80px;
                  border-radius: 50%;
                  object-fit: cover;
                "
              />
              <img
                v-else
                src="../../../assets/image/avator.png"
                style="
                  width: 80px;
                  height: 80px;
                  border-radius: 50%;
                  object-fit: cover;
                "
              />
              <div
                class="item-01-top-center"
                style="margin-left: 15px; flex: 1"
              >
                <div
                  style="
                    color: #000000;
                    font-size: 16px;
                    font-weight: bold;
                    text-overflow: ellipsis;
                    overflow: hidden;
                  "
                >
                  {{ userInfo.userName || "用户名" }}
                </div>
                <div
                  style="
                    color: #8f8f8a;
                    font-size: 13px;
                    margin-top: 5px;
                    text-overflow: ellipsis;
                    overflow: hidden;
                  "
                >
                  tel:{{ userInfo.tel || userInfo.phone || "联系方式" }}
                </div>
                <div
                  style="
                    color: #8f8f8a;
                    font-size: 13px;
                    margin-top: 5px;
                    text-overflow: ellipsis;
                    overflow: hidden;
                  "
                >
                  {{ userInfo.email || "邮箱" }}
                </div>
              </div>
            </div>
            <div class="item-01-under" @click="toUserInfo()">
              <div style="color: #585854; font-size: 14px; font-weight: bold">
                查看详情
              </div>
              <i
                class="el-icon-right"
                style="color: #585854; font-size: 14px; font-weight: bold"
              ></i>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="index-center">
      <el-col :span="16">
        <el-card shadow="always" class="item-07">
          <div ref="chartRef" id="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="always" class="item-08">
          <div ref="pieChartRef" id="pie-chart"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="index-center-01">
      <el-col :span="24">
        <el-card shadow="always" class="item-09">
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
            <el-table-column prop="name" label="课程名称"> </el-table-column>
            <el-table-column prop="teacherName" label="教师">
              <template slot-scope="scope">
                <div style="display: flex; align-items: center">
                  <img
                    style="
                      width: 35px;
                      height: 35px;
                      border-radius: 50%;
                      object-fit: cover;
                    "
                    src="../../../assets/image/avator.png"
                  />
                  <div style="margin-left: 10px">
                    {{ scope.row.teacherName }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="major" label="专业">
              <template slot-scope="scope">
                <el-button size="mini">{{ scope.row.major }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="classification" label="课程分类">
              <template slot-scope="scope">
                <el-button size="mini">{{
                  scope.row.classification
                }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="num" label="学生数量"> </el-table-column>
            <el-table-column prop="createTime" label="创建时间">
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="index-under">
      <el-col :span="16">
        <el-card shadow="always" class="item-10">
          <div
            style="
              width: 100%;
              height: 50px;
              display: flex;
              justify-content: space-between;
              padding: 0 10px;
              align-items: center;
              color: #3f3f3c;
              font-weight: 600;
            "
          >
            <span>课程 列表</span>
            <el-button
              class="view-all-btn"
              style="height: 35px"
              size="mini"
              type="primary"
              @click="toTask()"
              >查看全部</el-button
            >
          </div>
          <div style="width: 100%; height: 260px">
            <el-row>
              <el-col
                class="item-10-item"
                :span="7"
                v-for="(item, index) in taskCard"
                :key="index"
              >
                <img
                  :src="$store.state.configure.HOST + item.image"
                  style="border-radius: 20px; width: 90%"
                />
                <div>{{ item.name }}</div>
                <div
                  style="
                    width: 90%;
                    display: flex;
                    justify-content: space-around;
                  "
                >
                  <div
                    style="
                      height: 35px;
                      font-size: 13px;
                      display: flex;
                      align-items: center;
                      color: #8f8f8a;
                    "
                  >
                    <span>{{ item.createTime }}</span>
                  </div>
                  <el-button size="mini" style="height: 30px">{{
                    item.teacherName
                  }}</el-button>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="always" class="item-11">
          <el-calendar v-model="today"> </el-calendar>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {
  getIndexData,
  getIndexSexData,
  getTaskChart,
  getTaskIndexList,
  getUser,
} from "../../../api/api";
import * as echarts from "echarts";
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
  methods: {
    initCharts() {
      if (this.$refs.chartRef && !this.myChart) {
        this.myChart = echarts.init(this.$refs.chartRef);
      }
      if (this.$refs.pieChartRef && !this.pieChart) {
        this.pieChart = echarts.init(this.$refs.pieChartRef);
      }
    },
    renderBarLineChart() {
      if (!this.myChart) return;
      var option = {
        title: {
          text: "课程学生数量统计",
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
          data: ["学生数量(折线)", "学生数量(柱状)"],
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
            name: "学生数量(折线)",
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
            name: "学生数量(柱状)",
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
          text: "学生性别占比",
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
            name: "学生性别分析",
            type: "pie",
            radius: "50%",
            data: this.sex,
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
        url: "/task",
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push("/teacherTask");
      this.$store.commit("menu/setActiveMenu", "/teacherTask");
    },
    toStudent() {
      var param = {
        name: "学生管理",
        url: "/student",
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push("/student");
      this.$store.commit("menu/setActiveMenu", "/student");
    },
    toTeacher() {
      var param = {
        name: "教师管理",
        url: "/teacher",
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push("/teacher");
      this.$store.commit("menu/setActiveMenu", "/teacher");
    },
    toUserInfo() {
      var param = {
        name: "个人中心",
        url: "/center",
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push("/center");
      this.$store.commit("menu/setActiveMenu", "/center");
    },
    toUser() {
      var param = {
        name: "用户管理",
        url: "/user",
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push("/user");
      this.$store.commit("menu/setActiveMenu", "/user");
    },
  },
  created() {},
  mounted() {
    this.$nextTick(() => {
      this.initCharts();
      this.handleResize();
    });

    this.resizeHandler = this.handleResize.bind(this);
    window.addEventListener("resize", this.resizeHandler);

    getUser().then((res) => {
      if (res.code == 1000) {
        this.userInfo = res.data;
      }
    });

    getIndexData({ type: DASHBOARD_TYPE })
      .then((res) => {
        if (res.code !== 1000) {
          this.$message.warning("统计数据加载失败，请刷新重试");
          return;
        }
        this.top = res.data || { userNum: 0, studentNum: 0, teacherNum: 0 };
      })
      .catch((err) => {
        this.$message.error(
          "获取统计数据异常：" + (err.message || "网络请求失败"),
        );
        console.error("getIndexData error:", err);
      });
    getIndexSexData({ type: DASHBOARD_TYPE })
      .then((res) => {
        if (res.code !== 1000) {
          this.$message.warning("性别分析数据加载失败");
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
          "获取性别分析异常：" + (err.message || "网络请求失败"),
        );
        this.sex = [];
        console.error("getIndexSexData error:", err);
      });
    getTaskChart({ type: DASHBOARD_TYPE })
      .then((res) => {
        if (res.code !== 1000) {
          this.$message.warning("任务统计数据加载失败");
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
          "获取任务统计异常：" + (err.message || "网络请求失败"),
        );
        this.tasks = [];
        this.nums = [];
        console.error("getTaskChart error:", err);
      });
    getTaskIndexList({ type: DASHBOARD_TYPE })
      .then((res) => {
        if (res.code !== 1000) {
          this.$message.warning("任务列表数据加载失败");
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
          "获取任务列表异常：" + (err.message || "网络请求失败"),
        );
        this.taskList = [];
        this.taskCard = [];
        console.error("getTaskIndexList error:", err);
      });
  },
  beforeDestroy() {
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
.index-top {
  height: 210px;
}
.item-01 {
  border-radius: 12px;
  height: 170px;
  background-color: #f9f9f7;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
}
.item-02 {
  border-radius: 12px;
  margin-top: 20px;
  height: 90px;
  background-color: #ffffff;
}
.item-03 {
  border-radius: 12px;
  height: 170px;
  background-color: #f9f9f7;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
}
.item-04 {
  border-radius: 12px;
  margin-top: 20px;
  height: 90px;
  background-color: #ffffff;
}
.item-05 {
  border-radius: 12px;
  height: 170px;
  background-color: #f9f9f7;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
  color: #3f3f3c;
}
.item-06 {
  border-radius: 12px;
  height: 170px;
  background-color: #f9f9f7;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
}
.index-center {
  margin-top: 20px;
  height: 400px;
}
.item-07 {
  border-radius: 12px;
  height: 400px;
  background-color: #fcfcfb;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
}
.item-08 {
  border-radius: 12px;
  height: 400px;
  background-color: #fcfcfb;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
}
.index-center-01 {
  margin-top: 20px;
  height: 300px;
}
.item-09 {
  border-radius: 12px;
  height: 300px;
  background-color: #fcfcfb;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
  overflow: hidden;
}
.index-under {
  margin-top: 20px;
  height: 400px;
}
.item-10 {
  border-radius: 12px;
  height: 350px;
  background-color: #fcfcfb;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
}
.item-11 {
  border-radius: 12px;
  height: 450px;
  background-color: #fcfcfb;
  border: 1px solid #e8e8e5;
  box-shadow: 0 4px 12px rgba(34, 34, 34, 0.08);
}
.item-01-top {
  height: 90px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-around;
}
.item-01-top-center {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
}
.item-01-under {
  height: 60px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.item-06-top {
  display: flex;
  justify-content: center;
}
.item-06-under {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.item-05-top {
  display: flex;
  flex-direction: column;
  align-items: center;
}
#chart,
#pie-chart {
  width: 100%;
  height: 380px;
}
.item-09::-webkit-scrollbar {
  width: 0px;
  height: 0px;
}
.el-row {
  width: 100%;
  height: 100%;
}
.item-10-item {
  border: 1px solid #e0e0dc;
  border-radius: 12px;
  height: 100%;
  margin: 0 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-around;
  background: #fafaf8;
  transition: all 0.2s ease;
}
.item-10-item:hover {
  border-color: #d5d5d0;
  box-shadow: 0 6px 16px rgba(34, 34, 34, 0.1);
}
.view-all-btn {
  border: none;
  color: #ffffff;
  background: linear-gradient(135deg, #2f3437 0%, #4a5257 100%);
}
.view-all-btn:hover,
.view-all-btn:focus {
  color: #ffffff;
  background: linear-gradient(135deg, #3a4145 0%, #5a6369 100%);
}
/deep/ .el-calendar {
  background: linear-gradient(180deg, #ffffff 0%, #f8f9f7 100%);
  border: 1px solid #e5e7e3;
  border-radius: 12px;
}
/deep/ .el-calendar__header {
  padding: 12px 16px;
  border-bottom: 1px solid #e3e5e1;
  background: #f3f5f2;
}
/deep/ .el-calendar__title {
  color: #2f3437;
  font-weight: 700;
}
/deep/ .el-calendar-table thead th {
  color: #5f666b;
  font-weight: 600;
}
/deep/ .el-calendar-table td {
  border: 1px solid #eceee9;
}
/deep/ .el-calendar-day {
  height: 45px !important;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #3e4347;
  transition: all 0.2s ease;
}
/deep/ .el-calendar-day:hover {
  background: #e9efea;
}
/deep/
  .el-calendar-table
  tr
  td:nth-child(-n + 5):not(.is-selected)
  .el-calendar-day {
  color: #2f3437;
}
/deep/
  .el-calendar-table
  tr
  td:nth-child(n + 6):not(.is-selected)
  .el-calendar-day {
  color: #b3b8bd;
}
/deep/ .el-calendar-table td.is-today:not(.is-selected) .el-calendar-day {
  box-shadow: inset 0 0 0 1px #2f3437;
  background: #eef2ee;
}
/deep/ .el-calendar-table td.is-selected .el-calendar-day {
  color: #ffffff;
  background: linear-gradient(135deg, #2f3437 0%, #4a5257 100%);
}
</style>
