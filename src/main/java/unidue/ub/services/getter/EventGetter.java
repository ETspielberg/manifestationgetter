package unidue.ub.services.getter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.services.getter.queryresults.RawLoanEvent;
import unidue.ub.services.getter.queryresults.RawRequestEvent;

public class EventGetter {

	private JdbcTemplate jdbcTemplate;

	private int counter = 0;

	private final String getClosedLoans = "select z36h_rec_key, z36h_material, z36h_status, z36h_bor_status, z36h_loan_date, z36h_loan_hour, z36h_returned_date, z36h_returned_hour, z36h_sub_library from edu50.z36h where z36h_rec_key like ? and z36h_loan_date > '20000000' order by z36h_loan_date, z36h_loan_hour, z36h_number";

	private final String getOpenLoans = "select z36_rec_key, z36_material, z36_status, z36_bor_status, z36_loan_date, z36_loan_hour, z36_sub_library from edu50.z36 where z36_rec_key like ? and z36_loan_date > '20000000' order by z36_loan_date, z36_loan_hour, z36_number";

	private final String getClosedRequests = "select z37h_rec_key, z37h_open_date, z37h_open_hour, z37h_hold_date, z37h_pickup_location from edu50.z37h where z37h_rec_key like ? and z37h_open_date > '20000000' order by z37h_open_date, z37h_open_hour, z37h_rec_key";

	private final String getOpenRequests = "select z37_rec_key, z37_open_date, z37_open_hour, z37_pickup_location from edu50.z37 where z37_rec_key like ? and z37_open_date > '20000000' order by z37_open_date, z37_open_hour, z37_rec_key";

	private final String getAllOpenRequests = "select z37_rec_key, z37_open_date, z37_open_hour, z37_pickup_location from edu50.z37";
	
