package unidue.ub.services.getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

@Controller
@RefreshScope
public class GetterController implements GetterClient {

    private final
    JdbcTemplate jdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(GetterController.class);

    private Set<String> shelfmarks;

    @Value("${ub.statistics.shelfmark.regex}")
    String shelfmarkRegex;

    @Value("${ub.statistics.collections.ignored}")
    String ignoredCollections;

    @Autowired
    public GetterController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @GetMapping("/manifestations")
    public ResponseEntity<?> getManifestations(@RequestParam("identifier") String identifier, @RequestParam("exact") String exact,
                                               @RequestParam("mode") String mode) {

        ManifestationGetter manifestationgetter = new ManifestationGetter(jdbcTemplate);
        manifestationgetter.setShelfmarkRegex(shelfmarkRegex);
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

    @Override
    @GetMapping("/fullManifestation")
    public ResponseEntity<?> getFullManifestation(@RequestParam("identifier") String identifier,
                                                  @RequestParam("exact") String exact,
                                                  @RequestParam("barcode") Optional<String> barcode) {

        shelfmarks = new HashSet<>();

        Set<String> shelfmarksQueried = new HashSet<>();

        Set<Manifestation> manifestations = new HashSet<>();
        Set<String> manifestationsQueried = new HashSet<>();
        log.info("queried identifier: " + identifier);

        Boolean exactBoolean = "true".equals(exact);
        boolean shelfmarkNew;

        ManifestationGetter manifestationgetter = new ManifestationGetter(jdbcTemplate);
        manifestationgetter.setShelfmarkRegex(shelfmarkRegex);

        Pattern pattern = Pattern.compile(shelfmarkRegex);

        if (barcode.isPresent())
            shelfmarks.addAll(manifestationgetter.getShelfmarkFromBarcode(identifier));
        else {
            identifier = deleteItemIdentifier(identifier);
            if (identifier.contains(";")) {
                String[] individualIdentifiers = identifier.split(";");
                for (String individualIdentifier: individualIdentifiers)
                    if (!individualIdentifier.isEmpty())
                        shelfmarks.add(individualIdentifier);
            } else
                shelfmarks.add(identifier.trim());
        }
        do {
            for (String shelfmark : shelfmarks) {
                log.info("collecting shelfmark " + shelfmark);

                if (shelfmarksQueried.contains(shelfmark))
                    continue;
                List<Manifestation> foundManifestations = manifestationgetter.getDocumentsByShelfmark(shelfmark, exactBoolean);
                shelfmarksQueried.add(shelfmark);
                if (foundManifestations.isEmpty())
                    continue;

                for (Manifestation foundManifestation : foundManifestations) {
                    if (manifestationsQueried.contains(foundManifestation.getTitleID()))
                        continue;
                    extendManifestation(foundManifestation, false);
                    manifestations.add(foundManifestation);
                    if (!manifestationsQueried.contains(foundManifestation.getTitleID()))
                        manifestationsQueried.add(foundManifestation.getTitleID());
                }
            }
            shelfmarkNew = false;
            if (!exactBoolean) {
                List<String> shelfmarksInManifestations = new ArrayList<>();
                for (Manifestation manifestation : manifestations) {
                    shelfmarksInManifestations.addAll(Arrays.asList(manifestation.getShelfmarks()));
                }
                for (String callNo : shelfmarksInManifestations) {
                    callNo = deleteItemIdentifier(callNo);
                    callNo = getReferenceShelfmark(callNo);
                    shelfmarkNew = (shelfmarkNew || isShelfmarkNew(callNo)) && pattern.matcher(callNo).find();
                    if (shelfmarkNew)
                        shelfmarks.add(callNo);
                }
            }
        } while (shelfmarkNew);
        List<Manifestation> manifestationsList = new ArrayList<>(manifestations);
        Collections.sort(manifestationsList);
        return ResponseEntity.ok(manifestationsList);
    }

    private static String getReferenceShelfmark(String shelfmark) {
        return shelfmark.replaceAll("\\(\\d+\\)", "").trim();
    }

    private static String deleteItemIdentifier(String shelfmark) {
        shelfmark = shelfmark.replaceAll("\\+\\d+", "").trim();
        return shelfmark;
    }

    private boolean isShelfmarkNew(String shelfmark) {
        return !shelfmark.equals("???") && !shelfmarks.contains(shelfmark) && !shelfmark.isEmpty();
    }

    @Override
    @GetMapping("/buildFullManifestation")
    public ResponseEntity<?> buildFullManifestation(@RequestParam("identifier") String identifier) {
        Manifestation manifestation = new Manifestation(identifier);
        extendManifestation(manifestation, false);
        return ResponseEntity.ok(manifestation);
    }

    @Override
    @GetMapping("buildActiveManifestation")
    public ResponseEntity<?> buildActiveManifestation(@RequestParam("identifier") String identifier) {
        Manifestation manifestation = new Manifestation(identifier);
        extendManifestation(manifestation, true);
        return ResponseEntity.ok(manifestation);
    }

    @Override
    @GetMapping("journalprices")
    public void getJournalPrices(@RequestParam("identifier") String identifier, @RequestParam("type") String type) throws SQLException {
        JournalPriceGetter getter = new JournalPriceGetter(jdbcTemplate);
        switch (type) {
            case "journalcollection": {
                getter.getJournalcollectionprices(identifier);
                break;
            }
            case "journal": {
                getter.getJournalPrice(identifier);
                break;
            }
        }
    }

    private void extendManifestation(Manifestation manifestation, Boolean active) {
        Set<String> itemIds = new HashSet<>();
        ItemGetter itemGetter = new ItemGetter(jdbcTemplate);
        EventGetter eventgetter = new EventGetter(jdbcTemplate);
        MABGetter mabGetter = new MABGetter(jdbcTemplate);
        List<Item> items = itemGetter.getItemsByDocNumber(manifestation.getTitleID());
        for (Item item : items) {
            if (itemIds.contains(item.getItemId()))
                continue;
            if (ignoredCollections.contains(item.getCollection()))
                continue;
            manifestation.addItem(item);
            itemIds.add(item.getItemId());
        }
        if (active) {
            eventgetter.addAcitveEventsToManifestation(manifestation);
        } else {
            eventgetter.addEventsToManifestation(manifestation);
            StockEventsBuilder.buildStockEvents(manifestation);
            manifestation.buildUsergroupList();
        }
        mabGetter.addSimpleMAB(manifestation);
    }
}
