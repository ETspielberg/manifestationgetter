package unidue.ub.services.getter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;

public class EventGetter {

	private JdbcTemplate jdbcTemplate;

	private EventFilter eventFilter;

	private ItemFilter itemFilter;

	private int counter = 0;

	private final String getClosedLoans = "select z36h_rec_key, z36h_material, z36h_status, z36h_bor_status, z36h_loan_date, z36h_loan_hour, z36h_returned_date, z36h_returned_hour, z36h_sub_library from edu50.z36h where z36h_rec_key like ? and z36h_loan_date > '20000000' order by z36h_loan_date, z36h_loan_hour, z36h_number";

	private final String getOpenLoans = "select z36_rec_key, z36_material, z36_status, z36_bor_status, z36_loan_date, z36_loan_hour, z36_sub_library from edu50.z36 where z36_rec_key like ? and z36_loan_date > '20000000' order by z36_loan_date, z36_loan_hour, z36_number";

	private final String getClosedRequests = "select z37h_rec_key, z37h_open_date, z37h_open_hour, z37h_hold_date, z37h_pickup_location from edu50.z37h where z37h_rec_key like ? and z37h_open_date > '20000000' order by z37h_open_date, z37h_open_hour, z37h_rec_key";

	private final String getOpenRequests = "select z37_rec_key, z37_open_date, z37_open_hour, z37_pickup_location from edu50.z37 where z37_rec_key like ? and z37_open_date > '20000000' order by z37_open_date, z37_open_hour, z37_rec_key";

