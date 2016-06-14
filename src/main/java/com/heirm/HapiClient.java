package com.heirm;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.valueset.ObservationRelationshipTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import com.heirm.CDAParse.CDAParser;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import org.hl7.fhir.dstu3.model.CodeableConcept;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by murrayg on 4/7/2016.
 */
public class HapiClient {

  public static void main(String[] args) throws IOException {
    // Create a client (only needed once)
    FhirContext ctx = new FhirContext();
    IGenericClient client =
        ctx.newRestfulGenericClient("http://localhost:8080/hapi-fhir-jpaserver-example/baseDstu2/");

    // Load patient from xml CDA file into MDHT CDA object
// TOGGLE between visit summary and pt summary
String str = readFileAsString("c:/tmp/gwm-visit-20160208.xml");


   // String str = readFileAsString("c:/tmp/gwm.xml");

    InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));

    //System.out.println("CCDA XML"+ccda_xml);

    //Create a cdaParser
    CDAParser cdaParser = new CDAParser(is);





    // Create patient resource FHIR object for new Patient
    //Patient pt = new Patient();

    // Get existing patient resource FHIR object( demographics )
    Patient pt = client.read().resource(Patient.class).withId("52").execute();

    String patient_logical_id =  pt.getId().getIdPart();
    String patient_business_id 	= pt.getIdentifierFirstRep().getValue();


    List<MedicationStatement> meds = new ArrayList<MedicationStatement>();
    List<Condition> conditions = new ArrayList<Condition>();
    List<Observation> vitalSigns = new ArrayList<Observation>();
    List<DiagnosticReport> diagnosticReports = new ArrayList<DiagnosticReport>();


   // For new pt, get demographics from source document
    //pt = cdaParser.getDemographics(pt);
    vitalSigns = cdaParser.getVitals(vitalSigns);

    // For Epic Sutter Visit Summaries only. This section does not exist in TOC Summary
    // Assuming Vitals and Encounters have a 1 to 1 relationship, create new Encounter FHIR resource here
    // Vitals fed from machines, such as fit bits, will not have encounters.
    // Need to research how this is dealt with in FHIR
    // COMMENT OUT when not creating new Encounter. TO DO - Create EncounterParser
/*Encounter encounter = new Encounter();
    encounter.setPatient(new ResourceReferenceDt("Patient/52"));
    PeriodDt pd = new PeriodDt();
    pd.setStart(new DateTimeDt("2016-02-11T22:00:00Z"));
    pd.setEnd(new DateTimeDt("2016-02-11T22:45:00Z"));
    encounter.setPeriod(pd);
    encounter.setServiceProvider(new ResourceReferenceDt("Organization/53"));

     try {
     MethodOutcome outcome = client
         .create()
         .resource(encounter)
         .execute();
   } catch (PreconditionFailedException e) {
     // If we get here, the latest version has changed
     // on the server so our update failed.
   }
*/


    meds = cdaParser.getMedications(meds);
    conditions = cdaParser.getProblems(conditions);
    diagnosticReports = cdaParser.getResults(diagnosticReports);


   //Get all sections
    HashMap record = new HashMap();

    //record.put("medications", cdaParser.getMedications());
    //record.put("results", cdaParser.getResults());

    //  record.put("allergies", cdaParser.getAllergies());

    //record.put("problems", cdaParser.getProblems());

    //TOGGLE OFF when processing pt summary
 //record.put("vitalsigns", cdaParser.getVitals());

    //record.put("procedures", cdaParser.getProcedures());


    System.out.println("demographics patient business ID - " + patient_business_id);
    System.out.println("demographics patient logical ID - " + patient_logical_id);
    System.out.println("demographics last name - " + pt.getName().get(0).getFamily().get(0));
    System.out.println("demographics first name - " + pt.getName().get(0).getGiven().get(0));
    System.out.println("demographics birth date - " + pt.getBirthDate().toString());
    System.out.println("demographics address line - " + pt.getAddressFirstRep().getLineFirstRep());
    System.out.println("demographics address city - " + pt.getAddressFirstRep().getCity());
    System.out.println("demographics address state - " + pt.getAddressFirstRep().getState());
    System.out
        .println("demographics address country - " + pt.getAddressFirstRep().getPostalCode());

    System.out
        .println("marital status code - " + pt.getMaritalStatus().getCodingFirstRep().getCode());
    System.out.println(
        "language - " + pt.getCommunicationFirstRep().getLanguage().getCodingFirstRep().getCode());
    System.out.println("gender - " + pt.getGender().toString());

    // This section is for processing an Epic Sutter pt ambulatory transition of care summary
    // Current Medications, Current Problems and all lab results are processed

