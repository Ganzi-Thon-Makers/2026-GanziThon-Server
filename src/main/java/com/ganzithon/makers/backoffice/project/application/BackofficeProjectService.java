package com.ganzithon.makers.backoffice.project.application;

import com.ganzithon.makers.backoffice.image.storage.ImageStorage;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectCreateRequest;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectCreateRequest.MemberRequest;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectDetailResponse;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectListResponse;
import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.project.application.ProjectService;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.vote.application.VoteService;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/** 프로젝트 목록 조회/검색, 상세 조회, 등록 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeProjectService {

  private static final String THUMBNAIL_DIRECTORY = "projects/thumbnails";

  private final ProjectService projectService;
  private final VoteService voteService;
  private final UserService userService;
  private final ImageStorage imageStorage;

  // keyword 가 비어 있으면 전체 조회, 있으면 프로젝트명 부분 일치 검색
  public List<ProjectListResponse> getProjects(String keyword) {
    List<Project> projects = (keyword == null || keyword.isBlank())
        ? projectService.findAll()
        : projectService.searchByTitle(keyword.strip());

    Map<Long, Long> voteCounts = voteService.countVotesByProject();
    return projects.stream()
        .map(project -> ProjectListResponse.of(project, voteCounts.getOrDefault(project.getId(), 0L)))
        // 득표수 많은 순, 같으면 등록 순(id 오름차순)
        .sorted(Comparator.comparingLong(ProjectListResponse::voteCount).reversed()
            .thenComparing(ProjectListResponse::id))
        .toList();
  }

  public ProjectDetailResponse getProject(Long projectId) {
    Project project = projectService.findById(projectId);
    long voteCount = voteService.countByProjectId(projectId);
    return ProjectDetailResponse.of(project, voteCount, toMembers(userService.findAllByProjectId(projectId)));
  }

  @Transactional
  public ProjectDetailResponse create(ProjectCreateRequest request, MultipartFile thumbnail) {
    // 팀원 검증을 먼저 끝내서, 검증 실패 시 이미지가 업로드되지 않도록 함
    List<MemberRequest> memberRequests = request.membersOrEmpty();
    Map<Long, User> users = findMembers(memberRequests);
    users.values().forEach(this::validateNotAssigned);

    String thumbnailUrl = imageStorage.upload(thumbnail, THUMBNAIL_DIRECTORY);
    deleteImageOnRollback(thumbnailUrl);

    Project project = projectService.save(Project.create(
        request.title(),
        request.teamName(),
        request.summary(),
        request.description(),
        blankToNull(request.serviceUrl()),
        thumbnailUrl,
        request.techStack(),
        request.platform()
    ));

    for (MemberRequest member : memberRequests) {
      users.get(member.userId()).assignToProject(project.getId(), member.part());
    }

    return ProjectDetailResponse.of(project, 0L, toMembers(users.values()));
  }

  // 요청한 팀원(userId)을 조회. 중복이면 400, 없는 회원이 있으면 404
  private Map<Long, User> findMembers(List<MemberRequest> memberRequests) {
    List<Long> userIds = memberRequests.stream().map(MemberRequest::userId).toList();
    if (new HashSet<>(userIds).size() != userIds.size()) {
      throw new BusinessException(ErrorCode.DUPLICATE_PROJECT_MEMBER);
    }
    if (userIds.isEmpty()) {
      return Map.of();
    }

    Map<Long, User> users = userService.findAllByIds(userIds).stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));
    if (users.size() != userIds.size()) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    return users;
  }

  private void validateNotAssigned(User user) {
    if (user.getProjectId() != null) {
      throw new BusinessException(ErrorCode.PROJECT_MEMBER_ALREADY_ASSIGNED);
    }
  }

  // DB 저장이 실패(롤백)하면 이미 올린 이미지를 지워서 저장소에 주인 없는 파일이 남지 않게 함
  private void deleteImageOnRollback(String imageUrl) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCompletion(int status) {
        if (status == STATUS_COMMITTED) {
          return;
        }
        try {
          imageStorage.delete(imageUrl);
        } catch (RuntimeException e) {
          log.warn("[IMAGE-ROLLBACK-DELETE-FAILED] url={}", imageUrl, e);
        }
      }
    });
  }

  // 파트 순(PLAN → DESIGN → WEB → APP → SERVER), 같은 파트는 id 순. 파트 미지정은 맨 뒤
  private List<ProjectDetailResponse.Member> toMembers(Collection<User> users) {
    return users.stream()
        .sorted(Comparator.comparing(User::getPart, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(User::getId))
        .map(ProjectDetailResponse.Member::of)
        .toList();
  }

  private static String blankToNull(String value) {
    return (value == null || value.isBlank()) ? null : value.strip();
  }
}
