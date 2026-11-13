package com.ganzithon.makers.service.domain.booth.domain;

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
    @UniqueConstraint(name = "uk_booth_project_id", columnNames = "project_id")
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
}
