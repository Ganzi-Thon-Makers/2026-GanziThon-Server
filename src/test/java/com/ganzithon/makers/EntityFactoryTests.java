package com.ganzithon.makers;

import static org.assertj.core.api.Assertions.assertThat;

import com.ganzithon.makers.common.persistence.BaseEntity;
import com.ganzithon.makers.service.domain.booth.domain.Booth;
import com.ganzithon.makers.service.domain.booth.domain.Zone;
import com.ganzithon.makers.service.domain.project.domain.Project;
import com.ganzithon.makers.service.domain.stamp.domain.Stamp;
import com.ganzithon.makers.service.domain.timetable.domain.Timetable;
import com.ganzithon.makers.service.domain.user.domain.Role;
import com.ganzithon.makers.service.domain.user.domain.User;
import com.ganzithon.makers.service.domain.vote.domain.Vote;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EntityFactoryTests {

    @Test
    void createsUserWithDefaultFlags() {
        User user = User.create(
            "login",
            "encoded-password",
            "name",
            "01012345678",
            "school",
            Role.PARTICIPANT,
            1
        );

        assertThat(user.getLoginId()).isEqualTo("login");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getName()).isEqualTo("name");
        assertThat(user.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(user.getSchool()).isEqualTo("school");
        assertThat(user.getRole()).isEqualTo(Role.PARTICIPANT);
        assertThat(user.getTeamNumber()).isEqualTo(1);
        assertThat(user.isLuckyDrawEntered()).isFalse();
        assertThat(user.isCompleted()).isFalse();
        assertUnsaved(user);
    }

    @Test
    void createsProject() {
        Project project = Project.create(
            "title",
            "team",
            "summary",
            "description",
            null,
            "thumbnail",
            "Java"
        );

        assertThat(project.getTitle()).isEqualTo("title");
        assertThat(project.getTeamName()).isEqualTo("team");
        assertThat(project.getSummary()).isEqualTo("summary");
        assertThat(project.getDescription()).isEqualTo("description");
        assertThat(project.getServiceUrl()).isNull();
        assertThat(project.getThumbnailUrl()).isEqualTo("thumbnail");
        assertThat(project.getTechStack()).isEqualTo("Java");
        assertUnsaved(project);
    }

    @Test
    void createsBoothWithOptionalProject() {
        Booth booth = Booth.create(
            null,
            Zone.ZONE1,
            "101",
            2
        );

        assertThat(booth.getProjectId()).isNull();
        assertThat(booth.getZone()).isEqualTo(Zone.ZONE1);
        assertThat(booth.getRoomNumber()).isEqualTo("101");
        assertThat(booth.getBoothNumber()).isEqualTo(2);
        assertUnsaved(booth);
    }

    @Test
    void createsStamp() {
        Stamp stamp = Stamp.create(1L, 2L);

        assertThat(stamp.getUserId()).isEqualTo(1L);
        assertThat(stamp.getBoothId()).isEqualTo(2L);
        assertUnsaved(stamp);
    }

    @Test
    void createsVote() {
        Vote vote = Vote.create(1L, 2L);

        assertThat(vote.getUserId()).isEqualTo(1L);
        assertThat(vote.getProjectId()).isEqualTo(2L);
        assertUnsaved(vote);
    }

    @Test
    void createsTimetable() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 14, 9, 0);
        LocalDateTime end = start.plusHours(1);
        Timetable timetable = Timetable.create(
            "title",
            null,
            start,
            end
        );

        assertThat(timetable.getTitle()).isEqualTo("title");
        assertThat(timetable.getDescription()).isNull();
        assertThat(timetable.getStartTime()).isEqualTo(start);
        assertThat(timetable.getEndTime()).isEqualTo(end);
        assertUnsaved(timetable);
    }

    private void assertUnsaved(BaseEntity entity) {
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }
}
