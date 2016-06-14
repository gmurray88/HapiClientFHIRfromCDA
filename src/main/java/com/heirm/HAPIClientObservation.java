package com.heirm;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.*;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.ObservationStatusEnum;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.model.primitive.IdDt;

import java.util.Date;



/**
 * Created by murrayg on 4/14/2016.
 */
public class HAPIClientObservation {
  public static void main(String[] args) {
    /*
    // Create a client (only needed once)
    FhirContext ctx = new FhirContext();
    IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/hapi-fhir-jpaserver-example/baseDstu2/");

    Patient patient = client.read().resource(Patient.class).withId("1").execute();

    // Create an Observation instance
    Observation observation = new Observation();

    // Assign patient reference

    observation.setSubject(new ResourceReferenceDt(patient.getId().getValue()));
    // Give the observation a status
    observation.setStatus(ObservationStatusEnum.FINAL);

    // Give the observation a code (what kind of observation is this)
    CodingDt coding = observation.getCode().addCoding();
    coding.setCode("29463-7").setSystem("http://loinc.org").setDisplay("Body Weight");

    // Create a quantity datatype
    QuantityDt value = new QuantityDt();
    value.setValue(205).setSystem("http://unitsofmeasure.org").setCode("lb");
    observation.setValue(value);

    // Set the reference range
    SimpleQuantityDt low = new SimpleQuantityDt();
    low.setValue(110).setSystem("http://unitsofmeasure.org").setCode("lb");
    observation.getReferenceRangeFirstRep().setLow(low);
    SimpleQuantityDt high = new SimpleQuantityDt();
    low.setValue(250).setSystem("http://unitsofmeasure.org").setCode("lb");
    observation.getReferenceRangeFirstRep().setHigh(high);

    observation.setStatus(ObservationStatusEnum.REGISTERED);
    observation.setCategory(new CodeableConceptDt("observation-category","vital-signs"));
    observation.setIssued(new InstantDt(new Date()));

    // Invoke the server update method - Because the resource has
    // a version, it will be included in the request sent to
    // the server
try {
      MethodOutcome outcome = client
          .create()
          .resource(observation)
          .execute();
    } catch (PreconditionFailedException e) {
      // If we get here, the latest version has changed
      // on the server so our update failed.
    }



   */
  }
}