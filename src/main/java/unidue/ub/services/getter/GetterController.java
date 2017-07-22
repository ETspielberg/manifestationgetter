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

	private final
	JdbcTemplate jdbcTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(GetterController.class);

	private Set<String> shelfmarks;

	@Autowired
	public GetterController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@RequestMapping("/manifestations")
	public ResponseEntity<?> getManifestations(@RequestParam("identifier") String identifier, @RequestParam("exact") String exact,
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
		if (mode.equals("docNumber")) {
			LOGGER.info("retrieving items for docNumber " + identifier);
			items = itemGetter.getItemsByDocNumber(identifier);
			LOGGER.info("found " + items.size() + " items");
		}

		if (mode.equals("barcode")) {
			LOGGER.info("retrieving items for Barcode " + identifier);
			items = itemGetter.getItemsByBarcode(identifier);
			LOGGER.info("found " + items.size() + " items");
		}
		return ResponseEntity.ok(items);
	}

	@RequestMapping("/loans")
	public ResponseEntity<?> getLoans(@RequestParam("identifier") String identifier) {
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
		EventGetter eventGetter = new EventGetter(jdbcTemplate);
		List<Event> events = eventGetter.getOpenRequests();
		return ResponseEntity.ok(events);
	}



	@RequestMapping("/fullManifestation")
	public ResponseEntity<?> getFullManifestation(@RequestParam("identifier") String identifier,
			@RequestParam("collection") String collection, @RequestParam("material") String material,
			@RequestParam("exact") String exact) {

		shelfmarks = new HashSet<>();
		Set<String> shelfmarksQueried = new HashSet<>();
		Set<String> itemIds = new HashSet<>();
		Set<Manifestation> manifestations =  new HashSet<>();
		Set<String> manifestationsQueried = new HashSet<>();

		Boolean exactBoolean = "true".equals(exact);
		boolean shelfmarkNew = true;
		buildReferenceShelfmark(identifier,exactBoolean);
		shelfmarks.add(identifier);

		ManifestationGetter manifestationgetter = new ManifestationGetter(jdbcTemplate);
		ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
		EventGetter eventgetter = new EventGetter(jdbcTemplate);
		MABGetter mabGetter = new MABGetter(jdbcTemplate);
		LOGGER.info(String.valueOf(shelfmarks.size()));

		do {
			for (String shelfmark : shelfmarks) {
				shelfmarkNew = false;
				if (shelfmarksQueried.contains(shelfmark))
					continue;
				LOGGER.info("collecting shelfmark " + shelfmark);
				List<Manifestation> foundManifestations = manifestationgetter.getDocumentsByShelfmark(shelfmark, exactBoolean);
				shelfmarksQueried.add(shelfmark);
				if (foundManifestations.isEmpty())
					continue;
				for (Manifestation foundManifestation : foundManifestations) {
					if (manifestationsQueried.contains(foundManifestation.getTitleID()))
						continue;
					LOGGER.info("building manifestation with title ID " + foundManifestation.getTitleID());
					List<Item> items = itemGetter.getItemsByDocNumber(foundManifestation.getTitleID());
					for (Item item : items)
						if (!itemIds.contains(item.getItemId())) {
							foundManifestation.addItem(item);
							itemIds.add(item.getItemId());
						}
					eventgetter.addEventsToManifestation(foundManifestation);
					manifestations.add(foundManifestation);

					for (String callNo : foundManifestation.getShelfmarks()) {
						buildReferenceShelfmark(callNo, exactBoolean);
						shelfmarkNew = shelfmarkNew || isShelfmarkNew(callNo);
						if (shelfmarkNew)
							shelfmarks.add(callNo);
					}
					if (!manifestationsQueried.contains(foundManifestation.getTitleID()))
						manifestationsQueried.add(foundManifestation.getTitleID());
				}
			}
		} while (shelfmarkNew);

		for (Manifestation manifestation : manifestations) {
			StockEventsBuilder.buildStockEvents(manifestation);
			mabGetter.addSimpleMAB(manifestation);
		}
		return ResponseEntity.ok(new ArrayList<>(manifestations));
	}

	private void buildReferenceShelfmark(String shelfmark, boolean exact) {
		shelfmark = shelfmark.trim();
		shelfmark = shelfmark.replaceAll("\\+\\d+", "");
		if (!exact)
			shelfmark = shelfmark.replaceAll("\\(\\d+\\)", "");
	}

	private boolean isShelfmarkNew(String shelfmark) {
		return !shelfmark.equals("???") && !shelfmarks.contains(shelfmark) && !shelfmark.isEmpty();
	}

	@RequestMapping("/nrequests")
	public ResponseEntity<?> getOpenRequests() {
		Hashtable<String, Manifestation> requestedDocuments = new Hashtable<>();
		List<Nrequests> nrequests = new ArrayList<>();
		EventGetter eventGetter = new EventGetter(jdbcTemplate);
		ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
		List<Event> events = eventGetter.getOpenRequests();
		for (Event event : events) {
			if (event.getItemId().length() > 9) {
				String docNumber = event.getItemId().substring(0, 9);
				if (!requestedDocuments.containsKey(docNumber)) {
					Manifestation manifestation = new Manifestation(docNumber);
					manifestation.addItems(itemGetter.getItemsByDocNumber(docNumber));
					Item item = manifestation.getItem(event.getItemId());
					if (item == null) {
						item = new Item("", event.getItemId(), "", "", "", "");
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
