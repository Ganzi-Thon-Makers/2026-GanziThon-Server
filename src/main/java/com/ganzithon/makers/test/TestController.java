package com.ganzithon.makers.test;

import com.ganzithon.makers.common.dto.SuccessResponse;
import com.ganzithon.makers.exception.BusinessException;
import com.ganzithon.makers.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/tests")
public class TestController implements TestApiSpec {

  private static final List<TestDto.Response> TESTS = List.of(
      new TestDto.Response(1L, "해커톤 1회차", TestStatus.OPEN),
      new TestDto.Response(2L, "해커톤 2회차", TestStatus.CLOSED)
  );

  @Override
  @GetMapping("/{id}")
  public SuccessResponse<TestDto.Response> get(@PathVariable Long id) {
    TestDto.Response found = TESTS.stream()
        .filter(test -> test.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    return SuccessResponse.of(found);
  }

  @Override
  @GetMapping
  public SuccessResponse<List<TestDto.Response>> list(
      @RequestParam(required = false) TestStatus status) {
    Stream<TestDto.Response> stream = TESTS.stream();
    if (status != null) {
      stream = stream.filter(test -> test.status() == status);
    }
    return SuccessResponse.of(stream.toList());
  }

  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponse<TestDto.Response> create(
      @Valid @RequestBody TestDto.CreateRequest request) {
    TestDto.Response created = new TestDto.Response(
        99L,
        request.name(),
        request.status() != null ? request.status() : TestStatus.OPEN);
    return SuccessResponse.of(created, "생성되었습니다.");
  }

  @Override
  @DeleteMapping("/{id}")
  public SuccessResponse<Void> delete(@PathVariable Long id) {
    if (id == 1L) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    if (id != 2L) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    return SuccessResponse.of("삭제되었습니다.");
  }
}
