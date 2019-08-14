package unidue.ub.services.getter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import unidue.ub.media.monographs.BibliographicInformation;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.services.getter.Utilities;
import unidue.ub.services.getter.getter.*;

import java.util.*;
import java.util.regex.Pattern;

@Controller
@RefreshScope
public class MonographsController {

    private static final Logger log = LoggerFactory.getLogger(MonographsController.class);

    private Set<String> shelfmarks;

    @Value("${ub.statistics.shelfmark.regex}")
    String shelfmarkRegex;

    @Value("${ub.statistics.collections.ignored}")
    String ignoredCollections;

    @Value("${libintel.primo.search.url}")
    String primoApiUrl;

    @Value("${libintel.primo.url}")
    String primoUrl;

    @Value("${libintel.primo.api.key}")
    String primoApiKey;

    private final ManifestationGetterFactory manifestationGetterFactory;
    private final EventGetterFactory eventGetterFactory;
    private final ItemGetterFactory itemGetterFactory;
    private final MABGetterFactory mabGetterFactory;

    @Autowired
    public MonographsController(ManifestationGetterFactory manifestationGetterFactory,
                                EventGetterFactory eventGetterFactory,
                                ItemGetterFactory itemGetterFactory,
                                MABGetterFactory mabGetterFactory) {
        this.manifestationGetterFactory = manifestationGetterFactory;
        this.eventGetterFactory = eventGetterFactory;
        this.itemGetterFactory = itemGetterFactory;
        this.mabGetterFactory = mabGetterFactory;
    }

    @GetMapping("/manifestations")
    public ResponseEntity<?> getManifestations(@RequestParam("identifier") String identifier, @RequestParam("exact") String exact,
                                               @RequestParam("mode") String mode) {
        ManifestationGetter manifestationGetter = manifestationGetterFactory.getObject();
        manifestationGetter.setShelfmarkRegex(shelfmarkRegex);
        List<Manifestation> manifestations = new ArrayList<>();
        boolean exactBoolean = "true".equals(exact);
        switch (mode) {
            case "shelfmark": {
                manifestations = manifestationGetter.getDocumentsByShelfmark(identifier, exactBoolean);
                log.info("retriving manifestations by shelfmark " + identifier);
                break;
            }
            case "etat": {
                manifestations = manifestationGetter.getDocumentsByEtat(identifier);
                log.info("retrieving manifestations by etat " + identifier);
                break;
            }
            case "notation": {
                manifestations = manifestationGetter.getDocumentsByNotation(identifier);
                log.info("retrieving manifestations by notation " + identifier);
                break;
            }
            case "openRequests": {
                manifestations = manifestationGetter.getDocumentsByOpenRequests();
                log.info("retrieving manifestations by open requests");
                break;
            }
            case "barcode": {
                manifestations = manifestationGetter.getManifestationsByBarcode(identifier);
                log.info("retrieving manifestations by barcode " + identifier.toUpperCase());
                break;
            }
            case "collection": {
                manifestations = manifestationGetter.getManifestationsByCollection(identifier);
                log.info("retrieving manifestations by collection " + identifier.toUpperCase());
                break;
            }
        }
        return ResponseEntity.ok(manifestations);
    }

