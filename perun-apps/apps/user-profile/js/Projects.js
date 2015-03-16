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
        members.push({id: members[0].id, voId: 1121});
        var projectsNum = members.length;
        var projectsCurrent = 0;
        var projects = [];
        for (var i in members) {
            var loadVos = callPerun("vosManager", "getVoById", {id: members[i].voId});
            var loadExpiration = callPerun("attributesManager", "getAttribute",
                {member: members[i].id, attributeName: "urn:perun:member:attribute-def:def:membershipExpiration"});
            var loadRules = callPerun("attributesManager", "getAttribute",
                {vo: members[i].voId, attributeName: "urn:perun:vo:attribute-def:def:membershipExpirationRules"});
            var loadExtend = callPerunPost("membersManager", "canExtendMembership", {member: members[i].id} )
            $.when(loadVos, loadExpiration, loadRules, loadExtend).done(success(members[i]));
        }
        function success(member) {
            return function (vo, expiration, rules, extend) {
                vo = vo[0];
                var expirationDate = new Date(expiration[0].value);
                var rules = rules[0].value;
                console.log(extend[0]);
                var stateBtn = stateButton(expirationDate, rules, member);
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

function stateButton(expiration, rules, member) {
    if (expiration.getTime() < (new Date()).getTime()) {
        action = new TableButton(member.id, "expired", "extend", "danger", {fnc: extend, params: [member.id]});
        return action;
    }
    if (rules) {
        var gracePeriod = rules.gracePeriod;
        if (false) {
            action = new TableButton(member.id, "gracePeriod", "extend", "warning", {fnc: extend, params: [member.id]});
            return action;
        }
    }

    action = new TableButton(member.id, "ok", "<i class='glyphicon glyphicon-ok'></i>", "success", {fnc: extend, params: [member.id]});
    action.setDisabled(true);
    return action;
}



function extend(member) {
    alert("extend member " + member + ": unsupported yet");
}

function fillProjects(projects) {
    if (!projects) {
        (flowMessager.newMessage("Projects","can't be fill.","danger")).draw();
        return;
    }
    var projectTable = new PerunTable();
    projectTable.addColumn({type:"number", title:"#"});
    projectTable.addColumn({type:"text", title:"Virtual Organizations", name:"name"});
    projectTable.addColumn({type:"date", title:"Expiration", name:"expiration"});
    projectTable.addColumn({type: "button2", title: "Extend", button: "state"});
    projectTable.setValues(projects);
    var tableHtml = projectTable.draw();
    $("#projects-table").html(tableHtml);
}