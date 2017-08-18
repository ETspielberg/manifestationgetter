package unidue.ub.services.getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.ub.media.analysis.*;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;

import java.util.List;

public class TimelineGenerator {

    private Manifestation manifestation;

    private static final Logger LOGGER = LoggerFactory.getLogger(TimelineGenerator.class);

    TimelineGenerator(Manifestation manifestation) {
        this.manifestation = manifestation;
    }

    public void addTimelines() {
        //if no collections are present, just cancel the analysis
        if (manifestation.getCollections().size() == 0) {
            return;
        } else {
            UsageData usageData = manifestation.getStatistics();
            initializeUsageData();
            for (Item item : manifestation.getItems()) {
                Collection collectionInd = usageData.getCollection(item.getCollection());
                List<Event> events = item.getEvents();
                if (events.size() == 0)
                    continue;
                for (Event event : events) {
                    String type = event.getType();
                    if (type.equals("loan"))
                        addTimeAndCount(event, getUserTimeline(event, collectionInd));
                    else if (type.equals("request"))
                        addTimeAndCount(event, collectionInd.getRequests());
                    else if (type.equals("inventory"))
                        addTimeAndCount(event, collectionInd.getStock());
                }
            }
        }
    }

    private Timeline getUserTimeline(Event event, Collection collectionInd) {
        String usergroup = event.getBorrowerStatus();
        if (usergroup == null)
            usergroup = "else";
        TimelineGroup loans = collectionInd.getLoans();
        Timeline timeline = loans.getTimeline(usergroup);
        if (timeline == null) {
            timeline = new Timeline(usergroup);
            loans.addTimeline(timeline);
        }
        return timeline;
    }

    private void addTimeAndCount(Event event, Timeline timeline) {
        timeline.addTimeAndCount(event.getTime(), event.getDelta());
        if (event.getEndEvent() != null) {
            Event endEvent = event.getEndEvent();
            timeline.addTimeAndCount(endEvent.getTime(), endEvent.getDelta());
        }
    }

    private void initializeUsageData() {
        for (String collectionName : manifestation.getCollections()) {
            Collection collection = new Collection(collectionName);
            LOGGER.info("build usage collection " + collectionName);
            collection.setStock(new Timeline("stock"));
            collection.setRequests(new Timeline("requests"));
            collection.setLoans(new TimelineGroup("loans"));
            manifestation.getStatistics().addCollection(collection);
        }
    }
}
