package com.ganzithon.makers.platform.domain.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum Role {
    PARTICIPANT(false, false),
    TEAM(false, true),
    ADMIN(true, false),
    SUPER_ADMIN(true, false);

    private final boolean backoffice;
    private final boolean singleDevice;

    public static Set<Role> backofficeRoles() {
        return Arrays.stream(values())
                .filter(Role::isBackoffice)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
    }
}
