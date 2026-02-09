package com.mk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/11/16:59
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultimodalParseResult {
//    private Long memoryId;
    private String content;
    private float[] embedding;
    private String dataType;
}
