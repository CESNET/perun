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
        var projectsNum = members.length;
        var projectsCurrent = 0;
        var projects = [];
        for (var i in members) {
            var loadVos = callPerun("vosManager", "getVoById", {id: members[i].voId});
            var loadAttrs = callPerun("attributesManager", "getAttribute",
                {member: members[i].id, attributeName: "urn:perun:member:attribute-def:def:membershipExpiration"});
            $.when(loadVos, loadAttrs).done(success(members[i]));
        }
        function success(member) {
            return function (vo, expiration) {
                vo = vo[0];
                expiration = expiration[0];
                var project = {name: vo.name, expiration: expiration.value};
                projects.push(project);
                console.log({member:member, vo:vo, expiration:expiration});
                projectsCurrent++;
                if (projectsCurrent == projectsNum) {
                    loadImage.hide();
                    fillProjects(projects);
                }
            }
        }
    });
}


function fillProjects(projects) {
    if (!projects) {
        (flowMessager.newMessage("Projects","can't be fill.","danger")).draw();
        return;
    }
    var projectTable = new PerunTable();
    projectTable.addColumn({type:"number", title:"#"});
    projectTable.addColumn({type:"text", title:"Projects", name:"name"});
    projectTable.addColumn({type:"date", title:"Expiration", name:"expiration"});
    projectTable.setValues(projects);
    var tableHtml = projectTable.draw();
    $("#projects-table").html(tableHtml);
}