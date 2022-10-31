/*
package ru.practicum.main_server.service.private_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Header;
import org.mockserver.model.JsonSchemaBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_server.mapper.EventMapper;
import ru.practicum.main_server.model.*;
import ru.practicum.main_server.model.dto.*;
import ru.practicum.main_server.repository.CategoryRepository;
import ru.practicum.main_server.repository.EventRepository;
import ru.practicum.main_server.repository.UserRepository;
import ru.practicum.main_server.service.admin_service.AdminCategoryService;
import ru.practicum.main_server.service.public_service.PublicCategoryService;

import javax.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

@Slf4j
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PrivateEventServiceTest {
    @Autowired
    EventRepository repo;
    @Autowired
    UserRepository userRepo;
    @Autowired
    PrivateEventService privateService;
    @Autowired
    AdminCategoryService admService;
    @Autowired
    CategoryRepository catRepo;
    @Test
    void delete() {
        Category cat = new Category(1L, "UNNAMED");
        User initiator = new User(1L, "sdfsdfsf", "aiu@mail.ru");
        userRepo.save(initiator);
        catRepo.save(cat);
        Event event = new Event(1L, "sdfsdf", cat, LocalDateTime.now(), "b",
                LocalDateTime.now().plusHours(5), initiator, Location.builder().lat(026544).lon(56465).build(),
                true, 654, LocalDateTime.now().plusHours(1), false,
                State.PUBLISHED, "dfdfgdfdddd" );
        EventFullDto fullEvent = EventMapper.toEventFullDto(event);
        EventShortDto shortDto = EventMapper.toEventShortDto(event);
        NewEventDto newEventDto = new NewEventDto("sdfsdf", 1, "111",
                LocalDateTime.now().plusHours(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                Location.builder().lat(026544).lon(56465).build(),
                true, 654, false, "CONFIRMED", "dfgdfg");


        assertEquals(fullEvent, privateService.createEvent(1L, newEventDto));

    }
}

class TestMockServer {
    private void createExpectationForInvalidAuth() {
        Category cat = new Category(1L, "UNNAMED");
        User initiator = new User(1L, "sdfsdfsf", "aiu@mail.ru");
        Event event = new Event(1L, "sdfsdf", cat, LocalDateTime.now(), "b",
                LocalDateTime.now().plusHours(5), initiator, Location.builder().lat(026544).lon(56465).build(),
                true, 654, LocalDateTime.now().plusHours(1), false,
                State.PUBLISHED, "dfdfgdfdddd" );
        EventFullDto fullEvent = EventMapper.toEventFullDto(event);
        EventShortDto shortDto = EventMapper.toEventShortDto(event);
        NewEventDto newEventDto = new NewEventDto("sdfsdf", 1, "111",
                LocalDateTime.now().plusHours(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                Location.builder().lat(026544).lon(56465).build(),
                true, 654, false, "CONFIRMED", "dfgdfg");
        new MockServerClient("localhost", 9090)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("localhost:9090/stats")
                                .withHeader("\"Content-type\", \"application/json\"")
                                .withBody(exact("{username: 'foo', password: 'bar'}")),
                        exactly(1))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"))
                                .withBody()
                                .withDelay(TimeUnit.SECONDS,1)
                );
    }
    // ...
}*/