	public EventGetter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
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
			Event loanEvent = new Event(rawLoanEvent.getItemId(), rawLoanEvent.getLoanDate(), rawLoanEvent.getLoanHour(),
					"loan", rawLoanEvent.getBorrowerStatus(), counter++);
			Event returnEvent = new Event(rawLoanEvent.getItemId(), rawLoanEvent.getLoanDate(), rawLoanEvent.getLoanHour(),
					"return", rawLoanEvent.getBorrowerStatus(), counter--);
			loanEvent.setEndEvent(returnEvent);
			events.add(loanEvent);
			events.add(returnEvent);
		}
		rawOpenLoanEvents.addAll(jdbcTemplate.query(getOpenLoans, new Object[] { identifier + "%" },
				(rs, rowNum) -> new RawLoanEvent(rs.getString("z36_rec_key"), rs.getString("z36_sub_library"),
						rs.getString("z36_bor_status"), rs.getString("z36_material"), rs.getString("z36_loan_date"),
						rs.getString("z36_loan_hour"), "",
						"")));
		for (RawLoanEvent rawLoanEvent : rawOpenLoanEvents)
			events.add(new Event(rawLoanEvent.getItemId(), rawLoanEvent.getLoanDate(), rawLoanEvent.getLoanHour(),
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
						rs.getString("z37_open_hour"), rs.getString("z37_pickup_location"))));
		List<Event> events = new ArrayList<>();
		for (RawRequestEvent rawRequestEvent : rawClosedRequestEvents) {
			Event requestEvent = new Event(rawRequestEvent.getItemId(), rawRequestEvent.getOpenDate(),
					rawRequestEvent.getOpenHour(), "request", "", counter++);
			Event holdEvent = new Event(rawRequestEvent.getItemId(), rawRequestEvent.getHoldDate(),
					rawRequestEvent.getOpenHour(), "hold", "", counter--);
			requestEvent.setEndEvent(holdEvent);
			events.add(requestEvent);
			events.add(holdEvent);
		}
		for (RawRequestEvent rawRequestEvent : rawOpenRequestEvents) {
			events.add(new Event(rawRequestEvent.getItemId(), rawRequestEvent.getOpenDate(),
					rawRequestEvent.getOpenHour(), "request", "", counter++));
		}
		return events;
	}
	
	public List<Event> getOpenRequests() {
		List<RawRequestEvent> rawOpenRequestEvents = new ArrayList<>();
		rawOpenRequestEvents.addAll(jdbcTemplate.query(getAllOpenRequests,
				(rs, rowNum) -> new RawRequestEvent(rs.getString("z37_rec_key"), rs.getString("z37_open_date"),
						rs.getString("z37_open_hour"), rs.getString("z37_pickup_location"))));
		List<Event> events = new ArrayList<>();
		for (RawRequestEvent rawRequestEvent : rawOpenRequestEvents) {
			events.add(new Event(rawRequestEvent.getItemId(), rawRequestEvent.getOpenDate(),
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
				.addAll(jdbcTemplate.query(getClosedLoans, new Object[] { manifestation.getTitleID() + "%" },
						(rs, rowNum) -> new RawLoanEvent(rs.getString("z36h_rec_key"), rs.getString("z36h_sub_library"),
								rs.getString("z36h_bor_status"), rs.getString("z36h_material"),
								rs.getString("z36h_loan_date"), rs.getString("z36h_loan_hour"),
								rs.getString("z36h_returned_date"), rs.getString("z36h_returned_hour"))));
		rawOpenLoanEvents.addAll(jdbcTemplate.query(getOpenLoans, new Object[] { manifestation.getTitleID() + "%" },
				(rs, rowNum) -> new RawLoanEvent(rs.getString("z36_rec_key"), rs.getString("z36_sub_library"),
						rs.getString("z36_bor_status"), rs.getString("z36_material"), rs.getString("z36_loan_date"),
						rs.getString("z36_loan_hour"), "",
						"")));
		rawClosedRequestEvents
				.addAll(jdbcTemplate.query(getClosedRequests, new Object[] { manifestation.getTitleID() + "%" },
						(rs, rowNum) -> new RawRequestEvent(rs.getString("z37h_rec_key"),
								rs.getString("z37h_open_date"), rs.getString("z37h_open_hour"),
								rs.getString("z37h_hold_date"), rs.getString("z37h_pickup_location"))));
		rawOpenRequestEvents
				.addAll(jdbcTemplate.query(getOpenRequests, new Object[] { manifestation.getTitleID() + "%" },
						(rs, rowNum) -> new RawRequestEvent(rs.getString("z37_rec_key"), rs.getString("z37_open_date"),
								rs.getString("z37_open_hour"), rs.getString("z37_pick_up_location"))));

		// convert raw events to events, add extra items where necessary, and
		// connect events to items.
		for (RawLoanEvent rawLoanEvent : rawClosedLoanEvents) {
			Item item = manifestation.getItem(rawLoanEvent.getItemId());
			if (item == null) {
				item = new Item("", rawLoanEvent.getItemId(), "", "", "", "");
				manifestation.addItem(item);
			}
				Event loanEvent = new Event(rawLoanEvent.getItemId(), rawLoanEvent.getLoanDate(),
						rawLoanEvent.getLoanHour(), "loan", rawLoanEvent.getBorrowerStatus(), counter++);
				loanEvent.setItem(item);
				Event returnEvent = new Event(rawLoanEvent.getItemId(), rawLoanEvent.getReturnDate(),
						rawLoanEvent.getReturnHour(), "return", rawLoanEvent.getBorrowerStatus(), counter--);
				returnEvent.setItem(item);
				loanEvent.setEndEvent(returnEvent);
				item.addEvent(loanEvent);
				item.addEvent(returnEvent);
		}
		for (RawLoanEvent rawLoanEvent : rawOpenLoanEvents) {
			Item item = manifestation.getItem(rawLoanEvent.getItemId());
			if (item == null) {
				item = new Item("", rawLoanEvent.getItemId(), "", "", "", "");
				manifestation.addItem(item);
			}
				Event loanEvent = new Event(rawLoanEvent.getItemId(), rawLoanEvent.getLoanDate(),
						rawLoanEvent.getLoanHour(), "loan", rawLoanEvent.getBorrowerStatus(), counter++);
				loanEvent.setItem(item);
				item.addEvent(loanEvent);
		}
		for (RawRequestEvent rawRequestEvent : rawClosedRequestEvents) {
			Item item = manifestation.getItem(rawRequestEvent.getItemId());
			if (item == null) {
				item = new Item("", rawRequestEvent.getItemId(), "", "", "", "");
				manifestation.addItem(item);
			}
				Event requestEvent = new Event(rawRequestEvent.getItemId(), rawRequestEvent.getOpenDate(),
						rawRequestEvent.getOpenHour(), "request", "", counter++);
				requestEvent.setItem(item);
				Event holdEvent = new Event(rawRequestEvent.getItemId(), rawRequestEvent.getHoldDate(),
						rawRequestEvent.getOpenHour(), "hold", "", counter--);
				item.addEvent(holdEvent);
				requestEvent.setEndEvent(holdEvent);
				item.addEvent(requestEvent);
				holdEvent.setItem(item);
		}
		for (RawRequestEvent rawRequestEvent : rawOpenRequestEvents) {
			Item item = manifestation.getItem(rawRequestEvent.getItemId());
			if (item == null) {
				item = new Item("", rawRequestEvent.getItemId(), "", "", "", "");
				manifestation.addItem(item);
			}
				Event requestEvent = new Event(rawRequestEvent.getItemId(), rawRequestEvent.getOpenDate(),
						rawRequestEvent.getOpenHour(), "request", "", counter++);
				item.addEvent(requestEvent);
				requestEvent.setItem(item);
		}
	}
}
