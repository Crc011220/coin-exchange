<template>
  <div class="ucenter-wrap" v-wechat-title="$t('m.userCenter.advancedCertification.title')">
    <NavHeader activeTab="5"></NavHeader>

    <div class="ucenter-content">
      <div class="ucenter-row">
        <sidebar :activeId="1"></sidebar>
        <div class="ucenter-main">
          <div class="ucenter-header">
            <h3 class="title">{{$t('m.userCenter.advancedCertification.title')}}</h3>
          </div>
          <div class="ucenter-body">
            <el-alert type="warning"
                      :title="$t('m.userCenter.advancedCertification.uploadSizeLimit')"></el-alert>
            <el-form ref="ruleForm" :model="ruleForm" :rules="rules">
              <div class="item">
                <div class="item-title">{{$t('m.userCenter.advancedCertification.cardFrontTips')}}
                </div>
                <el-row :gutter="24">
                  <el-col :span="10">
                    <el-form-item prop="imageUrl1">
                      <div class="avatar-uploader">
                        <img v-if="ruleForm.imageUrl1" :src="ruleForm.imageUrl1" class="avatar">
                        <div 
                          v-else 
                          class="el-upload el-upload--text"
                          @click="handleUploadClick(1)"
                        >
                          <i class="el-icon-plus avatar-uploader-icon"></i>
                          <input 
                            type="file" 
                            ref="fileInput1" 
                            style="display: none"
                            @change="handleFileSelected($event, 1)"
                          />
                        </div>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="4" class="eg">
                    {{$t('m.userCenter.advancedCertification.example')}}<i
                    class="el-icon-caret-right"></i>
                  </el-col>
                  <el-col :span="10">
                    <img src="../../assets/usercenter/demo-id-a.jpg">
                  </el-col>
                </el-row>
              </div>

              <div class="item">
                <div class="item-title">{{$t('m.userCenter.advancedCertification.cardBackTips')}}
                </div>
                <el-row :gutter="24">
                  <el-col :span="10">
                    <el-form-item prop="imageUrl2">
                      <div class="avatar-uploader">
                        <img v-if="ruleForm.imageUrl2" :src="ruleForm.imageUrl2" class="avatar">
                        <div 
                          v-else 
                          class="el-upload el-upload--text"
                          @click="handleUploadClick(2)"
                        >
                          <i class="el-icon-plus avatar-uploader-icon"></i>
                          <input 
                            type="file" 
                            ref="fileInput2" 
                            style="display: none"
                            @change="handleFileSelected($event, 2)"
                          />
                        </div>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="4" class="eg">
                    {{$t('m.userCenter.advancedCertification.example')}}<i
                    class="el-icon-caret-right"></i>
                  </el-col>
                  <el-col :span="10">
                    <img src="../../assets/usercenter/demo-id-b.jpg">
                  </el-col>
                </el-row>
              </div>

              <div class="item">
                <div class="item-title">
                  {{$t('m.userCenter.advancedCertification.frontPhotoAndSignatureTips')}}
                  <p class="description">
                    {{$t('m.userCenter.advancedCertification.frontPhotoAndSignatureDesc')}}</p>
                </div>
                <el-row :gutter="24">
                  <el-col :span="10">
                    <el-form-item prop="imageUrl3">
                      <div class="avatar-uploader">
                        <img v-if="ruleForm.imageUrl3" :src="ruleForm.imageUrl3" class="avatar">
                        <div 
                          v-else 
                          class="el-upload el-upload--text"
                          @click="handleUploadClick(3)"
                        >
                          <i class="el-icon-plus avatar-uploader-icon"></i>
                          <input 
                            type="file" 
                            ref="fileInput3" 
                            style="display: none"
                            @change="handleFileSelected($event, 3)"
                          />
                        </div>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="4" class="eg">
                    {{$t('m.userCenter.advancedCertification.example')}}<i
                    class="el-icon-caret-right"></i>
                  </el-col>
                  <el-col :span="10">
                    <img src="../../assets/usercenter/demo-id-c.jpg">
                  </el-col>
                </el-row>
              </div>
              <div class="submit-btn">
                <el-button type="primary" @click="submitForm('ruleForm')">{{$t('m.yes')}}
                </el-button>
              </div>
            </el-form>

          </div>
        </div>
      </div>
    </div>
    <div class="ucenter-footer">
      <mFooter class="footer-bar"></mFooter>
    </div>
  </div>
