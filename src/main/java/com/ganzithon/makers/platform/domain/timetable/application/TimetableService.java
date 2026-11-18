package com.ganzithon.makers.platform.domain.timetable.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.timetable.application.dto.TimetableDetailInfo;
import com.ganzithon.makers.platform.domain.timetable.application.dto.TimetableInfo;
import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import com.ganzithon.makers.platform.domain.timetable.infrastructure.TimetableRepository;
import com.ganzithon.makers.platform.domain.timetable.presentation.dto.response.TimetableAllResponse;
import com.ganzithon.makers.platform.domain.timetable.presentation.dto.response.TimetableDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final TimetableRepository timetableRepository;

    public TimetableAllResponse getAllTimetables() {

        List<TimetableInfo> getTimetableAll = timetableRepository.findAllByOrderByStartTimeAsc()
                .stream()
                .map(TimetableInfo::from)
                .toList();

        return TimetableAllResponse.from(getTimetableAll);
    }

    public TimetableDetailResponse getTimetable(Long timetableId) {
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.TIMETABLE_NOT_FOUND)
                );
        return TimetableDetailResponse.from(TimetableDetailInfo.from(timetable));
    }
}
