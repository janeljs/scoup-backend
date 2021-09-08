package com.postsquad.scoup.web.group;

import com.postsquad.scoup.web.AcceptanceTestBase;
import com.postsquad.scoup.web.error.controller.response.ErrorResponse;
import com.postsquad.scoup.web.group.controller.request.GroupCreationRequest;
import com.postsquad.scoup.web.group.controller.request.GroupModificationRequest;
import com.postsquad.scoup.web.group.domain.Group;
import com.postsquad.scoup.web.group.repository.GroupRepository;
import com.postsquad.scoup.web.user.domain.User;
import com.postsquad.scoup.web.group.provider.CreateGroupProvider;
import com.postsquad.scoup.web.group.provider.CreateGroupWithExistingNameProvider;
import com.postsquad.scoup.web.group.provider.ModifyGroupProvider;
import com.postsquad.scoup.web.group.provider.ValidateGroupCreationRequestProvider;
import com.postsquad.scoup.web.user.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.BDDAssertions.then;

public class GroupAcceptanceTest extends AcceptanceTestBase {

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    // temporary token with sub(userId) = 1, exp = 2023-01-01T00:00:00.000(Korean Standard Time)
    private static String TEST_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjcyNDk4ODAwfQ.DXojMeUGIq77XWvQK0luZtZhsi-c6s9qjiiu9vHhkbg";
    public static User TEST_USER = User.builder()
                                       .nickname("nickname")
                                       .email("email@email.com")
                                       .password("password")
                                       .avatarUrl("url")
                                       .username("username")
                                       .build();

    @BeforeEach
    void setUp() {
        TEST_USER = userRepository.save(TEST_USER);
        groupRepository.save(Group.builder()
                                  .name("name")
                                  .description("description")
                                  .owner(TEST_USER)
                                  .build());
    }

    @AfterEach
    void tearDown() {
        groupRepository.deleteAll();
    }

    @ParameterizedTest
    @ArgumentsSource(CreateGroupProvider.class)
    @DisplayName("사용자가 새로운 그룹을 생성할 수 있다.")
    void createGroup(String description, GroupCreationRequest givenGroupCreationRequest, Group expectedGroup) {
        // given
        String path = "/api/groups";
        RequestSpecification givenRequest = RestAssured.given()
                                                       .baseUri(BASE_URL)
                                                       .port(port)
                                                       .basePath(path)
                                                       .contentType(ContentType.JSON)
                                                       .header("Accept-Language", "en-US")
                                                       .header("Authorization", TEST_TOKEN)
                                                       .body(givenGroupCreationRequest);

        // when
        Response actualResponse = givenRequest.when()
                                              .log().all()
                                              .post();

        // then
        actualResponse.then()
                      .log().all()
                      .statusCode(HttpStatus.CREATED.value());
        Group actualGroup = groupRepository.findById(actualResponse.body().as(Long.class)).orElse(null);
        then(actualGroup)
                .as("그룹 생성: %s", description)
                .usingRecursiveComparison()
                .ignoringFields(ignoringFieldsForResponseWithId)
                .ignoringFields("owner")
                .isEqualTo(expectedGroup);
        then(actualGroup.getOwner())
                .usingRecursiveComparison()
                .ignoringFields(ignoringFieldsForResponseWithId)
                .ignoringFields("oAuthUsers")
                .isEqualTo(expectedGroup.getOwner());
    }

    @ParameterizedTest
    @ArgumentsSource(CreateGroupWithExistingNameProvider.class)
    @DisplayName("중복된 그룹 이름이 있으면 그룹 생성을 할 수 없다.")
    void createGroupWithExistingName(String description, GroupCreationRequest givenGroupCreationRequest, ErrorResponse expectedResponse) {
        // given
        String path = "/api/groups";
        RequestSpecification givenRequest = RestAssured.given()
                                                       .baseUri(BASE_URL)
                                                       .port(port)
                                                       .basePath(path)
                                                       .contentType(ContentType.JSON)
                                                       .header("Accept-Language", "en-US")
                                                       .header("Authorization", TEST_TOKEN)
                                                       .body(givenGroupCreationRequest);

        // when
        Response actualResponse = givenRequest.when()
                                              .log().all()
                                              .post();

        actualResponse.then()
                      .log().all()
                      .statusCode(HttpStatus.BAD_REQUEST.value());
        then(actualResponse.as(ErrorResponse.class))
                .as("그룹 생성: %s", description)
                .usingRecursiveComparison()
                .ignoringFields(ignoringFieldsForErrorResponse)
                .isEqualTo(expectedResponse);
    }

    @ParameterizedTest
    @ArgumentsSource(ValidateGroupCreationRequestProvider.class)
    @DisplayName("GroupCreationRequest validation")
    void validateGroupCreationRequest(String description, GroupCreationRequest givenGroupCreationRequest, ErrorResponse expectedResponse) {
        // given
        String path = "/api/groups";
        RequestSpecification givenRequest = RestAssured.given()
                                                       .baseUri(BASE_URL)
                                                       .port(port)
                                                       .basePath(path)
                                                       .contentType(ContentType.JSON)
                                                       .header("Accept-Language", "en-US")
                                                       .header("Authorization", TEST_TOKEN)
                                                       .body(givenGroupCreationRequest);

        // when
        Response actualResponse = givenRequest.when()
                                              .log().all()
                                              .post();

        // then
        actualResponse.then()
                      .log().all()
                      .statusCode(HttpStatus.BAD_REQUEST.value());
        then(actualResponse.as(ErrorResponse.class))
                .as(description)
                .usingRecursiveComparison()
                .ignoringFields(ignoringFieldsForErrorResponse)
                .isEqualTo(expectedResponse);
    }

    @ParameterizedTest
    @ArgumentsSource(ModifyGroupProvider.class)
    @DisplayName("사용자가 그룹을 수정 할 수 있다")
    void modifyGroup(String description, Long givenGroupId, GroupModificationRequest givenGroupModificationRequest, Group expectedGroup) {
        // given
        String path = "/api/groups/";
        RequestSpecification givenRequest = RestAssured.given()
                                                       .baseUri(BASE_URL)
                                                       .port(port)
                                                       .basePath(path + givenGroupId)
                                                       .contentType(ContentType.JSON)
                                                       .header("Accept-Language", "en-US")
                                                       .header("Authorization", TEST_TOKEN)
                                                       .body(givenGroupModificationRequest);

        // when
        Response actualResponse = givenRequest.when()
                                              .log().all()
                                              .put();

        // then
        actualResponse.then()
                      .log().all()
                      .statusCode(HttpStatus.OK.value());
        then(groupRepository.findById(actualResponse.as(Long.class)).orElse(null))
                .as("그룹 수정: %s", description)
                .usingRecursiveComparison()
                .ignoringFields(ignoringFieldsForResponseWithId)
                .ignoringFields("owner")
                .isEqualTo(expectedGroup);
    }
}
