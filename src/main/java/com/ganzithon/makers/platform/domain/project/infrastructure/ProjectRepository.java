package com.ganzithon.makers.platform.domain.project.infrastructure;

import com.ganzithon.makers.platform.domain.project.domain.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  // 프로젝트명 부분 일치 검색 (%, _ 는 Spring Data 가 이스케이프)
  List<Project> findAllByTitleContainingIgnoreCase(String keyword);
}
