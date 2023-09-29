package com.akazimour.BackendForEto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UploadResponse {

    private String FileName;
    private String ContentType;
    private String Url;

}
