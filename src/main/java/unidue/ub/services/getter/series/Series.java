package unidue.ub.services.getter.series;

import com.fasterxml.jackson.annotation.JsonIgnore;
import unidue.ub.media.analysis.TimeAndCount;
import unidue.ub.media.monographs.Event;

import java.util.*;

/**
 * Group of events belonging to a specified categories.
 * 
 * @author Frank L\u00FCtzenkirchen, Eike Spielberg
 * @version 1
 */
public class Series {

	private String name;

	private List<TimeAndCount> data;

	@JsonIgnore
	private Set<String> eventTypes = new HashSet<>();

	@JsonIgnore
	private List<Event> events;

	public Series(String name, String... eventTypes) {
		this.name = name;
		for (String type : eventTypes)
			this.eventTypes.add(type);
	}

	public Series() {}

	public String getName() {
		return name;
	}


	public void addEventType(String type) {
		eventTypes.add(type);
	}

	private boolean isAccepted(Event event) {
		return eventTypes.contains(event.getType());
	}

	public void addEventIfAccepted(Event event) {
		if (isAccepted(event))
			events.add(event);
	}

	public void buildTimeAndCountList() {
		Collections.sort(events);
		data = new ArrayList<>(events.size());
		TimeAndCount current = new TimeAndCount(0, 0);
		for (Event event : events) {
			current = new TimeAndCount(event.getTime(), current.getY() + event.getDelta());
			data.add(current);
		}
	}
}
