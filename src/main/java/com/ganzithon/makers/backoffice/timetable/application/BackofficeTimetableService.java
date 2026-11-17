package com.ganzithon.makers.backoffice.timetable.application;

import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableCreateRequest;
import com.ganzithon.makers.backoffice.timetable.presentation.dto.TimetableUpdateRequest;
import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import com.ganzithon.makers.platform.domain.timetable.infrastructure.TimetableRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
      Timetable timetable = Timetable.create(
          request.title(),
          request.description(),
          request.startTime(),
          request.endTime()
      );
      validateNotOverlapping(timetable, null);
      return timetableRepository.save(timetable);
    }
    return createVote(request);
  }

  private Timetable createVote(TimetableCreateRequest request) {
    if (timetableRepository.existsByVoteTrue()) {
      throw new BusinessException(ErrorCode.VOTE_TIMETABLE_ALREADY_EXISTS);
    }

    Timetable vote = Timetable.createVote(
        request.title(),
        request.description(),
        request.startTime(),
        request.endTime()
    );
    validateNotOverlapping(vote, null);
    try {
      return timetableRepository.saveAndFlush(vote);
    } catch (DataIntegrityViolationException e) {
      // 동시 요청으로 위 존재 확인을 함께 통과한 경우, DB UNIQUE 제약(is_vote)이 막는다
      throw new BusinessException(ErrorCode.VOTE_TIMETABLE_ALREADY_EXISTS);
    }
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
    // 겹침 조회 전에 수정 내용이 먼저 반영(flush)되더라도, 예외 발생 시 트랜잭션이 롤백된다
    validateNotOverlapping(timetable, timetableId);
    return timetable;
  }

  @Transactional
  public void delete(Long timetableId) {
    Timetable timetable = findById(timetableId);
    timetable.validateDeletable();
    timetableRepository.delete(timetable);
  }

  // 다른 일정과 시간이 겹치면 등록/수정할 수 없음 (끝과 시작이 맞닿는 것은 허용)
  // 시간 순서(종료 > 시작) 검증은 엔티티에서 먼저 끝난 상태로 호출
  private void validateNotOverlapping(Timetable timetable, Long excludeId) {
    if (timetableRepository.existsOverlapping(timetable.getStartTime(), timetable.getEndTime(), excludeId)) {
      throw new BusinessException(ErrorCode.TIMETABLE_TIME_OVERLAP);
    }
  }
}
