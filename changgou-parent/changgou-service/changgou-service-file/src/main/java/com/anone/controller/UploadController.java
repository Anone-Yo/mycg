package com.anone.controller;

import entity.Result;
import entity.StatusCode;
import com.anone.file.FastDFSFile;
import com.anone.util.FastDFSClient;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传的控制层
 */
@RestController
public class UploadController {

    //文件下载
    @PostMapping
    public Result upload(@RequestParam("file") MultipartFile file) throws Exception {
        //获取文件名
        String originalFilename = file.getOriginalFilename();
        //获取文件内容
        byte[] bytes = file.getBytes();
        //获取文件扩展名
        String ext = StringUtils.getFilenameExtension(originalFilename);
        //创建FastDFs对象
        FastDFSFile fastDFSFile=new FastDFSFile(originalFilename,bytes,ext);
        //文件上传，并返回文件存放的路径信息
        String[] uploadFile = FastDFSClient.uploadFile(fastDFSFile);
        //获取上传文件的路径
        String url="http://192.168.211.132:8080/"+uploadFile[0]+"/"+uploadFile[1];
        return new Result(true, StatusCode.OK,"upload dull",url);
        //http://192.168.211.132:8080/group1/M00/00/00/wKjThF1k8M6EJerGAAAAABZOEhM716.jpg
    }



}
