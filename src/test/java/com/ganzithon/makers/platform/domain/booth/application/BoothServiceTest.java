package com.ganzithon.makers.platform.domain.booth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.booth.infrastructure.BoothRepository;
import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothDetailResponse;
import com.ganzithon.makers.platform.domain.booth.presentation.response.BoothListResponse;
import com.ganzithon.makers.platform.domain.project.domain.Platform;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.project.infrastructure.ProjectRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BoothServiceTest {

  private BoothRepository boothRepository;
  private ProjectRepository projectRepository;
  private BoothService boothService;

  @BeforeEach
  void setUp() {
    boothRepository = mock(BoothRepository.class);
    projectRepository = mock(ProjectRepository.class);
    boothService = new BoothService(boothRepository, projectRepository);
  }

  private Booth booth(Zone zone, String roomNumber, int boothNumber) {
    return Booth.create(null, zone, roomNumber, boothNumber);
  }

  private Project project() {
    return Project.create(
        "서비스 제목",
        "팀 이름",
        1,
        "한줄 소개",
        "상세 설명",
        "https://service.example.com",
        "https://cdn.example.com/thumb.png",
        "SPRING, REACT",
        Platform.WEB
    );
  }

  @Nested
  class GetBoothsAll {

    @Test
    @DisplayName("전체 부스는 Zone 이름 오름차순으로 그룹핑되어 반환된다")
    void returnsBoothsGroupedByZoneInOrder() {
      when(boothRepository.findAll()).thenReturn(List.of(
          booth(Zone.ZONE3, "신관 101호", 8),
          booth(Zone.ZONE1, "본관 301호", 1),
          booth(Zone.ZONE2, "신관 201호", 5),
          booth(Zone.ZONE1, "본관 301호", 2)));

      BoothListResponse response = boothService.getBoothsAll();

      assertThat(response.booths().keySet())
          .containsExactly("ZONE1", "ZONE2", "ZONE3");
      assertThat(response.booths().get("ZONE1")).hasSize(2);
      assertThat(response.booths().get("ZONE2")).hasSize(1);
      assertThat(response.booths().get("ZONE3")).hasSize(1);
    }

    @Test
    @DisplayName("부스 항목에는 호수와 부스 번호가 포함된다")
    void containsRoomNumberAndBoothNumber() {
      when(boothRepository.findAll()).thenReturn(List.of(
          booth(Zone.ZONE1, "본관 301호", 1)));

      BoothListResponse response = boothService.getBoothsAll();

      BoothListResponse.BoothResponse booth = response.booths().get("ZONE1").get(0);
      assertThat(booth.roomNumber()).isEqualTo("본관 301호");
      assertThat(booth.boothNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("부스가 없으면 빈 그룹을 반환한다")
    void returnsEmptyMapWhenNoBooths() {
      when(boothRepository.findAll()).thenReturn(List.of());

      BoothListResponse response = boothService.getBoothsAll();

      assertThat(response.booths()).isEmpty();
    }
  }

  @Nested
  class GetBoothDetail {

    @Test
    @DisplayName("부스 상세 조회는 배정된 프로젝트 정보를 반환한다")
    void returnsAssignedProjectDetail() {
      Booth booth = Booth.create(10L, Zone.ZONE1, "본관 301호", 1);
      when(boothRepository.findById(1L)).thenReturn(Optional.of(booth));
      when(projectRepository.findById(10L)).thenReturn(Optional.of(project()));

      BoothDetailResponse response = boothService.getBoothDetail(1L);

      assertThat(response.platform()).isEqualTo("WEB");
      assertThat(response.teamName()).isEqualTo("팀 이름");
      assertThat(response.title()).isEqualTo("서비스 제목");
      assertThat(response.summary()).isEqualTo("한줄 소개");
      assertThat(response.thumbnailUrl()).isEqualTo("https://cdn.example.com/thumb.png");
    }

    @Test
    @DisplayName("존재하지 않는 부스면 BOOTH_NOT_FOUND")
    void unknownBoothThrowsNotFound() {
      when(boothRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> boothService.getBoothDetail(99L))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.BOOTH_NOT_FOUND);
    }

    @Test
    @DisplayName("부스에 배정된 프로젝트가 없으면 PROJECT_NOT_FOUND")
    void unassignedBoothThrowsProjectNotFound() {
      Booth booth = Booth.create(null, Zone.ZONE1, "본관 301호", 1);
      when(boothRepository.findById(1L)).thenReturn(Optional.of(booth));

      assertThatThrownBy(() -> boothService.getBoothDetail(1L))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    @Test
    @DisplayName("연결된 프로젝트가 삭제됐으면 PROJECT_NOT_FOUND")
    void deletedProjectThrowsProjectNotFound() {
      Booth booth = Booth.create(10L, Zone.ZONE1, "본관 301호", 1);
      when(boothRepository.findById(1L)).thenReturn(Optional.of(booth));
      when(projectRepository.findById(10L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> boothService.getBoothDetail(1L))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }
  }
}
