package com.ganzithon.makers.platform.domain.vote.infrastructure;

import com.ganzithon.makers.platform.domain.vote.domain.Vote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VoteRepository extends JpaRepository<Vote, Long> {

  // 표를 받은 프로젝트만 조회 (0표 프로젝트는 결과에 없음)
  @Query("""
      select v.projectId as projectId, count(v) as voteCount
      from Vote v
      group by v.projectId
      """)
  List<ProjectVoteCount> countGroupByProjectId();

  long countByProjectId(Long projectId);
}
