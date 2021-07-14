package com.yang.thyleafaxois.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DataDTO {
    private Long id;
    private String fileName;
    private String fileBase64;
}
