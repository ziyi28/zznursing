package com.zzyl;

import com.zzyl.common.utils.PDFUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @Author: ziyi
 * @Date: 2026/5/22 15:10
 * @Version: v1.0.0
 * @Description: TODO
 **/
public class PDFTest {
    public static void main(String[] args) throws FileNotFoundException {
        InputStream is= new FileInputStream("E:\\01_Projects\\zzyl\\20260515101240\\体检报告样例\\体检报告-刘爱国-男-69岁.pdf");
        String content = PDFUtil.pdfToString(is);
        System.out.println(content);


    }
}
