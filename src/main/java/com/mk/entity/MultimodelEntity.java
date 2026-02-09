package com.mk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/11/16:55
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultimodelEntity {
    private String memoryId;
    private Long userId;
    private String content;
    private MultipartFile file;
    private String type;
}
