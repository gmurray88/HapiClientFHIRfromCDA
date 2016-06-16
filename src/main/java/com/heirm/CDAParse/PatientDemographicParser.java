package com.heirm.CDAParse;


import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.MaritalStatusCodesEnum;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.StringDt;
import org.openhealthtools.mdht.uml.cda.RecordTarget;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;

public class PatientDemographicParser {

  RecordTarget rt = null;

  public PatientDemographicParser(RecordTarget rt) {
    this.rt = rt;
  }

  public ca.uhn.fhir.model.dstu2.resource.Patient parse(ca.uhn.fhir.model.dstu2.resource.Patient patientFHIR) {

    try {


      org.openhealthtools.mdht.uml.cda.Patient patientCDA = rt.getPatientRole().getPatient();
      // Patient ID - get EPIC mrn for now. SMF mrn is stored in element(0).
        patientFHIR.addIdentifier().setValue(rt.getPatientRole().getIds().get(1).getExtension());

      // Patient name
      for (PN pname : patientCDA.getNames()) {
        if (pname.getGivens().size() > 0 && pname.getFamilies().size() > 0) {
          //        System.out.println("family name - " + pname.getFamilies().get(0).toString());
          //        System.out.println("given name - " + pname.getGivens().get(0).toString());
          patientFHIR.addName().addFamily(pname.getFamilies().get(0).getText())
              .addGiven(pname.getGivens().get(0).getText());
        }
      }

      // Patient birth date
      if (patientCDA.getBirthTime() != null) {
        patientFHIR.setBirthDate(new DateDt(patientCDA.getBirthTime().getValue()));
      }

      // Gender Code
      String gender = "";
      CE gcode = patientCDA.getAdministrativeGenderCode();
      if (gcode != null) {
        gender = gcode.getCode();
        if ("M".equals(gender)) {
          patientFHIR.setGender(AdministrativeGenderEnum.MALE);
        } else {
          if ("F".equals(gender)) {
            patientFHIR.setGender(AdministrativeGenderEnum.FEMALE);
          } else {
            if ("UN".equals(gender)) {
              patientFHIR.setGender(AdministrativeGenderEnum.OTHER);
            } else {
              patientFHIR.setGender(AdministrativeGenderEnum.UNKNOWN);
            }
          }
        }
      } else {
        patientFHIR.setGender(AdministrativeGenderEnum.UNKNOWN);
      }
     // Marital Status skipped. Epic sends non-standard values.
      CE mcode = patientCDA.getMaritalStatusCode();
      if (mcode != null) {
        CodingDt maritalStatusCoding = patientFHIR.getMaritalStatus().addCoding();
        maritalStatusCoding.setSystem("EpicV8-1/MaritalStatusCodes");
        maritalStatusCoding.setCode(mcode.getCode());
        maritalStatusCoding.setDisplay(mcode.getDisplayName());
      }

    // Languages
        String languages = "";
        for (org.openhealthtools.mdht.uml.cda.LanguageCommunication lc : patientCDA
            .getLanguageCommunications()) {
          CodingDt languageCoding = patientFHIR.addCommunication().getLanguage().addCoding();
          languageCoding.setSystem("EpicV8-1/LanguageCodes");
          languageCoding.setCode(lc.getLanguageCode().getCode());
          languageCoding.setDisplay(lc.getLanguageCode().getDisplayName());
       }

/*
      if(recordTarget.getPatientRole().getProviderOrganization()!=null)
        provider_org = recordTarget.getPatientRole().getProviderOrganization().getNames().get(0).getText();
*/
      // Addresses

        for (AD ad : rt.getPatientRole().getAddrs()) {
          AddressDt ptAddress = patientFHIR.addAddress();
          if (ad.getStreetAddressLines().size() > 0)
            ptAddress.addLine(ad.getStreetAddressLines().get(0).getText());

          if (ad.getCities().size() > 0)
            ptAddress.setCity(ad.getCities().get(0).getText());

          if (ad.getStates().size() > 0)
            ptAddress.setState(ad.getStates().get(0).getText());

          if (ad.getCountries().size() > 0)
            ptAddress.setCountry(ad.getCountries().get(0).getText());

          if (ad.getPostalCodes().size() > 0)
            ptAddress.setPostalCode(ad.getPostalCodes().get(0).getText());
        }

      /* get provider
      move to separate parser later
      String provider_last_name = "", provider_first_name =  "";

      EList<PN> names= cd.getDocumentationOfs().get(0).getServiceEvent().getPerformers().get(0).getAssignedEntity().getAssignedPerson().getNames();
      if (names.get(0).getGivens().size()>0){
        provider_first_name = ((ENXP)names.get(0).getGivens().get(0)).getText();
        provider_last_name = ((ENXP)names.get(0).getFamilies().get(0)).getText();
      }
      */
        //get mrn
/*
      id = rt.getPatientRole().getIds().get(0).getExtension();
      ptInfo.put("id", id);
      ptInfo.put("first", first.trim());
      ptInfo.put("last", last.trim());
      ptInfo.put("gender", gender);
      ptInfo.put("birth_time", birthTime.trim());
      ptInfo.put("marital_status", marital_status.trim());
      ptInfo.put("provider_org", provider_org.trim() );
      ptInfo.put("provider_first", provider_first_name.trim());
      ptInfo.put("provider_last", provider_last_name.trim());
      ptInfo.put("languages", languages);
      ptInfo.put("street", street.trim());
      ptInfo.put("city", city.trim() );
      ptInfo.put("state", state.trim());
      ptInfo.put("country", country.trim() );
      */
      }catch(Exception e){
        e.printStackTrace();
      }

      return patientFHIR;
    }
  }
