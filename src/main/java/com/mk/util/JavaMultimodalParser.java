package com.mk.util;

import com.alibaba.fastjson2.JSON;
import com.mk.entity.JavaCodeQuestion;
import com.mk.entity.MultimodalParseResult;
import com.mk.entity.MultimodelEntity;
import com.mk.util.JavaProblemExtractor;
import com.mk.util.TokenCompressor;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/09/17:52
 * @Description:
 */
@Component
@Slf4j
public class JavaMultimodalParser {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private EmbeddingModel qwenEmbeddingModel;
    @Value("${minio.bucket-name}")
    private String minioBucket;

    @Autowired
    private TokenCompressor tokenCompressor;

    @Autowired
    private JavaProblemExtractor javaProblemExtractor;

    // 统一多模态解析入口
    public MultimodalParseResult parse(MultimodelEntity entity) {
//        MultipartFile file = entity.getFile();
//        String content = entity.getContent();
        String type = entity.getType();
        try {
            if (type == null) {
                // 默认处理为文本类型
                return new MultimodalParseResult(entity.getContent() != null ? entity.getContent() : "", null, "TEXT");
            }
            switch (type) {
                case "TEXT": // 文本提问
                    return parseText(entity);
                case "IMAGE": // 编程题截图
                    return parseImage(entity);
                default:
                    throw new IllegalArgumentException("不支持的输入类型：" + type);
            }
        } catch (Exception e) {
            log.error("多模态解析失败", e);
            throw new RuntimeException("解析失败：" + e.getMessage());
        }
    }
    // 文本解析（提取Java面试题核心）
    private MultimodalParseResult parseText(MultimodelEntity entity) throws IOException {
        long startTime = System.currentTimeMillis();

        MultipartFile file = entity.getFile();
        if(file == null){
            return new MultimodalParseResult("", null, "TEXT");
        }

//        String content = entity.getContent();
        InputStream inputStream = file.getInputStream();
        TextDocumentParser parser = new TextDocumentParser(StandardCharsets.UTF_8);
        String text = parser.parse(inputStream).text();
        //System.out.println("Java试卷：" + text);
        // 1. Token压缩：过滤冗余描述，提取核心问题izainali
        String compressedText = tokenCompressor.compressJavaQuestion(text);
        // 2. 生成嵌入向量
        Embedding embedding = qwenEmbeddingModel.embed(compressedText).content();

        long endTime = System.currentTimeMillis();
        long costTime = endTime - startTime;
        System.out.printf("【parseText，执行耗时：%d 毫秒%n", costTime);

        return new MultimodalParseResult(compressedText, embedding.vector(), "TEXT");
    }

    // 图片解析（编程题截图OCR）
    private MultimodalParseResult parseImage(MultimodelEntity entity) throws Exception {
        MultipartFile file = entity.getFile();
        String content = entity.getContent();
        // 1. OCR识别图片中的代码/题目
        ITesseract tesseract = new Tesseract();
        tesseract.setLanguage("eng+chi_sim"); // 支持中英文（Java代码+中文题目）
        String ocrText = tesseract.doOCR(ImageIO.read(file.getInputStream()));
        // 2. 结构化提取Java编程题（题干/要求/代码片段）
        JavaCodeQuestion struct = javaProblemExtractor.extractProblem(ocrText);
        // 3. 上传图片到MinIO
        String imageUrl = uploadToMinio(file, "image");
        // 4. 生成嵌入向量
        Embedding embedding = qwenEmbeddingModel.embed(JSON.toJSONString(struct)).content();
        return new MultimodalParseResult(JSON.toJSONString(struct), embedding.vector(), "IMAGE");
    }

    // 上传文件到MinIO
    private String uploadToMinio(MultipartFile file, String prefix) throws Exception {
        String fileName = prefix + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioBucket)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(minioBucket)
                .object(fileName)
                .method(Method.GET)
                .expiry(86400) // URL有效期1天
                .build());
    }

}
