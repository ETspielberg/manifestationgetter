package unidue.ub.services.getter;

import java.sql.SQLException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.services.getter.queryresults.RawJournalprices;

@Controller
@RefreshScope
@RequestMapping("/getter")
@CrossOrigin(origins = "http://localhost")
public class GetterController {

    @Value("${ub.statistics.settings.url}")
    String settingsUrl;

    private final
    JdbcTemplate jdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(GetterController.class);

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
            case "shelfmark": {
                manifestations = manifestationgetter.getDocumentsByShelfmark(identifier, exactBoolean);
                log.info("retriving manifestations by shelfmark " + identifier);
                break;
            }
            case "etat": {
                manifestations = manifestationgetter.getDocumentsByEtat(identifier);
                log.info("retrieving manifestations by etat " + identifier);
                break;
            }
            case "notation": {
                manifestations = manifestationgetter.getDocumentsByNotation(identifier);
                log.info("retrieving manifestations by notation " + identifier);
                break;
            }
            case "openRequests": {
                manifestations = manifestationgetter.getDocumentsByOpenRequests();
                log.info("retrieving manifestations by open requests");
                break;
            }
            case "barcode": {
                manifestations = manifestationgetter.getManifestationsByBarcode(identifier);
                log.info("retrieving manifestations by barcode " + identifier.toUpperCase());
                break;
            }
        }
        return ResponseEntity.ok(manifestations);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping("/fullManifestation")
    public ResponseEntity<?> getFullManifestation(@RequestParam("identifier") String identifier,
                                                  @RequestParam("exact") String exact) {

        shelfmarks = new HashSet<>();
        Set<String> shelfmarksQueried = new HashSet<>();
        Set<String> itemIds = new HashSet<>();
        Set<Manifestation> manifestations = new HashSet<>();
        Set<String> manifestationsQueried = new HashSet<>();

        Boolean exactBoolean = "true".equals(exact);
        boolean shelfmarkNew = true;
        buildReferenceShelfmark(identifier, exactBoolean);
        shelfmarks.add(identifier);

        ManifestationGetter manifestationgetter = new ManifestationGetter(jdbcTemplate);
        ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
        EventGetter eventgetter = new EventGetter(jdbcTemplate);
        MABGetter mabGetter = new MABGetter(jdbcTemplate);

        do {
            for (String shelfmark : shelfmarks) {
                shelfmarkNew = false;
                if (shelfmarksQueried.contains(shelfmark))
                    continue;
                List<Manifestation> foundManifestations = manifestationgetter.getDocumentsByShelfmark(shelfmark, exactBoolean);
                shelfmarksQueried.add(shelfmark);
                if (foundManifestations.isEmpty())
                    continue;
                for (Manifestation foundManifestation : foundManifestations) {
                    if (manifestationsQueried.contains(foundManifestation.getTitleID()))
                        continue;
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
            manifestation.buildUsergroupList();
        }
        return ResponseEntity.ok(new ArrayList<>(manifestations));
    }

    private static void buildReferenceShelfmark(String shelfmark, boolean exact) {
        shelfmark = shelfmark.trim();
        shelfmark = shelfmark.replaceAll("\\+\\d+", "");
        if (!exact)
            shelfmark = shelfmark.replaceAll("\\(\\d+\\)", "");
    }

    private boolean isShelfmarkNew(String shelfmark) {
        return !shelfmark.equals("???") && !shelfmarks.contains(shelfmark) && !shelfmark.isEmpty();
    }

    @RequestMapping("/buildFullManifestation")
    public ResponseEntity<?> buildFullManifestation(@RequestParam("identifier") String identifier) {
        Manifestation manifestation = new Manifestation(identifier);
        extendManifestation(manifestation);
        return ResponseEntity.ok(manifestation);
    }
    
    @RequestMapping("buildActiveManifestation")
    public ResponseEntity<?> buildActiveManifestation(@RequestParam("identifier") String identifier) {
        Manifestation manifestation = new Manifestation(identifier);
        extendActiveManifestation(manifestation);
        return ResponseEntity.ok(manifestation);
    }

    @RequestMapping("journalprices")
    public void getJournalPrices(@RequestParam("identifier") String identifier, @RequestParam("type") String type) throws SQLException {
        JournalPriceGetter getter = new JournalPriceGetter(jdbcTemplate);
        switch (type) {
            case "journalcollection" :  {
                getter.getJournalcollectionprices(identifier);
                break;
            }
            case "journal" :  {
                getter.getJournalPrice(identifier);
                break;
            }
        }
    }

    private void extendActiveManifestation(Manifestation manifestation) {
        Set<String> itemIds = new HashSet<>();
        ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
        EventGetter eventgetter = new EventGetter(jdbcTemplate);
        MABGetter mabGetter = new MABGetter(jdbcTemplate);
        List<Item> items = itemGetter.getItemsByDocNumber(manifestation.getTitleID());
        for (Item item : items)
            if (!itemIds.contains(item.getItemId())) {
                manifestation.addItem(item);
                itemIds.add(item.getItemId());
            }
        eventgetter.addAcitveEventsToManifestation(manifestation);
        mabGetter.addSimpleMAB(manifestation);
    }

    private void extendManifestation(Manifestation manifestation) {
        Set<String> itemIds = new HashSet<>();
        ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
        EventGetter eventgetter = new EventGetter(jdbcTemplate);
        MABGetter mabGetter = new MABGetter(jdbcTemplate);
        List<Item> items = itemGetter.getItemsByDocNumber(manifestation.getTitleID());
        for (Item item : items)
            if (!itemIds.contains(item.getItemId())) {
                manifestation.addItem(item);
                itemIds.add(item.getItemId());
            }
        eventgetter.addEventsToManifestation(manifestation);
        StockEventsBuilder.buildStockEvents(manifestation);
        mabGetter.addSimpleMAB(manifestation);
        manifestation.buildUsergroupList();
    }
}