</template>
<script>
  import ToggleButton from 'vue-js-toggle-button'
  import Vue from 'vue'
  import NavHeader from 'components/nav-header/nav-header'
  import mFooter from 'components/m-footer/m-footer'
  import {util} from 'common/js/util'
  import {getchilds, getLoginRecord, setAuth, seniorAuth} from 'api/usercenter'
  import {OK} from 'api/config'
  import sidebar from 'components/usercenter/sidebar'
  import {uploadApi} from "../../api/uploadApi";
  import {mapGetters} from 'vuex'

  Vue.use(ToggleButton)
  export default {
    computed: {
      ...mapGetters(['token'])
    },
    data() {
      return {
        // S3上传相关
        uploadInProgress: false,
        s3UploadData: null,
        
        uploadImgUrl: uploadApi.aliyunUrl,
        imageUrl: '',
        ruleForm: {
          imageUrl1: '',
          imageUrl2: '',
          imageUrl3: '',
        },
        rules: {
          imageUrl1: [
            {
              validator: (rule, value, callback) => {
                if (value === '') {
                  callback(new Error(this.$t('m.userCenter.advancedCertification.plzUpFrontPhoto')));
                } else {
                  callback()
                }
              },
              trigger: 'blur'
            }
          ],
          imageUrl2: [
            {
              validator: (rule, value, callback) => {
                if (value === '') {
                  callback(new Error(this.$t('m.userCenter.advancedCertification.plzUpBackPhoto')));
                } else {
                  callback()
                }
              },
              trigger: 'blur'
            }
          ],
          imageUrl3: [
            {
              validator: (rule, value, callback) => {
                if (value === '') {
                  callback(new Error(this.$t('m.userCenter.advancedCertification.plzUpHandheldPhoto')));
                } else {
                  callback()
                }
              },
              trigger: 'blur'
            }
          ]
        }
      }
    },
    created() {

    },
    mounted() {
      console.log("token", this.token)
    },
    components: {
      NavHeader,
      mFooter,
      sidebar
    },

    methods: {
      // 点击上传按钮，触发文件选择
      handleUploadClick(imageIndex) {
        this.$refs[`fileInput${imageIndex}`].click();
      },
      
      // 检查文件格式和大小
      checkFile(file) {
        const isImage = file.type.startsWith('image/');
        const isLt3M = file.size / 1024 / 1024 < 3;

        if (!isImage) {
          this.$message.error(this.$t('m.userCenter.advancedCertification.uploadFormatError'));
          return false;
        }
        if (!isLt3M) {
          this.$message.error(this.$t('m.userCenter.advancedCertification.uploadPhotoSizeLimit'));
          return false;
        }
        return true;
      },
      
      // 获取预签名URL
      async getPresignedUrl() {
        try {
          const response = await uploadApi.getPreUpload();
          console.log('Pre-upload response:', response);
          if (response.data && response.data.url) {
            return response.data;
          } else {
            throw new Error('Invalid pre-upload response');
          }
        } catch (error) {
          console.error('Error getting presigned URL:', error);
          this.$message.error('获取上传链接失败');
          return null;
        }
      },
      
      // 处理文件选择并上传
      async handleFileSelected(event, imageIndex) {
        const file = event.target.files[0];
        if (!file) return;
        
        // 检查文件
        if (!this.checkFile(file)) {
          return;
        }
        
        // 显示加载中
        const loadingInstance = this.$loading({
          lock: true,
          text: '上传中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        try {
          // 获取预签名URL
          const uploadData = await this.getPresignedUrl();
          if (!uploadData) {
            loadingInstance.close();
            return;
          }
          
          // 直接使用PUT请求上传到S3
          const response = await fetch(uploadData.url, {
            method: 'PUT',
            headers: {
              'Content-Type': file.type
            },
            body: file
          });
          
          if (!response.ok) {
            const errorText = await response.text();
            console.error('Upload failed:', errorText);
            throw new Error('Upload failed: ' + errorText);
          }
          
          // 构建文件访问URL
          const fileUrl = `https://${uploadData.bucket}.s3.${uploadData.region}.amazonaws.com/${uploadData.objectKey}`;
          
          // 根据imageIndex设置对应的URL
          this.ruleForm[`imageUrl${imageIndex}`] = fileUrl;
          
          this.$message.success('上传成功');
        } catch (error) {
          console.error('Upload error:', error);
          this.$message.error('上传失败: ' + error.message);
        } finally {
          loadingInstance.close();
          // 清空文件输入，允许上传相同文件
          event.target.value = '';
        }
      },

      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let avatar = [this.ruleForm.imageUrl1, this.ruleForm.imageUrl2, this.ruleForm.imageUrl3]
            console.log(avatar)
            this._seniorAuth(avatar)
          } else {
            console.log('error submit!!');
            return false;
          }
        });
      },
      
      async _seniorAuth(avatar) {
        await seniorAuth(avatar, this.token)
        this.$notify({
          type: 'success',
          title: this.$t('m.prompt'),
          message: this.$t('m.userCenter.advancedCertification.uploadSuccess')
        });
        this.$router.push("modify-id")
      },
      
      open2() {
        const h = this.$createElement;
        this.$msgbox({
          title: this.$t('m.userCenter.certificationTip'),
          message: h('p', null, [
            h('div', null),
            h('div', {style: 'color: teal'}, this.$t('m.userCenter.certificationSuccess'))
          ]),
          confirmButtonText: this.$t('m.yes'),
          beforeClose: (action, instance, done) => {
            if (action === 'confirm') {
              done();
              this.$router.push({path: '/usercenter/modify-id'});
            } else {
              done();
              this.$router.push({path: '/usercenter/modify-id'});
            }
          }
        }).then(action => {
          this.$message({
            type: 'info',
            message: 'action: ' + action
          });
        });
      }
    }
  }
</script>
<style lang="stylus" rel="stylesheet/stylus">
  @import "~common/stylus/usercenter"
</style>
<style scoped lang="stylus" rel="stylesheet/stylus">
  .ucenter-wrap {
    .submit-btn {
      text-align center
      margin 10px 0
    }
    .item {
      border-bottom 1px solid #ededed
      padding 15px 0
      font-size 14px
      .item-title {
        text-align center
        padding-bottom 15px
        .description {
          color #999
          margin-top 10px
        }
      }
      .eg {
        margin-top 100px
        text-align center
      }
    }

    .avatar-uploader {
      height: 200px;
      background: #eee;
      margin: auto;
    }
    .avatar-uploader .el-upload {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      width: 348px;
      height: 200px;
    }
    .avatar-uploader .el-upload:hover {
      border-color: #409EFF;
    }
    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 348px;
      height: 200px;
      line-height: 200px;
      text-align: center;
    }
    .avatar {
      display: block;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
</style>