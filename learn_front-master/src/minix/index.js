export const mixin = {
    methods:{
        // 上传地址
        uploadImageUrl(){
            return this.$config.HOST + "/common/uploadImg"
        },
        uploadAvatarUrl(){
            return this.$config.HOST + "/user/setUserAvatar"
        },
        uploadFileUrl(){
            return this.$config.HOST + "/common/uploadFile"
        },
        uploadVideoUrl(){
            return this.$config.HOST + "/common/uploadVideo"
        },
        uploadHeaders(){
            return { x_access_token: window.localStorage.getItem("user_token") }
        },
        // 文件超出个数限制时的钩子
        handleExceed(files, fileList) {
            this.$message.warning("当前文件数量超出上传限制");
        },
        //上传图片之前的校验
        beforeAvatorUpload(file){
            const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif', 'image/bmp'];
            if (!validTypes.includes(file.type)) {
                this.$message.error('仅支持 JPG、PNG、WebP、GIF、BMP 格式的图片');
                return false;
            }
            const isLt2M = (file.size / 1024 /1024) < 2;
            if(!isLt2M){
                this.$message.error('上传图片单张大小不能超过2MB');
                return false;
            }
            return true;
        },
        handleExceed(files, fileList) {
            this.$message.warning(`本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件,超出文件个数限制`);
        },
    }
}


