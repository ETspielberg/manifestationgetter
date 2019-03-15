package unidue.ub.services.getter.getter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.services.getter.model.PrimoData;
import unidue.ub.services.getter.model.PrimoResponse;

import java.util.List;
import java.util.Map;

public class PrimoGetter {

    private final String primoApiUrl;

    private final String primoApiKey;

    private final String primoUrl;

    private final Logger log = LoggerFactory.getLogger(PrimoGetter.class);

    public PrimoGetter(String primoApiUrl, String primoApiKey, String primoUrl) {
        this.primoApiUrl = primoApiUrl;
        this.primoApiKey = primoApiKey;
        this.primoUrl = primoUrl;
    }

    public PrimoResponse getPrimoResponse(String identifier) {
        PrimoResponse primoResponse = new PrimoResponse();
        String response = getResponseForJson(identifier, "");
        if (!"".equals(response)) {
            DocumentContext jsonContext = JsonPath.parse(response);
            List<Object> documents = jsonContext.read("$['docs'][*]");
            log.info("found " + documents.size() + " documents");
            int numberOfDocs = documents.size();
            for (int i = 0; i < numberOfDocs; i++) {
                String basePath = "$['docs'][" + i + "]";
                try {
                    String test = jsonContext.read(basePath + "['pnx']['links']['lln15'][0]");
                    log.info(test);
                    String frbrGroupId = jsonContext.read(basePath + "['pnx']['facets']['frbrgroupid'][0]");
                    String frbrRPrimoResponse = getResponseForJson(identifier,frbrGroupId);
                    DocumentContext frbrContext = JsonPath.parse(frbrRPrimoResponse);
                    List<Object> frbrDocuments = frbrContext.read("$['docs'][*]");
                    log.info("found " + frbrDocuments.size() + " documents");
                    for (int k = 0; k < frbrDocuments.size(); k++) {
                        log.info("processing entry " + k);
                        String frbrBasePath = "$['docs'][" + k + "]";
                        PrimoData primoData = convertContextToPrimoData(frbrContext, frbrBasePath);
                        primoResponse.addPrimoData(primoData);
                    }
                } catch (PathNotFoundException pnfe) {
                    log.info("caught exception processing orginal response");
                    PrimoData primoData = convertContextToPrimoData(jsonContext, basePath);
                    primoResponse.addPrimoData(primoData);
                }
            }
        }

        return primoResponse;
    }

    private String getResponseForJson(String identifier, String frbrGroupId) {
        String frbrSearch = "";
        if (!frbrGroupId.isEmpty())
            frbrSearch = "facet_frbrgroupid,exact," + frbrGroupId;
        RestTemplate restTemplate = new RestTemplate();
        String query = "isbn,contains," + identifier;
        String resourceUrl
                = primoApiUrl + "&q=" + query + "&qInclude=" + frbrSearch + "&apikey=" + primoApiKey;
        log.info("querying Primo API with " + resourceUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK))
            return response.getBody();
        else
            return "";
    }

    private String getPrimoLink(String identifier) {
        return primoUrl + "&docid=" + identifier;
    }

    private PrimoData convertContextToPrimoData(DocumentContext jsonContext, String basePath) {
        PrimoData primoData = new PrimoData();
        String isbns = jsonContext.read(basePath + "['pnx']['display']['identifier'][0]");
        if (isbns.contains("DOI")) {
            String[] parts = isbns.split("DOI");
            isbns = parts[0];
            primoData.setDoi(parts[1].trim());
        }
        if (isbns.contains(" "))
            isbns = isbns.replace(" ", "");
        if (isbns.contains("ISBN"))
            isbns = isbns.replace("ISBN", "");
        if (isbns.contains("-"))
            isbns = isbns.replace("-","");
        primoData.setIsbn(isbns.trim());
        primoData.setRecordId(jsonContext.read(basePath + "['pnx']['control']['sourcerecordid'][0]"));
        primoData.setType(jsonContext.read(basePath + "['pnx']['delivery']['delcategory'][0]"));
        primoData.setTitle(jsonContext.read(basePath + "['pnx']['display']['title'][0]"));
        try {
            primoData.setShelfmarks(jsonContext.read(basePath + "['pnx']['display']['lds48'][0]"));
        } catch (PathNotFoundException pnfe) {
            primoData.setShelfmarks("");
        }
        primoData.setLink(getPrimoLink(jsonContext.read(basePath + "['pnx']['search']['recordid'][0]")));
        try {
            primoData.setAuthors(jsonContext.read(basePath + "['pnx']['display']['lds07'][0]"));
        } catch (PathNotFoundException pnfe) {
            primoData.setAuthors("");
        }
        try {
            primoData.setEdition(jsonContext.read(basePath + "['pnx']['display']['edition'][0]"));
        } catch (PathNotFoundException pnfe) {
            primoData.setEdition("1");
        }
        try {
            primoData.setYear(jsonContext.read(basePath + "['pnx']['display']['creationdate'][0]"));
        } catch (PathNotFoundException pnfe) {
            primoData.setYear("0");
        }
        List<Map<String, Object>> linkObjects = jsonContext.read(basePath + "['delivery']['link'][*]");
        for (int j = 0; j < linkObjects.size(); j++) {
            String linkBasePath = basePath +  "['delivery']['link'][" + j + "]";
            String type = jsonContext.read(linkBasePath + "['displayLabel']");
            if ("thumbnail".equals(type))
                primoData.setLinkThumbnail(jsonContext.read(linkBasePath + "['linkURL']"));
            if ("$$Elinktorsrc".equals(type))
                primoData.setFulltextLink(jsonContext.read(linkBasePath + "['linkURL']"));
        }
        return primoData;
    }
}
