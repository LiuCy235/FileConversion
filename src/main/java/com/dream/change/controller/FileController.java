package com.dream.change.controller;

import com.dream.change.annotation.NoRepeatSubmit;
import com.dream.change.service.PdfToWordService;
import com.dream.change.service.WordToPdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private PdfToWordService pdfToWordService;

    @Autowired
    private WordToPdfService wordToPdfService;

    /**
     * 检查 LibreOffice 是否安装
     *
     * @return 检查结果
     */
    @GetMapping("/check/libreoffice")
    public ResponseEntity<?> checkLibreOffice() {
        logger.info("接收到 LibreOffice 状态检查请求");
        boolean available = pdfToWordService.checkLibreOfficeAvailable();
        logger.info("LibreOffice 状态检查结果: {}", available ? "可用" : "不可用");
        return ResponseEntity.ok().body("{\"available\": " + available + ",\"message\": \"" + (available ? "LibreOffice 可用" : "LibreOffice 不可用") + "\"}");
    }

    /**
     * 上传 PDF 文件并转换为 Word
     *
     * @param file 上传的 PDF 文件
     * @return 转换后的 Word 文件
     */
    @PostMapping("/pdf-to-word")
    @NoRepeatSubmit(3) // 3秒内防止重复提交
    public ResponseEntity<?> convertPdfToWord(@RequestParam("file") MultipartFile file) {
        logger.info("接收到 PDF 转 Word 请求，文件名: {}", file.getOriginalFilename());

        // 检查文件是否为空
        if (file.isEmpty()) {
            logger.warn("上传文件为空");
            return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"请选择要上传的 PDF 文件\"}");
        }

        // 检查文件类型
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            logger.warn("文件类型错误: {}", file.getOriginalFilename());
            return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"请上传 PDF 格式的文件\"}");
        }

        // 检查文件大小（限制为10MB）
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            logger.warn("文件大小超过限制: {} bytes", file.getSize());
            return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"文件大小超过限制，最大允许 10MB\"}");
        }

        // 检查 LibreOffice 是否可用
        if (!pdfToWordService.checkLibreOfficeAvailable()) {
            logger.error("LibreOffice 不可用，无法进行转换");
            return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"LibreOffice 不可用，无法进行 PDF 转换\"}");
        }

        try {
            // 创建临时 PDF 文件
            File tempPdfFile = File.createTempFile("temp", ".pdf");
            file.transferTo(tempPdfFile);
            logger.debug("创建临时 PDF 文件: {}", tempPdfFile.getAbsolutePath());

            // 转换 PDF 为 Word
            File wordFile = pdfToWordService.convertPdfToWord(tempPdfFile);

            // 读取转换后的 Word 文件
            InputStream inputStream = new FileInputStream(wordFile);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            byte[] bytes = outputStream.toByteArray();
            logger.debug("读取转换后的 Word 文件，大小: {} bytes", bytes.length);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", file.getName()+".docx");

            // 构建响应
            ResponseEntity<byte[]> response = ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);

            // 关闭流
            inputStream.close();
            outputStream.close();

            // 删除临时文件
            tempPdfFile.delete();
            wordFile.delete();
            logger.debug("删除临时文件");

            logger.info("PDF 转 Word 成功，文件名: {}", file.getOriginalFilename());
            return response;
        } catch (Exception e) {
            logger.error("PDF 转 Word 失败: {}", e.getMessage(), e);
            // 检查是否是防重复提交错误
            if (e.getMessage().contains("请勿重复提交")) {
                return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"" + e.getMessage() + "\"}");
            }
            return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"转换失败：" + e.getMessage() + "\"}");
        }
    }

    /**
     * 上传 Word 文件并转换为 PDF
     *
     * @param file 上传的 Word 文件
     * @return 转换后的 PDF 文件
     */
    @PostMapping("/word-to-pdf")
    @NoRepeatSubmit(3) // 3秒内防止重复提交
    public ResponseEntity<?> convertWordToPdf(@RequestParam("file") MultipartFile file) {
        logger.info("接收到 Word 转 PDF 请求，文件名: {}", file.getOriginalFilename());

        // 检查文件是否为空
        if (file.isEmpty()) {
            logger.warn("上传文件为空");
            return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"请选择要上传的 Word 文件\"}");
        }

        // 检查文件类型
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".docx") && !fileName.endsWith(".doc")) {
            logger.warn("文件类型错误: {}", file.getOriginalFilename());
            return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"请上传 Word 格式的文件 (.docx 或 .doc)\"}");
        }

        // 检查文件大小（限制为10MB）
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            logger.warn("文件大小超过限制: {} bytes", file.getSize());
            return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"文件大小超过限制，最大允许 10MB\"}");
        }

        try {
            // 转换 Word 为 PDF
            byte[] pdfBytes = wordToPdfService.convertWordToPdf(file);
            logger.debug("Word 转 PDF 成功，文件大小: {} bytes", pdfBytes.length);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", file.getName() + ".pdf");

            // 构建响应
            ResponseEntity<byte[]> response = ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

            logger.info("Word 转 PDF 成功，文件名: {}", file.getOriginalFilename());
            return response;
        } catch (Exception e) {
            logger.error("Word 转 PDF 失败: {}", e.getMessage(), e);
            // 检查是否是防重复提交错误
            if (e.getMessage().contains("请勿重复提交")) {
                return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"" + e.getMessage() + "\"}");
            }
            return ResponseEntity.badRequest().body("{\"status\": \"error\",\"message\": \"转换失败：" + e.getMessage() + "\"}");
        }
    }
}