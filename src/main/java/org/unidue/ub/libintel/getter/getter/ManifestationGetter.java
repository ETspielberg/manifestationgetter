package org.unidue.ub.libintel.getter.getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import unidue.ub.media.monographs.Manifestation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManifestationGetter {

    private final String orderBy = "order by titleId";

    private JdbcTemplate jdbcTemplate;

    private final static String SUB_D = "_d";

    private String shelfmarkRegex;

    private final Logger log = LoggerFactory.getLogger(ManifestationGetter.class);

    public ManifestationGetter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Manifestation> getDocumentsByShelfmark(String identifier, boolean exact) {
        String select = "select distinct substr(z30_rec_key,1,9) as titleId from edu50.z30 where ( z30_call_no = ?) ";
        String like = "or ( z30_call_no like ?) ";

        List<Manifestation> manifestations = new ArrayList<>();

        boolean hasSuffix = identifier.endsWith(SUB_D);
        String suffix = "";
        if (hasSuffix) {
            identifier = identifier.substring(0, identifier.length() - SUB_D.length());
            suffix = SUB_D;
        }
        identifier = identifier.toUpperCase();
        Pattern pattern = Pattern.compile(shelfmarkRegex);
        Matcher shelfmarkMatcher =  pattern.matcher(identifier);
        boolean isRegularShelfmark = shelfmarkMatcher.find();
        String query;
        if (exact || !isRegularShelfmark) {
            query = select + like + orderBy;
            manifestations.addAll(jdbcTemplate.query(query, new Object[]{identifier + suffix, identifier + "+%" + suffix}, (rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
        } else {
            query = select + like + like + like + orderBy;
            String signature = shelfmarkMatcher.group();
            String additionalInformation = pattern.matcher(identifier).replaceAll("");
            if (additionalInformation.contains("(")) {
                additionalInformation = additionalInformation.replaceAll("\\(\\d+\\)", "");
            }

            log.info("querying for shelfmark " + signature + " with additional information " + additionalInformation);

            // signature of the base shelfmark (without editions) and without further items ("+1", "+2" etc). Handled with the "select" expression.
            String baseShelfmarkFirstItem = signature + additionalInformation + suffix;
            log.info("query string 1: " + baseShelfmarkFirstItem );
            //the same signature, but with additional items. Handled with one "like" expression.
            String baseShelfmarkMoreItems = signature + additionalInformation + "+%" + suffix;
            log.info("query string 2: " + baseShelfmarkMoreItems);
                    //the same signature, but with the indication of higher editions ("(2)", "(3)" etc). Handled with one "like" expression.
            String higherEditionsFirstItem = signature + "(%" + additionalInformation + suffix;
            log.info("query string 3: " + higherEditionsFirstItem);
                    //the same signatures but with higher educations and additional items. Handled with one "like" expression.
            String higherEditionsMoreItems = signature + "(%" + additionalInformation + "+%" + suffix;
            log.info("query string 4: " + higherEditionsMoreItems);

                    manifestations.addAll(jdbcTemplate.query(query, new Object[]{baseShelfmarkFirstItem, baseShelfmarkMoreItems, higherEditionsFirstItem, higherEditionsMoreItems}, (rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
        }
        return manifestations;
    }


    public List<Manifestation> getDocumentsByOpenRequests() {
        String getByOpenRequests = "select distinct substr(z37_rec_key,1,9) as titleId from edu50.z37 where z37_pickup_location != 'ILLDT' and (z37_end_request_date >(select to_char(sysdate, 'YYYYMMDD') from dual))";
        return new ArrayList<>(jdbcTemplate.query(getByOpenRequests, new Object[]{}, (rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
    }

    public List<String> getShelfmarksByCollection(String collection) {
        String query = "select z30_call_no, z30_collection from edu50.z30 where (z30_collection like ?)";
        return new ArrayList<>(jdbcTemplate.query(query, new Object[]{collection.toUpperCase().trim() + "%"}, (rs, rowNum) -> rs.getString("z30_call_no")));
    }

    public List<Manifestation> getManifestationsByCollection(String collection) {
        String getByCollection = "select substr(z30_rec_key,1,9) as titleId from edu50.z30 where (z30_collection like ?)";
        return new ArrayList<>(jdbcTemplate.query(getByCollection, new Object[]{collection.toUpperCase().trim() + "%"}, (rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
    }

    public List<Manifestation> getManifestationsByBarcode(String barcode) {
        String getByBarcode = "select substr(z30_rec_key,1,9) as titleId from edu50.z30 where (z30_barcode like ?)";
        log.info("querying barcode with " + getByBarcode + " for " + barcode.toUpperCase().trim() + "%");
        return new ArrayList<>(jdbcTemplate.query(getByBarcode, new Object[]{barcode.toUpperCase().trim() + "%"}, (rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
    }

    public List<String> getShelfmarkFromBarcode(String barcode) {
        String getSehlfmarkByBarcode = "select z30_call_no, z30_barcode from edu50.z30 where (z30_barcode like ?)";
        return new ArrayList<>(jdbcTemplate.query(getSehlfmarkByBarcode, new Object[]{barcode.toUpperCase().trim() + "%"}, (rs, rowNum) -> rs.getString("z30_call_no")));
    }


    public List<Manifestation> getDocumentsByEtat(String identifier) {
        String getEtat = "select distinct substr(z75_rec_key,1,9) as titleId from edu50.z601, edu50.z75 where z601_rec_key_2 = z75_rec_key_2 and z601_rec_key like ? and z601_type = 'INV'";
        String query = getEtat + orderBy;
        return new ArrayList<>(jdbcTemplate.query(query, new Object[]{identifier + "%"}, (rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
    }

    public List<Manifestation> getDocumentsByNotation(String identifier) {
        String getByNotation = "select distinct substr(z30_rec_key,1,9) as titleId from edu50.z30 where ( z30_call_no like ?)";
        return new ArrayList<>(jdbcTemplate.query(getByNotation, new Object[]{identifier + "%"}, (rs, rowNum) -> new Manifestation(rs.getString("titleId"))));
    }

    public void setShelfmarkRegex(String shelfmarkRegex) {
        this.shelfmarkRegex = shelfmarkRegex;
    }
}
