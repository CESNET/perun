/**
 * Created by ondrej on 13.1.15.
 */

function entryPoint(user) {
    console.log(roles);
    if (roles.FACILITYADMIN || roles.PERUNADMIN) {
        loadFacilities();
    } else {
        (staticMessager.newMessage("Privilege required", "Aplication is intended for facility admins.", "warning")).draw();
    }
}

function loadFacilities() {
    var loadImage = new LoadImage($('#facilities-table'), "64px");
    callPerun("facilitiesManager", "getRichFacilities", {}, function (facilities) {
        var facilitiesFriendly = [];
        for (var i in facilities) {
            var owners = getTechnicalOwners(facilities[i].facilityOwners);
            facilitiesFriendly.push({id:facilities[i].id, name:facilities[i].name, owners:owners ,
                description:(facilities[i].description ? facilities[i].description : "-"),
                monitored:"glyphicon glyphicon-chevron-down", managed:"glyphicon glyphicon-chevron-down" });
        }
        loadImage.hide();
        fillFacilities(facilitiesFriendly);
    });
}


function fillFacilities(facilities) {

    facilities.sort(function (a, b) {
        return a.name.localeCompare(b.name);
    });
    /*var facilitiesTables = [];
    for(var i in facilities) {
        var hostsTable = new PerunTable();
        hostsTable.addColumn({type: "text", title: "Facility name", name: "name"});
        hostsTable.addColumn({type: "list", title: "Owners", name: "owners"});
        hostsTable.addColumn({type: "boolean", title: "Monitored", name: "monitored"});
        hostsTable.addColumn({type: "boolean", title: "Managed", name: "managed"});
        hostsTable.setHasHeader(false);
        hostsTable.addValue(facilities[i]);
        facilitiesTables.push({table: hostsTable});
    }*/

    var facilitiesTable = new PerunTable();
    facilitiesTable.setClicableRows({isClicable: true, id: "id", prefix: "facility-", toggle: true});
    facilitiesTable.addColumn({type: "text", title: "Facility name", name: "name"});
    facilitiesTable.addColumn({type: "list", title: "Owners", name: "owners"});
    facilitiesTable.addColumn({type: "text", title: "Description", name: "description"});
    facilitiesTable.addColumn({type: "icon", title: "Monitored", name: "monitored"});
    facilitiesTable.addColumn({type: "icon", title: "Managed", name: "managed"});
    facilitiesTable.setValues(facilities);

    $("#facilities-table").html(facilitiesTable.draw());

    $("#facilities-table").find("table tbody > tr[id^=facility-]").each(function (i) {
        loadHosts($(this).attr("id").split("-")[1]);
        var toggle = $("#facilities-table").find("table tbody tr[id=toggle-"+$(this).attr("id")+"] .toggle-wrap");
        toggle.show();
        $(this).click(function() {
            toggle.slideToggle(150);
        });
    });
}


function loadHosts(facility) {
    callPerun("facilitiesManager", "getHosts", {facility: facility}, function(hosts) {
        setProgressBar(20,$("#toggle-facility-" + facility + " .progress-bar"));
        loadHostsAttrs(hosts, facility);
    });
}

function loadHostsAttrs(hosts, facility) {
    if (hosts.length == 0) {
        fillHosts([], facility);
        return;
    }
    var hostsCount = hosts.length;
    var hostsCurrent = 0;
    var hostsFriendly = [];
    for (var i in hosts) {
        //console.log(facilities[i]);
        var monitoredFnc = callPerun("attributesManager", "getAttribute",
            {host: hosts[i].id, attributeName: "urn:perun:host:attribute-def:opt:metaIsMonitored"});
        var managedFnc = callPerun("attributesManager", "getAttribute",
            {host: hosts[i].id, attributeName: "urn:perun:host:attribute-def:opt:metaIsManaged"});
        $.when(monitoredFnc, managedFnc).done(success(hosts[i]));
    }
    function success(host) {
        return function (monitored, managed) {
            var mon = false;
            var man = false;
            if (monitored[0].value == 1) {
                mon = true;
            }
            if (managed[0].value == 1) {
                man = true;
            }
            hostsFriendly.push({id:host.id, name:host.hostname, monitored:mon, managed:man, facility:facility});
            hostsCurrent++;
            setProgressBar(20+hostsCurrent*(80/hostsCount),$("#toggle-facility-" + facility + " .progress-bar"))
            if (hostsCurrent == hostsCount) {
                //console.log(hostsFriendly);
                fillHosts(hostsFriendly, facility);
            }
        }
    }
}

function fillHosts(hosts, facility) {
    if (hosts.length == 0) {
        $("tr#toggle-facility-" + facility + " td .toggle-wrap").html("no hosts");
        return;
    }
    hosts.sort(function (a, b) {
        return a.name.localeCompare(b.name);
    });
    var hostsTable = new PerunTable();
    hostsTable.addColumn({type: "text", title: "Host name", name: "name"});
    hostsTable.addColumn({type: "boolean", title: "Monitored", name: "monitored"});
    hostsTable.addColumn({type: "boolean", title: "Managed", name: "managed"});
    hostsTable.setValues(hosts);
    hostsTable.setHasHeader(false);
    $("tr#toggle-facility-" + facility + " td .toggle-wrap").html(hostsTable.draw());
}


function getTechnicalOwners(owners) {
    var technics = [];
    for (var i in owners) {
        if (owners[i].type == "technical") {
            technics.push(owners[i].name);
        }
    }
    return technics;
}


function setProgressBar(value, bar) {
    var showProgress = Math.floor(value);
    if (bar.attr('aria-valuenow') != showProgress) {
        bar.css('width', showProgress + '%').attr('aria-valuenow', showProgress).text("Loading " + showProgress + '%');
    }
}