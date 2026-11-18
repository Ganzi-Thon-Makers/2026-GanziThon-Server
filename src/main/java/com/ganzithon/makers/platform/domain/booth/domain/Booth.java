package com.ganzithon.makers.platform.domain.booth.domain;

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
@Table(name = "booth", uniqueConstraints = {
    @UniqueConstraint(name = "uk_booth_project_id", columnNames = "project_id"),
    @UniqueConstraint(name = "uk_booth_zone_booth_number", columnNames = {"zone", "booth_number"})
})
public class Booth extends BaseEntity {

    @Column(name = "project_id")
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone", length = 20, nullable = false)
    private Zone zone;

    @Column(name = "room_number", length = 20, nullable = false)
    private String roomNumber;

    @Column(name = "booth_number", nullable = false)
    private Integer boothNumber;

    private Booth(
        Long projectId,
        Zone zone,
        String roomNumber,
        Integer boothNumber
    ) {
        this.projectId = projectId;
        this.zone = zone;
        this.roomNumber = roomNumber;
        this.boothNumber = boothNumber;
    }

    public static Booth create(
        Long projectId,
        Zone zone,
        String roomNumber,
        Integer boothNumber
    ) {
        return new Booth(
            projectId,
            zone,
            roomNumber,
            boothNumber
        );
    }

    // 부스에 프로젝트(팀) 배정. 다른 프로젝트가 배정되어 있으면 교체
    public void assignProject(Long projectId) {
        this.projectId = projectId;
    }

    // 배정 취소 (부스는 유지)
    public void clearProject() {
        this.projectId = null;
    }
}
