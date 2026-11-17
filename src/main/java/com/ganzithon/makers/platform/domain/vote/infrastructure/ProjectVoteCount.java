package com.ganzithon.makers.platform.domain.vote.infrastructure;

// 프로젝트별 득표수 집계 결과
public interface ProjectVoteCount {
  Long getProjectId();
  Long getVoteCount();
}
