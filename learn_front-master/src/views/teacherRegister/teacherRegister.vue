<template>
  <div class="login">
	<div class="Rectangle">
		<img style="width:300px" src="../../assets/image/Rectangle.png">
	</div>
	<div class="login-top">
		<div class="logo">
				<img style="width:120px;padding-left:100px" src="../../assets/image/brand/zhixue-brand-mark.png" :alt="$t('智学云途')">
		</div>
		<div class="btns">
			<div style="padding-right:100px;display:flex">
				<div class="toReg" @click="toRegister">
					<div>{{ $t("注册") }}</div>
				</div>
				<div class="toRegTeacher" @click="toLogin">
					<div>{{ $t("登陆") }}</div>
				</div>
			</div>
		</div>
	</div>
	<div class="login-center">
		<div class="about">
			<div style="text-align:center">
				<div class="huanying">{{ $t("欢迎你的加入！") }}</div>
				<div class="huanying1">{{ $t("书本上有这样一段话：“教育的意义在于让人明白作为人类社会中的一员所应承担的责任与义务。") }}</div>
				<div class="huanying1">{{ $t("只有当一个人明白了自己的责任与义务，才能成为对社会有用的人，只有完成了自己的责任与义务，") }}</div>
				<div class="huanying1">{{ $t("才能最终成为真正有用的人。") }}</div>
			</div>
		</div>
		<div class="teacherFrom">
			<div class="ruzhu-content">
				<div class="ruzhu-title">{{ $t("入驻信息") }}</div>
				<el-form :model="form" :rules="rules" ref="ruleForm" label-width="0px" class="demo-ruleForm">
					<el-form-item prop="userName">
						<div class="item-title">{{ $t("姓名：") }}</div>
						<el-input v-model="form.userName"></el-input>
					</el-form-item>
					<el-form-item prop="password">
						<div class="item-title">{{ $t("密码：") }}</div>
						<el-input type="password" v-model="form.password"></el-input>
					</el-form-item>
					<el-form-item prop="passwordNew">
						<div class="item-title">{{ $t("确认密码：") }}</div>
						<el-input type="password" v-model="form.passwordNew"></el-input>
					</el-form-item>
					<el-form-item prop="loginAccount">
						<div class="item-title">{{ $t("登陆账号：") }}</div>
						<el-input v-model="form.loginAccount"></el-input>
					</el-form-item>
					<el-form-item prop="email">
						<div class="item-title">{{ $t("邮箱：") }}</div>
						<el-input v-model="form.email"></el-input>
					</el-form-item>
					<el-form-item prop="tel">
						<div class="item-title">{{ $t("联系方式：") }}</div>
						<el-input v-model="form.tel"></el-input>
					</el-form-item>
					<el-form-item prop="country">
						<div class="item-title">{{ $t("国家：") }}</div>
						<el-select v-model="form.country">
							<el-option :label="$t('中国')" value="中国"></el-option>
							<el-option :label="$t('日本')" value="日本"></el-option>
							<el-option :label="$t('韩国')" value="韩国"></el-option>
							<el-option :label="$t('美国')" value="美国"></el-option>
							<el-option :label="$t('俄罗斯')" value="俄罗斯"></el-option>
							<el-option :label="$t('印度')" value="印度"></el-option>
							<el-option :label="$t('巴西')" value="巴西"></el-option>
							<el-option :label="$t('新加坡')" value="新加坡"></el-option>
							<el-option :label="$t('加拿大')" value="加拿大"></el-option>
						</el-select>
					</el-form-item>
					<el-form-item prop="country">
						<div class="item-title">{{ $t("性别：") }}</div>
						<el-select v-model="form.sex">
							<el-option :label="$t('男')" value="0"></el-option>
							<el-option :label="$t('女')" value="1"></el-option>
						</el-select>
					</el-form-item>
					<el-form-item prop="school">
						<div class="item-title">{{ $t("院校：") }}</div>
						<el-select :model-value="$demoSource(form, 'school')" @update:model-value="form.school = $event">
							<el-option v-for="(item,index) in school" :key="item.id || index" :label="item.name" :value="$demoSource(item, 'name')"></el-option>
						</el-select>
					</el-form-item>
					<el-form-item prop="major">
						<div class="item-title">{{ $t("专业：") }}</div>
						<el-select :model-value="$demoSource(form, 'major')" @update:model-value="form.major = $event">
							<el-option v-for="(item,index) in major" :key="item.id || index" :label="item.name" :value="$demoSource(item, 'name')"></el-option>
						</el-select>
					</el-form-item>
					<el-form-item prop="agree">
						<div class="item-title">{{ $t("职称：") }}</div>
						<el-select v-model="form.agree">
							<el-option :label="$t('初级讲师')" value="初级讲师"></el-option>
							<el-option :label="$t('中级讲师')" value="中级讲师"></el-option>
							<el-option :label="$t('高级讲师')" value="高级讲师"></el-option>
							<el-option :label="$t('副教授')" value="副教授"></el-option>
							<el-option :label="$t('教授')" value="教授"></el-option>
							<el-option :label="$t('院士')" value="院士"></el-option>
						</el-select>
					</el-form-item>
					<el-form-item prop="age">
						<div class="item-title">{{ $t("年龄：") }}</div>
						<el-input type="number" v-model="form.age"></el-input>
					</el-form-item>
					<el-form-item prop="birth">
						<div class="item-title">{{ $t("生日：") }}</div>
						<el-date-picker
							v-model="form.birth"
							type="date">
						</el-date-picker>
					</el-form-item>
					<el-form-item prop="workDate">
						<div class="item-title">{{ $t("教龄：") }}</div>
						<el-input type="number" v-model="form.workDate"></el-input>
					</el-form-item>
					<el-form-item prop="address">
						<div class="item-title">{{ $t("地址：") }}</div>
						<el-input type="textarea" rows="3" v-model="form.address"></el-input>
					</el-form-item>
					<el-form-item prop="flair">
						<div class="item-title">{{ $t("资质：") }}</div>
						<el-upload :on-error="$uploadError"
							:file-list="fileList"
							ref="upload"
							:action="uploadImageUrl()"
                        :headers="uploadHeaders()"
							accept="image/*"
							:multiple="false"
							:limit="1"
							:before-upload="beforeAvatorUpload"
							list-type="picture-card"
							:on-preview="handlePictureCardPreview"
							:on-exceed="handleExceed"
							:on-success="handleAvatorSuccess"
							:on-remove="handleRemove">
							<i class="el-icon-plus"></i>
						</el-upload>
						<el-dialog append-to-body v-model="dialogVisible">
							<img width="100%" :src="dialogImageUrl" alt="">
						</el-dialog>
					</el-form-item>
					<el-form-item prop="remark">
						<div class="item-title">{{ $t("自我介绍：") }}</div>
						<el-input type="textarea" rows="8" v-model="form.remark"></el-input>
					</el-form-item>
				</el-form>
				<div class="ruzhu">
					<div class="ruzhu-btn" @click="toRuZhu()">
						<div>{{ $t("入 驻") }}</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<bottomPage></bottomPage>
  </div>
