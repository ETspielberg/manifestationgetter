package unidue.ub.services.getter.getter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.services.getter.model.PrimoData;
import unidue.ub.services.getter.model.PrimoResponse;

import java.util.List;

public class PrimoGetter {

    private final String primoUrl;

    private final String primoApiKey;

    private final Logger log = LoggerFactory.getLogger(PrimoGetter.class);

    private final String basePath = "$['docs'][*]['pnx']";

    private final String sourcetypePath = basePath + "['delivery']['delcategory'][*]";

    private final String isbnPath = basePath + "['display']['identifier'][*]";

    private final String recordIdPath = basePath + "['control']['sourcerecordid'][*]";

    private final String titlePath = basePath + "['display']['title'][*]";

    private final String authorsPath = basePath + "['display']['lds07'][*]";

    private final String editionPath = basePath + "['display']['edition'][*]";

    public PrimoGetter(String primoUrl, String primoApiKey) {
        this.primoUrl = primoUrl;
        this.primoApiKey = primoApiKey;
    }

    public PrimoResponse getPrimoResponse(String identifier) {
        PrimoResponse primoResponse = new PrimoResponse();
        String response = getResponseForJson(identifier);
        if (!"".equals(response)) {
            DocumentContext jsonContext = JsonPath.parse(response);
            List<String> sourceIds = jsonContext.read(basePath + "['display']['identifier'][*]");
            List<String> sourcetypes = jsonContext.read(basePath + "['delivery']['delcategory'][*]");
            List<String> recordIds = jsonContext.read(basePath + "['control']['sourcerecordid'][*]");
            List<String> authors = jsonContext.read(basePath + "['display']['lds07'][*]");
            List<String> titles = jsonContext.read(basePath + "['display']['title'][*]");
            List<String> editions = jsonContext.read(basePath + "['display']['edition'][*]");
            List<String> years = jsonContext.read(basePath + "['display']['creationdate'][*]");
            for (int i = 0; i < sourcetypes.size(); i++) {
                PrimoData primoData = new PrimoData();
                primoData.setIsbn(sourceIds.get(i));
                primoData.setRecordId(recordIds.get(i));
                primoData.setType(sourcetypes.get(i));
                primoData.setTitle(titles.get(i));
                primoData.setAuthors(authors.get(i));
                primoData.setEdition(editions.get(i));
                primoData.setYear(years.get(i));
                primoResponse.addIsbnRecordIdRelation(primoData);
            }
        }
        return primoResponse;
    }
    private String getResponseForJson(String identifier) {
        RestTemplate restTemplate = new RestTemplate();
        String query = "isbn,contains," + identifier;
        String resourceUrl
                = primoUrl + "&q=" + query + "&apikey=" + primoApiKey;
        ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK))
            return response.getBody();
        else
            return "";
    }
}
