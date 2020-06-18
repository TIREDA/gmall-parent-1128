package com.atguigu.gmall.all.controller;


import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestMain {
    public static void main(String[] args) throws IOException {

        List<String> list = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            list.add(""+i);
        }

        String join = StringUtils.join(list,"|");

        System.out.println(join);


        File file = new File("c:/a/a.json");

        FileOutputStream fos = new FileOutputStream(file);

        fos.write(join.getBytes());
    }
}
