package com.heirm.CDAParse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Medication;

import ca.uhn.fhir.model.dstu2.valueset.MedicationStatementStatusEnum;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Precondition;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.CEImpl;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.ENImpl;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;

public class MedicationParser {

  MedicationsSection medSection = null;

  public MedicationParser(MedicationsSection medSection) {
    this.medSection = medSection;
  }

  public List<MedicationStatement> parse(List<MedicationStatement> medicationsFHIR) {


    if (this.medSection == null) {
      return null;
    }

    for (SubstanceAdministration sa : medSection.getSubstanceAdministrations()) {

      MedicationStatement medStatement = new MedicationStatement();
      HashMap<String, Object> med = new HashMap<String, Object>();
      Consumable consumable = sa.getConsumable();

      ManufacturedProduct manufacturedProduct = consumable.getManufacturedProduct();
      Material mf = manufacturedProduct.getManufacturedMaterial();
      CEImpl mm_code = (CEImpl) mf.getCode();

      if (mm_code != null) {
            medStatement.setMedication(
            new CodeableConceptDt("urn:oid" + mm_code.getCodeSystem(), mm_code.getCode()));
      }


      IVL_PQ dose = sa.getDoseQuantity();

      if (dose != null) {
        medStatement.addDosage()
            .setQuantity(new SimpleQuantityDt().setValue(dose.getValue()).setUnit(dose.getUnit()));

        //Route code
        CE routeCode = sa.getRouteCode();

        if (routeCode != null) {
          medStatement.getDosageFirstRep().setRoute(new CodeableConceptDt(routeCode.getCodeSystem(),routeCode.getCode()));
              }
      }


        //Time
      /*
        String period_unit = "";
        HashMap ts = null;

        for (SXCM_TS effectiveTime : sa.getEffectiveTimes()) {
          //System.out.println(effectiveTime);
          if (effectiveTime instanceof PIVL_TS) {
            PIVL_TS periodTS = (PIVL_TS) effectiveTime;
            period_unit +=
                "" + periodTS.getPeriod().getValue() + " " + periodTS.getPeriod().getUnit();
          }
          if (effectiveTime instanceof IVL_TS) {
            IVL_TS pTime = (IVL_TS) effectiveTime;
            ts = CDAParserUtil.getTS(pTime);
          }
        }
*/


        //status
        String status_code = sa.getStatusCode().getCode();
      medStatement.setStatus(MedicationStatementStatusEnum.ACTIVE);


 /*     for(Observation os : sa.getObservations()){

        if(os instanceof ProblemObservation){
          ProblemObservation pobs = (ProblemObservation)os;
          String problem_name = ((CD)pobs.getValues().get(0)).getDisplayName();
          //String problem_status = ((CD)pobs.getProblemStatus().getValues().get(0)).getDisplayName();
          HashMap rcond = new HashMap();
          rcond.put("name", problem_name);
          HashMap ots = CDAParserUtil.getTS(pobs.getEffectiveTime());
          rcond.put("ts", ots);
          //rcond.put("status", problem_status);
          related_conditions.add(rcond);
        }
      }

      */
      medicationsFHIR.add(medStatement);

    }

    return medicationsFHIR;
  }
}
