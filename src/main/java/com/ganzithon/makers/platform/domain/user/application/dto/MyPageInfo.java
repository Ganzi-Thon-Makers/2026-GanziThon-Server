package com.ganzithon.makers.platform.domain.user.application.dto;

import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.User;

import java.util.List;

public record MyPageInfo(
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
        public static TeamMember from(User user) {
            return new TeamMember(
                    user.getName(),
                    user.getPart()
            );
        }
    }
    public static MyPageInfo from(User user, List<TeamMember> teamMemberList) {
        return new MyPageInfo(
                user.getId(),
                user.getName(),
                user.getLoginId(),
                user.getTeamNumber(),
                teamMemberList
        );
    }
}
