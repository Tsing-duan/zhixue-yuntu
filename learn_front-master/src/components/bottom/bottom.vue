<template>
  <div class="bottom">
    <div class="bottom-container">
      <!-- 左侧：Logo区域 -->
      <div class="bottom-section bottom-logo">
        <div class="logo-title">
          <img class="logo-mark" src="../../assets/image/brand/zhixue-mark.svg" alt="智学云途">
          <span class="logo-text">智学云途</span>
        </div>
        <div class="about-us">
          <div class="section-title">
            <span>关于我们</span>
            <span class="en-title">About Us</span>
          </div>
          <p class="description">
            致力于提供优质的在线教育服务，让学习更高效、更有趣。
            汇聚顶尖师资力量，打造精品课程体系。
          </p>
          <div class="contact-email">
            <i class="el-icon-message"></i>
            support@example.invalid
          </div>
          <div class="social-icons">
            <i class="el-icon-chat-dot-round"></i>
            <i class="el-icon-mobile-phone"></i>
            <i class="el-icon-share"></i>
            <i class="el-icon-connection"></i>
          </div>
        </div>
      </div>

      <!-- 中间左侧：快速导航 -->
      <div class="bottom-section bottom-nav">
        <div class="section-title">
          <span>快速导航</span>
          <span class="en-title">Quick Links</span>
        </div>
        <ul class="nav-links">
          <li @click="toPage(0)">主页</li>
          <li @click="toPage(1)">课程</li>
          <li @click="toPage(2)">教师</li>
          <li @click="toPage(3)">考试</li>
          <li @click="toPage(4)">笔记</li>
        </ul>
      </div>

      <!-- 中间右侧：更多服务 -->
      <div class="bottom-section bottom-nav">
        <div class="section-title">
          <span>更多服务</span>
          <span class="en-title">More Services</span>
        </div>
        <ul class="nav-links">
          <li @click="toPage(5)">公告</li>
          <li @click="toPage(6)">留言</li>
          <li @click="toPage(7)">关于</li>
          <li @click="toPage(8)">个人中心</li>
          <li @click="toPage(9)">错题集</li>
        </ul>
      </div>

      <!-- 右侧：联系方式 -->
      <div class="bottom-section bottom-contact">
        <div class="section-title">
          <span>联系方式</span>
          <span class="en-title">Contact Us</span>
        </div>
        <div class="contact-info">
          <div class="contact-item">
            <i class="el-icon-message"></i>
            <span>support@example.invalid</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-phone-outline"></i>
            <span>400-888-1234</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-service"></i>
            <span>QQ: 演示占位</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-chat-line-round"></i>
            <span>微信: demo-contact</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-location-outline"></i>
            <span>Portfolio Demo · 本地演示环境</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 版权信息 -->
    <div class="bottom-copyright">
      <div class="copyright-line"></div>
      <div class="copyright-text">
        Copyright © 2026 智学云途 | All Rights Reserved
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Bottom',
  data() {
    return {}
  },
  methods: {
    toPage(index) {
      if (index == 0) {
        this.$router.push("/")
      } else if (index == 1) {
        this.$router.push("/task")
      } else if (index == 2) {
        this.$router.push("/teacher")
      } else if (index == 3) {
        this.$router.push("/test")
      } else if (index == 4) {
        this.$router.push("/article")
      } else if (index == 5) {
        this.$router.push("/notice")
      } else if (index == 6) {
        this.$router.push("/message")
      } else if (index == 7) {
        this.$router.push("/about")
      } else if (index == 8) {
        this.$router.push("/center")
      } else if (index == 9) {
        this.$router.push("/myWrongAssign")
      }
    }
  }
}
</script>

<style scoped>
.bottom {
  width: 100%;
  background: #e8edf2;
  margin-top: 60px;
  position: relative;
  border-top: 1px solid #dce3e9;
}

/* 顶部装饰线 */
.bottom::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #20B486, #58C7A4, #20B486);
}

.bottom-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 48px 32px 32px 32px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 32px;
}

/* 底部区域通用样式 */
.bottom-section {
  flex: 1;
  min-width: 200px;
}

/* Logo区域 */
.bottom-logo {
  flex: 1.5;
}

.logo-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
}

.logo-mark {
  width: 52px;
  height: 52px;
  padding: 5px;
  box-sizing: border-box;
  object-fit: contain;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid rgba(24, 180, 176, 0.18);
  box-shadow: 0 9px 18px rgba(22, 105, 202, 0.1);
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #1a1a2e, #2c3e50);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  letter-spacing: -0.5px;
}

.section-title {
  margin-bottom: 24px;
}

.section-title span:first-child {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 6px;
  letter-spacing: -0.3px;
}

.section-title .en-title {
  font-size: 12px;
  font-weight: 400;
  color: #7f8c8d;
  letter-spacing: 0.5px;
}

.description {
  font-size: 14px;
  color: #5a6e7c;
  line-height: 1.7;
  margin-bottom: 20px;
}

.contact-email {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #5a6e7c;
  margin-bottom: 20px;
}

.contact-email i {
  color: #20B486;
  font-size: 16px;
}

.social-icons {
  display: flex;
  gap: 20px;
}

.social-icons i {
  font-size: 20px;
  color: #7f8c8d;
  cursor: pointer;
  transition: all 0.3s ease;
}

.social-icons i:hover {
  color: #20B486;
  transform: translateY(-3px);
}

/* 导航链接样式 */
.nav-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-links li {
  font-size: 14px;
  color: #5a6e7c;
  cursor: pointer;
  padding: 8px 0;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-links li:hover {
  color: #20B486;
  transform: translateX(6px);
}

/* 联系方式区域 */
.contact-info {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #5a6e7c;
  transition: all 0.3s ease;
}

.contact-item i {
  width: 24px;
  color: #20B486;
  font-size: 16px;
}

.contact-item:hover {
  color: #2c3e50;
  transform: translateX(4px);
}

/* 版权信息 */
.bottom-copyright {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 32px 32px 32px;
}

.copyright-line {
  height: 1px;
  background: linear-gradient(90deg, transparent, #cbd5e1, #20B486, #cbd5e1, transparent);
  margin-bottom: 20px;
}

.copyright-text {
  text-align: center;
  font-size: 12px;
  color: #7f8c8d;
  letter-spacing: 0.5px;
}

/* ========== 响应式设计 ========== */
@media (max-width: 1024px) {
  .bottom-container {
    padding: 40px 24px 24px 24px;
    gap: 28px;
  }
  
  .bottom-logo {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .bottom-container {
    flex-direction: column;
    gap: 32px;
    padding: 32px 20px 20px 20px;
  }
  
  .bottom-section {
    width: 100%;
  }
  
  .logo-title {
    justify-content: center;
  }
  
  .section-title {
    text-align: center;
  }
  
  .description {
    text-align: center;
  }
  
  .contact-email {
    justify-content: center;
  }
  
  .social-icons {
    justify-content: center;
  }
  
  .nav-links li {
    justify-content: center;
  }
  
  .contact-item {
    justify-content: center;
  }
  
  .bottom-copyright {
    padding: 16px 20px 24px 20px;
  }
}

@media (max-width: 480px) {
  .logo-text {
    font-size: 18px;
  }
  
  .section-title span:first-child {
    font-size: 16px;
  }
  
  .description {
    font-size: 13px;
  }
  
  .nav-links li {
    font-size: 13px;
    padding: 6px 0;
  }
  
  .contact-item {
    font-size: 12px;
  }
}
</style>
