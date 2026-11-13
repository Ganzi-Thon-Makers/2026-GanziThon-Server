package com.ganzithon.makers.service.domain.user.domain;

import com.ganzithon.makers.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_login_id", columnNames = "login_id")
})
public class User extends BaseEntity {

    @Column(name = "login_id", length = 50, nullable = false)
    private String loginId;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "phoneNumber", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "school", length = 50, nullable = false)
    private String school;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    @Column(name = "teamNumber", nullable = false)
    private Integer teamNumber;

    @Column(name = "is_lucky_draw_entered", nullable = false)
    private boolean luckyDrawEntered = false;

    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;

    private User(
        String loginId,
        String password,
        String name,
        String phoneNumber,
        String school,
        Role role,
        Integer teamNumber
    ) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.school = school;
        this.role = role;
        this.teamNumber = teamNumber;
    }

    public static User create(
        String loginId,
        String password,
        String name,
        String phoneNumber,
        String school,
        Role role,
        Integer teamNumber
    ) {
        return new User(
            loginId,
            password,
            name,
            phoneNumber,
            school,
            role,
            teamNumber
        );
    }
}
