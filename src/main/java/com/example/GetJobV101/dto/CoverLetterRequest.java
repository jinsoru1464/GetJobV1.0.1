// com.example.GetJobV101.dto.CoverLetterRequest.java
package com.example.GetJobV101.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoverLetterRequest {

    @Schema(description = "자기소개서 본문", example = "저는 책임감이 강하고 팀워크를 중요시하는 개발자입니다.")
    private String content;
}
