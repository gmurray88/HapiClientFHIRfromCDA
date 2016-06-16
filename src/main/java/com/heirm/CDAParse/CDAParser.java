package com.heirm.CDAParse;

import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.consol.ProceduresSection;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.hitsp.DiagnosticResultsSection;
import org.openhealthtools.mdht.uml.cda.hitsp.PatientSummary;
import org.openhealthtools.mdht.uml.cda.mu2consol.ClinicalOfficeVisitSummary;
import org.openhealthtools.mdht.uml.cda.mu2consol.TransitionOfCareAmbulatorySummary;
import org.openhealthtools.mdht.uml.cda.mu2consol.Mu2consolPackage;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by murrayg on 4/28/2016.
 */
public class CDAParser {

  // TOGGLE between Pt Summary and Visit Summary
public TransitionOfCareAmbulatorySummary covs			= null; //for Mu2 C-CDA
 //public ClinicalOfficeVisitSummary covs = null; //for Mu2 C-CDA
  //Initialize the document variables
  ClinicalDocument cd = null;
  MedicationsSection medicationsSection = null;
  ProblemSection problemSection = null;
  AllergiesSection allergySection = null;
  ResultsSection resultsSection = null;
  DiagnosticResultsSection diagnosticResultsSection = null;
  ProceduresSection proceduresSection = null;
  PatientSummary ps = null; //for HITSP 83
  ContinuityOfCareDocument ccd = null; //for HITSP 32
  Section VitalSection = null;

  /**
   * Constructor for the CDA Parser. Input stream can be TransitionOfCareAmbulatorySummary or
   * ClinicalOfficeVisitSummary
   *
   * @param cda_input
   */
  public CDAParser(InputStream cda_input) {


    Mu2consolPackage.eINSTANCE.eClass();

    ValidationResult result = new ValidationResult();

    try {

      // TOGGLE between Pt Summary and Visit Summary


      cd = CDAUtil
          .loadAs(cda_input, Mu2consolPackage.eINSTANCE.getTransitionOfCareAmbulatorySummary(),
              result);
  /*
      cd = CDAUtil
          .loadAs(cda_input, Mu2consolPackage.eINSTANCE.getClinicalOfficeVisitSummary(), result);
*/
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // TOGGLE between Pt Summary and Visit Summary
 // covs = (ClinicalOfficeVisitSummary) cd;
covs = (TransitionOfCareAmbulatorySummary)cd;
covs.getDocumentationOfs().get(0).getServiceEvent().getEffectiveTime().getHigh().getValue();
    for (Section sec : covs.getAllSections()) {
      String r = sec.getClass().getName();
      System.out.println("Results section name - " + r);
      if ("org.openhealthtools.mdht.uml.cda.consol.impl.VitalSignsSectionImpl".equals(r)) {
        VitalSection = sec;
             }
    }

    medicationsSection = covs.getMedicationsSection();
    problemSection = covs.getProblemSection();
    resultsSection = covs.getResultsSection();
    // TO DO -Allergies and Procedures

  }

  /**
   * Get Medications from the CDA
   *
   * @return ArrayList of parsed medications
   */
  public List<MedicationStatement> getMedications(List<MedicationStatement> medicationsFHIR) {
    return (new MedicationParser(medicationsSection)).parse(medicationsFHIR);
  }

   /**
   * Get results section from the CDA
   *
   * @return ArrayList of parsed results
   */
  public List<DiagnosticReport> getResults(List<DiagnosticReport> diagnosticReports) {
    return (new ResultParser(resultsSection)).parse(diagnosticReports);
  }

  /**
   * Get problems from the CDA
   *
   * @return ArrayList of parsed problems
   */
  public List<Condition> getProblems(List<Condition> conditions) {
    return (new ProblemParser(problemSection)).parse(conditions);
  }


  public List<Observation> getVitals(List<Observation> vitalSigns) {

    return (new VitalSignParser(VitalSection)).parse(vitalSigns);
  }


  /**
   * Get demographics from the CDA
   *
   * @return HashMap/Dictionary of Demographics
   */
  public Patient getDemographics(ca.uhn.fhir.model.dstu2.resource.Patient patientFHIR) {
    return (new PatientDemographicParser(covs.getRecordTargets().get(0))).parse(patientFHIR);
  }

}
