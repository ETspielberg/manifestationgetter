package unidue.ub.services.getter;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;

public interface GetterClient {
    @GetMapping("/manifestations")
    ResponseEntity<?> getManifestations(@RequestParam("identifier") String identifier, @RequestParam("exact") String exact,
                                        @RequestParam("mode") String mode);

    @GetMapping("/fullManifestation")
    ResponseEntity<?> getFullManifestation(@RequestParam("identifier") String identifier,
                                           @RequestParam("exact") String exact);

    @GetMapping("/buildFullManifestation")
    ResponseEntity<?> buildFullManifestation(@RequestParam("identifier") String identifier);

    @GetMapping("buildActiveManifestation")
    ResponseEntity<?> buildActiveManifestation(@RequestParam("identifier") String identifier);

    @GetMapping("journalprices")
    void getJournalPrices(@RequestParam("identifier") String identifier, @RequestParam("type") String type) throws SQLException;
}
