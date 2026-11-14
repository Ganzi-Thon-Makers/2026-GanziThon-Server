package com.ganzithon.makers.service.domain.project.infrastructure;

import com.ganzithon.makers.service.domain.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
