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
            <div class="item-01-under" @click="toUser()">
              <div style="color: #585854; font-size: 14px; font-weight: bold">
                查看用户列表
              </div>
              <i
                class="el-icon-right"
                style="color: #585854; font-size: 14px; font-weight: bold"
              ></i>
            </div>
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
            <div class="item-01-under" @click="toStudent()">
              <div style="color: #585854; font-size: 14px; font-weight: bold">
                查看全部
              </div>
              <i
                class="el-icon-right"
                style="color: #585854; font-size: 14px; font-weight: bold"
              ></i>
            </div>
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
            <div class="item-01-under" @click="toTeacher()">
              <div style="color: #585854; font-size: 14px; font-weight: bold">
                查看全部
              </div>
              <i
                class="el-icon-right"
                style="color: #585854; font-size: 14px; font-weight: bold"
              ></i>
            </div>
          </el-card>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple">
          <el-card shadow="always" class="item-06">
            <div class="item-01-top">
              <img
                v-if="userInfo.avatar"
                :src="$resolveAvatar(userInfo.avatar, '/img/avatars/zhixue-admin-default.png')"
                @error="$avatarError($event, '/img/avatars/zhixue-admin-default.png')"
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
                    color: #2f2f2c;
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
                    :src="resolveAvatar(scope.row)"
                    @error="$avatarError($event, '/img/avatars/zhixue-teacher-default.png')"
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
  getUserPage,
  getUser,
} from "../../../api/api";
import * as echarts from "echarts";
export default {
  data() {
    return {
      top: {},
      sex: [],
      tasks: [],
      nums: [],
      taskList: [],
      taskCard: [],
      today: new Date(),
      myChart: null,
      pieChart: null,
      resizeHandler: null,
      chartReady: false,
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
          data: ["学生数量"],
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
            type: "bar",
            data: this.nums,
            barWidth: "15px",
            name: "学生数量",
            itemStyle: {
              normal: {
                color: function (params) {
                  let colorArr =
                    params.value > 0
                      ? ["#22c55e", "#16a34a"]
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
    loadCurrentUser() {
      const userStr = this.$store.getters["user/getUser"];
      if (userStr) {
        try {
          this.userInfo = JSON.parse(userStr) || {};
        } catch (e) {
          this.userInfo = {};
        }
      }
      getUser().then((res) => {
        if (res.code == 1000 && res.data) {
          this.userInfo = res.data;
          this.$store.commit("user/setUser", JSON.stringify(res.data));
        }
      });
    },
    resolveAvatar(row) {
      const avatar = row && (row.avatar || row.teacherAvatar);
      return this.$resolveAvatar(avatar, "/img/avatars/zhixue-teacher-default.png");
    },
    loadTeacherAvatarMap() {
      return getUserPage({
        userType: 1,
        pageNumber: 1,
        pageSize: 2000,
      })
        .then((res) => {
          if (
            res.code !== 1000 ||
            !res.data ||
            !Array.isArray(res.data.records)
          ) {
            return {};
          }
          const avatarMap = {};
          res.data.records.forEach((user) => {
            if (user && user.id != null && user.avatar) {
              avatarMap[String(user.id)] = user.avatar;
            }
          });
          return avatarMap;
        })
        .catch(() => ({}));
    },
    normalizePath(path) {
      if (!path) {
        return "";
      }
      return path.startsWith("/") ? path : `/${path}`;
    },
    getMenuRouteByNames(names) {
      const menus =
        (this.$store.state.menu && this.$store.state.menu.menus) || [];
      for (let i = 0; i < menus.length; i++) {
        const item = menus[i];
        if (
          item &&
          item.menuType == 1 &&
          names.indexOf(item.menuName) !== -1 &&
          item.routeUrl
        ) {
          return this.normalizePath(item.routeUrl);
        }
      }
      for (let i = 0; i < menus.length; i++) {
        const item = menus[i];
        const componentUrl =
          item && item.componentUrl
            ? String(item.componentUrl).toLowerCase()
            : "";
        if (
          item &&
          item.menuType == 1 &&
          item.routeUrl &&
          (componentUrl.indexOf("allmember") !== -1)
        ) {
          return this.normalizePath(item.routeUrl);
        }
      }
      return "";
    },
    openMenuByPaths(name, paths) {
      let target = this.normalizePath(paths[0]);
      for (let i = 0; i < paths.length; i++) {
        const current = this.normalizePath(paths[i]);
        const matched = this.$router.match(current);
        if (matched && matched.matched && matched.matched.length > 0) {
          target = current;
          break;
        }
      }
      var param = {
        name: name,
        url: target,
      };
      this.$store.commit("menu/addActiveMenu", param);
      this.$router.push(target);
      this.$store.commit("menu/setActiveMenu", target);
    },
    toUser() {
      const menuRoute = this.getMenuRouteByNames(["用户管理", "用户列表"]);
      if (menuRoute) {
        this.openMenuByPaths("用户管理", [menuRoute]);
        return;
      }
      this.openMenuByPaths("用户管理", ["/allmember", "allmember"]);
    },
    toTask() {
      this.openMenuByPaths("课程管理", ["/task", "task"]);
    },
    toStudent() {
      this.openMenuByPaths("学生管理", ["/student", "student"]);
    },
    toTeacher() {
      this.openMenuByPaths("教师管理", ["/teacher", "teacher"]);
    },
    toUserInfo() {
      this.openMenuByPaths("个人中心", ["/center", "center"]);
    },
  },
  created() {},
  mounted() {
    this.loadCurrentUser();
    this.$nextTick(() => {
      this.initCharts();
      this.handleResize();
    });
    getIndexData({ type: 0 }).then((res) => {
      if (res.code == 1000) {
        this.top = res.data;
      }
    });
    getIndexSexData({ type: 0 }).then((res) => {
      if (res.code == 1000) {
        this.sex = Array.isArray(res.data) ? res.data : [];
        this.$nextTick(() => {
          this.initCharts();
          this.renderPieChart();
        });
      }
    });
    getTaskChart({ type: 0 }).then((res) => {
      if (res.code == 1000) {
        this.tasks =
          res.data && Array.isArray(res.data.tasks) ? res.data.tasks : [];
        this.nums =
          res.data && Array.isArray(res.data.nums) ? res.data.nums : [];
        this.$nextTick(() => {
          this.initCharts();
          this.renderBarLineChart();
        });
      }
    });
    Promise.all([
      getTaskIndexList({ type: 0 }),
      this.loadTeacherAvatarMap(),
    ]).then(([taskRes, avatarMap]) => {
      if (taskRes.code == 1000) {
        const sourceList = Array.isArray(taskRes.data) ? taskRes.data : [];
        const normalizedList = sourceList.map((item) => {
          const mappedAvatar =
            item && item.teacherId != null
              ? avatarMap[String(item.teacherId)] || ""
              : "";
          return {
            ...item,
            avatar: item.avatar || item.teacherAvatar || mappedAvatar || "",
          };
        });
        this.taskList = normalizedList.slice(0, 5);
        this.taskCard = normalizedList.slice(0, 3);
      }
    });
    this.resizeHandler = () => {
      if (this.myChart && typeof this.myChart.resize === "function") {
        this.myChart.resize();
      }
      if (this.pieChart && typeof this.pieChart.resize === "function") {
        this.pieChart.resize();
      }
    };
    window.addEventListener("resize", this.resizeHandler);
  },
  beforeDestroy() {
    if (this.resizeHandler) {
      window.removeEventListener("resize", this.resizeHandler);
    }
    if (this.myChart && typeof this.myChart.dispose === "function") {
      this.myChart.dispose();
      this.myChart = null;
    }
    if (this.pieChart && typeof this.pieChart.dispose === "function") {
      this.pieChart.dispose();
      this.pieChart = null;
    }
  },
};
</script>

<style scoped>
.index-top {
  height: 172px;
}

.item-01,
.item-03,
.item-05,
.item-06 {
  height: 154px;
  background: #ffffff;
  border: 1px solid #e6eaf0;
  color: #243145;
  overflow: hidden;
}

.index-center {
  margin-top: 18px;
  height: 400px;
}

.item-07,
.item-08 {
  height: 400px;
  background: #ffffff;
}

.index-center-01 {
  margin-top: 18px;
  height: 300px;
}
.item-09 {
  height: 300px;
  background: #ffffff;
  overflow: hidden;
}
.index-under {
  margin-top: 18px;
  height: 400px;
}

.item-10,
.item-11 {
  background: #ffffff;
}

.item-10 {
  height: 350px;
}

.item-11 {
  height: 450px;
}
.item-01-top {
  height: 92px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  gap: 16px;
}
.item-01-top-center {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  min-width: 0;
}
.item-01 .item-01-top-center > div:first-child,
.item-03 .item-01-top-center > div:first-child,
.item-05 .item-01-top-center > div:first-child {
  color: #344054 !important;
  font-size: 16px !important;
}
.item-01 .item-01-top-center > div:nth-child(2),
.item-03 .item-01-top-center > div:nth-child(2),
.item-05 .item-01-top-center > div:nth-child(2) {
  color: #111827 !important;
  font-size: 28px !important;
}
.item-06 .item-01-top-center > div:first-child {
  color: #243145 !important;
}
.item-06 .item-01-top-center > div:not(:first-child) {
  color: #667085 !important;
}
.item-01-under {
  height: 38px;
  border-top: 1px solid #eef2f7;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  cursor: pointer;
}
.item-01-under:hover div,
.item-01-under:hover i {
  color: #2563eb !important;
}
.item-01-top img {
  width: 64px !important;
  height: 64px !important;
  border-radius: 14px !important;
  object-fit: contain;
  flex-shrink: 0;
}
.item-06 .item-01-top img {
  width: 58px !important;
  height: 58px !important;
  border-radius: 50% !important;
  object-fit: cover;
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
.index-top,
.index-center,
.index-center-01,
.index-under {
  width: 100%;
}

.index-top .el-col,
.index-center .el-col,
.index-center-01 .el-col,
.index-under .el-col {
  height: 100%;
}
.item-10-item {
  border: 1px solid #e6eaf0;
  border-radius: 10px;
  height: 100%;
  margin: 0 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-around;
  background: #f8fafc;
  transition: all 0.2s ease;
}
.item-10-item:hover {
  border-color: #bfd2f5;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}
.view-all-btn {
  border: none;
  color: #ffffff;
}
.view-all-btn:hover,
.view-all-btn:focus {
  color: #ffffff;
}
/deep/ .el-calendar {
  background: #ffffff;
  border: 1px solid #e6eaf0;
  border-radius: 12px;
}
/deep/ .el-calendar__header {
  padding: 12px 16px;
  border-bottom: 1px solid #e6eaf0;
  background: #f8fafc;
}
/deep/ .el-calendar__title {
  color: #243145;
  font-weight: 700;
}
/deep/ .el-calendar-table thead th {
  color: #667085;
  font-weight: 600;
}
/deep/ .el-calendar-table td {
  border: 1px solid #eef2f7;
}
/deep/ .el-calendar-day {
  height: 45px !important;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #344054;
  transition: all 0.2s ease;
}
/deep/ .el-calendar-day:hover {
  background: #eff6ff;
}
/deep/
  .el-calendar-table
  tr
  td:nth-child(-n + 5):not(.is-selected)
  .el-calendar-day {
  color: #243145;
}
/deep/
  .el-calendar-table
  tr
  td:nth-child(n + 6):not(.is-selected)
  .el-calendar-day {
  color: #98a2b3;
}
/deep/ .el-calendar-table td.is-today:not(.is-selected) .el-calendar-day {
  box-shadow: inset 0 0 0 1px #2563eb;
  background: #eff6ff;
}
/deep/ .el-calendar-table td.is-selected .el-calendar-day {
  color: #ffffff;
  background: #2563eb;
}
</style>
