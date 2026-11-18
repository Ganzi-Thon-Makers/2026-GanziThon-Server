package com.ganzithon.makers.platform.domain.stamp.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.booth.application.BoothService;
import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.project.application.ProjectService;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.stamp.domain.QrToken;
import com.ganzithon.makers.platform.domain.stamp.domain.Stamp;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.QrTokenResponse;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampBoardResponse;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampCollectResponse;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StampFacade {

  private static final int LUCKY_DRAW_THRESHOLD = 15;

  private final StampService stampService;
  private final QrTokenService qrTokenService;
  private final UserService userService;
  private final BoothService boothService;
  private final ProjectService projectService;
  private final StampProperties stampProperties;

  @Transactional
  public QrTokenResponse issueQrToken(Long userId) {
    User user = userService.getById(userId);
    if (user.getRole() != Role.TEAM) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    if (user.getProjectId() == null) {
      throw new BusinessException(ErrorCode.BOOTH_NOT_FOUND);
    }

    Booth booth = boothService.findByProjectId(user.getProjectId());
    QrToken qrToken = qrTokenService.issue(booth.getId());

    return new QrTokenResponse(
        qrToken.getToken(),
        qrToken.getExpiresAt(),
        stampProperties.qrTokenTtlSeconds()
    );
  }

  @Transactional
  public StampCollectResponse collect(Long userId, String token) {
    // 행 락으로 동일 회원의 동시 적립 요청을 직렬화해 쿨다운 우회와 럭키드로우 중복 등록을 막는다
    User user = userService.getByIdForUpdate(userId);
    if (user.getRole() != Role.PARTICIPANT) {
      throw new BusinessException(ErrorCode.STAMP_PARTICIPANT_ONLY);
    }

    QrToken qrToken = qrTokenService.validate(token);
    Booth booth = boothService.findById(qrToken.getBoothId());
    if (booth.getProjectId() != null && booth.getProjectId().equals(user.getProjectId())) {
      throw new BusinessException(ErrorCode.STAMP_OWN_BOOTH);
    }

    // 이미 적립한 부스는 에러 대신 200으로 응답해 어떤 부스인지 알려준다
    if (stampService.existsByUserIdAndBoothId(userId, booth.getId())) {
      return alreadyCollectedResponse(userId, booth.getId(), user);
    }

    try {
      stampService.collect(userId, booth.getId());
    } catch (BusinessException e) {
      if (e.getErrorCode() == ErrorCode.STAMP_ALREADY_COLLECTED) {
        return alreadyCollectedResponse(userId, booth.getId(), user);
      }
      throw e;
    }

    long collectedCount = stampService.countByUserId(userId);
    boolean justEntered = false;
    if (collectedCount >= LUCKY_DRAW_THRESHOLD && !user.isLuckyDrawEntered()) {
      user.enterLuckyDraw();
      justEntered = true;
    }

    return new StampCollectResponse(
        booth.getId(),
        (int) collectedCount,
        remainingToLuckyDraw(collectedCount),
        user.isLuckyDrawEntered(),
        justEntered,
        false
    );
  }

  private StampCollectResponse alreadyCollectedResponse(Long userId, Long boothId, User user) {
    long collectedCount = stampService.countByUserId(userId);
    return new StampCollectResponse(
        boothId,
        (int) collectedCount,
        remainingToLuckyDraw(collectedCount),
        user.isLuckyDrawEntered(),
        false,
        true
    );
  }

  public StampBoardResponse getBoard(Long userId) {
    User user = userService.getById(userId);
    if (user.getRole() != Role.PARTICIPANT) {
      throw new BusinessException(ErrorCode.STAMP_PARTICIPANT_ONLY);
    }

    List<Stamp> stamps = stampService.findAllByUserId(userId);
    List<Booth> allBooths = boothService.findAllOrderByBoothNumber();
    Map<Long, Stamp> stampsByBoothId = stamps.stream()
        .collect(Collectors.toMap(Stamp::getBoothId, stamp -> stamp));

    List<Long> projectIds = allBooths.stream()
        .map(Booth::getProjectId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, String> teamNamesByProjectId = projectService.findAllByIds(projectIds).stream()
        .collect(Collectors.toMap(Project::getId, Project::getTeamName));

    Map<Zone, List<Booth>> boothsByZone = allBooths.stream()
        .collect(Collectors.groupingBy(Booth::getZone));

    List<StampBoardResponse.ZoneStatus> zoneStatuses = Arrays.stream(Zone.values())
        .map(zone -> {
          List<Booth> zoneBooths = boothsByZone.getOrDefault(zone, List.of());
          List<StampBoardResponse.BoothStamp> boothStamps = zoneBooths.stream()
              .map(booth -> {
                Stamp stamp = stampsByBoothId.get(booth.getId());
                return new StampBoardResponse.BoothStamp(
                    booth.getId(),
                    teamNamesByProjectId.get(booth.getProjectId()),
                    booth.getBoothNumber(),
                    stamp != null,
                    stamp != null ? stamp.getCreatedAt() : null
                );
              })
              .toList();
          int total = zoneBooths.size();
          int collected = (int) boothStamps.stream().filter(StampBoardResponse.BoothStamp::collected).count();
          return new StampBoardResponse.ZoneStatus(
              zone,
              total,
              collected,
              total - collected,
              collected >= stampProperties.zoneCompletionThreshold(),
              boothStamps
          );
        })
        .toList();

    return new StampBoardResponse(
        stamps.size(),
        remainingToLuckyDraw(stamps.size()),
        user.isLuckyDrawEntered(),
        zoneStatuses
    );
  }

  private int remainingToLuckyDraw(long collectedCount) {
    return (int) Math.max(0, LUCKY_DRAW_THRESHOLD - collectedCount);
  }
}
