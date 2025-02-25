package org.bahmni.feed.openerp.domain.encounter;


import org.apache.log4j.Logger;
import org.bahmni.feed.openerp.ObjectMapperRepository;
import org.bahmni.feed.openerp.domain.visit.OpenMRSVisit;
import org.bahmni.feed.openerp.domain.visit.OpenMRSVisitEncounter;
import org.bahmni.feed.openerp.domain.visit.VisitAttributes;
import org.bahmni.openerp.web.request.builder.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;

public class MapERPOrders extends OpenMRSEncounterEvent {

	private Logger logger = Logger.getLogger(MapERPOrders.class);
    private OpenMRSEncounter openMRSEncounter;
    private OpenMRSVisit openMRSVisit;

    public MapERPOrders(OpenMRSEncounter openMRSEncounter, OpenMRSVisit openMRSVisit) {
        this.openMRSEncounter = openMRSEncounter;
        this.openMRSVisit = openMRSVisit;

    }

    public List<Parameter> getParameters(String eventId, String feedURIForLastReadEntry, String feedURI) throws IOException {
        List<Parameter> parameters = new ArrayList<>();
        validateUrls(feedURIForLastReadEntry, feedURI);
        if (openMRSEncounter.getEncounterType().compareToIgnoreCase("REG") == 0) {
        	parameters.add(createParameter("category", "update.attributes", "string"));
        }
        else {
        	parameters.add(createParameter("category", "create.sale.order", "string"));
        }
        parameters.add(createParameter("customer_id", openMRSEncounter.getPatientId(), "string"));
        parameters.add(createParameter("encounter_id", openMRSEncounter.getEncounterUuid(), "string"));
        parameters.add(createParameter("feed_uri", feedURI, "string"));
        parameters.add(createParameter("last_read_entry_id", eventId, "string"));
        parameters.add(createParameter("feed_uri_for_last_read_entry", feedURIForLastReadEntry, "string"));
        parameters.add(createParameter("visitAttributes", getVisitAttributes(), "string"));
        parameters.add(createParameter("ConsultantInfo", getConsultantInformation(), "string"));
        parameters.add(createParameter("ReferralInfo", getReferralInformation(), "string"));
        parameters.add(createParameter("orders", mapOpenERPOrders(), "string"));
        parameters.add(createParameter("locationName", openMRSEncounter.getLocationName(), "string"));
        parameters.add(createParameter("attributes", openMRSEncounter.getgroupMembersJSON(), "string"));
        return parameters;
    }


    private String mapOpenERPOrders() throws IOException {
        OpenERPOrders openERPOrders = new OpenERPOrders(openMRSEncounter.getEncounterUuid());
        List<Provider> providers = openMRSEncounter.getProviders();

        List<OpenMRSObservation> observations = openMRSEncounter.getObservations();
        String providerName = providers.size() != 0 ? providers.get(0).getName() : "";
        for (OpenMRSDrugOrder drugOrder : openMRSEncounter.getDrugOrders()) {
            if(drugOrder.getDrugNonCoded() != null) {
                continue;
            }
            OpenERPOrder openERPOrder = new OpenERPOrder();
            openERPOrder.setVisitId(openMRSEncounter.getVisitUuid());
            openERPOrder.setOrderId(drugOrder.getUuid());
            openERPOrder.setDispensed("false");
            observations.stream().filter(o -> o.getOrderUuid() != null && o.getOrderUuid().equals(openERPOrder.getOrderId()))
                    .forEach(o -> {
                        if ((o.getConcept()  != null)
                                && o.getConcept().getName().equalsIgnoreCase("Dispensed")) {
                            if (Boolean.parseBoolean(o.getValue().toString())) {
                                openERPOrder.setDispensed("true");
                            }
                        }
                    });
            openERPOrder.setPreviousOrderId(drugOrder.getPreviousOrderUuid());
            openERPOrder.setEncounterId(openMRSEncounter.getEncounterUuid());
            openERPOrder.setProductId(drugOrder.getDrugUuid());
            openERPOrder.setProductName(drugOrder.getDrugName());
            openERPOrder.setAction(drugOrder.getAction());
            openERPOrder.setQuantity(drugOrder.getQuantity());
            openERPOrder.setQuantityUnits(drugOrder.getQuantityUnits());
            openERPOrder.setVoided(drugOrder.isVoided());
            openERPOrder.setType(drugOrder.getOrderType());
            openERPOrder.setVisitType(getVisitType());
            openERPOrder.setProviderName(providerName);
            openERPOrder.setConceptName(drugOrder.getConceptName());
            openERPOrders.add(openERPOrder);

        }



        for (OpenMRSOrder order : openMRSEncounter.getOrders()) {
            OpenERPOrder openERPOrder = new OpenERPOrder();
            openERPOrder.setVisitId(openMRSEncounter.getVisitUuid());
            openERPOrder.setOrderId(order.getUuid());
            openERPOrder.setDispensed("false");
            openERPOrder.setPreviousOrderId(order.getPreviousOrderUuid());
            openERPOrder.setEncounterId(openMRSEncounter.getEncounterUuid());
            openERPOrder.setProductId(order.getConceptUuid());
            openERPOrder.setProductName(order.getConceptName());
            openERPOrder.setAction(order.getAction());
            openERPOrder.setQuantity((double) 1);
            openERPOrder.setQuantityUnits("Unit(s)");
            openERPOrder.setVoided(order.isVoided());
            openERPOrder.setType(order.getOrderType());
            openERPOrder.setVisitType(getVisitType());
            openERPOrder.setProviderName(providerName);
            openERPOrders.add(openERPOrder);
        }

        if (openMRSEncounter.getEncounterType().compareToIgnoreCase("REG") == 0){
        	//Add registration fee as order
            for (OpenMRSObservation observation : observations) {
            	List<OpenMRSObservationGroupMember> groupMembers = observation.getgroupMembers();
            	for (OpenMRSObservationGroupMember groupMember :groupMembers  ) {
            		if ((groupMember.getconcept().getconceptClass().compareToIgnoreCase("RegFee") == 0 && 
            		   (groupMember.getvalueAsString().compareToIgnoreCase("Yes") == 0)) || (groupMember.getvalue().getconceptClass().compareToIgnoreCase("Billable Service") == 0 )) {
	                    OpenERPOrder openERPOrder = new OpenERPOrder();
	                    openERPOrder.setVisitId(openMRSEncounter.getVisitUuid());
	                    openERPOrder.setOrderId(UUID.randomUUID().toString());
	                    openERPOrder.setDispensed("false");
	                    openERPOrder.setPreviousOrderId("");
	                    openERPOrder.setEncounterId(openMRSEncounter.getEncounterUuid());
	                    if (groupMember.getvalue().getconceptClass().compareToIgnoreCase("Billable Service") == 0 ) {
                            openERPOrder.setProductId(groupMember.getvalue().getuuid());
	                        openERPOrder.setProductName(groupMember.getvalue().getname());
                        }
                        else {
                            openERPOrder.setProductId(groupMember.getconceptUuid());
	                        openERPOrder.setProductName(groupMember.getconceptNameToDisplay());
                        }
	                    openERPOrder.setAction("NEW");
	                    openERPOrder.setQuantity((double) 1);
	                    openERPOrder.setQuantityUnits("Unit(s)");
	                    openERPOrder.setVoided(false);
	                    openERPOrder.setType("Registration Fee");
	                    openERPOrder.setVisitType(getVisitType());
	                    openERPOrder.setProviderName(providerName);
	                    openERPOrders.add(openERPOrder);
            		}
            	}
            }
        }
        return ObjectMapperRepository.objectMapper.writeValueAsString(openERPOrders);
    }

