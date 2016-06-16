package com.heirm.CDAParse;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;

import java.util.HashMap;
import java.util.List;
import ca.uhn.fhir.model.dstu2.valueset.QuantityComparatorEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;

public class VitalSignParser {
  Section vitalSection  = null;

  public VitalSignParser(Section vitalSection) {
    this.vitalSection = vitalSection;
  }

  public List<Observation> parse(List<Observation> vitalSignsFHIR){
     try{
      for( Entry e:vitalSection.getEntries()){
        Organizer k = e.getOrganizer();
        HashMap vmap = new HashMap<String, String>();
        for (Component4 x : k.getComponents()){
          Observation vitalSign = new Observation();
          org.openhealthtools.mdht.uml.cda.Observation obs = x.getObservation();
          CD co = obs.getCode();
          String vital_name = co.getDisplayName();
          IVL_TS pTime = obs.getEffectiveTime();
          String tr = pTime.getValue();

          if (vital_name!=null  && obs.getValues().size() > 0){
            PQ kl = (PQ) obs.getValues().get(0);
            String value = kl.getValue() + ";" +kl.getUnit();
            vmap.put(vital_name, value);
            vitalSign.setValue(new QuantityDt(QuantityComparatorEnum.forCode(""), kl.getValue().doubleValue(), kl.getUnit()) );
                        vitalSign
                    .setCode(new CodeableConceptDt("urn:oid" + co.getCodeSystem(), co.getCode()));
            vitalSign.getCode().getCodingFirstRep()
                .setDisplay(co.getDisplayName());

            // Does not like this format -  20160208230700+0000  - which is used by Epic Sutter
            // Use Zulu instead. Hardcode until helper method developed for conversion
            vitalSign.setEffective(new DateTimeDt("2016-02-11T22:10:00Z"));
        //    vitalSign.setEffective(new DateTimeDt(pTime.getValue()));
            vitalSignsFHIR.add(vitalSign);
          }
          System.out.println("Vital DateTime - " + vitalSign.getEffective().toString());
          System.out.println("Vital Name - " + vitalSign.getCode().getCodingFirstRep()
              .getDisplay());
          System.out.println("Vital Code - " + vitalSign.getCode().getCodingFirstRep()
              .getCode());
          System.out.println("");

        }
      }
    }catch(Exception ex){
      System.out.println("Vitals Parsing Error");
      ex.printStackTrace();
    }
    return vitalSignsFHIR;
  }
}
