package com.heirm.CDAParse;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.valueset.ObservationRelationshipTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.QuantityComparatorEnum;
import ca.uhn.fhir.model.primitive.TimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import org.apache.commons.lang3.StringUtils;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.ReferenceRange;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSection;
import org.openhealthtools.mdht.uml.cda.hitsp.DiagnosticResultsSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultParser {

  ResultsSection resultsSection;
  DiagnosticResultsSection diagnosticResultsSection;
  VitalSignsSection vitalsignSection;

  public ResultParser(ResultsSection resultsSection) {
    this.resultsSection = resultsSection;
  }

  public ResultParser(DiagnosticResultsSection diagnosticResultsSection) {
    this.diagnosticResultsSection = diagnosticResultsSection;
  }

  public ResultParser(VitalSignsSection vitalsignSection) {
    this.vitalsignSection = vitalsignSection;
  }

  public List<DiagnosticReport> parse(List<DiagnosticReport> diagnosticReports) {
    if (resultsSection != null) {
      System.out.println("About to Enter parse32" );
      return parseH32(diagnosticReports);
    }
    /*
    if (diagnosticResultsSection != null) {
      return parseH83(diagnosticReports);
    }
    */
    return null;
  }

  private List<DiagnosticReport> parseH32(List<DiagnosticReport> diagnosticReports) {

    ArrayList resultsList = new ArrayList<HashMap>();

    if (resultsSection == null) {
      return null;
    }
    System.out.println(
        "Entered Parse32");
    try {

      for (ResultOrganizer rorg : resultsSection.getResultOrganizers()) {

        HashMap res_org = new HashMap();
        String organizer_name = rorg.getCode().getDisplayName();
        ArrayList res_items = new ArrayList<HashMap>();


        for (ResultObservation robs : rorg.getResultObservations()) {
          DiagnosticReport diagnosticReport = new DiagnosticReport();
          HashMap res_item = new HashMap<String, String>();
          //result name
          String result_name = robs.getCode().getDisplayName();
          String code = robs.getCode().getCode();
          String code_system = robs.getCode().getCodeSystem();
          String result_value = "";
          String result_unit = "";
          String reference_value = "";
          Observation obs = new Observation();

          //result val
          ANY rval = robs.getValues().get(0);

          if (rval instanceof PQ) {
            PQ rpq = (PQ) rval;
            obs.setValue(
                new QuantityDt(QuantityComparatorEnum.forCode(""), rpq.getValue().doubleValue(), rpq.getUnit()));
result_value = rpq.getValue().toString();
result_unit =  rpq.getUnit();
          }

          //Get reference ranges
          for (ReferenceRange rr : robs.getReferenceRanges()) {
            ANY rr_val = rr.getObservationRange().getValue();
            if (rr_val instanceof IVL_PQ) {

              obs.addReferenceRange()
                  .setLow(new SimpleQuantityDt(((IVL_PQ) rr_val).getLow().getValue().doubleValue()))
                  .setHigh(
                      new SimpleQuantityDt(((IVL_PQ) rr_val).getHigh().getValue().doubleValue()));
            } else {
              obs.addReferenceRange().setText(rr.getObservationRange().getText().getText());
              reference_value = rr.getObservationRange().getText().getText();

            }
          }
            //TimeStamp
         //   HashMap ts = CDAParserUtil.getTS(robs.getEffectiveTime());

          diagnosticReport.setEffective(new TimeDt(robs.getEffectiveTime().getValue()));



          //Interpretation
            String interpretation = "";
            if (robs.getInterpretationCodes().size() > 0) {
              interpretation = ((CE) robs.getInterpretationCodes().get(0)).getDisplayName();
            }

            diagnosticReport.setCode(new CodeableConceptDt("urn:oid" + robs.getCode().getCodeSystem(),
                    robs.getCode().getCode()));
            diagnosticReport.getCode().getCodingFirstRep()
                .setDisplay(robs.getCode().getDisplayName());

            ResourceReferenceDt result = diagnosticReport.addResult();
            result.setResource(obs);


         //   res_item.put("name", result_name);
         //   res_item.put("value", result_value);
        //    res_item.put("unit", result_unit);
         //   res_item.put("code", code);
         //   res_item.put("code_system", code_system);
        //    res_item.put("ref_low", reference_range_low);
       //     res_item.put("ref_high", reference_range_high);
        //    res_item.put("ts", ts);
       //     res_item.put("interpretation", interpretation);

            res_items.add(res_item);
            ;
  if (StringUtils.isNotEmpty(robs.getCode().getCode()) && StringUtils.isNotEmpty(result_value)) {

   diagnosticReports.add(diagnosticReport);

  //System.out.println("Value - " +result_value);
 // System.out.println("Unit - " + result_unit);
 // System.out.println("Reference Values - " + reference_value);
//  System.out.println("");

}

            }
          res_org.put("category", organizer_name);
          res_org.put("results", res_items);
          resultsList.add(res_org);

        }

    } catch (Exception ex) {
      System.out.println("Result Parsing Exception:");
      ex.printStackTrace();
    }
   // return resultsList;

     return diagnosticReports;
  }


  private ArrayList parseH83(List<DiagnosticReport> diagnosticReports) {
    ArrayList resultsList = new ArrayList<HashMap>();

    if (diagnosticResultsSection == null) {
      return null;
    }

    //	System.out.println(diagnosticResultsSection.getOrganizers());
    //	System.out.println(diagnosticResultsSection.getDiagnosticProcedures());

    for (Organizer org : diagnosticResultsSection.getOrganizers()) {

      ResultOrganizer rorg = (ResultOrganizer) org;
      //System.out.println(rorg);

      HashMap res_org = new HashMap();
      String organizer_name = rorg.getCode().getDisplayName();
      //System.out.println("PATI ORG"+organizer_name);
      ArrayList res_items = new ArrayList<HashMap>();

      for (ResultObservation robs : rorg.getResultObservations()) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        HashMap res_item = new HashMap<String, String>();
        //result name
        String result_name = robs.getCode().getDisplayName();
        String code = robs.getCode().getCode();
        String code_system = robs.getCode().getCodeSystem();

        String result_value = "";
        String result_unit = "";

        //result val
        ANY rval = robs.getValues().get(0);

        if (rval instanceof PQ) {
          PQ rpq = (PQ) rval;
          result_value = rpq.getValue() + " ";
          result_unit = rpq.getUnit();
        }

        //Get reference ranges
        String reference_range_text = "";
        String reference_range_low = "";
        String reference_range_high = "";

        for (ReferenceRange rr : robs.getReferenceRanges()) {
          ANY rr_val = rr.getObservationRange().getValue();
          if (rr_val instanceof IVL_PQ) {
            reference_range_low = "" + ((IVL_PQ) rr_val).getLow().getValue();
            reference_range_high = "" + ((IVL_PQ) rr_val).getHigh().getValue();
          }
        }

        //TimeStamp
        HashMap ts = CDAParserUtil.getTS(robs.getEffectiveTime());

        //Interpretation
        String interpretation = "";
        if (robs.getInterpretationCodes().size() > 0) {
          interpretation = ((CE) robs.getInterpretationCodes().get(0)).getDisplayName();
        }


        diagnosticReport.setCode(new CodeableConceptDt("urn:oid" + robs.getCode().getCodeSystem(),
            robs.getCode().getCode()));
        diagnosticReport.getCode().getCodingFirstRep().setDisplay(robs.getCode().getDisplayName());

        res_item.put("name", result_name);
        res_item.put("value", result_value);
        res_item.put("unit", result_unit);
        res_item.put("code", code);
        res_item.put("code_system", code_system);
        res_item.put("ref_low", reference_range_low);
        res_item.put("ref_high", reference_range_high);
        res_item.put("ts", ts);
        res_item.put("interpretation", interpretation);

        res_items.add(res_item);
        //    System.out.println("res_item H83 Test Name - " + res_item.get("name").toString());
        //    System.out.println("res_item H83 Test Code - "+res_item.get("code").toString());

        diagnosticReports.add(diagnosticReport);
        System.out.println(
            "Name - " + diagnosticReport.getCode().getCodingFirstRep().getDisplay());
        System.out.println(
            "Code - " + diagnosticReport.getCode().getCodingFirstRep().getCode());
        System.out.println(
            "Value - " +result_value);
        System.out.println(
            "Unit - " + result_unit);

      }
      res_org.put("category", organizer_name);
      res_org.put("results", res_items);

      resultsList.add(res_org);
    }

return resultsList;
  }
}
