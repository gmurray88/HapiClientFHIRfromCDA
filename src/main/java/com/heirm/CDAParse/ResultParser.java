package com.heirm.CDAParse;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.valueset.QuantityComparatorEnum;
import ca.uhn.fhir.model.primitive.TimeDt;
import org.apache.commons.lang3.StringUtils;
import org.openhealthtools.mdht.uml.cda.ReferenceRange;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;

import java.util.List;

public class ResultParser {

  ResultsSection resultsSection;

  public ResultParser(ResultsSection resultsSection) {
    this.resultsSection = resultsSection;
  }

  public List<DiagnosticReport> parse(List<DiagnosticReport> diagnosticReports) {
      if (resultsSection == null) {
      return null;
    }
    try {

      for (ResultOrganizer rorg : resultsSection.getResultOrganizers()) {
      for (ResultObservation robs : rorg.getResultObservations()) {
          DiagnosticReport diagnosticReport = new DiagnosticReport();
          String result_value = "";
        Observation obs = new Observation();
        ANY rval = robs.getValues().get(0);

          if (rval instanceof PQ) {
            PQ rpq = (PQ) rval;
            obs.setValue(
                new QuantityDt(QuantityComparatorEnum.forCode(""), rpq.getValue().doubleValue(),
                    rpq.getUnit()));
            result_value = rpq.getValue().toString();
          }

          for (ReferenceRange rr : robs.getReferenceRanges()) {
            ANY rr_val = rr.getObservationRange().getValue();
            if (rr_val instanceof IVL_PQ) {

              obs.addReferenceRange()
                  .setLow(new SimpleQuantityDt(((IVL_PQ) rr_val).getLow().getValue().doubleValue()))
                  .setHigh(
                      new SimpleQuantityDt(((IVL_PQ) rr_val).getHigh().getValue().doubleValue()));
            } else {
              obs.addReferenceRange().setText(rr.getObservationRange().getText().getText());
            }
          }
          diagnosticReport.setEffective(new TimeDt(robs.getEffectiveTime().getValue()));
          diagnosticReport.setCode(new CodeableConceptDt("urn:oid" + robs.getCode().getCodeSystem(),
              robs.getCode().getCode()));
          diagnosticReport.getCode().getCodingFirstRep()
              .setDisplay(robs.getCode().getDisplayName());

          ResourceReferenceDt result = diagnosticReport.addResult();
          result.setResource(obs);
          if (StringUtils.isNotEmpty(robs.getCode().getCode()) && StringUtils
              .isNotEmpty(result_value)) {

            diagnosticReports.add(diagnosticReport);
          }
        }
      }
    } catch (Exception ex) {
      System.out.println("Result Parsing Exception:");
      ex.printStackTrace();
    }
    return diagnosticReports;
  }

}
