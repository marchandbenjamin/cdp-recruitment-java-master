package adeo.leroymerlin.cdp.service;

import adeo.leroymerlin.cdp.exception.ErrorMessages;
import adeo.leroymerlin.cdp.model.Event;
import adeo.leroymerlin.cdp.repository.EventRepository;
import adeo.leroymerlin.cdp.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

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
        // Filter the events list in pure JAVA here

        return events;
    }
}
