package unidue.ub.services.getter.getter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.services.getter.model.PrimoData;
import unidue.ub.services.getter.model.PrimoResponse;

import java.util.List;

public class PrimoGetter {

    private final String primoUrl;

    private final String primoApiKey;

    public PrimoGetter(String primoUrl, String primoApiKey) {
        this.primoUrl = primoUrl;
        this.primoApiKey = primoApiKey;
    }

    public PrimoResponse getPrimoResponse(String identifier) {
        PrimoResponse primoResponse = new PrimoResponse();
        String response = getResponseForJson(identifier);
        if (!"".equals(response)) {
            DocumentContext jsonContext = JsonPath.parse(response);
            List<String> documents = jsonContext.read("$['docs'][*]");
            for (String document : documents) {
                DocumentContext documentContext = JsonPath.parse(document);
                PrimoData primoData = new PrimoData();
                String isbns = documentContext.read("$['pnx']['display']['identifier'][*]");
                if (isbns.contains("ISBN"))
                    isbns = isbns.replace("ISBN", "");
                if (isbns.contains("-"))
                    isbns = isbns.replace("-","");
                if (isbns.contains(";"))
                    isbns = isbns.substring(0, isbns.indexOf(";"));
                primoData.setIsbn(isbns.trim());
                primoData.setRecordId(documentContext.read( "$[pnx]['control']['sourcerecordid'][*]"));
                primoData.setType(documentContext.read("$['pnx']['delivery']['delcategory'][*]"));
                primoData.setTitle(documentContext.read("$['pnx']['display']['title'][*]"));
                try {
                    primoData.setAuthors(documentContext.read("$['pnx']['display']['lds07'][*]"));
                } catch (IndexOutOfBoundsException ioobe) {
                    primoData.setAuthors("");
                }
                try {
                    primoData.setEdition(documentContext.read("$['pnx']['display']['edition'][*]"));
                } catch (IndexOutOfBoundsException ioobe) {
                    primoData.setEdition("1");
                }
                try {
                    primoData.setYear(documentContext.read("$['pnx']['display']['creationdate'][*]"));
                } catch (IndexOutOfBoundsException ioobe) {
                    primoData.setYear("0");
                }
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
