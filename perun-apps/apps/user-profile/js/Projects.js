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
    if (!user) {
        (flowMessager.newMessage("Projects","can't be loaded because user isn't loaded.","danger")).draw();
        return;
    }
    var loadImage = new LoadImage($("#projects-table"), "auto");
    
    callPerun("usersManager", "getVosWhereUserIsMember", {user: user.id}, function(projects) {
        if (!projects) {
            (flowMessager.newMessage("Projects","can't be loaded.","danger")).draw();
            return;
        }
        fillProjects(projects);
        loadImage.hide();
        //(flowMessager.newMessage("Projects","was loaded successfully.","success"));
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
    projectTable.setValues(projects);
    var tableHtml = projectTable.draw();
    $("#projects-table").html(tableHtml);
}