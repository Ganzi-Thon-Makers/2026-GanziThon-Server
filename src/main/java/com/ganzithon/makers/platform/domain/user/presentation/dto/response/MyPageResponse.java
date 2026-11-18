package com.ganzithon.makers.platform.domain.user.presentation.dto.response;

import com.ganzithon.makers.platform.domain.user.application.dto.MyPageInfo;
import com.ganzithon.makers.platform.domain.user.domain.Part;
import java.util.List;

public record MyPageResponse(
        Long userId,
        String name,
        String loginId,
        Integer teamNumber,
        List<TeamMember> teamMemberList
) {

    public record TeamMember(
            String name,
            Part part
    ) {
        public static TeamMember from(MyPageInfo.TeamMember member) {
            return new TeamMember(
                    member.name(),
                    member.part()
            );
        }
    }

    public static MyPageResponse from(MyPageInfo info) {
        return new MyPageResponse(
                info.userId(),
                info.name(),
                info.loginId(),
                info.teamNumber(),
                info.teamMemberList().stream()
                        .map(TeamMember::from)
                        .toList()
        );
    }
}
