package com.ganzithon.makers.service.domain.booth.infrastructure;

import com.ganzithon.makers.service.domain.booth.domain.Booth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoothRepository extends JpaRepository<Booth, Long> {

  List<Booth> findAllByOrderByBoothNumberAsc();

  Optional<Booth> findByProjectId(Long projectId);
}
