package com.postsquad.scoup.web.schedule;

import com.postsquad.scoup.web.group.domain.Group;
import com.postsquad.scoup.web.schedule.domain.ConfirmedSchedule;
import com.postsquad.scoup.web.schedule.domain.Schedule;
import com.postsquad.scoup.web.user.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfirmedScheduleTestDataSetup {

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public void execute() {
        User user = entityManager.find(User.class, 1L);
        Group group = Group.builder()
                           .name("name")
                           .description("")
                           .schedules(new ArrayList<>())
                           .owner(user)
                           .build();

        Schedule schedule = Schedule.builder()
                                    .group(group)
                                    .title("schedule title")
                                    .description("schedule description")
                                    .dueDateTime(LocalDateTime.of(2021, 9, 23, 0, 0))
                                    .build();
        group.addSchedule(schedule);

        ConfirmedSchedule confirmedSchedule = ConfirmedSchedule.builder()
                                                               .colorCode("#CAB8FF")
                                                               .startDateTime(LocalDateTime.of(2021, 9, 25, 9, 0))
                                                               .endDateTime(LocalDateTime.of(2021, 9, 25, 11, 0))
                                                               .confirmedParticipants(List.of(user))
                                                               .build();
        schedule.setConfirmedSchedule(confirmedSchedule);
        entityManager.persist(group);
    }
}
