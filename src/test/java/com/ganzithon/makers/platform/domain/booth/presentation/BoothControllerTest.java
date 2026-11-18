package com.ganzithon.makers.platform.domain.booth.presentation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.exception.GlobalExceptionHandler;
import com.ganzithon.makers.platform.domain.booth.application.BoothService;
import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothDetailResponse;
import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothListResponse;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class BoothControllerTest {

  private BoothService boothService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    boothService = mock(BoothService.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new BoothController(boothService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Nested
  class GetBoothsAll {

    @Test
    @DisplayName("전체 부스는 Zone별 그룹으로 반환된다")
    void returnsBoothsGroupedByZone() throws Exception {
      Map<String, List<BoothListResponse.BoothResponse>> booths = new TreeMap<>();
      booths.put("ZONE1", List.of(
          new BoothListResponse.BoothResponse(1L, "본관 301호", 1),
          new BoothListResponse.BoothResponse(2L, "본관 301호", 2)));
      booths.put("ZONE2", List.of(
          new BoothListResponse.BoothResponse(5L, "신관 201호", 5)));

      when(boothService.getBoothsAll()).thenReturn(new BoothListResponse(booths));

      mockMvc.perform(get("/api/v1/booth"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.booths.ZONE1").isArray())
          .andExpect(jsonPath("$.data.booths.ZONE1.length()").value(2))
          .andExpect(jsonPath("$.data.booths.ZONE1[0].boothId").value(1))
          .andExpect(jsonPath("$.data.booths.ZONE1[0].roomNumber").value("본관 301호"))
          .andExpect(jsonPath("$.data.booths.ZONE1[0].boothNumber").value(1))
          .andExpect(jsonPath("$.data.booths.ZONE2[0].boothNumber").value(5));
    }

    @Test
    @DisplayName("부스가 없으면 빈 booths 객체를 반환한다")
    void returnsEmptyBooths() throws Exception {
      when(boothService.getBoothsAll()).thenReturn(new BoothListResponse(Map.of()));

      mockMvc.perform(get("/api/v1/booth"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.booths").isEmpty());
    }
  }

  @Nested
  class GetBoothDetail {

    @Test
    @DisplayName("부스 상세 조회는 프로젝트 정보를 반환한다")
    void returnsBoothDetail() throws Exception {
      when(boothService.getBoothDetail(1L)).thenReturn(new BoothDetailResponse(
          10L, "WEB", "팀 이름", "서비스 제목", "한줄 소개", "https://cdn.example.com/thumb.png"));

      mockMvc.perform(get("/api/v1/booth/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.projectId").value(10))
          .andExpect(jsonPath("$.data.platform").value("WEB"))
          .andExpect(jsonPath("$.data.teamName").value("팀 이름"))
          .andExpect(jsonPath("$.data.title").value("서비스 제목"))
          .andExpect(jsonPath("$.data.summary").value("한줄 소개"))
          .andExpect(jsonPath("$.data.thumbnailUrl").value("https://cdn.example.com/thumb.png"));
    }

    @Test
    @DisplayName("존재하지 않는 부스면 404를 반환한다")
    void unknownBoothReturns404() throws Exception {
      when(boothService.getBoothDetail(99L))
          .thenThrow(new BusinessException(ErrorCode.BOOTH_NOT_FOUND));

      mockMvc.perform(get("/api/v1/booth/99"))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("배정된 프로젝트가 없으면 404를 반환한다")
    void unassignedProjectReturns404() throws Exception {
      when(boothService.getBoothDetail(1L))
          .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

      mockMvc.perform(get("/api/v1/booth/1"))
          .andExpect(status().isNotFound());
    }
  }
}
