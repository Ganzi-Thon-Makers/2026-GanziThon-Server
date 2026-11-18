package com.ganzithon.makers.common.seed;

import com.ganzithon.makers.platform.domain.booth.domain.Booth;
import com.ganzithon.makers.platform.domain.booth.domain.Zone;
import com.ganzithon.makers.platform.domain.booth.infrastructure.BoothRepository;
import com.ganzithon.makers.platform.domain.project.domain.Platform;
import com.ganzithon.makers.platform.domain.project.domain.Project;
import com.ganzithon.makers.platform.domain.project.infrastructure.ProjectRepository;
import com.ganzithon.makers.platform.domain.timetable.domain.Timetable;
import com.ganzithon.makers.platform.domain.timetable.infrastructure.TimetableRepository;
import com.ganzithon.makers.platform.domain.user.domain.Part;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import com.ganzithon.makers.platform.domain.user.domain.User;
import com.ganzithon.makers.platform.domain.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

  private static final String THUMBNAIL_URL =
      "https://kr.object.ncloudstorage.com/ganzithon-makers/thumnail.png";

  private static final Zone[] ZONE_ORDER = {Zone.ZONE1, Zone.ZONE2, Zone.ZONE3};
  private static final String[] ZONE_FLOORS = {"본관 3층", "신관 2층", "신관 1층"};

  private static final String[] SURNAMES =
      {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"};
  private static final String[] GIVEN_NAMES =
      {"서연", "준혁", "민수", "지우", "다은", "현우", "소영", "태웅", "채린", "민재",
          "예진", "도현", "수아", "성민", "하늘", "유진", "재민", "서윤", "지훈", "태리"};

  private final ProjectRepository projectRepository;
  private final BoothRepository boothRepository;
  private final TimetableRepository timetableRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${seed.password:seed_password}")
  private String seedPassword;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedProjectsAndBooths();
    seedTimetables();
    seedUsers();
  }

  private void seedProjectsAndBooths() {
    if (projectRepository.count() > 0) {
      log.info("[DataSeeder] project already seeded. skip.");
      return;
    }

    record ProjectSeed(
        String title,
        String teamName,
        String summary,
        String description,
        String serviceUrl,
        String techStack,
        Platform platform
    ) {}

    List<ProjectSeed> seeds = List.of(
        new ProjectSeed(
            "반차 — 직장인 반려동물 돌봄 매칭",
            "댕댕이지키미",
            "혼자 사는 직장인의 반려동물을 같은 동네 이웃과 돌봄 매칭해주는 플랫폼",
            "야근이나 갑작스러운 출장으로 반려동물을 혼자 둬야 할 때, 인증된 같은 동네 이웃에게 산책·급식을 맡길 수 있는 돌봄 매칭 서비스입니다. 위치 기반 매칭, 돌봄 일지 사진 전송, 실시간 산책 경로 공유 기능을 제공하며, 돌봄 완료 후 크레딧으로 보상받아 다음에 내 강아지를 맡길 때 사용할 수 있습니다.",
            "https://bancha.ganzithon.com",
            "React, TypeScript, Spring Boot 3, MySQL 8, NCP Object Storage, Kakao Map API",
            Platform.WEB
        ),
        new ProjectSeed(
            "약속잡자 — 그룹 일정 자동 조율 앱",
            "언제되니",
            "팀원 각자 캘린더를 연동하면 겹치는 빈 시간을 자동으로 찾아주는 일정 조율 서비스",
            "단체 카톡방에서 '언제 돼?'라고 물어보며 며칠씩 걸리던 약속 잡기를 자동화했습니다. 구글 캘린더를 연동하면 모두가 비어있는 시간대를 자동 계산해 상위 3개 후보를 추천하고, 투표로 확정하면 각자 캘린더에 일정이 자동 등록됩니다. 장소는 중간 지점 기준으로 카카오맵 연동 추천까지 지원합니다.",
            null,
            "Flutter, Riverpod, NestJS, PostgreSQL, Google Calendar API",
            Platform.APP
        ),
        new ProjectSeed(
            "대충산 — 등산 입문자를 위한 코스 추천 & 인증",
            "산타는토끼",
            "체력 수준에 맞는 등산 코스 추천과 정상 인증 사진으로 동기부여를 주는 등산 도우미",
            "등산을 시작하고 싶지만 어디부터 가야 할지 모르는 입문자를 위한 서비스입니다. 평소 운동량과 원하는 소요 시간을 입력하면 난이도별 코스를 추천하고, 정상에서 GPS 인증 사진을 찍으면 배지를 수여합니다. 등산로 실시간 혼잡도와 등산 후 근처 맛집 추천도 함께 제공합니다.",
            "https://daechungsan.ganzithon.com",
            "Next.js 14, TailwindCSS, Spring Boot, JPA, MySQL, 공공데이터포털 산림청 API",
            Platform.WEB
        ),
        new ProjectSeed(
            "냉털레시피 — 냉장고 재료로 만드는 AI 레시피",
            "냉장고털이",
            "냉장고에 있는 재료 사진을 찍으면 만들 수 있는 요리 레시피를 추천해주는 앱",
            "냉장고 안 재료를 카메라로 찍으면 Vision AI가 재료를 인식하고, 그 재료만으로 만들 수 있는 레시피를 난이도·조리시간 순으로 추천합니다. 유통기한 임박 재료를 우선 활용하도록 정렬해 식재료 낭비를 줄이고, 부족한 양념 하나만 사도 되는 최소 구매 옵션도 안내합니다.",
            null,
            "Kotlin, Jetpack Compose, Hilt, FastAPI, YOLOv8, OpenAI GPT-4o",
            Platform.APP
        ),
        new ProjectSeed(
            "독서모임 북클럽 — 온라인 독서 토론 플랫폼",
            "책읽는사람들",
            "챕터별 진도를 맞춰가며 비동기로 토론하는 온라인 독서모임 서비스",
            "오프라인 독서모임은 시간 맞추기가 어려워 지속하기 힘듭니다. 북클럽은 챕터 단위로 진도를 설정하고, 각 챕터마다 발제문과 질문을 남기며 멤버들이 댓글로 비동기 토론하는 구조입니다. 형광펜 공유, 발제문 템플릿, 주간 진도 알림으로 모임 운영 부담을 최소화했습니다.",
            "https://bookclub.ganzithon.com",
            "Vue 3, Pinia, Spring Boot, Spring Security, Redis, MySQL",
            Platform.WEB
        ),
        new ProjectSeed(
            "중고거래 사기 방지 — 안전거래 체크리스트 봇",
            "사기없는세상",
            "중고거래 채팅을 분석해 사기 위험도를 실시간으로 알려주는 크롬 확장 프로그램",
            "중고나라·당근마켓 채팅창에서 판매자의 계좌번호, 전화번호를 자동 인식해 사기 이력 데이터베이스와 대조하고, '직거래 거부', '선입금 요구' 같은 위험 패턴을 감지해 경고합니다. 더치트 API 연동으로 조회 이력이 있는 계좌는 즉시 위험 표시합니다.",
            "https://safetrade.ganzithon.com",
            "Chrome Extension, TypeScript, Spring Boot, MySQL, 더치트 API",
            Platform.WEB
        ),
        new ProjectSeed(
            "공강메이트 — 대학생 공강 시간표 겹치기 찾기",
            "공강의민족",
            "같은 과 친구들과 시간표를 공유하면 겹치는 공강 시간을 자동으로 찾아주는 앱",
            "대학생은 수업 사이 공강이 제각각이라 같이 밥 먹거나 스터디할 시간 찾기가 어렵습니다. 시간표를 등록하면 친구들과 겹치는 공강을 자동 계산하고, 학식당 메뉴·빈 강의실 정보도 함께 보여줍니다. 에브리타임 시간표 이미지를 업로드하면 OCR로 자동 등록됩니다.",
            null,
            "React Native, Expo, Spring Boot, JPA, MySQL, Tesseract OCR",
            Platform.APP
        ),
        new ProjectSeed(
            "집중모드 — 스마트폰 사용 줄이기 챌린지",
            "폰내려놔",
            "친구와 함께하는 스크린타임 줄이기 경쟁 앱. 폰을 뒤집으면 타이머가 시작됩니다",
            "공부나 업무 중 스마트폰을 못 놓는 사람들을 위한 집중 타이머 앱입니다. 폰을 화면 아래로 뒤집으면 집중 시간이 기록되고, 들면 중단됩니다. 친구와 주간 집중 시간을 경쟁하고, 목표 달성 시 키우는 가상 식물이 자라는 게이미피케이션으로 지속 동기를 부여합니다.",
            null,
            "Swift, SwiftUI, CoreMotion, Spring Boot, Firebase Cloud Messaging",
            Platform.APP
        ),
        new ProjectSeed(
            "전시회 큐레이터 — 전시 일정 모아보기 & 동행 구하기",
            "미술관앞커플",
            "서울 전시회 정보를 한곳에 모으고, 같이 볼 사람을 구하는 문화생활 플랫폼",
            "인스타그램·예술의전당 등 흩어진 전시 정보를 크롤링해 하나의 캘린더로 보여주고, 마감 임박 전시를 알림으로 알려줍니다. 혼자 전시 보기 아쉬운 사람을 위해 같은 전시에 관심 있는 사람끼리 동행을 구할 수 있는 게시판과 평점 기반 전시 리뷰를 제공합니다.",
            "https://exhibit.ganzithon.com",
            "React, Spring Boot, Selenium 크롤러, Elasticsearch, MySQL",
            Platform.WEB
        ),
        new ProjectSeed(
            "월세 백년전쟁 — 우리 동네 월세 시세 비교",
            "방구석연구소",
            "같은 건물·동네의 실제 월세 계약 데이터로 내 월세가 비싼지 비교해주는 서비스",
            "부동산 실거래가 공개 데이터를 분석해 내가 사는 건물과 반경 500m의 월세 시세를 비교해줍니다. 면적·층수·준공연도를 보정한 '적정 월세'를 산출하고, 재계약 시 참고할 협상 근거 리포트를 자동 생성합니다. 관리비·보증금 포함 총주거비 기준 비교도 지원합니다.",
            "https://wolse.ganzithon.com",
            "Next.js, D3.js, Spring Batch, Spring Boot, 국토교통부 실거래가 API, MySQL",
            Platform.WEB
        )
    );

    List<Project> projects = seeds.stream()
        .map(s -> Project.create(
            s.title(),
            s.teamName(),
            s.summary(),
            s.description(),
            s.serviceUrl(),
            THUMBNAIL_URL,
            s.techStack(),
            s.platform()
        ))
        .toList();
    projectRepository.saveAll(projects);

    // 팀마다 구역/자리가 다르도록 구역을 순환 배정하고, 구역 안에서 자리 번호를 올림
    List<Booth> booths = new ArrayList<>();
    for (int i = 0; i < projects.size(); i++) {
      int zoneIdx = i % ZONE_ORDER.length;
      int slot = i / ZONE_ORDER.length + 1;
      booths.add(Booth.create(
          projects.get(i).getId(),
          ZONE_ORDER[zoneIdx],
          String.format("%s %d번 테이블", ZONE_FLOORS[zoneIdx], slot),
          i + 1
      ));
    }
    boothRepository.saveAll(booths);
    log.info("[DataSeeder] seeded {} projects and {} booths.", projects.size(), booths.size());
  }

  private void seedUsers() {
    if (userRepository.count() > 0) {
      log.info("[DataSeeder] user already seeded. skip.");
      return;
    }

    // 팀 번호가 부스 번호(i + 1)와 항상 일치하도록 id 순으로 고정
    List<Project> projects = projectRepository.findAll().stream()
        .sorted(Comparator.comparing(Project::getId))
        .toList();
    if (projects.isEmpty()) {
      log.info("[DataSeeder] no projects found. skip user seeding.");
      return;
    }

    // BCrypt는 의도적으로 느리므로 한 번만 인코딩해서 모든 시드 계정이 같은 해시를 공유
    String encodedPassword = passwordEncoder.encode(seedPassword);

    List<User> users = new ArrayList<>();

    // 0) 백오피스 로그인 테스트용 관리자 계정 (ADMIN / SUPER_ADMIN만 백오피스 로그인 허용)
    // 주의: User.create의 마지막 인자(teamNumber)가 null을 허용하는지 확인 필요
    users.add(User.create(
        "admin",
        encodedPassword,
        "관리자",
        "010-0000-0000",
        "메이커스대학교",
        Role.SUPER_ADMIN,
        null
    ));

    for (int i = 0; i < projects.size(); i++) {
      Project project = projects.get(i);
      int teamNumber = i + 1;
      Part clientPart = (project.getPlatform() == Platform.APP) ? Part.APP : Part.WEB;

      // 1) 팀 계정: 부스 운영자 (QR 발급)
      User teamAccount = User.create(
          String.format("team%02d", teamNumber),
          encodedPassword,
          project.getTeamName(),
          String.format("010-1234-%04d", teamNumber * 10),
          "메이커스대학교",
          Role.TEAM,
          teamNumber
      );
      teamAccount.assignToProject(project.getId(), null);
      users.add(teamAccount);

      // 2) 팀원 6명: 일반 참가자 (스탬프 수집)
      Part[] parts = {Part.PLAN, Part.DESIGN, clientPart, clientPart, Part.SERVER, Part.SERVER};
      for (int j = 0; j < parts.length; j++) {
        User member = User.create(
            String.format("team%02d_%d", teamNumber, j + 1),
            encodedPassword,
            SURNAMES[(i + j) % SURNAMES.length]
                + GIVEN_NAMES[(i * parts.length + j) % GIVEN_NAMES.length],
            String.format("010-1234-%04d", teamNumber * 10 + j + 1),
            "메이커스대학교",
            Role.PARTICIPANT,
            teamNumber
        );
        member.assignToProject(project.getId(), parts[j]);
        users.add(member);
      }
    }

    userRepository.saveAll(users);
    log.info("[DataSeeder] seeded {} users across {} projects.", users.size(), projects.size());
  }

  private void seedTimetables() {
    if (timetableRepository.count() > 0) {
      log.info("[DataSeeder] timetable already seeded. skip.");
      return;
    }

    // 모든 일정은 앞 일정의 종료 시각 = 다음 일정의 시작 시각 (겹침 없음)
    LocalDateTime base = LocalDateTime.of(2026, 11, 21, 9, 0);
    List<Timetable> timetables = List.of(
        Timetable.create("참가자 등록 및 입장", "행사장 입구에서 체크인 후 명찰 수령",
            base, base.plusHours(1)),                          // 09:00 ~ 10:00
        Timetable.create("오프닝 세레모니", "개회사, 행사 안내, 팀 소개",
            base.plusHours(1), base.plusHours(2)),             // 10:00 ~ 11:00
        Timetable.create("부스 투어 1부", "각 팀 부스 자유 관람 및 스탬프 투어 시작",
            base.plusHours(2), base.plusHours(4)),             // 11:00 ~ 13:00
        Timetable.create("점심 시간", "행사장 1층 라운지에서 도시락 제공",
            base.plusHours(4), base.plusHours(5)),             // 13:00 ~ 14:00
        Timetable.create("부스 투어 2부", "각 팀 부스 자유 관람 및 스탬프 투어 마무리",
            base.plusHours(5), base.plusHours(6)),             // 14:00 ~ 15:00
        Timetable.create("팀 발표 세션", "각 팀 5분 발표 + 3분 Q&A",
            base.plusHours(6), base.plusHours(8)),             // 15:00 ~ 17:00
        Timetable.create("인기 투표", "참가자 대상 현장 인기 투표 진행",
            base.plusHours(8), base.plusHours(8).plusMinutes(30)), // 17:00 ~ 17:30
        Timetable.create("시상식 및 폐회", "수상팀 발표, 경품 추첨, 단체 사진 촬영",
            base.plusHours(9), base.plusHours(10))             // 18:00 ~ 19:00
    );
    timetableRepository.saveAll(timetables);
    log.info("[DataSeeder] seeded {} timetables.", timetables.size());
  }
}
