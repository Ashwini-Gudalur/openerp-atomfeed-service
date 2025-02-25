/*
* The contents of this file are subject to the Mozilla Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific language governing rights and limitations under
* the License.
*
* The Original Code is OpenELIS code.
*
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
*/

package org.bahmni.feed.openerp.domain.encounter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.bahmni.feed.openerp.ObjectMapperRepository;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMRSEncounter {
    private List<OpenMRSDrugOrder> drugOrders = new ArrayList<>();
    private List<OpenMRSOrder> orders = new ArrayList<>();
    private List<OpenMRSObservation> observations = new ArrayList<>();
    private List<OpenMRSObs> obs = new ArrayList<>();
    private List<Provider> providers = new ArrayList<>();
    private String patientUuid;
    private String patientId;
    private String encounterUuid;
    private String visitUuid;
    private String visitTypeUuid;
    private String locationName;
    private String encounterType;
    private String display;

    public boolean shouldERPConsumeEvent() {
        return hasDrugOrders() || hasOrders() || hasObservations();
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public String getEncounterType() {
        return encounterType;
    }

    private boolean hasDrugOrders() {
        return getDrugOrders().size() > 0;
    }

    private boolean hasOrders() {
        return getOrders().size() > 0;
    }

    private boolean hasObservations() {
    	return getObservations().size() > 0;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public String getPatientId() {
        return patientId;
    }

    public List<OpenMRSDrugOrder> getDrugOrders() {
        return drugOrders;
    }

    public List<OpenMRSOrder> getOrders() {
        return orders;
    }

    public List<OpenMRSObservation> getObservations() {
        return observations;
    }

    public void setObservations(List<OpenMRSObservation> observations) {
        this.observations = observations;
    }

    public List<OpenMRSObs> getObs() {
        return obs;
    }

    public void setObs(List<OpenMRSObs> obs) {
        this.obs = obs;
    }

    public String getDisplay() {
        return display;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public String getVisitTypeUuid() {
        return visitTypeUuid;
    }

    public String getLocationName() { return locationName; }

    public List<Provider> getProviders() {
        return providers;
    }
    public String getgroupMembersJSON() {
    	
        HashMap<String, Object> groupMembers = new HashMap<>();
    	for (OpenMRSObservation openMRSObservation: this.observations)
    	{
    		for (OpenMRSObservationGroupMember groupMember: openMRSObservation.getgroupMembers())
    		{
    			groupMembers.put(groupMember.getconceptNameToDisplay(), groupMember.getvalueAsString());
    		}
    	}
    	String groupMembersJSON = "";
    	try {
    		
        	groupMembersJSON = ObjectMapperRepository.objectMapper.writeValueAsString(groupMembers);
    	}  catch (IOException e) {
            Logger logger = Logger.getLogger(OpenMRSEncounter.class);
            logger.error("Unable to convert groupMembers hash to json string. " + e.getMessage());
        }
    	return groupMembersJSON;
    }
}
