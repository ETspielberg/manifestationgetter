package unidue.ub.services.getter.getter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
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

    public PrimoGetter(String primoApiUrl, String primoApiKey, String primoUrl) {
        this.primoApiUrl = primoApiUrl;
        this.primoApiKey = primoApiKey;
        this.primoUrl = primoUrl;
    }

    public PrimoResponse getPrimoResponse(String identifier) {
        PrimoResponse primoResponse = new PrimoResponse();
        String response = getResponseForJson(identifier);
        if (!"".equals(response)) {
            DocumentContext jsonContext = JsonPath.parse(response);
            List<Map<String, Object>> documents = jsonContext.read("$['docs']");
            int numberOfDocs = documents.size();
            for (int i = 0; i < numberOfDocs; i++) {
                PrimoData primoData = new PrimoData();
                String basePath = "$['docs'][" + i + "]";
                String isbns = jsonContext.read(basePath + "['pnx']['display']['identifier'][0]");
                if (isbns.contains("ISBN"))
                    isbns = isbns.replace("ISBN", "");
                if (isbns.contains("-"))
                    isbns = isbns.replace("-","");
                if (isbns.contains(";"))
                    isbns = isbns.substring(0, isbns.indexOf(";"));
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
                primoResponse.addPrimoData(primoData);
            }
        }

        return primoResponse;
    }

    private String getResponseForJson(String identifier) {
        RestTemplate restTemplate = new RestTemplate();
        String query = "isbn,contains," + identifier;
        String resourceUrl
                = primoApiUrl + "&q=" + query + "&apikey=" + primoApiKey;
        ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK))
            return response.getBody();
        else
            return "";
    }

    private String getPrimoLink(String identifier) {
        return primoUrl + "&docid=" + identifier;
    }
}
