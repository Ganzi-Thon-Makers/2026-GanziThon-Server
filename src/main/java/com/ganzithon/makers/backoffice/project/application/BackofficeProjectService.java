package com.ganzithon.makers.backoffice.project.application;

import com.ganzithon.makers.backoffice.image.storage.ImageStorage;
import com.ganzithon.makers.backoffice.project.presentation.dto.MemberCandidateResponse;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectCreateRequest;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectDetailResponse;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectListResponse;
import com.ganzithon.makers.backoffice.project.presentation.dto.ProjectUpdateRequest;
import com.ganzithon.makers.backoffice.project.presentation.dto.TeamResponse;
import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.booth.application.BoothService;
import com.ganzithon.makers.platform.domain.project.application.ProjectService;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Role;
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

/** 프로젝트 목록 조회/검색, 상세 조회, 등록, 수정, 삭제 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeProjectService {

  private static final String THUMBNAIL_DIRECTORY = "projects/thumbnails";
  private static final List<Role> MEMBER_ROLES = List.of(Role.PARTICIPANT, Role.TEAM);

  private final ProjectService projectService;
  private final VoteService voteService;
  private final UserService userService;
  private final BoothService boothService;
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

  // 팀 번호 선택용 (팀 번호가 없는 프로젝트는 제외)
  public List<TeamResponse> getTeams() {
    return projectService.findAllWithTeamNumber().stream()
        .map(TeamResponse::of)
        .toList();
  }

  // 팀원 선택용 후보: 참가자, 팀 계정. 다른 프로젝트 소속 여부는 projectId 로 판단
  public List<MemberCandidateResponse> getMemberCandidates(String keyword) {
    return userService.searchByRoles(MEMBER_ROLES, keyword).stream()
        .map(MemberCandidateResponse::of)
        .toList();
  }

  public ProjectDetailResponse getProject(Long projectId) {
    Project project = projectService.findById(projectId);
    return toDetail(project, voteService.countByProjectId(projectId));
  }

  @Transactional
  public ProjectDetailResponse create(ProjectCreateRequest request, MultipartFile thumbnail) {
    // 팀원·팀 번호 검증을 먼저 끝내서, 검증 실패 시 이미지가 업로드되지 않도록 함
    Map<Long, User> users = findMembers(request.memberIdsOrEmpty());
    validateMemberRoles(users.values());
    users.values().forEach(user -> validateAssignable(user, null));
    if (projectService.existsByTeamNumber(request.teamNumber())) {
      throw new BusinessException(ErrorCode.DUPLICATE_TEAM_NUMBER);
    }

    String thumbnailUrl = imageStorage.upload(thumbnail, THUMBNAIL_DIRECTORY);
    deleteImageOnRollback(thumbnailUrl);

    Project project = projectService.save(Project.create(
        request.title(),
        request.teamName(),
        request.teamNumber(),
        request.summary(),
        request.description(),
        blankToNull(request.serviceUrl()),
        thumbnailUrl,
        request.techStack(),
        request.platform()
    ));

    users.values().forEach(user -> user.joinProject(project.getId()));

    return toDetail(project, 0L);
  }

  // thumbnail 이 없으면 기존 대표 이미지 유지
  @Transactional
  public ProjectDetailResponse update(Long projectId, ProjectUpdateRequest request, MultipartFile thumbnail) {
    Project project = projectService.findById(projectId);

    // 팀원·팀 번호 검증을 먼저 끝내서, 검증 실패 시 이미지가 업로드되지 않도록 함
    Map<Long, User> users = findMembers(request.memberIdsOrEmpty());
    validateMemberRoles(users.values());
    users.values().forEach(user -> validateAssignable(user, projectId));
    if (projectService.existsByTeamNumberExcept(request.teamNumber(), projectId)) {
      throw new BusinessException(ErrorCode.DUPLICATE_TEAM_NUMBER);
    }

    if (thumbnail != null && !thumbnail.isEmpty()) {
      String oldThumbnailUrl = project.getThumbnailUrl();
      String newThumbnailUrl = imageStorage.upload(thumbnail, THUMBNAIL_DIRECTORY);
      deleteImageOnRollback(newThumbnailUrl);
      deleteImageOnCommit(oldThumbnailUrl);
      project.changeThumbnail(newThumbnailUrl);
    }

    project.update(
        request.title(),
        request.teamName(),
        request.teamNumber(),
        request.summary(),
        request.description(),
        blankToNull(request.serviceUrl()),
        request.techStack(),
        request.platform()
    );

    // 참가자: 요청 목록에서 빠지면 연결 해제
    for (User current : userService.findAllByProjectIdAndRole(projectId, Role.PARTICIPANT)) {
      if (!users.containsKey(current.getId())) {
        current.leaveProject();
      }
    }
    // 팀 계정: 요청에 있으면 교체, 없으면 기존 연결 유지
    boolean teamAccountRequested = users.values().stream().anyMatch(user -> user.getRole() == Role.TEAM);
    if (teamAccountRequested) {
      for (User current : userService.findAllByProjectIdAndRole(projectId, Role.TEAM)) {
        if (!users.containsKey(current.getId())) {
          current.leaveProject();
        }
      }
    }
    users.values().forEach(user -> user.joinProject(projectId));

    return toDetail(project, voteService.countByProjectId(projectId));
  }

  // 팀원·부스는 연결만 해제하고, 받은 투표는 삭제. 대표 이미지는 커밋 후 삭제 (롤백되면 유지)
  @Transactional
  public void delete(Long projectId) {
    Project project = projectService.findById(projectId);
    userService.clearProject(projectId);
    boothService.clearProject(projectId);
    voteService.deleteAllByProjectId(projectId);
    projectService.delete(project);
    deleteImageOnCommit(project.getThumbnailUrl());
  }

  // 요청한 팀원(userId)을 조회. 중복이면 400, 없는 회원이 있으면 404
  private Map<Long, User> findMembers(List<Long> userIds) {
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

  // 참가자, 팀 계정만 가능. 팀 계정은 최대 1개
  private void validateMemberRoles(Collection<User> users) {
    if (users.stream().anyMatch(user -> !MEMBER_ROLES.contains(user.getRole()))) {
      throw new BusinessException(ErrorCode.INVALID_PROJECT_MEMBER_ROLE);
    }
    if (users.stream().filter(user -> user.getRole() == Role.TEAM).count() > 1) {
      throw new BusinessException(ErrorCode.MULTIPLE_TEAM_ACCOUNTS);
    }
  }

  // 상세 응답: 팀원 목록은 참가자만 (팀 계정은 응답하지 않음)
  private ProjectDetailResponse toDetail(Project project, long voteCount) {
    List<User> participants = userService.findAllByProjectIdAndRole(project.getId(), Role.PARTICIPANT);
    return ProjectDetailResponse.of(project, voteCount, toMembers(participants));
  }

  // 소속이 없거나 이 프로젝트 소속이면 연결 가능. 다른 프로젝트 소속이면 409 (등록 시 projectId 는 null)
  private void validateAssignable(User user, Long projectId) {
    if (user.getProjectId() != null && !user.getProjectId().equals(projectId)) {
      throw new BusinessException(ErrorCode.PROJECT_MEMBER_ALREADY_ASSIGNED);
    }
  }

  // DB 저장이 실패(롤백)하면 이미 올린 이미지를 지워서 저장소에 주인 없는 파일이 남지 않게 함
  private void deleteImageOnRollback(String imageUrl) {
    deleteImageAfterCompletion(imageUrl, false);
  }

  // 수정이 반영(커밋)된 뒤에만 이전 이미지를 지움. 커밋 전에 지우면 롤백 시 이미지가 사라짐
  private void deleteImageOnCommit(String imageUrl) {
    deleteImageAfterCompletion(imageUrl, true);
  }

  private void deleteImageAfterCompletion(String imageUrl, boolean onCommit) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCompletion(int status) {
        if ((status == STATUS_COMMITTED) != onCommit) {
          return;
        }
        try {
          imageStorage.delete(imageUrl);
        } catch (RuntimeException e) {
          log.warn("[IMAGE-DELETE-AFTER-TX-FAILED] url={}", imageUrl, e);
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
