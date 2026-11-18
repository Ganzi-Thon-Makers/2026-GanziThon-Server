package com.ganzithon.makers.platform.domain.timetable.presentation;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.platform.domain.timetable.application.TimetableService;
import com.ganzithon.makers.platform.domain.timetable.presentation.dto.response.TimetableAllResponse;
import com.ganzithon.makers.platform.domain.timetable.presentation.dto.response.TimetableDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/timetable")
@RequiredArgsConstructor
public class TimetableController implements TimetableApi {

    private final TimetableService timetableService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<TimetableAllResponse>> getAllTimeTables(){
        return ResponseEntity.ok(SuccessResponse.of(timetableService.getAllTimetables()));
    }

    @Override
    @GetMapping("/{timetableId}")
    public ResponseEntity<SuccessResponse<TimetableDetailResponse>> getTimeTable(
            @PathVariable Long timetableId
    ){
        return ResponseEntity.ok(SuccessResponse.of(timetableService.getTimetable(timetableId)));
    }
}