	public EventGetter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.itemFilter = new ItemFilter("", "");
		this.eventFilter = new EventFilter("");
	}

	public EventGetter(JdbcTemplate jdbcTemplate, ItemFilter itemFilter) {
		this.jdbcTemplate = jdbcTemplate;
		this.itemFilter = itemFilter;
		this.eventFilter = new EventFilter("");
	}

	public EventGetter(JdbcTemplate jdbcTemplate, EventFilter eventFilter) {
		this.jdbcTemplate = jdbcTemplate;
		this.itemFilter = new ItemFilter("", "");
		this.eventFilter = eventFilter;
	}

	public EventGetter(JdbcTemplate jdbcTemplate, ItemFilter itemFilter, EventFilter eventFilter) {
		this.jdbcTemplate = jdbcTemplate;
		this.itemFilter = itemFilter;
		this.eventFilter = eventFilter;
	}

	public List<Event> getLoansByDocNumber(String identifier) {
		List<RawLoanEvent> rawClosedLoanEvents = new ArrayList<>();
		List<RawLoanEvent> rawOpenLoanEvents = new ArrayList<>();
		List<Event> events = new ArrayList<>();
		rawClosedLoanEvents.addAll(jdbcTemplate.query(getClosedLoans, new Object[] { identifier + "%" },
				(rs, rowNum) -> new RawLoanEvent(rs.getString("z36h_rec_key"), rs.getString("z36h_sub_library"),
						rs.getString("z36h_bor_status"), rs.getString("z36h_material"), rs.getString("z36h_loan_date"),
						rs.getString("z36h_loan_hour"), rs.getString("z36h_returned_date"),
						rs.getString("z36h_returned_hour"))));

		for (RawLoanEvent rawLoanEvent : rawClosedLoanEvents) {
			events.add(new Event(rawLoanEvent.getRecKey(), rawLoanEvent.getLoanDate(), rawLoanEvent.getLoanHour(),
					"loan", rawLoanEvent.getBorrowerStatus(), counter++));
			events.add(new Event(rawLoanEvent.getRecKey(), rawLoanEvent.getLoanDate(), rawLoanEvent.getLoanHour(),
					"return", rawLoanEvent.getBorrowerStatus(), counter--));
		}
		rawOpenLoanEvents.addAll(jdbcTemplate.query(getOpenLoans, new Object[] { identifier + "%" },
				(rs, rowNum) -> new RawLoanEvent(rs.getString("z36h_rec_key"), rs.getString("z36h_sub_library"),
						rs.getString("z36h_bor_status"), rs.getString("z36h_material"), rs.getString("z36h_loan_date"),
						rs.getString("z36h_loan_hour"), rs.getString("z36h_returned_date"),
						rs.getString("z36h_returned_hour"))));
		for (RawLoanEvent rawLoanEvent : rawOpenLoanEvents)
			events.add(new Event(rawLoanEvent.getRecKey(), rawLoanEvent.getLoanDate(), rawLoanEvent.getLoanHour(),
					"loan", rawLoanEvent.getBorrowerStatus(), counter++));
		return events;
	}

	public List<Event> getRequestsByDocNumber(String identifier) {
		List<RawRequestEvent> rawClosedRequestEvents = new ArrayList<>();
		List<RawRequestEvent> rawOpenRequestEvents = new ArrayList<>();
		rawClosedRequestEvents.addAll(jdbcTemplate.query(getClosedRequests, new Object[] { identifier + "%" },
				(rs, rowNum) -> new RawRequestEvent(rs.getString("z37h_rec_key"), rs.getString("z37h_open_date"),
						rs.getString("z37h_open_hour"), rs.getString("z37h_hold_date"),
						rs.getString("z37h_pickup_location"))));
		rawOpenRequestEvents.addAll(jdbcTemplate.query(getOpenRequests, new Object[] { identifier + "%" },
				(rs, rowNum) -> new RawRequestEvent(rs.getString("z37_rec_key"), rs.getString("z37_open_date"),
						rs.getString("z37_open_hour"), rs.getString("z37_pick_up_location"))));
		List<Event> events = new ArrayList<>();
		for (RawRequestEvent rawRequestEvent : rawClosedRequestEvents) {
			events.add(new Event(rawRequestEvent.getRecKey(), rawRequestEvent.getOpenDate(),
					rawRequestEvent.getOpenHour(), "request", "", counter++));
			events.add(new Event(rawRequestEvent.getRecKey(), rawRequestEvent.getHoldDate(),
					rawRequestEvent.getOpenHour(), "hold", "", counter--));
		}
		for (RawRequestEvent rawRequestEvent : rawOpenRequestEvents) {
			events.add(new Event(rawRequestEvent.getRecKey(), rawRequestEvent.getOpenDate(),
					rawRequestEvent.getOpenHour(), "request", "", counter++));
		}
		return events;
	}

	public void addEventsToManifestation(Manifestation manifestation) {
		// prepare all raw events
		List<RawLoanEvent> rawClosedLoanEvents = new ArrayList<>();
		List<RawLoanEvent> rawOpenLoanEvents = new ArrayList<>();
		List<RawRequestEvent> rawClosedRequestEvents = new ArrayList<>();
		List<RawRequestEvent> rawOpenRequestEvents = new ArrayList<>();

		// collect all raw events
		rawClosedLoanEvents
				.addAll(jdbcTemplate.query(getClosedLoans, new Object[] { manifestation.getDocNumber() + "%" },
						(rs, rowNum) -> new RawLoanEvent(rs.getString("z36h_rec_key"), rs.getString("z36h_sub_library"),
								rs.getString("z36h_bor_status"), rs.getString("z36h_material"),
								rs.getString("z36h_loan_date"), rs.getString("z36h_loan_hour"),
								rs.getString("z36h_returned_date"), rs.getString("z36h_returned_hour"))));
		rawOpenLoanEvents.addAll(jdbcTemplate.query(getOpenLoans, new Object[] { manifestation.getDocNumber() + "%" },
				(rs, rowNum) -> new RawLoanEvent(rs.getString("z36h_rec_key"), rs.getString("z36h_sub_library"),
						rs.getString("z36h_bor_status"), rs.getString("z36h_material"), rs.getString("z36h_loan_date"),
						rs.getString("z36h_loan_hour"), rs.getString("z36h_returned_date"),
						rs.getString("z36h_returned_hour"))));
		rawClosedRequestEvents
				.addAll(jdbcTemplate.query(getClosedRequests, new Object[] { manifestation.getDocNumber() + "%" },
						(rs, rowNum) -> new RawRequestEvent(rs.getString("z37h_rec_key"),
								rs.getString("z37h_open_date"), rs.getString("z37h_open_hour"),
								rs.getString("z37h_hold_date"), rs.getString("z37h_pickup_location"))));
		rawOpenRequestEvents
				.addAll(jdbcTemplate.query(getOpenRequests, new Object[] { manifestation.getDocNumber() + "%" },
						(rs, rowNum) -> new RawRequestEvent(rs.getString("z37_rec_key"), rs.getString("z37_open_date"),
								rs.getString("z37_open_hour"), rs.getString("z37_pick_up_location"))));

		// convert raw events to events, add extra items where necessary, and
		// connect events to items.
		for (RawLoanEvent rawLoanEvent : rawClosedLoanEvents) {
			Item item = manifestation.getItem(rawLoanEvent.getItemSequence());
			if (item == null) {
				item = new Item("", rawLoanEvent.getItemSequence(), "", "", "", "");
				manifestation.addItem(item);
			}
			if (itemFilter.matches(item)) {
				manifestation.addItem(item);
				Event loanEvent = new Event(rawLoanEvent.getRecKey(), rawLoanEvent.getLoanDate(),
						rawLoanEvent.getLoanHour(), "loan", rawLoanEvent.getBorrowerStatus(), counter++);
				loanEvent.setItem(item);
				item.addEvent(loanEvent);
				Event returnEvent = new Event(rawLoanEvent.getRecKey(), rawLoanEvent.getLoanDate(),
						rawLoanEvent.getLoanHour(), "return", rawLoanEvent.getBorrowerStatus(), counter--);
				returnEvent.setItem(item);
				item.addEvent(returnEvent);
			}
		}
		for (RawLoanEvent rawLoanEvent : rawOpenLoanEvents) {
			Item item = manifestation.getItem(rawLoanEvent.getItemSequence());
			if (item == null) {
				item = new Item("", rawLoanEvent.getItemSequence(), "", "", "", "");
				manifestation.addItem(item);
			}
			if (itemFilter.matches(item)) {
				Event loanEvent = new Event(rawLoanEvent.getRecKey(), rawLoanEvent.getLoanDate(),
						rawLoanEvent.getLoanHour(), "loan", rawLoanEvent.getBorrowerStatus(), counter++);
				loanEvent.setItem(item);
				item.addEvent(loanEvent);
			}
		}
		for (RawRequestEvent rawRequestEvent : rawClosedRequestEvents) {
			Item item = manifestation.getItem(rawRequestEvent.getItemSequence());
			if (item == null) {
				item = new Item("", rawRequestEvent.getItemSequence(), "", "", "", "");
				manifestation.addItem(item);
			}
			if (itemFilter.matches(item)) {
				Event requestEvent = new Event(rawRequestEvent.getRecKey(), rawRequestEvent.getOpenDate(),
						rawRequestEvent.getOpenHour(), "request", "", counter++);
				item.addEvent(requestEvent);
				requestEvent.setItem(item);
				Event holdEvent = new Event(rawRequestEvent.getRecKey(), rawRequestEvent.getHoldDate(),
						rawRequestEvent.getOpenHour(), "hold", "", counter--);
				item.addEvent(holdEvent);
				holdEvent.setItem(item);
			}
		}
		for (RawRequestEvent rawRequestEvent : rawOpenRequestEvents) {
			Item item = manifestation.getItem(rawRequestEvent.getItemSequence());
			if (item == null) {
				item = new Item("", rawRequestEvent.getItemSequence(), "", "", "", "");
				manifestation.addItem(item);
			}
			if (itemFilter.matches(item)) {
				Event requestEvent = new Event(rawRequestEvent.getRecKey(), rawRequestEvent.getOpenDate(),
						rawRequestEvent.getOpenHour(), "request", "", counter++);
				item.addEvent(requestEvent);
				requestEvent.setItem(item);
			}
		}
	}
}