// current medications
    for (MedicationStatement med : meds) {

      CodeableConceptDt ccdt = (CodeableConceptDt) med.getMedication();
  //    System.out.println("medication1 code = " + ccdt.getCodingFirstRep().getCode());
  //    System.out.println("medication1 status = " + med.getStatus());

    }

    // current problems ( medical conditions )
    for (Condition cons : conditions){
//      System.out
//         .println("problems code = " + cons.getCode().getCodingFirstRep().getCode());
      cons.setPatient(new ResourceReferenceDt("Patient/" + patient_logical_id));
    /*
     try {
     MethodOutcome outcome = client
         .create()
         .resource(cons)
         .execute();
   } catch (PreconditionFailedException e) {
     // If we get here, the latest version has changed
     // on the server so our update failed.
   }
    */

    }

// lab results
    for (DiagnosticReport diagrep : diagnosticReports) {
  diagrep.setSubject(new ResourceReferenceDt("Patient/" + patient_logical_id));
//      System.out.println("result name = " + diagrep.getCode().getCodingFirstRep().getDisplay());
//      System.out.println("result date = " + diagrep.getEffective().toString());
      /*
     try {
     MethodOutcome outcome = client
         .create()
         .resource(diagrep)
         .execute();
   } catch (PreconditionFailedException e) {
     // If we get here, the latest version has changed
     // on the server so our update failed.
   }

    */
    }

    // This section is for entering vital signs from  a pt visit summary.
    // Vitals entered as observations using proposed standards for vitals in DSTU 3.

    // First, create Vital Sign group observation
Observation VitalSignSet = new Observation();
    VitalSignSet.setSubject(new ResourceReferenceDt("Patient/" + patient_logical_id));
    VitalSignSet.setEncounter(new ResourceReferenceDt("Encounter/325"));
    VitalSignSet.setStatus(ObservationStatusEnum.FINAL);
    VitalSignSet.setCategory(new CodeableConceptDt("http://hl7.org/fhir/observation-category","vital-signs"));
    VitalSignSet.getCategory().setText("Vital Signs");
    VitalSignSet.getCategory().getCodingFirstRep().setDisplay("Vital Signs");
    VitalSignSet.setCode(new CodeableConceptDt("http://loinc.org","8716-3"));
    VitalSignSet.getCode().getCodingFirstRep().setDisplay("Vital signs");
    VitalSignSet.setEffective(new DateTimeDt("2016-02-11T22:10:00Z"));

    // Now add related links for each vital sign

    for (Observation vitalSign : vitalSigns) {


      vitalSign.setSubject(new ResourceReferenceDt("Patient/" + patient_logical_id));
      vitalSign.setEncounter(new ResourceReferenceDt("Encounter/325"));
      vitalSign.setStatus(ObservationStatusEnum.FINAL);
      vitalSign.setCategory(
          new CodeableConceptDt("http://hl7.org/fhir/observation-category", "vital-signs"));
      vitalSign.getCategory().setText("Vital Signs");
      vitalSign.getCategory().getCodingFirstRep().setDisplay("Vital Signs");

      // Snippets for creating / updating patients

      MethodOutcome outcome = new MethodOutcome();
      try {
        outcome = client.create().resource(vitalSign).execute();

      } catch (PreconditionFailedException e) {
        // If we get here, the latest version has changed
        // on the server so our update failed.
      }
      VitalSignSet.addRelated().setType(ObservationRelationshipTypeEnum.HAS_MEMBER).setTarget(new ResourceReferenceDt(outcome.getId().getValue()));
    }
    try {
      MethodOutcome outcome = client.create().resource(VitalSignSet).execute();

    } catch (PreconditionFailedException e) {
      // If we get here, the latest version has changed
      // on the server so our update failed.
    }

















    // Update existing Patient  #1


    // If the server is a version aware server, we should now know the latest version
    // of the resource
    //  System.out.println("Version ID: " + pt.getId().getVersionIdPart());
    // Now let's make a change to the resource



    // Invoke the server update method - Because the resource has
    // a version, it will be included in the request sent to
    // the server
 /*   try {
      MethodOutcome outcome = client
          .update()
          .resource(pt)
          .execute();
    } catch (PreconditionFailedException e) {
      // If we get here, the latest version has changed
      // on the server so our update failed.
    }
*/


    // Random client bundle search
    // Invoke the client
    //    Bundle bundle = client.search().forResource(Patient.class)
    //        .prettyPrint()
    //        .encodedJson()
    //        .execute();
    //bundle.getEntries().get(0).getResource().getResourceName();
    // System.out.println(bundle.getEntries().get(0).getResource().getResourceName());
  }

  private static String readFileAsString(String filePath) throws IOException {
    StringBuffer fileData = new StringBuffer();
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    char[] buf = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(buf)) != -1) {
      String readData = String.valueOf(buf, 0, numRead);
      fileData.append(readData);
    }
    reader.close();
    return fileData.toString();
  }


}
