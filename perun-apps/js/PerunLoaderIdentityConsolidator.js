// Empty initial perunSession object
var perunSession = {};

$(document).ready(function(){

    // Show errors
     var ex = getURLParameter('ex');
     var exId = getURLParameter('exId');
     var target = getURLParameter('target');

     if (ex != "null") {
      $("#message").css('display','block');

     if (ex == "OK") {
	$("#message").addClass('alert-success');
	$("#message").html("Your identities have been successfully joined. You can continue to <a href='" + target + "'>here</a>."); 
     } else if (ex == "UserExtSourceExistsException") {
        $("#message").addClass('alert-info');
        $("#message").html("<strong>Info:</strong> Your identities have been already joined. You can continue to <a href='" + target + "'>here</a>."); 
     } else  {
        if (exId != "null") {
          $("#message").html("<strong>Error occured</strong>: " + ex + " (ErrorId: " + exId + "). Report to perun@cesnet.cz."); 
        } else {
          $("#message").html("<strong>Error occured</strong>: " + ex + ". Report to perun@cesnet.cz."); 
        }
        $("#message").addClass('alert-error');
      }

      $("#main").css('display','none');
     }

     // Get PerunSession
     callPerunSync("authzResolver", "getPerunPrincipal", perunSession);

      var orgs = [];
      orgs["https://idp.upce.cz/idp/shibboleth"] = "University in Pardubice";
      orgs["https://idp.slu.cz/idp/shibboleth"] = "University in Opava";
      orgs["https://login.feld.cvut.cz/idp/shibboleth"] = "Faculty of Electrical Engineering, Czech Technical University In Prague";            
      orgs["https://www.vutbr.cz/SSO/saml2/idp"] = "Brno University of Technology";
      orgs["https://shibboleth.nkp.cz/idp/shibboleth"] = "The National Library of the Czech Republic";
      orgs["https://idp2.civ.cvut.cz/idp/shibboleth"] = "Czech Technical University In Prague";
      orgs["https://shibbo.tul.cz/idp/shibboleth"] = "Technical University of Liberec";
      orgs["https://idp.mendelu.cz/idp/shibboleth"] = "Mendel University in Brno";
      orgs["https://cas.cuni.cz/idp/shibboleth"] = "Charles University in Prague";
      orgs["https://wsso.vscht.cz/idp/shibboleth"] = "Institute of Chemical Technology Prague";
      orgs["https://idp.vsb.cz/idp/shibboleth"] = "VSB – Technical University of Ostrava";
      orgs["https://whoami.cesnet.cz/idp/shibboleth"] = "CESNET";
      orgs["https://helium.jcu.cz/idp/shibboleth"] = "University of South Bohemia";
      orgs["https://idp.ujep.cz/idp/shibboleth"] = "Jan Evangelista Purkyne University in Usti nad Labem";
      orgs["https://idp.amu.cz/idp/shibboleth"] = "Academy of Performing Arts in Prague";
      orgs["https://idp.lib.cas.cz/idp/shibboleth"] = "Academy of Sciences Library";
      orgs["https://shibboleth.mzk.cz/simplesaml/metadata.xml"] = "Moravian  Library";
      orgs["https://idp2.ics.muni.cz/idp/shibboleth"] = "Masaryk University";
      orgs["https://idp.upol.cz/idp/shibboleth"] = "Palacky University, Olomouc";
      orgs["https://idp.fnplzen.cz/idp/shibboleth"] = "FN Plzen";
      orgs["https://id.vse.cz/idp/shibboleth"] = "University of Economics, Prague";
      orgs["https://shib.zcu.cz/idp/shibboleth"] = "University of West Bohemia";
      orgs["https://idptoo.osu.cz/simplesaml/saml2/idp/metadata.php"] = "University of Ostrava";
      orgs["https://login.ics.muni.cz/idp/shibboleth"] = "MetaCentrum";
      orgs["https://idp.hostel.eduid.cz/idp/shibboleth"] = "eduID.cz Hostel";
      orgs["https://shibboleth.techlib.cz/idp/shibboleth"] = "National Library of Technology";

        // Fill basic info about the user
     $("#user-login").text(perunSession.actor);
     $("#user-organization").text(orgs[perunSession.extSourceName]);
});