    @GetMapping("/fullManifestation")
    public ResponseEntity<?> getFullManifestation(@RequestParam("identifier") String identifier,
                                                  @RequestParam("exact") String exact,
                                                  @RequestParam(value = "collection", required = false) String collection,
                                                  @RequestParam(value = "barcode", required = false) String barcode) {

        ManifestationGetter manifestationGetter = manifestationGetterFactory.getObject();
        shelfmarks = new HashSet<>();
        Set<String> shelfmarksQueried = new HashSet<>();
        Set<Manifestation> manifestations = new HashSet<>();
        Set<String> manifestationsQueried = new HashSet<>();
        log.info("queried identifier: " + identifier);

        boolean exactBoolean = "true".equals(exact);
        boolean shelfmarkNew;

        manifestationGetter.setShelfmarkRegex(shelfmarkRegex);

        Pattern pattern = Pattern.compile(shelfmarkRegex);

        if (barcode != null)
                shelfmarks.addAll(manifestationGetter.getShelfmarkFromBarcode(identifier));
        else if (collection != null)
                shelfmarks.addAll(manifestationGetter.getShelfmarksByCollection(identifier));
        else {
            identifier = deleteItemIdentifier(identifier);
            log.info("reduced shelfmark: " + identifier);
            if (identifier.contains(";")) {
                String[] individualIdentifiers = identifier.split(";");
                for (String individualIdentifier : individualIdentifiers)
                    if (!individualIdentifier.isEmpty()) {
                        shelfmarks.add(individualIdentifier);
                    }
            } else
                shelfmarks.add(identifier.trim());
        }
        do {
            for (String shelfmark : shelfmarks) {
                log.info("collecting shelfmark " + shelfmark);

                if (shelfmarksQueried.contains(shelfmark))
                    continue;
                List<Manifestation> foundManifestations = manifestationGetter.getDocumentsByShelfmark(shelfmark, exactBoolean);
                shelfmarksQueried.add(shelfmark);
                if (foundManifestations.isEmpty())
                    continue;

                for (Manifestation foundManifestation : foundManifestations) {
                    if (manifestationsQueried.contains(foundManifestation.getTitleID()))
                        continue;
                    extendManifestation(foundManifestation, false);
                    manifestations.add(foundManifestation);
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

    @GetMapping("/buildFullManifestation")
    public ResponseEntity<?> buildFullManifestation(@RequestParam("identifier") String identifier) {
        Manifestation manifestation = new Manifestation(identifier);
        extendManifestation(manifestation, false);
        return ResponseEntity.ok(manifestation);
    }

    @GetMapping("buildActiveManifestation")
    public ResponseEntity<?> buildActiveManifestation(@RequestParam("identifier") String identifier) {
        Manifestation manifestation = new Manifestation(identifier);
        extendManifestation(manifestation, true);
        return ResponseEntity.ok(manifestation);
    }

    @GetMapping("getBibliographicInformation/{identifier}")
    public ResponseEntity<BibliographicInformation> getBibliographicInformation(@PathVariable String identifier) {
        MABGetter mabGetter = mabGetterFactory.getObject();
        return ResponseEntity.ok(mabGetter.getSimpleMab(identifier));
    }

    @GetMapping("getItemList")
    public ResponseEntity<?> getItemList(@RequestParam("identifier") String identifier,
                                         @RequestParam("mode") String mode,
                                         @RequestParam(value = "collections", required = false) String collections) {
        ItemGetter itemGetter = itemGetterFactory.getObject();
        List<Item> items = new ArrayList<>();
        switch (mode) {
            case "barcode": {
                log.info("retrieving items by barcode" );
                items = itemGetter.getItemsByBarcode(identifier);
                break;
            }
            case "shelfmark": {
                log.info("retrieving items by shelfmark with collection filter " + collections);
                items = filterItemList(itemGetter.getItemsByShelfmark(identifier), collections);
                break;
            }
            case "adm": {
                log.info("retrieving items by ADM with collection filter " + collections);
                items = filterItemList(itemGetter.getItemsByDocNumber(identifier), collections);
                break;
            }
        }
        return ResponseEntity.ok(items);
    }

    private List<Item> filterItemList(List<Item> unfilteredItems, String collections) {
        if (collections == null) {
            log.info("no collections filter given, returning original list");
            return unfilteredItems;
        }
        List<Item> filteredItems = new ArrayList<>();
        for (Item item: unfilteredItems) {
            log.info("checking, whether collection " + item.getCollection() + " is in filter " + collections);
            if (collections.contains(item.getCollection()))
                filteredItems.add(item);
        }
        return filteredItems;
    }

    private void extendManifestation(Manifestation manifestation, Boolean active) {
        EventGetter eventGetter = eventGetterFactory.getObject();
        ItemGetter itemGetter = itemGetterFactory.getObject();
        MABGetter mabGetter = mabGetterFactory.getObject();
        Set<String> itemIds = new HashSet<>();
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
            eventGetter.addActiveEventsToManifestation(manifestation);
        } else {
            eventGetter.addEventsToManifestation(manifestation);
            Utilities.buildStockEvents(manifestation);
            manifestation.buildUsergroupList();
        }
        mabGetter.addSimpleMAB(manifestation);
    }

    @GetMapping("getPrimoResponse/isbn/{identifier}")
    public ResponseEntity<?> getIdentifiersForIsbn(@PathVariable("identifier") String identifier) {
        PrimoGetter primoGetter = new PrimoGetter(primoApiUrl, primoApiKey, primoUrl);
        return ResponseEntity.ok(primoGetter.getPrimoResponse(identifier, "isbn"));
    }

    @GetMapping("getPrimoResponse/shelfmark/{identifier}")
    public ResponseEntity<?> getIdentifiersForShelfmark(@PathVariable("identifier") String identifier) {
        PrimoGetter primoGetter = new PrimoGetter(primoApiUrl, primoApiKey, primoUrl);
        return ResponseEntity.ok(primoGetter.getPrimoResponse(identifier, "shelfmark"));
    }
}
