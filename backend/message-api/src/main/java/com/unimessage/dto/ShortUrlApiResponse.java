package com.unimessage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 海明
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlApiResponse {
    private Integer code;
    private String message;
    private ShortUrlResponse data;
}
