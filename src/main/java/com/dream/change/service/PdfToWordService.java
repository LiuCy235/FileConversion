package com.dream.change.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * PDF转Word服务类
 * 使用Python的pdf2docx库进行转换
 */
@Service
public class PdfToWordService {

    private static final Logger logger = LoggerFactory.getLogger(PdfToWordService.class);
    
    // 缓存Python和pdf2docx的可用性检查结果
    private Boolean pythonAvailable = null;
    private Boolean pdf2DocxAvailable = null;

    public PdfToWordService() {
        logger.info("PdfToWordService initialized");
    }

    /**
     * 将 PDF 文件转换为 Word 文件
     * @param pdfFile PDF 文件
     * @return 转换后的 Word 文件
     * @throws Exception 转换过程中的异常
     */
    public File convertPdfToWord(File pdfFile) throws Exception {
        logger.info("开始转换 PDF 文件: {}", pdfFile.getName());
        logger.info("PDF 文件大小: {} bytes", pdfFile.length());
        
        // 生成临时文件路径
        String tempDir = System.getProperty("java.io.tmpdir");
        String outputFileName = UUID.randomUUID().toString() + ".docx";
        File outputFile = new File(tempDir, outputFileName);

        try {
            // 检查Python和pdf2docx是否可用
            if (!isPythonAvailable() || !isPdf2DocxAvailable()) {
                throw new Exception("Python 或 pdf2docx 不可用，请确保已正确安装");
            }
            
            // 使用 Python 的 pdf2docx 库进行转换
            logger.info("使用 Python pdf2docx 库进行转换");
            logger.debug("转换目标文件: {}", outputFile.getAbsolutePath());
            
            // 构建命令 - 使用正确的方式调用 pdf2docx
            String pythonScript = buildPythonScript(pdfFile, outputFile);
            ProcessBuilder pb = new ProcessBuilder(
                "python", "-c", pythonScript
            );
            
            // 执行命令并获取输出
            String commandOutput = executeCommand(pb);
            
            logger.info("PDF 转换完成: {}", pdfFile.getName());
        } catch (Exception e) {
            logger.error("PDF 转换失败: {}", e.getMessage(), e);
            throw e;
        }

        // 检查输出文件是否存在
        if (!outputFile.exists()) {
            logger.error("转换后的 Word 文件未找到: {}", outputFile.getAbsolutePath());
            throw new Exception("转换后的 Word 文件未找到");
        }

        logger.info("转换后的 Word 文件: {}", outputFile.getName());
        logger.info("转换后的 Word 文件大小: {} bytes", outputFile.length());
        return outputFile;
    }

    /**
     * 构建Python脚本
     * @param pdfFile PDF文件
     * @param outputFile 输出文件
     * @return Python脚本字符串
     */
    private String buildPythonScript(File pdfFile, File outputFile) {
        String pdfPath = pdfFile.getAbsolutePath().replace("\\", "\\\\");
        String outputPath = outputFile.getAbsolutePath().replace("\\", "\\\\");
        return "from pdf2docx import Converter; " +
               "cv = Converter('" + pdfPath + "'); " +
               "cv.convert('" + outputPath + "', start=0, end=None); " +
               "cv.close()";
    }

    /**
     * 执行命令并获取输出
     * @param pb ProcessBuilder
     * @return 命令输出
     * @throws Exception 执行异常
     */
    private String executeCommand(ProcessBuilder pb) throws Exception {
        StringBuilder output = new StringBuilder();
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.debug("Python 输出: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        logger.debug("Python 命令执行完成，退出码: {}", exitCode);
        
        if (exitCode != 0) {
            logger.error("Python 命令执行失败，退出码: {}", exitCode);
            throw new Exception("Python 执行失败: " + output.toString());
        }
        
        return output.toString();
    }

    /**
     * 检查 Python 和 pdf2docx 是否可用
     * @return 是否可用
     */
    public boolean checkLibreOfficeAvailable() {
        logger.info("检查 Python 和 pdf2docx 是否可用");
        return isPythonAvailable() && isPdf2DocxAvailable();
    }

    /**
     * 检查Python是否可用
     * @return 是否可用
     */
    private boolean isPythonAvailable() {
        if (pythonAvailable != null) {
            return pythonAvailable;
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            pythonAvailable = (exitCode == 0);
            if (!pythonAvailable) {
                logger.error("Python 不可用");
            }
            return pythonAvailable;
        } catch (Exception e) {
            logger.error("检查 Python 可用性失败: {}", e.getMessage());
            pythonAvailable = false;
            return false;
        }
    }

    /**
     * 检查pdf2docx是否可用
     * @return 是否可用
     */
    private boolean isPdf2DocxAvailable() {
        if (pdf2DocxAvailable != null) {
            return pdf2DocxAvailable;
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "-m", "pip", "list");
            Process process = pb.start();
            
            // 捕获输出并检查是否包含 pdf2docx
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            pdf2DocxAvailable = (exitCode == 0 && output.toString().contains("pdf2docx"));
            if (!pdf2DocxAvailable) {
                logger.error("pdf2docx 不可用，请运行: pip install pdf2docx");
            }
            return pdf2DocxAvailable;
        } catch (Exception e) {
            logger.error("检查 pdf2docx 可用性失败: {}", e.getMessage());
            pdf2DocxAvailable = false;
            return false;
        }
    }
}