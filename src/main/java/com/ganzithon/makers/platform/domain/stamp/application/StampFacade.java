package com.ganzithon.makers.platform.domain.stamp.application;

import com.ganzithon.makers.common.exception.BusinessException;
import com.ganzithon.makers.common.exception.ErrorCode;
import com.ganzithon.makers.platform.domain.booth.application.BoothService;
import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.stamp.domain.QrToken;
import com.ganzithon.makers.platform.domain.stamp.domain.Stamp;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.QrTokenResponse;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampBoardResponse;
import com.ganzithon.makers.platform.domain.stamp.presentation.dto.StampCollectResponse;
import com.ganzithon.makers.platform.domain.user.application.UserService;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import java.util.List;
import java.util.Map;
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
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    QrToken qrToken = qrTokenService.validate(token);
    stampService.collect(userId, qrToken.getBoothId());

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
    List<Stamp> stamps = stampService.findAllByUserId(userId);

    List<Long> boothIds = stamps.stream()
        .map(Stamp::getBoothId)
        .toList();
    Map<Long, Booth> boothsById = boothService.findAllByIds(boothIds).stream()
        .collect(Collectors.toMap(Booth::getId, booth -> booth));

    List<StampBoardResponse.CollectedStamp> collectedStamps = stamps.stream()
        .map(stamp -> {
          Booth booth = boothsById.get(stamp.getBoothId());
          return new StampBoardResponse.CollectedStamp(
              stamp.getBoothId(),
              booth != null ? booth.getZone() : null,
              booth != null ? booth.getBoothNumber() : null,
              stamp.getCreatedAt()
          );
        })
        .toList();

    return new StampBoardResponse(
        collectedStamps.size(),
        remainingToLuckyDraw(collectedStamps.size()),
        user.isLuckyDrawEntered(),
        collectedStamps
    );
  }

  private int remainingToLuckyDraw(long collectedCount) {
    return (int) Math.max(0, LUCKY_DRAW_THRESHOLD - collectedCount);
  }
}