    private String getVisitType() {
        for (VisitAttributes visitAttribute : openMRSVisit.getAttributes()) {
            if (visitAttribute.getAttributeType().getDisplay().equals("Visit Status")) {
                return visitAttribute.getValue();
            }
        }
        return null;
    }

    private String getVisitAttributes() {
        HashMap<String, Object> visitAttributes = new HashMap<>();
        String attrName;
        String attrValue;
        for (VisitAttributes visitAttribute : openMRSVisit.getAttributes()) {
            attrName = visitAttribute.getAttributeType().getDisplay();
            attrValue = visitAttribute.getValue();
            visitAttributes.put(attrName, attrValue);
        }
        String visitAttributesJson = "";
        try {
            visitAttributesJson = ObjectMapperRepository.objectMapper.writeValueAsString(visitAttributes);
        } catch (IOException e) {
            Logger logger = Logger.getLogger(MapERPOrders.class);
            logger.error("Unable to convert visitAttributes hash to json string. " + e.getMessage());
        }

        return visitAttributesJson;
    }
    private String getConsultantInformation() {
        for (OpenMRSVisitEncounter enc: openMRSVisit.getEncounters()) {
            if (enc.getDisplay().toUpperCase().contains("REG")){
                logger.error("Encounter Type " + enc.getDisplay());
                logger.error("Number of Observations " + enc.getObs().size());
                //Add registration fee as order
                for (OpenMRSObs observation : enc.getObs()) {
                    String consultantInfoConcept = "Consultant Information: ";
                    logger.error("Consultant Info " + observation.getDisplay());
                    if (observation.getDisplay().toUpperCase().contains(consultantInfoConcept.toUpperCase())) {
                        return observation.getDisplay().substring(consultantInfoConcept.length());
                    }
                }
            }
        }
        return "";
    }
    private String getReferralInformation() {
        for (OpenMRSVisitEncounter enc: openMRSVisit.getEncounters()) {
            if (enc.getDisplay().toUpperCase().contains("REG")){
                logger.error("Encounter Type " + enc.getDisplay());
                logger.error("Number of Observations " + enc.getObs().size());
                for (OpenMRSObs observation : enc.getObs()) {
                    String referralInfoConcept = "Referral Information: ";
                    logger.error("Referred By Doctor " + observation.getDisplay());
                    if (observation.getDisplay().toUpperCase().contains(referralInfoConcept.toUpperCase())) {
                        return observation.getDisplay().substring(referralInfoConcept.length());
                    }
                }
            }
        }
        return "";
    }
}
