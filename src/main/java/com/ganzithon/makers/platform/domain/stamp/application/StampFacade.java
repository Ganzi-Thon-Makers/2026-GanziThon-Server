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
    User user = userService.getById(userId);
    if (user.getRole() != Role.PARTICIPANT) {
      throw new BusinessException(ErrorCode.STAMP_PARTICIPANT_ONLY);
    }

    QrToken qrToken = qrTokenService.validate(token);
    Booth booth = boothService.findById(qrToken.getBoothId());
    if (booth.getProjectId().equals(user.getProjectId())) {
      throw new BusinessException(ErrorCode.STAMP_OWN_BOOTH);
    }

    stampService.collect(userId, booth.getId());

    long collectedCount = stampService.countByUserId(userId);
    boolean justEntered = false;
    if (collectedCount >= LUCKY_DRAW_THRESHOLD && !user.isLuckyDrawEntered()) {
      user.enterLuckyDraw();
      justEntered = true;
    }

    return new StampCollectResponse(
        (int) collectedCount,
        remainingToLuckyDraw(collectedCount),
        user.isLuckyDrawEntered(),
        justEntered
    );
  }

  public StampBoardResponse getBoard(Long userId) {
    User user = userService.getById(userId);
    if (user.getRole() != Role.PARTICIPANT) {
      throw new BusinessException(ErrorCode.STAMP_PARTICIPANT_ONLY);
    }

    List<Stamp> stamps = stampService.findAllByUserId(userId);
    List<Booth> allBooths = boothService.findAllOrderByBoothNumber();
    Map<Long, Booth> boothsById = allBooths.stream()
        .collect(Collectors.toMap(Booth::getId, booth -> booth));

    List<Long> projectIds = allBooths.stream()
        .map(Booth::getProjectId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, String> teamNamesByProjectId = projectService.findAllByIds(projectIds).stream()
        .collect(Collectors.toMap(Project::getId, Project::getTeamName));

    List<StampBoardResponse.CollectedStamp> collectedStamps = stamps.stream()
        .map(stamp -> {
          Booth booth = boothsById.get(stamp.getBoothId());
          String boothName = booth != null
              ? teamNamesByProjectId.get(booth.getProjectId())
              : null;
          return new StampBoardResponse.CollectedStamp(
              stamp.getBoothId(),
              boothName,
              booth != null ? booth.getZone() : null,
              booth != null ? booth.getBoothNumber() : null,
              stamp.getCreatedAt()
          );
        })
        .toList();

    Map<Zone, List<StampBoardResponse.CollectedStamp>> stampsByZone = collectedStamps.stream()
        .filter(s -> s.zone() != null)
        .collect(Collectors.groupingBy(StampBoardResponse.CollectedStamp::zone));

    Map<Zone, Long> totalByZone = allBooths.stream()
        .collect(Collectors.groupingBy(Booth::getZone, Collectors.counting()));

    List<StampBoardResponse.ZoneStatus> zoneStatuses = Arrays.stream(Zone.values())
        .map(zone -> {
          int total = totalByZone.getOrDefault(zone, 0L).intValue();
          List<StampBoardResponse.CollectedStamp> zoneStamps =
              stampsByZone.getOrDefault(zone, List.of());
          int collected = zoneStamps.size();
          return new StampBoardResponse.ZoneStatus(
              zone,
              total,
              collected,
              total - collected,
              collected >= stampProperties.zoneCompletionThreshold(),
              zoneStamps
          );
        })
        .toList();

    return new StampBoardResponse(
        collectedStamps.size(),
        remainingToLuckyDraw(collectedStamps.size()),
        user.isLuckyDrawEntered(),
        zoneStatuses
    );
  }

  private int remainingToLuckyDraw(long collectedCount) {
    return (int) Math.max(0, LUCKY_DRAW_THRESHOLD - collectedCount);
  }
}
