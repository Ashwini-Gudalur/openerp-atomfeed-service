package org.bahmni.feed.openerp.domain.visit;
import org.bahmni.feed.openerp.domain.encounter.OpenMRSEncounter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMRSVisit {

    private String uuid;
    private List<VisitAttributes> attributes = new ArrayList<>();
    private List<OpenMRSVisitEncounter> encounters = new ArrayList<>();

    public List<VisitAttributes> getAttributes() {
        return attributes;
    }

    public List<OpenMRSVisitEncounter> getEncounters() {
        return encounters;
    }
    
    public String getUuid() {
        return uuid;
    }
}
