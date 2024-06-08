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

package org.bahmni.feed.openerp.domain.visit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bahmni.feed.openerp.domain.encounter.OpenMRSObs;
import org.apache.log4j.Logger;
import org.bahmni.feed.openerp.ObjectMapperRepository;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMRSVisitEncounter {
    private List<OpenMRSObs> obs = new ArrayList<>();
    private String display;

    public List<OpenMRSObs> getObs() {
        return obs;
    }

    public void setObs(List<OpenMRSObs> obs) {
        this.obs = obs;
    }

    public String getDisplay() {
        return display;
    }

}
