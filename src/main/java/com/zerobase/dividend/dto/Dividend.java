package com.zerobase.dividend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class Dividend {

    private LocalDateTime date;
    private String dividend;
}
