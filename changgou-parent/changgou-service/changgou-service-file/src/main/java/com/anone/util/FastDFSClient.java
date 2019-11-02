package com.anone.util;


import com.anone.file.FastDFSFile;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * fastDFs 客户端对象
 */
public class FastDFSClient {
    /**
     * 初始化Tracker
     */
    static {
        //获取配置文件的路径
        String path = new ClassPathResource("fdfs_client.conf").getPath();
        try {
            //初始化Tracker
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     */
    public static String[] uploadFile(FastDFSFile fastDFSFile) throws Exception {
        //获取Tracker客户端对象
        StorageClient storageClient = getStorageClient();
        //获取storage客户端
        //使用storage客户端进行文件上传操作
        //参数1：上传的内容  参数2：扩展名  参数3：自定义的属性
        String[] uploadFile = storageClient.upload_appender_file(fastDFSFile.getContent(), fastDFSFile.getExt(), null);
        return uploadFile;

    }

    /**
     * 文件下载
     */
    public static InputStream downloadFile(String groupName, String remoteFileName) throws Exception {
        //获取Tracker客户端
        StorageClient storageClient = getStorageClient();
        //进行下载草
        //参数1：组名 参数2：文件的路径详细信息
        byte[] bytes = storageClient.download_file(groupName, remoteFileName);
        //将字节数组转换为输入流
        InputStream is=new ByteArrayInputStream(bytes);
        return is;
    }

    /**
     * 获取文件信息
     */
    public static FileInfo getFileInfo(String groupName, String remoteFileName) throws Exception {
        //获取Tracker客户端
        StorageClient storageClient = getStorageClient();
        //获取文件信息
        FileInfo fileInfo = storageClient.get_file_info(groupName, remoteFileName);
        return fileInfo;
    }

    /**
     * 删除文件信息
     */
    public static int deleteFile(String groupName, String remoteFileName) throws Exception {
        StorageClient storageClient = getStorageClient();

        //删除文件信息
        int para = storageClient.delete_file(groupName, remoteFileName);
        //返回成功参数0，！0就是删除失败
        return para;
    }
    //抽取的方法===获取storage客户端
    public static StorageClient getStorageClient() throws IOException {
        //获取Tracker客户端
        TrackerClient trackerClient=new TrackerClient();
        //获取Tracker服务
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取storage客户端
        return new StorageClient(trackerServer,null);
    }

    /**
     * 获取storage信息
     */
    public static StorageServer getStorage() throws Exception {
        TrackerClient trackerClient=new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storage = trackerClient.getStoreStorage(trackerServer);
        return storage;
    }

    /**
     * 获取tracker信息
     */
    public static TrackerServer getTracker() throws Exception {
        TrackerClient trackerClient=new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;
    }

    //测试
    public static void main(String[] args) throws Exception{
        String groupName="group1";
        String remoteName="M00/00/00/wKjThF1k8M6EJerGAAAAABZOEhM716.jpg";
       /* //获取上传文件信息
        FileInfo fileInfo = getFileInfo(groupName, remoteName);
        System.out.println(fileInfo);*/

    /*   //文件下载
        InputStream is = downloadFile(groupName, remoteName);
        //设置循环遍历bytes
      byte[] bytes=new byte[1024];
        //创建输出流
        FileOutputStream os=new FileOutputStream("D:/1.jpg");
        while ((is.read(bytes)) != -1) {
            os.write(bytes);
        }
        os.flush();
        os.close();
        is.close();*/

    //删除文件
        /*int row = deleteFile(groupName, remoteName);
        System.out.println(row);*/
    }



}