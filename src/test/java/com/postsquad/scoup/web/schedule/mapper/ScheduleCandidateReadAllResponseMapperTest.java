package com.postsquad.scoup.web.schedule.mapper;

import com.postsquad.scoup.web.schedule.controller.response.ScheduleCandidateReadAllResponse;
import com.postsquad.scoup.web.schedule.domain.Schedule;
import com.postsquad.scoup.web.schedule.domain.ScheduleCandidate;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Stream;

class ScheduleCandidateReadAllResponseMapperTest {

    @ParameterizedTest
    @MethodSource("mapFromScheduleCandidateProvider")
    void mapFromScheduleCandidate(ScheduleCandidate givenScheduleCandidate, ScheduleCandidateReadAllResponse expectedScheduleCandidateReadAllResponse) {
        // when
        ScheduleCandidateReadAllResponse actualScheduleCandidateResponse = ScheduleCandidateReadAllResponseMapper.INSTANCE.map(givenScheduleCandidate);

        // then
        BDDAssertions.then(actualScheduleCandidateResponse)
                     .usingRecursiveComparison()
                     .isEqualTo(expectedScheduleCandidateReadAllResponse);
    }

    static Stream<Arguments> mapFromScheduleCandidateProvider() {
        return Stream.of(
                Arguments.arguments(
                        ScheduleCandidate.builder()
                                         .schedule(Schedule.builder()
                                                           .title("title")
                                                           .description("description")
                                                           .dueDateTime(LocalDateTime.of(2021, 9, 8, 0, 0))
                                                           .colorCode("#00ff0000")
                                                           .scheduleCandidates(Collections.emptyList())
                                                           .build())
                                         .isConfirmed(true)
                                         .startDateTime(LocalDateTime.of(2021, 9, 8, 0, 0))
                                         .endDateTime(LocalDateTime.of(2021, 9, 8, 0, 0))
                                         .build(),
                        ScheduleCandidateReadAllResponse.builder()
                                                        .startDateTime(LocalDateTime.of(2021, 9, 8, 0, 0))
                                                        .endDateTime(LocalDateTime.of(2021, 9, 8, 0, 0))
                                                        .isConfirmed(true)
                                                        .scheduleTitle("title")
                                                        .scheduleDescription("description")
                                                        .colorCode("#00ff0000")
                                                        .build()
                )
        );
    }
}
