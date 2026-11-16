package com.ganzithon.makers.platform.domain.project.infrastructure;

import com.ganzithon.makers.platform.domain.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
