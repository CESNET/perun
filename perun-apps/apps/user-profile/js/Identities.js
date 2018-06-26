/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
$(document).ready(function () {
    $("#identitiesLink").click(function () {
        loadIdentities(user);
    });
});

function loadIdentities(user) {
    if (!user) {
        (flowMessager.newMessage("Identities", "can't be loaded because user isn't loaded", "danger")).draw();
        return;
    }
    var loadImage = new LoadImage($("#federations-table, #certificates-table"), "auto");

    callPerun("usersManager", "getUserExtSources", {user: user.id}, function (extSources) {
        if (!extSources) {
            (flowMessager.newMessage("Identities", "can't be loaded", "danger")).draw();
            return;
        }
        fillExtSources(extSources);
        loadImage.hide();
        //(flowMessager.newMessage("Identities","was loaded successfully.","success")).draw();
    });
}

function fillExtSources(extSources) {
    if (!extSources) {
        (flowMessager.newMessage("Identities", "can't be fill", "danger")).draw();
        return;
    }

    var federations = [];
    var certificates = [];
    for (var extSourcesId in extSources) {
        switch (extSources[extSourcesId].extSource.type) {
            case "cz.metacentrum.perun.core.impl.ExtSourceIdp":
                federations.push(extSources[extSourcesId]);
                break;
            case "cz.metacentrum.perun.core.impl.ExtSourceX509":
                certificates.push(extSources[extSourcesId]);
                break;
        }
    }
    fillFederations(federations);
    fillCertificates(certificates);
}

function fillFederations(federations) {
    if (!federations) {
        (flowMessager.newMessage("Federations", "can't be filled", "danger")).draw();
        return;
    }

    var federationsFriendly = [];
    for (var id in federations) {
        federationsFriendly[id] = {};
        federationsFriendly[id]["id"] = federations[id].id;
        federationsFriendly[id]["name"] = federations[id].extSource.name;   //default
        federationsFriendly[id]["login"] = federations[id].login;           //default
        if (federations[id].extSource.name.split("/")[2] == 'extidp.cesnet.cz') {   //if is extIdp
            var login = federations[id].login.split('@')[0];
            for (var s in social) {
                if (federations[id].login.search(s) >= 0) {
                    federationsFriendly[id]["name"] = social[s];
                    federationsFriendly[id]["login"] = login;
                }
            }
        } else {
            var orgsName = orgs[federations[id].extSource.name];
            if (orgsName) {
                federationsFriendly[id]["name"] = orgsName;
            }
        }
    }

    var federationsTable = new PerunTable();
    federationsTable.addColumn({type: "number", title: "#"});
    federationsTable.addColumn({type: "text", title: "Federated identities", name: "name"});
    federationsTable.addColumn({type: "text", title: "Login", name: "login"});
    federationsTable.addColumn({type: "button", title:"", btnText:"remove", btnType:"danger", btnId:"id", btnName:"removeFed"});
    federationsTable.setValues(federationsFriendly);
    $("#federations-table").html(federationsTable.draw());

    $('#federations-table button[id^="removeFed-"]').click(function() {

        var response = confirm("WARNING: If you remove your identity, some services might not work for you anymore. Do not remove it, if you don't know what you are doing or you are unable to add it later again (you can't login-in by that identity).");

        if (response == true) {

	        var fedId = parseInt(this.id.split('-')[1]);
	        var loadImage = new LoadImage($("#federations-table"), "auto");

	        callPerunPost("usersManager", "removeUserExtSource", {user: user.id, userExtSource: fedId}, function () {
		        loadIdentities(user);
		        loadImage.hide();
		        (flowMessager.newMessage("Federated identity", "was removed successfully", "success")).draw();
	        }, function(perunError) {
		        loadIdentities(user);
		        loadImage.hide();
		        (flowMessager.newMessage("Federated identity", "was not removed. " + perunError.message, "danger")).draw();
	        });

        }

    });
}

function fillCertificates(certificates) {
    if (!certificates) {
        (flowMessager.newMessage("Certificates", "can't be fill", "danger")).draw();
        return;
    }

    var certificatesFriendly = [];
    for (var id in certificates) {
        certificatesFriendly[id] = {};
        certificatesFriendly[id]["id"] = certificates[id].id;
        certificatesFriendly[id]["name"] = certificates[id].extSource.name;
        var login = decodeURIComponent((certificates[id].login).replace(/\\x/g, '%'));
        certificatesFriendly[id]["login"] = login;
    }

    var certificatesTable = new PerunTable();
    certificatesTable.addColumn({type: "number", title: "#"});
    certificatesTable.addColumn({type: "text", title: "Issuer", name: "name"});
    certificatesTable.addColumn({type: "text", title: "Identity", name: "login"});
    certificatesTable.addColumn({type: "button", title:"", btnText:"remove", btnType:"danger", btnId:"id", btnName:"removeCert"});
    certificatesTable.setValues(certificatesFriendly);
    $("#certificates-table").html(certificatesTable.draw());

    $('#certificates-table button[id^="removeCert-"]').click(function() {
        var certId = parseInt(this.id.split('-')[1]);
        var loadImage = new LoadImage($("#certificates-table"), "auto");

        callPerunPost("usersManager", "removeUserExtSource", {user: user.id, userExtSource: certId}, function () {
            loadIdentities(user);
            loadImage.hide();
            (flowMessager.newMessage("Certificate", "was removed successfully", "success")).draw();
        });
    });
}


var social = [];
social["@google.extidp.cesnet.cz"] = "Google";
social["@facebook.extidp.cesnet.cz"] = "Facebook";
social["@mojeid.extidp.cesnet.cz"] = "mojeID";
social["@linkedin.extidp.cesnet.cz"] = "LinkedIn";
social["@twitter.extidp.cesnet.cz"] = "Twitter";
social["@seznam.extidp.cesnet.cz"] = "Seznam";
social["@github.extidp.cesnet.cz"] = "Github";

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
orgs["https://www.egi.eu/idp/shibboleth"] = "EGI SSO";
orgs["https://engine.elixir-czech.org/authentication/idp/metadata"] = "ELIXIR ID";
