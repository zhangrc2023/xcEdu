package org.xuecheng.media.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xuecheng.exception.XueChengPlusException;
import org.xuecheng.media.mapper.MediaFilesMapper;
import org.xuecheng.media.mapper.MediaProcessMapper;
import org.xuecheng.media.model.dto.QueryMediaParamsDto;
import org.xuecheng.media.model.dto.UploadFileParamsDto;
import org.xuecheng.media.model.dto.UploadFileResultDto;
import org.xuecheng.media.model.po.MediaFiles;
import org.xuecheng.media.model.po.MediaProcess;
import org.xuecheng.model.PageParams;
import org.xuecheng.model.PageResult;
import org.xuecheng.model.RestResponse;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileService currentProxy;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        return mediaFiles;
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    //根据扩展名获取mimeType
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;

    }

    /**
     * 将文件上传到minio
     *
     * @param localFilePath 文件本地路径
     * @param mimeType      媒体类型
     * @param bucket        桶
     * @param objectName    对象名
     * @return
     */
    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)//桶
                    .filename(localFilePath) //指定本地文件路径
                    .object(objectName)//对象名 放在子目录下
                    .contentType(mimeType)//设置媒体文件类型
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //    @Transactional
// 方法中包含网络请求时千万不要进行事务控制，因为网络请求的完成时间不受控制，导致数据库资源被较长占用时间
// 建议在仅包含数据库操作的方法进行事务控制，如方法addMediaFilesToDb()
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {

        //文件名
        String filename = uploadFileParamsDto.getFilename();
        //先得到扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //得到mimeType
        String mimeType = getMimeType(extension);

        //约定以"年/月/日"为子目录
        String defaultFolderPath = getDefaultFolderPath();
        //约定以上传文件的名字构建md5值作为存储的本地文件名
        String fileMd5 = getFileMd5(new File(localFilePath));
        //拼接defaultFolderPath/fileMd5得到上传文件在minio中的存储路径
        if (StringUtils.isEmpty(objectName)){
//            objectName表明上传的是静态页面文件（存放路径为minIO/mediafiles/course/下）
//            否则是图片文件存放路径为minIO/年/月/日/下）
            objectName = defaultFolderPath + fileMd5 + extension;
        }
        //上传文件到minio
        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
        if (!result) {
            XueChengPlusException.cast("上传文件失败");
        }
        //入库文件信息到数据库zrc_media中的表media_files
        // 为什么要用currentProxy？原因见196行
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件上传后保存信息失败");
        }

        //准备返回的对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }


    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     * @description 将文件信息添加到数据库表media_files
     * @author Mr.M
     * @date 2022/10/12 21:22
     */
    @Transactional
//    @Transactional仅对代理对象生效，而addMediaFilesToDb()只是实MediaFileServiceImpl的实例对象，实例对象事务注解是没用的
//    非事务方法调用同类一个事务方法，事务无法控制，意味着addMediaFilesToDb()方法运行时产生异常的话，事务无法回滚
//    解决办法很简单：只要注入容器就是代理对象，那么可以把方法提到接口成为抽象方法，然后通过自动注入容器绑定到类中一个属性即可得到代理对象
//    见52、53以及170行代码：
//    将addMediaFilesToDb()提升为接口MediaFileService的一个抽象方法，然后结合以下代码实现事务控制：
//    @Autowired
//    MediaFileService currentProxy;
//    MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
//    以上方法可能造成循环依赖的问题？解决办法->编程式事务
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //将文件信息保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            //文件id
            mediaFiles.setId(fileMd5);
            //机构id
            mediaFiles.setCompanyId(companyId);
            //桶
            mediaFiles.setBucket(bucket);
            //file_path
            mediaFiles.setFilePath(objectName);
            //file_id
            mediaFiles.setFileId(fileMd5);
            //url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            //状态
            mediaFiles.setStatus("1");
            //审核状态
            mediaFiles.setAuditStatus("002003");
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.debug("向数据库保存文件失败,bucket:{},objectName:{}", bucket, objectName);
                return null;
            }
            //记录待处理任务
            addWaitingTask(mediaFiles);
            return mediaFiles;

        }
        return mediaFiles;

    }

    /**
     * 视频上传成功的同时必须添加到待处理任务列表media_process中，等待调度中心分派任务给执行器进行视频转码
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        //文件名称
        String filename = mediaFiles.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //获取文件的 mimeType
        String mimeType = getMimeType(extension);
        if(mimeType.equals("video/x-msvideo")){//如果是avi视频写入待处理任务
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            //状态是未处理
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);//失败次数默认0
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }

    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //桶
            String bucket = mediaFiles.getBucket();
            //objectname
            String filePath = mediaFiles.getFilePath();
            //如果数据库存在再查询 minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            //查询远程服务获取到一个流对象
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null) {
                    //文件已存在
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //文件不存在
        return RestResponse.success(false);
    }


    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

        //根据md5得到分块文件所在目录的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        //如果数据库存在再查询 minio
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        //查询远程服务获取到一个流对象
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null) {
                //文件已存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //文件不存在
        return RestResponse.success(false);
    }


    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        //分块文件的路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        //获取mimeType
        String mimeType = getMimeType(null);
        //将分块文件上传到minio
        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFilePath);
        if (!b) {
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        //上传成功
        return RestResponse.success(true);
    }


    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //找到所有的分块文件
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath + i).build()).collect(Collectors.toList());
        //源文件名称
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //合并后文件的objectname
        String objectName = getFilePathByMd5(fileMd5, extension);
        //指定合并后的objectName等信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)//合并后的文件的objectname
                .sources(sources)//指定源文件
                .build();
        //===========合并文件============
        //报错size 1048576 must be greater than 5242880，minio默认的分块文件大小为5M
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错,bucket:{},objectName:{},错误信息:{}", bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }

        //===========校验合并后的和源文件是否一致，视频上传才成功===========
        //先下载合并后的文件
        File file = downloadFileFromMinIO(bucket_video, objectName);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            //计算合并后文件的md5
            String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
            //比较原始md5和合并后文件的md5
            if (!fileMd5.equals(mergeFile_md5)) {
                log.error("校验合并文件md5值不一致,原始文件:{},合并文件:{}", fileMd5, mergeFile_md5);
                return RestResponse.validfail(false, "文件校验失败");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(file.length());
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件校验失败");
        }

        //==============将文件信息入库============
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件入库失败");
        }
        //==========清理分块文件=========
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }

    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal          分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i)
                                               .limit(chunkTotal)
                                               .map(i -> new DeleteObject(chunkFileFolderPath + i))
                                               .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        //要想真正删除
        results.forEach(f -> {
            try {
                DeleteError deleteError = f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 从minio下载文件
     *
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 得到合并后的文件的地址
     *
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }


}
