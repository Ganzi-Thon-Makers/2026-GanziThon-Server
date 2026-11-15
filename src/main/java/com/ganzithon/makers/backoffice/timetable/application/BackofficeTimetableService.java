package com.ganzithon.makers.backoffice.timetable.application;

import com.ganzithon.makers.backoffice.timetable.infrastructure.TimetableRepository;
import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableCreateRequest;
import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableUpdateRequest;
import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.service.domain.timetable.domain.Timetable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 타임테이블 등록/수정/삭제/조회 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeTimetableService {

  private final TimetableRepository timetableRepository;

  public List<Timetable> findAllOrdered() {
    return timetableRepository.findAllByOrderByStartTimeAscEndTimeAscIdAsc();
  }

  public Timetable findById(Long timetableId) {
    return timetableRepository.findById(timetableId)
        .orElseThrow(() -> new BusinessException(ErrorCode.TIMETABLE_NOT_FOUND));
  }

  // TODO: 투표 관리 기능 구현 시 VOTE 일정 조회 메서드 추가

  @Transactional
  public Timetable create(TimetableCreateRequest request) {
    if (!request.isVote()) {
      return timetableRepository.save(Timetable.create(
          request.title(),
          request.description(),
          request.startTime(),
          request.endTime()
      ));
    }
    return createVote(request);
  }

  private Timetable createVote(TimetableCreateRequest request) {
    if (timetableRepository.existsByVoteTrue()) {
      throw new BusinessException(ErrorCode.VOTE_TIMETABLE_ALREADY_EXISTS);
    }

    return timetableRepository.save(Timetable.createVote(
        request.title(),
        request.description(),
        request.startTime(),
        request.endTime()
    ));
  }

  @Transactional
  public Timetable update(Long timetableId, TimetableUpdateRequest request) {
    Timetable timetable = findById(timetableId);
    timetable.update(
        request.title(),
        request.description(),
        request.startTime(),
        request.endTime()
    );
    return timetable;
  }

  @Transactional
  public void delete(Long timetableId) {
    Timetable timetable = findById(timetableId);
    timetable.validateDeletable();
    timetableRepository.delete(timetable);
  }
}
