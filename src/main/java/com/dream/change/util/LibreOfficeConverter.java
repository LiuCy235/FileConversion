package com.dream.change.util;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

import java.io.File;

public class LibreOfficeConverter {

    // PDF转Word
    public static void convertPdfToWord(File inputFile, File outputFile, String officeHome) throws Exception {
        OfficeManager officeManager = null;
        try {
            // 启动Office管理器
            officeManager = LocalOfficeManager.builder()
                    .officeHome(officeHome)
                    .install()
                    .build();
            officeManager.start();

            // 执行转换
            LocalConverter.builder()
                    .officeManager(officeManager)
                    .build()
                    .convert(inputFile)
                    .to(outputFile)
                    .execute();
        } finally {
            // 确保关闭Office管理器
            if (officeManager != null) {
                try {
                    officeManager.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Word转PDF
    public static void convertWordToPdf(File inputFile, File outputFile, String officeHome) throws Exception {
        OfficeManager officeManager = null;
        try {
            // 启动Office管理器
            officeManager = LocalOfficeManager.builder()
                    .officeHome(officeHome)
                    .install()
                    .build();
            officeManager.start();

            // 执行转换
            LocalConverter.builder()
                    .officeManager(officeManager)
                    .build()
                    .convert(inputFile)
                    .to(outputFile)
                    .execute();
        } finally {
            // 确保关闭Office管理器
            if (officeManager != null) {
                try {
                    officeManager.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        // 定义LibreOffice安装路径
        String os = System.getProperty("os.name").toLowerCase();
        String officeHome;
        if (os.contains("windows")) {
            officeHome = "C:\\Program Files\\LibreOffice";
        } else if (os.contains("linux")) {
            officeHome = "/usr/lib/libreoffice";
        } else {
            throw new RuntimeException("不支持的操作系统");
        }

        // 输入和输出文件路径
        File inputFile = new File("D:\\pdf\\向日葵返利销售合同-（河北马瑞华53178）.pdf");
        File outputFile = new File("D:\\word\\向日葵返利销售合同-（河北马瑞华53178）.doc");

        try {
            // 执行转换
            convertPdfToWord(inputFile, outputFile, officeHome);
            System.out.println("转换完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}