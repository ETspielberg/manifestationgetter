# The Getter #

The getter provides access to the informationwithin the library system. it offers a number of services to retrieve and enrich data:

## /manifestations ##
gives out a list of basic manifestations containing only the title-ID. Five different possibilities exist: querying for a single shelfmark. querying for a shelfmark range, querying for a etat, querying for a barcode and querying all entries with open requests. Depending on the query, different additional parameters need to be supplied within the HTTP-GET-request:
 
- mode=shelfmark: identifier=*the shelfmark to be queried*&exact=*true, if only the named edition is to be queried*
- mode=etat: identifier=*the etat used to buy this manifestation*
- mode=notation identifier=*the notation for which all manifestations are collected*
- mode=barcode: identifier=*the barcode of one book of the queried manifestation*
- mode=openRequests: *returns all manifestations with open requests*

## /fullManifestation ##
Supplied with a shelfmark, and the boolean exact, this service returns a list of manifestations for a given shelfmark, similar to /manifestation in mode shelfmark. In contrast the manifestation are already expanded with all items, events and bibliographic information. 

##/buildFullManifestation ##
returns a fully expanded manifestation for a given title ID. This option is used, if the title IDs are retrieved by another mechanism.

## /items ##

## /loans ###

## /requests ##