package unidue.ub.services.getter;

import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Methods to build the inventory and deletion events for a particular item.
 * 
 * @author Eike Spielberg, Frank L\u00FCtzenkirchen
 * @version 1
 */
public class StockEventsBuilder {

	private static final String EARLY_IN_THE_MORNING = "0630";

	private static final String LATE_IN_THE_EVENING = "2000";

	private static final String BY_LIBRARY_STAFF = "05";

	private static String DATE_OF_ANCIENT_INVENTORY = "20000101";

	/**
	 * builds the stock events for all items of a given document.
	 *
	 * @param manifestation
	 *            the document holding the items
	 * 
	 * @exception ParseException
	 *                exception parsing the date field
	 */
	public static void buildStockEvents(Manifestation manifestation) {
		setLatestInventoryDate(manifestation.getItems());

		// go through all items and set the inventory and deletion dates. In
		// case of an existing deletion date,
		// this date is set as end-event for the "lifetime" of the item.

		for (Item item : manifestation.getItems()) {
			buildStockEvents(item);
		}
	}

	/**
	 * builds the stock events for an item.
	 *
	 * @param item
	 *            the item
	 * @exception ParseException
	 *                exception parsing the date field
	 */
	private static void buildStockEvents(Item item) {
		Event inventoryEvent = buildInventoryEvent(item);
		Event deletionEvent = buildDeletionEvent(item);
		if (deletionEvent != null)
			inventoryEvent.setEndEvent(deletionEvent);
	}

	/**
	 * retrieves the stock events for an item.
	 *
	 * @param item
	 *            the item
	 * @return events list of stock events
	 * @exception ParseException
	 *                exception parsing the date field
	 */
	public static List<Event> getStockEvents(Item item) {
		List<Event> events = new ArrayList<>();
		Event inventoryEvent = buildInventoryEvent(item);
		Event deletionEvent = buildDeletionEvent(item);
		if (deletionEvent != null)
			inventoryEvent.setEndEvent(deletionEvent);
		events.add(inventoryEvent);
		events.add(deletionEvent);
		return events;
	}

	/**
	 * retrieves the stock events for a list of item.
	 *
	 * @param items
	 *            the list of items
	 * @exception ParseException
	 *                exception parsing the date field
	 * @return events list of stock events
	 */

	public static List<Event> getStockEvents(List<Item> items) {
		setLatestInventoryDate(items);
		List<Event> events = new ArrayList<>();
		for (Item item : items) {
			Event inventoryEvent = buildInventoryEvent(item);
			Event deletionEvent = buildDeletionEvent(item);
			if (deletionEvent != null)
				inventoryEvent.setEndEvent(deletionEvent);
			events.add(inventoryEvent);
			events.add(deletionEvent);
		}
		return events;
	}

	private static Event buildInventoryEvent(Item item) {
		int sortNumber = item.getItemSequence();
		String date = item.getInventoryDate();
		if (date.length() != 8)
			date = DATE_OF_ANCIENT_INVENTORY;
		return new Event(item, date, EARLY_IN_THE_MORNING, "inventory", BY_LIBRARY_STAFF, sortNumber, +1);
	}

	private static Event buildDeletionEvent(Item item) {
		String earliestPossibleDeletionDate = getEarliestPossibleDeletionDate(item);
		int sortNumber = item.getItemSequence();
		String date = item.getDeletionDate();
		if (date.isEmpty())
			return null;
		else if (date.length() != 8)
			date = earliestPossibleDeletionDate;

		return new Event(item, date, LATE_IN_THE_EVENING, "deletion", BY_LIBRARY_STAFF, sortNumber, -1);
	}

	private static String currentDate() {
		return LocalDate.now().toString();
	}

	private static void setLatestInventoryDate(List<Item> items) {
		// arrange all correct dates both from the items and from loan and
		// request events as integers in a list,
		// select the smallest one and save this as the earliest possible
		// inventory date.
		// If no dates are given, the Date of ancient inventory is set to the
		// first of January 2000.
		List<Integer> dates = getTimesListForItems(items);
		if (!dates.isEmpty())
			DATE_OF_ANCIENT_INVENTORY = String.valueOf(Collections.min(dates));
	}

	private static List<Integer> getTimesListForItems(List<Item> items) {
		List<Integer> dates = new ArrayList<>();
		for (Item item : items) {
			String date = item.getInventoryDate();
			if (date.length() == 8)
				dates.add(Integer.parseInt(date));
			dates.addAll(getTimesListForEvents(item.getEvents()));
		}
		return dates;
	}

	private static List<Integer> getTimesListForEvents(List<Event> events) {
		List<Integer> dates = new ArrayList<>();
		if (events.size()> 0) {
			for (Event event : events) {
				String dateString = event.getDate();
				if (dateString == null)
					continue;
				if (dateString.contains("-"))
					dateString = dateString.replace("-", "");
				if (dateString.length() < 8)
					continue;
				dates.add(Integer.parseInt(dateString.substring(0, 8)));
			}
		}
		return dates;
	}

	private static String getEarliestPossibleDeletionDate(Item item) {
		// set the earliest possible deletion date as the Date of the latest
		// event. In case there are no events, the
		// deletion date is set to "now".
		String earliestPossibleDeletionDate = currentDate();
		List<Event> events = item.getEvents();
		List<Integer>dates = getTimesListForEvents(events);
		if (!dates.isEmpty())
			earliestPossibleDeletionDate = String.valueOf(Collections.max(dates));
		return earliestPossibleDeletionDate;
	}

}
