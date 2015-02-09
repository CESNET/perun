/**
 * Created by ondrej on 13.1.15.
 */

var voShortName = "provozmeta";
var resourcesCount = 0;
var resourcesCurrent = 0;
var hostsFriendly = [];


function entryPoint(user) {
    callPerun("vosManager", "getVoByShortName", {shortName: voShortName}, function(vo) {
        callPerun("membersManager", "getMemberByUser", {vo: vo.id, user: user.id}, function(member) {
            loadResources(member);
        });
    });
}

function loadResources(member) {
    callPerun("resourcesManager", "getAllowedResources", {member: member.id}, function(resources) {
        resourcesCount = resources.length;
        addProgressBar(1);
        loadFacilities(resources);
    });
}

function loadFacilities(resources) {
    for(var i in resources) {
        callPerun("resourcesManager", "getFacility", {resource: resources[i].id}, function(facility) {
            addProgressBar(10/resourcesCount);
            $.when(loadHosts(facility), loadOwners(facility)).done(function(hosts, owners){
                addProgressBar(10/resourcesCount);
                loadHostsAttrs(hosts[0], getTechnicalOwners(owners[0]));
            });
        });
    }
}



function loadHostsAttrs(hosts, owners) {
    if (hosts.length == 0) {
        resourcesCurrent++;
        addProgressBar(80/resourcesCount);
        return;
    }
    var hostsCount = hosts.length;
    var hostsCurrent = 0;
    for(var i in hosts) {
        callPerun("attributesManager", "getAttributes", {host: hosts[i].id}, success(hosts[i]));
    }
    function success(host) {
        return function (attrs) {
            var isMonitored = false;
            var isManaged = false;
            for (var n in attrs) {
                if (attrs[n].friendlyName == "metaIsMonitored") {
                    if (attrs[n].value == 1) {
                        isMonitored = true;
                    }
                }
                if (attrs[n].friendlyName == "metaIsManaged") {
                    if (attrs[n].value == 1) {
                        isManaged = true;
                    }
                }
            }
            hostsFriendly.push({name: host.hostname, owners: owners, isMonitored: isMonitored, isManaged: isManaged});
            hostsCurrent++;
            addProgressBar((80/resourcesCount)/hostsCount);
            if (hostsCurrent == hostsCount) {
                resourcesCurrent++;
                if (resourcesCurrent == resourcesCount) {
                    fillHosts(hostsFriendly);
                }
            }
        }
    }
}



function loadHosts(facility) {
    return callPerun("facilitiesManager", "getHosts", {facility: facility.id});
}
function loadOwners(facility) {
    return callPerun("facilitiesManager", "getOwners", {facility: facility.id});
}


function fillHosts(hosts) {
    hosts.sort(function(a,b) { return a.name.localeCompare(b.name); });
    var hostsTable = new PerunTable();
    hostsTable.addColumn({type: "text", title: "Host name", name: "name"});
    hostsTable.addColumn({type: "list", title: "Owners", name: "owners"});
    hostsTable.addColumn({type: "boolean", title: "Monitored", name: "isMonitored"});
    hostsTable.addColumn({type: "boolean", title: "Managed", name: "isManaged"});
    hostsTable.setValues(hosts);
    $("#hosts-table").html(hostsTable.draw());
}


function getTechnicalOwners(owners) {
    var technics = [];
    for(var i in owners) {
        if (owners[i].type == "technical") {
            technics.push(owners[i].name);
        }
    }
    return technics;
}

var progress = 0;
var progressBar = $('#hosts-table').find('.progress-bar');
function addProgressBar(value) {
    progress += value;
    var showProgress = Math.floor(progress);
    if (progressBar.attr('aria-valuenow') != showProgress) {
        progressBar.css('width', showProgress+'%').attr('aria-valuenow', showProgress).text(showProgress+'%');
    }
}