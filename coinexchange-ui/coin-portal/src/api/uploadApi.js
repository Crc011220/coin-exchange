import request from './request'

export const uploadApi = {
  // aliyunUrl: process.env.BASE_API + "/v2/s/image/AliYunImgUpload",
  // normalUrl: process.env.BASE_API + "/v2/s/image/commonImgUpload",
  aliyunFileUrl:'https://coin-exchange-imgs.s3.ap-southeast-2.amazonaws.com/',
  getPreUpload() {
    return request({
      url: `/admin/image/pre/upload`,
      method: 'get'
    })
  }
};

// 直接上传到 S3
export async function uploadToS3(file) {
  try {
    const { data } = await uploadApi.getPreUpload();  // 拿到预签名 URL
    const presignedUrl = data.url;
    const fileType = file.type || 'application/octet-stream';

    const res = await fetch(presignedUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': fileType
      },
      body: file
    });

    if (!res.ok) throw new Error('Upload failed');

    return {
      success: true,
      fileUrl: presignedUrl.split('?')[0]  // 这是最终上传后的地址
    };
  } catch (err) {
    console.error("Upload error:", err);
    return { success: false, error: err };
  }
}