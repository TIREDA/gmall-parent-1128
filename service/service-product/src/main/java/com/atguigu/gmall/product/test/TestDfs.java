package com.atguigu.gmall.product.test;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

public class TestDfs {

    public static void main(String[] args) throws IOException, MyException {
        fileUpload();


    }

    private static String fileUpload() throws IOException, MyException {
        // 读取配置文件
        String path = TestDfs.class.getClassLoader().getResource("tracker.conf").getPath();

        System.out.println(path);

        ClientGlobal.init(path);

        // 创建tracker连接
        TrackerClient trackerClient = new TrackerClient();

        TrackerServer connection = trackerClient.getConnection();


        // 创建storage
        StorageClient storageClient = new StorageClient(connection,null);

        // 上传文件
        String[] jpgs = storageClient.upload_file("d:/hw.jpg", "jpg", null);

        String imageUrl = "http://192.168.222.2:8080";

        for (String jpg : jpgs) {

            imageUrl = imageUrl + "/" + jpg;
        }

        System.out.println("================================"+imageUrl);

        return imageUrl;
    }
}
