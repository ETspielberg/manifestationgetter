package unidue.ub.services.getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import unidue.ub.media.analysis.Nrequests;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;

@Controller
@RefreshScope
@RequestMapping("/getter")
@CrossOrigin(origins = "http://localhost")
public class GetterController {

	@Autowired
	JdbcTemplate jdbcTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(GetterController.class);

	@RequestMapping("/manifestations")
	public ResponseEntity<?> get(@RequestParam("identifier") String identifier, @RequestParam("exact") String exact,
			@RequestParam("mode") String mode) {

		ManifestationGetter manifestationgetter = new ManifestationGetter(jdbcTemplate);
		List<Manifestation> manifestations = new ArrayList<>();
		Boolean exactBoolean = "true".equals(exact);
		switch (mode) {
		case "shelfmark":
			manifestations = manifestationgetter.getDocumentsByShelfmark(identifier, exactBoolean);
		case "etat":
			manifestations = manifestationgetter.getDocumentsByEtat(identifier, exactBoolean);
		case "notation":
			manifestations = manifestationgetter.getDocumentsByNotation(identifier);
		}
		return ResponseEntity.ok(manifestations);
	}

	@RequestMapping("/items")
	public ResponseEntity<?> getItems(@RequestParam("identifier") String identifier,
			@RequestParam("mode") String mode) {
		List<Item> items = new ArrayList<>();
		ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
		switch (mode) {
		case "docNumber":
			items = itemGetter.getItemsByDocNumber(identifier);
		case "barcode":

		}
		return ResponseEntity.ok(items);
	}

	@RequestMapping("/loans")
	public ResponseEntity<?> getLoans(@RequestParam("identifier") String identifier,
			@RequestParam("mode") String mode) {
		EventGetter eventGetter = new EventGetter(jdbcTemplate);
		List<Event> events = eventGetter.getLoansByDocNumber(identifier);
		return ResponseEntity.ok(events);
	}

	@RequestMapping("/requests")
	public ResponseEntity<?> getEvents(@RequestParam("identifier") String identifier,
			@RequestParam("mode") String mode) {
		List<Event> events = new ArrayList<>();
		EventGetter eventGetter = new EventGetter(jdbcTemplate);
		switch (mode) {
		case "docNumber":
			events = eventGetter.getRequestsByDocNumber(identifier);
		case "latestRequests":

		}
		return ResponseEntity.ok(events);
	}

	@RequestMapping("/allopenrequests")
	public ResponseEntity<?> getEvents() {
		List<Event> events = new ArrayList<>();
		EventGetter eventGetter = new EventGetter(jdbcTemplate);
		events = eventGetter.getOpenRequests();
		return ResponseEntity.ok(events);
	}

	private Set<String> shelfmarks;

	private boolean shelfmarkAdded = true;

	private Set<Manifestation> documents;

	private Set<Integer> itemSequences;

	@RequestMapping("/fullManifestation")
	public ResponseEntity<?> getFullManifestation(@RequestParam("identifier") String identifier,
			@RequestParam("collection") String collection, @RequestParam("material") String material,
			@RequestParam("exact") String exact) {

		documents = new HashSet<Manifestation>();
		shelfmarks = new HashSet<String>();
		itemSequences = new HashSet<>();

		Boolean exactBoolean = "true".equals(exact);
		addShelfmarkIfNew(identifier.trim(), exactBoolean);
		ItemFilter itemFilter = new ItemFilter(collection, material);

		ManifestationGetter manifestationgetter = new ManifestationGetter(jdbcTemplate);
		ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
		EventGetter eventgetter = new EventGetter(jdbcTemplate, itemFilter);
		MABGetter mabGetter = new MABGetter(jdbcTemplate);
		List<Manifestation> manifestations = new ArrayList<>();

		do {
			for (String shelfmark : shelfmarks) {
				manifestations.addAll(manifestationgetter.getDocumentsByShelfmark(shelfmark, exactBoolean));
				for (Manifestation manifestation : manifestations) {
					List<Item> items = itemGetter.getItemsByDocNumber(manifestation.getDocNumber());
					mabGetter.addSimpleMAB(manifestation);

					for (Item item : items)
						if (itemFilter.matches(item) && !itemSequences.contains(item.getItemSequence())) {
							LOGGER.info("adding item " + item.getItemSequence());
							manifestation.addItem(item);
							itemSequences.add(item.getItemSequence());
						}

					eventgetter.addEventsToManifestation(manifestation);
				}
				shelfmarkAdded = false;

				for (Manifestation document : documents) {
					for (String callNo : document.getCallNo().split(","))
						addShelfmarkIfNew(callNo, exactBoolean);
					for (Item item : document.getItems())
						addShelfmarkIfNew(item.getCallNo(), exactBoolean);
				}
			}
		} while (shelfmarkAdded);
		return ResponseEntity.ok(manifestations);
	}

	private void addShelfmarkIfNew(String shelfmark, boolean exact) {
		shelfmark = shelfmark.trim();
		shelfmark = shelfmark.replaceAll("\\+\\d+", "");

		if ("???".equals(shelfmark))
			return;

		if (!exact)
			shelfmark = shelfmark.replaceAll("\\(\\d+\\)", "");

		if (shelfmarks.contains(shelfmark))
			return;

		LOGGER.debug("added shelfmark " + shelfmark);
		shelfmarks.add(shelfmark);
		shelfmarkAdded = true;
	}

	@RequestMapping("/nrequests")
	public ResponseEntity<?> getOpenRequests() {
		Hashtable<String, Manifestation> requestedDocuments = new Hashtable<>();
		List<Nrequests> nrequests = new ArrayList<>();
		EventGetter eventGetter = new EventGetter(jdbcTemplate);
		ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
		List<Event> events = eventGetter.getOpenRequests();
		for (Event event : events) {
			if (event.getRecKey().length() > 9) {
				String docNumber = event.getRecKey().substring(0, 9);
				Integer itemSequence = Integer.parseInt(event.getRecKey().substring(9));
				if (!requestedDocuments.containsKey(docNumber)) {
					Manifestation manifestation = new Manifestation(docNumber);
					manifestation.addItems(itemGetter.getItemsByDocNumber(docNumber));
					Item item = manifestation.getItem(itemSequence);
					if (item == null) {
						item = new Item("", itemSequence, "", "", "", "");
						manifestation.addItem(item);
					}
					item.addEvent(event);
				} else {
					Manifestation manifestation = requestedDocuments.get(docNumber);
				}
			} else {

			}

		}

		return ResponseEntity.ok(nrequests);
	}
}
