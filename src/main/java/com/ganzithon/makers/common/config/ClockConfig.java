package com.ganzithon.makers.common.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

  // 행사 시간은 한국 시간 기준으로 저장/비교하므로, 서버 시간대(UTC 등)와 무관하게 Asia/Seoul을 사용한다
  @Bean
  public Clock clock() {
    return Clock.system(ZoneId.of("Asia/Seoul"));
  }
}
