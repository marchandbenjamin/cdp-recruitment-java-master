package adeo.leroymerlin.cdp.service;

import adeo.leroymerlin.cdp.exception.ErrorMessages;
import adeo.leroymerlin.cdp.model.Event;
import adeo.leroymerlin.cdp.repository.EventRepository;
import adeo.leroymerlin.cdp.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (eventRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException(String.format(ErrorMessages.EVENT_NOT_FOUND, id));
        }
        eventRepository.deleteById(id);
    }

    public List<Event> getFilteredEvents(String query) {
        List<Event> events = eventRepository.findAll();
        List<Event> filteredEvents = events.stream()
                .filter(event -> event.getBands().stream()
                        .anyMatch(band -> band.getMembers().stream()
                                .anyMatch(member -> member.getName().toLowerCase().contains(query.toLowerCase()))
                        )
                ).collect(Collectors.toList());

        filteredEvents.forEach(event -> {
            int bandCount = event.getBands().size();
            event.setTitle(event.getTitle() + " [" + bandCount + "]");
            event.getBands().forEach(band -> {
                int memberCount = band.getMembers().size();
                band.setName(band.getName() + " [" + memberCount + "]");
            });
        });

        return filteredEvents;
    }

    public void updateEvent(Long id, Event newEventData) {
        Event existingEvent = eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.EVENT_NOT_FOUND, id)));

        existingEvent.setComment(newEventData.getComment());
        eventRepository.save(existingEvent);
    }
}
