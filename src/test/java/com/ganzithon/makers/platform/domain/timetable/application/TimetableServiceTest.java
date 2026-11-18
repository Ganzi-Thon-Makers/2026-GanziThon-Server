package com.ganzithon.makers.platform.domain.timetable.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import com.ganzithon.makers.platform.domain.timetable.infrastructure.TimetableRepository;
import com.ganzithon.makers.platform.domain.timetable.presentation.dto.response.TimetableAllResponse;
import com.ganzithon.makers.platform.domain.timetable.presentation.dto.response.TimetableDetailResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TimetableServiceTest {

  private TimetableRepository timetableRepository;
  private TimetableService timetableService;

  @BeforeEach
  void setUp() {
    timetableRepository = mock(TimetableRepository.class);
    timetableService = new TimetableService(timetableRepository);
  }

  private Timetable timetable(String title, String description,
      int startHour, int startMinute, int endHour, int endMinute) {
    LocalDateTime base = LocalDateTime.of(2026, 2, 20, 0, 0);
    return Timetable.create(title, description,
        base.withHour(startHour).withMinute(startMinute),
        base.withHour(endHour).withMinute(endMinute));
  }

  @Test
  @DisplayName("전체 일정은 시작 시간순 목록으로 반환되고 시간은 HH:mm 형식이다")
  void returnsAllTimetablesSortedByStartTime() {
    when(timetableRepository.findAllByOrderByStartTimeAsc()).thenReturn(List.of(
        timetable("개회식", "행사 시작", 10, 0, 10, 30),
        timetable("해커톤", "본격 개발", 11, 0, 18, 0)));

    TimetableAllResponse response = timetableService.getAllTimetables();

    assertThat(response.timetables()).hasSize(2);
    assertThat(response.timetables()).extracting(TimetableAllResponse.Timetable::description)
        .containsExactly("행사 시작", "본격 개발");
    assertThat(response.timetables().get(0).title()).isEqualTo("개회식");
    assertThat(response.timetables().get(0).startTime()).isEqualTo("10:00");
    assertThat(response.timetables().get(0).endTime()).isEqualTo("10:30");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"첫 번째 안내\n두 번째 안내"})
  @DisplayName("전체 조회는 null, 빈 문자열, 여러 줄 설명을 그대로 반환한다")
  void preservesDescriptionInAllTimetables(String description) {
    when(timetableRepository.findAllByOrderByStartTimeAsc()).thenReturn(List.of(
        timetable("개회식", description, 10, 0, 10, 30)));

    TimetableAllResponse response = timetableService.getAllTimetables();

    assertThat(response.timetables()).hasSize(1);
    assertThat(response.timetables().get(0).description()).isEqualTo(description);
  }

  @Test
  @DisplayName("일정이 없으면 빈 목록을 반환한다")
  void returnsEmptyListWhenNoTimetables() {
    when(timetableRepository.findAllByOrderByStartTimeAsc()).thenReturn(List.of());

    TimetableAllResponse response = timetableService.getAllTimetables();

    assertThat(response.timetables()).isEmpty();
  }

  @Test
  @DisplayName("상세 조회는 제목, 설명, 시간을 HH:mm 형식으로 반환한다")
  void returnsTimetableDetail() {
    Timetable timetable = timetable("점심시간", "도시락 배부", 12, 30, 13, 30);
    when(timetableRepository.findById(1L)).thenReturn(Optional.of(timetable));

    TimetableDetailResponse response = timetableService.getTimetable(1L);

    assertThat(response.title()).isEqualTo("점심시간");
    assertThat(response.description()).isEqualTo("도시락 배부");
    assertThat(response.startTime()).isEqualTo("12:30");
    assertThat(response.endTime()).isEqualTo("13:30");
  }

  @Test
  @DisplayName("존재하지 않는 일정이면 TIMETABLE_NOT_FOUND")
  void unknownTimetableThrowsNotFound() {
    when(timetableRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> timetableService.getTimetable(99L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.TIMETABLE_NOT_FOUND);
  }
}
