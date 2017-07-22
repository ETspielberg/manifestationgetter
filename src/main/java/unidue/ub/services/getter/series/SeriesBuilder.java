package unidue.ub.services.getter.series;


import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Manifestation;

import java.util.*;
import java.util.Map.Entry;

/**
 * Methods to build series from a list of events.
 * 
 * @author Frank L\u00FCtzenkirchen, Eike Spielberg
 * @version 1
 */
public class SeriesBuilder {
	
	/**
	 * prepares a list of series from a list of events and a
	 * <code>Hashtable</code> defining the individual categories
	 * 
	 * @param events
	 *            list of events
	 * @param groups
	 *            <code>Hashtable</code> defining the individual categories
	 * @return list list of series
	 * 
	 */
	public static List<Series> buildSeries(List<Event> events, Hashtable<String, String[]> groups) {
		List<Series> list = new ArrayList<Series>();
		Iterator<Entry<String, String[]>> iterator = groups.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String[]> group = iterator.next();
			list.add(new Series(group.getKey(), group.getValue()));
		}
		for (Series series : list) {
			for (Event event : events)
				series.addEventIfAccepted(event);
		}
		return list;
	}

	/**
	 * prepares a list of series from a document and a <code>Hashtable</code>
	 * defining the individual categories
	 * 
	 * @param document
	 *            document,holding the events
	 * @param groups
	 *            <code>Hashtable</code> defining the individual categories
	 * @return list list of series
	 * 
	 */
	public static List<Series> buildSeries(Manifestation document, Hashtable<String, String[]> groups) {
		List<Event> events = document.getEvents();
		Collections.sort(events);
		return buildSeries(events, groups);
	}
}
