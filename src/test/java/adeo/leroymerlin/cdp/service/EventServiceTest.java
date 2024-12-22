package adeo.leroymerlin.cdp.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import adeo.leroymerlin.cdp.exception.ErrorMessages;
import adeo.leroymerlin.cdp.exception.ResourceNotFoundException;
import adeo.leroymerlin.cdp.model.Band;
import adeo.leroymerlin.cdp.model.Event;
import adeo.leroymerlin.cdp.model.Member;
import adeo.leroymerlin.cdp.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void delete_ShouldDeleteEvent_WhenEventExists() {
        // Arrange
        Long existingEventId = 1000L;
        when(eventRepository.findById(existingEventId)).thenReturn(Optional.of(new Event()));

        // Act
        eventService.delete(existingEventId);

        // Assert
        verify(eventRepository, times(1)).findById(existingEventId);
        verify(eventRepository, times(1)).deleteById(existingEventId);
    }

    @Test
    void delete_ShouldThrowException_WhenEventDoesNotExist() {
        // Arrange
        Long nonExistingEventId = 1L;
        when(eventRepository.findById(nonExistingEventId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> eventService.delete(nonExistingEventId)
        );

        assertEquals(String.format(ErrorMessages.EVENT_NOT_FOUND, nonExistingEventId), exception.getMessage());
        verify(eventRepository, times(1)).findById(nonExistingEventId);
        verify(eventRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateEvent_ShouldUpdateEvent_WhenEventExists() {
        // Arrange
        Long existingEventId = 1L;
        Event existingEvent = new Event();
        existingEvent.setComment("Ajouter un commentaire");

        Event newEventData = new Event();
        newEventData.setComment("Je suis un commentaire");

        when(eventRepository.findById(existingEventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(existingEvent);

        // Act
        eventService.updateEvent(existingEventId, newEventData);

        // Assert
        assertEquals("Je suis un commentaire", existingEvent.getComment());
        verify(eventRepository, times(1)).findById(existingEventId);
        verify(eventRepository, times(1)).save(existingEvent);
    }

    @Test
    void updateEvent_ShouldThrowException_WhenEventDoesNotExist() {
        // Arrange
        Long nonExistingEventId = 1000L;
        Event newEventData = new Event();
        newEventData.setComment("Je suis un commentaire");

        when(eventRepository.findById(nonExistingEventId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> eventService.updateEvent(nonExistingEventId, newEventData)
        );

        assertEquals(String.format(ErrorMessages.EVENT_NOT_FOUND, nonExistingEventId), exception.getMessage());
        verify(eventRepository, times(1)).findById(nonExistingEventId);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void getFilteredEvents_ShouldReturnFilteredEvents_WhenQueryMatches() {
        // Arrange
        String query = "John";

        Member member1 = new Member();
        member1.setName("John Doe");
        Member member2 = new Member();
        member2.setName("Jane Smith");

        Band band1 = new Band();
        band1.setName("Metallica");
        band1.setMembers(Set.of(member1));

        Band band2 = new Band();
        band2.setName("Nirvana");
        band2.setMembers(Set.of(member2));

        Event event1 = new Event();
        event1.setTitle("Rock Fest");
        event1.setBands(Set.of(band1, band2));

        Event event2 = new Event();
        event2.setTitle("Jazz Night");
        event2.setBands(Set.of(band2));

        when(eventRepository.findAll()).thenReturn(List.of(event1, event2));

        // Act
        List<Event> result = eventService.getFilteredEvents(query);

        // Assert
        assertEquals(1, result.size());
        Event filteredEvent = result.getFirst();
        assertEquals("Rock Fest [2]", filteredEvent.getTitle());
        assertEquals(2, filteredEvent.getBands().size());
        assertTrue(filteredEvent.getBands().stream()
                .anyMatch(band -> band.getName().equals("Metallica [1]")));
        assertTrue(filteredEvent.getBands().stream()
                .anyMatch(band -> band.getName().equals("Nirvana [1]")));

        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getFilteredEvents_ShouldReturnEmptyList_WhenQueryDoesNotMatch() {
        // Arrange
        String query = "Nonexistent";

        Member member = new Member();
        member.setName("Jane Smith");

        Band band = new Band();
        band.setName("Nirvana");
        band.setMembers(Set.of(member));

        Event event = new Event();
        event.setTitle("Jazz Night");
        event.setBands(Set.of(band));

        when(eventRepository.findAll()).thenReturn(List.of(event));

        // Act
        List<Event> result = eventService.getFilteredEvents(query);

        // Assert
        assertTrue(result.isEmpty());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getFilteredEvents_ShouldHandleNoEventsGracefully() {
        // Arrange
        when(eventRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Event> result = eventService.getFilteredEvents("Query");

        // Assert
        assertTrue(result.isEmpty());
        verify(eventRepository, times(1)).findAll();
    }
}