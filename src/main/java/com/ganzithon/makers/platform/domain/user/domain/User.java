package com.ganzithon.makers.platform.domain.user.domain;

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

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "school", length = 50, nullable = false)
    private String school;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    @Column(name = "team_number")
    private Integer teamNumber;

    @Column(name = "project_id")
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "part", length = 20)
    private Part part;

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

    // 백오피스 참가자 등록용. 럭키드로우 응모·수료 여부는 false 로 시작
    public static User createParticipant(
        String loginId,
        String encodedPassword,
        String name,
        String phoneNumber,
        String school,
        Integer teamNumber,
        Part part
    ) {
        User user = new User(
            loginId,
            encodedPassword,
            name,
            phoneNumber,
            school,
            Role.PARTICIPANT,
            teamNumber
        );
        user.part = part;
        return user;
    }

    // 백오피스 참가자 수정용. 비밀번호, 소속 프로젝트, 럭키드로우 응모,수료 여부는 바꾸지 않음
    public void update(
        String loginId,
        String name,
        String phoneNumber,
        String school,
        Integer teamNumber,
        Part part
    ) {
        this.loginId = loginId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.school = school;
        this.teamNumber = teamNumber;
        this.part = part;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void assignToProject(Long projectId, Part part) {
        this.projectId = projectId;
        this.part = part;
    }

    // 파트는 회원 정보라 그대로 두고 소속 프로젝트만 바꿈
    public void joinProject(Long projectId) {
        this.projectId = projectId;
    }

    public void leaveProject() {
        this.projectId = null;
    }

    public void enterLuckyDraw() {
        this.luckyDrawEntered = true;
    }
}
