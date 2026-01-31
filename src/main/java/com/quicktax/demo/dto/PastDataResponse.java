package com.quicktax.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List; /**
 * 2. result 내부의 "pastdata_1": [] 구조를 위한 클래스
 */
@Getter
@AllArgsConstructor
public class PastDataResponse {
    @JsonProperty("pastdata_1")
    private List<PastDataDto> pastDataList;
}
