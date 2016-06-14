package com.heirm.CDAParse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.primitive.DateDt;
import org.apache.commons.lang3.StringUtils;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.consol.EpisodeObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.consol.ProblemStatus;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;

public class ProblemParser {

  ProblemSection problemSection = null;

  public ProblemParser(ProblemSection problemSection){
    this.problemSection = problemSection;
  }

  public List<Condition> parse(List<Condition> conditions){
    ArrayList condList = new ArrayList<HashMap>();

    if(problemSection == null){
      System.out.println("There are no problems");
      return null;
    }

    try{
      for(ProblemConcernAct problemAct: problemSection.getProblemConcerns()){

        for(Observation obs : problemAct.getObservations()){

          if(obs instanceof ProblemObservation){
            Condition condition = new Condition();

            HashMap cond = new HashMap<String, String>();
            ProblemObservation pobs = (ProblemObservation)obs;


            //        patientFHIR.setBirthDate(new DateDt(patientCDA.getBirthTime().getValue()));
            IVL_TS pTime = pobs.getEffectiveTime();
            IVXB_TS effTime = pTime.getLow();
            String shortDate =  StringUtils.left(effTime.getValue(), 8);
            if (shortDate != null) {
              condition.setDateRecorded(new DateDt(shortDate));
            }
            condition.setCode(new CodeableConceptDt(((CD)pobs.getValues().get(0)).getCodeSystem(),((CD)pobs.getValues().get(0)).getCode()));

            //System.out.println(pobs.getCode());
                //System.out.println(((CD)pobs.getProblemStatus().getValues().get(0)));
            HashMap ts = CDAParserUtil.getTS(pTime);
            //cond.put("code1", pobs.getCode().getCode());
           // cond.put("name", problem_name);
           // cond.put("status", problem_status);
           // cond.put("ts", ts);
           // cond.put("code", code);
           // condList.add(cond);
           conditions.add(condition);
          }
          if(obs instanceof EpisodeObservation){
            EpisodeObservation eobs = (EpisodeObservation)obs;
            //E.g. clinical finding
            //System.out.println("EPISODE "+eobs.getValues());
          }

        }

      }
    }catch(Exception ex){
      System.out.println("Problem Parsing Exception:");
      ex.printStackTrace();
    }

    return conditions;

  }
}
