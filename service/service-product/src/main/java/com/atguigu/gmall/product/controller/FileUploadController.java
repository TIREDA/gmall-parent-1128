package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.test.TestDfs;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("admin/product/")
@CrossOrigin
public class FileUploadController {


    @RequestMapping("fileUpload")
    private static Result<String> fileUpload(MultipartFile file) throws IOException, MyException {
        // 读取配置文件
        String path = FileUploadController.class.getClassLoader().getResource("tracker.conf").getPath();
        ClientGlobal.init(path);
        // 创建tracker连接
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = trackerClient.getConnection();
        // 创建storage
        StorageClient storageClient = new StorageClient(connection,null);
        // 上传文件
        String originalFilename = file.getOriginalFilename();

//        int i = originalFilename.lastIndexOf(".");
//        String substring = originalFilename.substring(i);
        String extension = FilenameUtils.getExtension(originalFilename);

        String[] jpgs = storageClient.upload_file(file.getBytes(), extension, null);
        String imageUrl = "http://192.168.222.2:8080";
        for (String jpg : jpgs) {
            imageUrl = imageUrl + "/" + jpg;
        }
        return Result.ok(imageUrl);
    }
}
