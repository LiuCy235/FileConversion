package com.dream.change.service;

import com.dream.change.config.LibreOfficeConfig;
import com.dream.change.util.LibreOfficeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class WordToPdfService {

    @Autowired
    private LibreOfficeConfig libreOfficeConfig;

    public byte[] convertWordToPdf(MultipartFile wordFile) throws Exception {
        // 1. 创建临时文件
        String tempDir = System.getProperty("java.io.tmpdir");
        String tempWordPath = tempDir + File.separator + UUID.randomUUID() + "_input.docx";
        String tempPdfPath = tempDir + File.separator + UUID.randomUUID() + "_output.pdf";

        try {
            // 2. 保存上传的 Word 文件到临时路径
            try (FileOutputStream fos = new FileOutputStream(tempWordPath)) {
                fos.write(wordFile.getBytes());
            }

            // 3. 获取 LibreOffice 路径
            String officeHome = libreOfficeConfig.getOfficeHome();

            // 4. 调用 LibreOffice 转换
            LibreOfficeConverter.convertWordToPdf(new File(tempWordPath), new File(tempPdfPath), officeHome);

            // 5. 读取转换后的 PDF 文件
            File pdfFile = new File(tempPdfPath);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            java.nio.file.Files.copy(pdfFile.toPath(), out);
            return out.toByteArray();
        } finally {
            // 6. 清理临时文件
            new File(tempWordPath).delete();
            new File(tempPdfPath).delete();
        }
    }

}