</template>

<script>
import { t } from '@/i18n'

  import {getApeSchoolList,getApeMajorList,register} from '../../api/api'
  import bottomPage from "../../components/bottom/login-bottom.vue"
  import { mixin } from '../../minix/index';
  import {validateAccount, validatePassword} from '../../utils/accountPasswordRules'
  export default {
	mixins: [mixin],
	data() {
	  const validateConfirmPassword = (rule, value, callback) => {
		if (!value || !value.trim()) {
		  callback(new Error('请输入确认密码'))
		  return
		}
		if (value !== this.form.password) {
		  callback(new Error('两次输入的密码不一致'))
		  return
		}
		callback()
	  }
	  return{
		userInfo: {
			username: "",
			password: ""
		},
		school: [],
		major: [],
		dialogImageUrl: '',
        dialogVisible: false,
		form: {
			userName: "",
			password: "",
			passwordNew: "",
			loginAccount: "",
			email: "",
			tel: "",
			sex: "0",
			country: "",
			school: "",
			major: "",
			agree: "",
			age: "",
			birth: "",
			flair: "",
			address: "",
			workDate: "",
			remark: "",
			userType: 1,
			status: 1,
		},
		fileList: [],
		rules: {
          userName: [
            { required: true, get message() { return t("请输入用户名") }, trigger: 'blur' },
          ],
          password: [
            { validator: validatePassword, trigger: 'blur' }
		  ],
		  passwordNew: [
            { validator: validateConfirmPassword, trigger: 'blur' }
		  ],
		  loginAccount: [
            { validator: validateAccount, trigger: 'blur' }
		  ],
		  email: [
            { required: true, get message() { return t("请输入邮箱") }, trigger: 'blur' }
		  ],
		  tel: [
            { required: true, get message() { return t("请输入联系方式") }, trigger: 'blur' }
		  ],
		  sex: [
            { required: true, get message() { return t("请选择性别") }, trigger: 'blur' }
		  ],
		  country: [
            { required: true, get message() { return t("请选择国家") }, trigger: 'blur' }
		  ],
		  school: [
            { required: true, get message() { return t("请选择院校") }, trigger: 'blur' }
		  ],
		  major: [
            { required: true, get message() { return t("请选择专业") }, trigger: 'blur' }
		  ],
		  agree: [
            { required: true, get message() { return t("请选择职称") }, trigger: 'blur' }
		  ],
		  age: [
            { required: true, get message() { return t("请输入年龄") }, trigger: 'blur' }
		  ],
		  birth: [
            { required: true, get message() { return t("请输入出生日期") }, trigger: 'blur' }
		  ],
		  address: [
            { required: true, get message() { return t("请输入地址") }, trigger: 'blur' }
		  ],
		  workDate: [
            { required: true, get message() { return t("请输入教龄") }, trigger: 'blur' }
		  ],
		  flair: [
            { required: true, get message() { return t("请上传教学资质") }, trigger: 'blur' },
          ],
		  remark: [
            { required: true, get message() { return t("请输入自我介绍") }, trigger: 'blur' }
          ],
        }
	  }
	},
	components: {
        bottomPage
	},
	methods: {
		handleAvatorSuccess(res) {
			let _this = this;
			if(res.code == 1000){
				_this.$message({
				type: 'success',
				get message() { return t("上传成功!") }
				});
				this.fileList.push({url:this.$config.HOST + res.message})
				this.form.flair = res.message
			}else{
			_this.$notify.error({
				get title() { return t("错误") },
				message: res.message
			});
			}
		},
		handleRemove(file, fileList) {
			this.form.flair = ""
			this.fileList = []
		},
		handlePictureCardPreview(file) {
			this.dialogImageUrl = file.url;
			this.dialogVisible = true;
		},
		getApeSchoolList() {
			getApeSchoolList().then(res=> {
			if(res.code == 1000) {
				this.school = res.data
			} else {
				this.$notify.error({
				get title() { return t("错误") },
				message: res.message
				});
			}
			}).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
		},
		getApeMajorList() {
			getApeMajorList().then(res => {
			if(res.code == 1000) {
				this.major = res.data
			} else {
				this.$notify.error({
				get title() { return t("错误") },
				message: res.message
				});
			}
			}).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
		},
		toRegister() {
			this.$router.push("/register")
		},
		toLogin() {
			this.$router.push("/login")
		},
		toRuZhu() {
			this.$refs["ruleForm"].validate((valid) => {
				if (valid) {
					if (this.form.passwordNew != this.form.password) {
						this.$message({
							get message() { return t("两次输入的密码不一致！") },
							type: 'warning'
						});
						return;
					}
					if (this.fileList.length <= 0) {
						this.$message({
							get message() { return t("请上传教学资质！") },
							type: 'warning'
						});
						return;
					}
					register(this.form).then(res => {
						if(res.code == 1000) {
							this.$message({
								get message() { return t("入驻成功,请等待平台审核") },
								type: 'success'
							});
							var that = this
							setTimeout(function() {
								that.$router.push("/login")
							},500)
						} else {
							this.$message.error(res.message);
						}
					}).catch(() => { /* Feedback is provided by the request layer. */ if ("loading" in this) this.loading = false })
				} else {
					return false;
				}
			});
		}
	},
	created() {
	 
	},
	mounted() {
	  this.getApeSchoolList()
	  this.getApeMajorList()
	}
 }
