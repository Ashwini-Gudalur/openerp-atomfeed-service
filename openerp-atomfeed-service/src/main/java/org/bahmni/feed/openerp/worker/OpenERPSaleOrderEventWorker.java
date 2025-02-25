package org.bahmni.feed.openerp.worker;

import org.apache.log4j.Logger;
import org.bahmni.feed.openerp.ObjectMapperRepository;
import org.bahmni.feed.openerp.client.OpenMRSWebClient;
import org.bahmni.feed.openerp.domain.encounter.MapERPOrders;
import org.bahmni.feed.openerp.domain.encounter.OpenMRSEncounter;
import org.bahmni.feed.openerp.domain.visit.OpenMRSVisit;
import org.bahmni.openerp.web.client.OpenERPClient;
import org.bahmni.openerp.web.request.OpenERPRequest;
import org.bahmni.openerp.web.request.builder.Parameter;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;

import java.io.IOException;
import java.net.URI;

public class OpenERPSaleOrderEventWorker implements EventWorker {
    OpenERPClient openERPClient;
    private String feedUrl;
    private OpenMRSWebClient webClient;
    private String urlPrefix;


    private static Logger logger = Logger.getLogger(OpenERPSaleOrderEventWorker.class);

    public OpenERPSaleOrderEventWorker(String feedUrl, OpenERPClient openERPClient, OpenMRSWebClient webClient, String urlPrefix) {
        this.feedUrl = feedUrl;
        this.openERPClient = openERPClient;
        this.webClient = webClient;
        this.urlPrefix = urlPrefix;
    }

    @Override
    public void process(Event event) {
        try {
            OpenERPRequest openERPRequest = mapRequest(event);
            if (!openERPRequest.shouldERPConsumeEvent()) {
               logger.error("Should ERP consume event failed");
               return;
            }
            logger.error("Should ERP consume event success");
            openERPClient.execute(openERPRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cleanUp(Event event) {
    }

    private OpenERPRequest mapRequest(Event event) throws IOException {

        String encounterEventContent = webClient.get(URI.create(urlPrefix + event.getContent()));
        OpenMRSEncounter openMRSEncounter = ObjectMapperRepository.objectMapper.readValue(encounterEventContent, OpenMRSEncounter.class);

        // Ignore Bed Assignment Encounter events
        if(!openMRSEncounter.shouldERPConsumeEvent())
            return OpenERPRequest.DO_NOT_CONSUME;

        String visitURL = "/openmrs/ws/rest/v1/visit/" + openMRSEncounter.getVisitUuid() + "?v=full";
        String visitContent = webClient.get(URI.create(urlPrefix + visitURL));
        logger.error("Before openMRSVisit mapper");
        OpenMRSVisit openMRSVisit = ObjectMapperRepository.objectMapper.readValue(visitContent, OpenMRSVisit.class);
        logger.error("After openMRSVisit mapper");        
        MapERPOrders mapERPOrders = new MapERPOrders(openMRSEncounter, openMRSVisit);

        OpenERPRequest erpRequest = new OpenERPRequest("atom.event.worker", "process_event", mapERPOrders.getParameters(event.getId(), event.getFeedUri(), feedUrl));
        if (event.getFeedUri() == null)
            erpRequest.addParameter(createParameter("is_failed_event", "1", "boolean"));

        return erpRequest;
    }

    private Parameter createParameter(String name, String value, String type) {
        return new Parameter(name, value, type);
    }
}
