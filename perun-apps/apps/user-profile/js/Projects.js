/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

$(document).ready(function() {
    $("#projectsLink").click(function() {
        loadProjects(user);
    });
});


function loadProjects(user) {
    var loadImage = new LoadImage($("#projects-table"), "auto");
    callPerun("membersManager", "getMembersByUser", {user: user.id}, function(members) {
        //members.push({id: members[0].id, voId: 1121});
        var projectsNum = members.length;
        var projectsCurrent = 0;
        var projects = [];
        for (var i in members) {
            var loadVos = callPerun("vosManager", "getVoById", {id: members[i].voId});
            var loadExpiration = callPerun("attributesManager", "getAttribute",
                {member: members[i].id, attributeName: "urn:perun:member:attribute-def:def:membershipExpiration"});
            //var loadRules = callPerun("attributesManager", "getAttribute",
            //    {vo: members[i].voId, attributeName: "urn:perun:vo:attribute-def:def:membershipExpirationRules"});
            var loadExtend = callPerunPost("membersManager", "canExtendMembership", {member: members[i].id} )
            $.when(loadVos, loadExpiration, loadExtend).done(success(members[i]));
        }
        function success(member) {
            return function (vo, expiration, extend) {
                vo = vo[0];
                extend = extend[0];
                var expirationDate,stateBtn;
                if (expiration[0].value) {
                    expirationDate = new Date(expiration[0].value);
                    stateBtn = stateButton(expirationDate, extend, vo);
                } else {
                    expirationDate = "never";
                    stateBtn = null;
                }
                var project = {name: vo.name, expiration: expirationDate, state: stateBtn};
                projects.push(project);
                projectsCurrent++;
                if (projectsCurrent == projectsNum) {
                    loadImage.hide();
                    fillProjects(projects);
                }
            }
        }
    });
}

function stateButton(expiration, canExtend, vo) {
    var fnc = {fnc: extend, params: [vo.id, "'"+vo.shortName+"'"]};
    if (expiration instanceof Date) {
        if (expiration.getTime() < (new Date()).getTime()) {
            action = new TableButton(vo.id, "expired", "extend", "danger", fnc);
            return action;
        }
    }
    if (canExtend) {
        //var gracePeriod = rules.gracePeriod;
        action = new TableButton(vo.id, "gracePeriod", "extend", "warning", fnc);
        return action;
    }
    action = new TableButton(vo.id, "ok", "<i class='glyphicon glyphicon-ok'></i>", "success", fnc);
    action.setDisabled(true);
    return action;
}

function extend(voId, voShortName) {
    var loadImage = new LoadImage($("#projects-table .btn[id*='"+voId+"']"), "18px");
    callPerun("attributesManager", "getAttribute",
        {vo: voId, attributeName: "urn:perun:vo:attribute-def:def:registrarURL"}, function(url) {
            var regUrl = buildRegistrarUrl(url.value, voShortName);
            window.location.href = regUrl;
            loadImage.hide();
        });
}

function buildRegistrarUrl(attrUrl, voShortName) {
    var host, authz;
    if (attrUrl) {
        var parser = document.createElement('a');
        parser.href = attrUrl;
        host = parser.protocol + "//" + parser.host;
        if (parser.pathname.length <= 1) {  // true when pathname is "/"
            authz = getAuthz();
        } else {
            authz = parser.pathname.split("/")[1];
        }
    } else {
        host = location.protocol + "//" + location.host;
        if (voShortName == "meta") {
            authz = "fed"
        } else {
            authz = getAuthz();
        }
    }
    return host + "/" + authz + "/registrar/?vo=" + voShortName;
}

function fillProjects(projects) {
    if (!projects) {
        (flowMessager.newMessage("Projects","can't be fill.","danger")).draw();
        return;
    }
    var projectTable = new PerunTable();
    projectTable.addColumn({type:"number", title:"#"});
    projectTable.addColumn({type:"text", title:"Virtual Organizations", name:"name"});
    projectTable.addColumn({type:"date", title:"Membership Expiration", name:"expiration"});
    projectTable.addColumn({type: "button2", title: "Extend", button: "state"});
    projectTable.setValues(projects);
    var tableHtml = projectTable.draw();
    $("#projects-table").html(tableHtml);
}