</script>

<style scoped>
  .login {
	  position: relative;
	  width: 100%;
	  height: 100%;
	  background-color: #FCFCFC;
  }
  .login-top {
	  width: 100%;
	  height: 100px;
	  display: flex;
	  flex-direction: row;
	  align-items: center;
	  justify-content: space-between;
  }
  .logo {
	  width: 55%;
	  text-align: center;
	  height: 100%;
	  display: flex;
	  align-items: center;
  }
  .btns {
	  width: 45%;
	  display: flex;
	  background-color: #ffffff;
	  height: 100%;
	  display: flex;
	  align-items: center;
	  justify-content: flex-end;
  }
  .toReg {
	  width: 130px;
	  height: 40px;
	  border:1px solid #474747;
	  font-family: '黑体';
	  cursor: pointer;
	  display: flex;
	  justify-content: center;
	  align-items: center;
  }
  .toRegTeacher {
	  margin-left: 20px;
	  width: 130px;
	  height: 40px;
	  border:1px solid #474747;
	  font-family: '黑体';
	  cursor: pointer;
	  display: flex;
	  justify-content: center;
	  align-items: center;
  }
  .login-bottom {
	  width: 100%;
	  height: 250px;
	  background-color: #262543;
	  display: flex;
  }
  .login-content {
	  width: 55%;
	  height: 100%;
	  display: flex;
	  flex-direction: column;
  }
  .login-form {
	  position: relative;
	  display: flex;
	  justify-content: center;
	  width: 45%;
	  height: 100%;
	  background-color: #ffffff;
  }
  .title {
	  font-weight: '黑体';
	  font-size: 55px;
	  font-weight: bold;
  }
  .jiantou1 {
	  position: absolute;
	  left: 10px;
	  top: 20px;
  }
  .jiantou2 {
	  position: absolute;
	  left: 10px;
	  bottom: 100px;
  }
  .yuan {
	  position: absolute;
	  right: 10px;
	  bottom: 200px;
  }
  .form {
	  width: 50%;
	  height: 70%;
	  display: flex;
	  flex-direction: column;
	  align-items: center;
	  justify-content: space-around;
  }
  .login-title {
	  font-size: 50px;
	  font-weight: bold;
	  height: 40%;
	  display: flex;
	  align-items: center;
  }
  .login-btn {
	  width: 130px;
	  height: 40px;
	  border:1px solid #FF5202;
	  background-color: #FF5202;
	  color: #ffffff;
	  font-size: 18px;
	  font-family: '黑体';
	  cursor: pointer;
	  display: flex;
	  justify-content: center;
	  align-items: center;
  }
  .bottom-one {
	  width: 34%;
	  height: 100%;
	  display: flex;
	  align-items: center;
	  justify-content: center;
	  flex-direction: column;
  }
  .bottom-two {
	  width: 22%;
	  height: 100%;
	  display: flex;
	  align-items: center;
	  justify-content: center;
	  flex-direction: column;
  }
  .bottom-three {
	  width: 22%;
	  height: 100%;
	  display: flex;
	  align-items: center;
	  justify-content: center;
	  flex-direction: column;
  }
  .bottom-four {
	  width: 22%;
	  height: 100%;
	  display: flex;
	  align-items: center;
	  justify-content: center;
	  flex-direction: column;
  }
  .one-1 {
	  font-size: 40px;
	  font-weight: bold;
	  color: #ffffff;
  }
  .one-2 {
	  margin-top: 15px;
	  font-size: 18px;
	  color: #ffffff;
	  font-family: '黑体';
  }
  .two-2 {
	  margin-top: 15px;
	  font-size: 25px;
	  color: #ffffff;
	  font-weight: bold;
  }
  .two-3 {
	  margin-top: 15px;
	  font-size: 18px;
	  color: #ffffff;
	  font-family: '黑体';
  }
  .about {
	  width: 100%;
	  height: 400px;
	  background-color: #FF9E00;
	  display: flex;
	  align-items: center;
	  justify-content: center;
  }
  .huanying {
	  font-size: 50px;
	  color: #ffffff;
	  font-weight: bold;
	  margin-bottom: 40px;
  }
  .huanying1 {
	  font-size: 20px;
	  color: #ffffff;
	  font-family: '黑体';
  }
  .Rectangle {
	  position: absolute;
	  left: -10px;
	  top: 80%;
  }
  .teacherFrom {
	  width: 100%;
	  background-image: url('../../assets/image/index/index_back.png');
	  display: flex;
	  justify-content: center;
  }
  .ruzhu-content {
	  width: 60%;
  }
  .ruzhu-title {
	  text-align: center;
	  color: black;
	  margin-top: 140px;
	  margin-bottom: 50px;
	  font-size: 50px;
	  font-weight: bold;
  }
  .item-title {
	  font-family: '黑体';
	  font-size: 20px;
  }
  .ruzhu {
	  display: flex;
	  justify-content: center;
	  margin-top: 80px;
  }
  .ruzhu-btn {
	  margin-bottom: 80px;
	  width: 130px;
	  height: 40px;
	  border:1px solid #FF5202;
	  background-color: #FF5202;
	  color: #ffffff;
	  font-size: 18px;
	  font-family: '黑体';
	  cursor: pointer;
	  display: flex;
	  justify-content: center;
	  align-items: center;
  }
</style>
