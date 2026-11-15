package com.ganzithon.makers.service.domain.timetable.domain;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "timetable")
public class Timetable extends BaseEntity {

    // 투표 일정이면 true, 일반 일정이면 NULL
    // UNIQUE는 NULL끼리는 중복으로 보지 않으므로, DB에서 투표 일정이 1개로 제한된다
    @Getter(AccessLevel.NONE)
    @Column(name = "is_vote", unique = true)
    private Boolean vote;

    @Column(name = "is_paused", nullable = false)
    private boolean paused = false;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    private Timetable(
        boolean vote,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        validateTimeRange(startTime, endTime);
        this.vote = vote ? Boolean.TRUE : null;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // 일반 일정 생성
    public static Timetable create(
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        return new Timetable(false, title, description, startTime, endTime);
    }

    // 투표 일정 생성
    public static Timetable createVote(
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        return new Timetable(true, title, description, startTime, endTime);
    }

    // 전체 교체 수정. 투표 일정 여부는 등록 시에만 정하며 변경할 수 없다
    public void update(
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        validateTimeRange(startTime, endTime);
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isVote() {
        return Boolean.TRUE.equals(vote);
    }

    // 투표 일시중지 (이미 중지 상태여도 그대로 둔다)
    public void pause() {
        requireVote();
        this.paused = true;
    }

    // 투표 재개 (이미 진행 상태여도 그대로 둔다)
    public void resume() {
        requireVote();
        this.paused = false;
    }

    private void requireVote() {
        if (!isVote()) {
            throw new IllegalStateException("투표 일정만 일시중지/재개할 수 있습니다.");
        }
    }

    // 투표 일정은 투표/스탬프 투어 시간의 기준이므로 삭제할 수 없다
    public void validateDeletable() {
        if (isVote()) {
            throw new BusinessException(ErrorCode.VOTE_TIMETABLE_NOT_DELETABLE);
        }
    }

    // 종료시간은 시작시간보다 엄격하게 늦어야 한다 (같은 시각 불가)
    private static void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new BusinessException(ErrorCode.INVALID_TIMETABLE_TIME_RANGE);
        }
    }
}
