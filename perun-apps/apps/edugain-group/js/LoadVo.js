/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var innerTabs;
function entryPoint(user) {
    innerTabs = new Tabs($("#innerTabs"));
    //callMeAfter(afterLogout, [], "logout");
    loadVo();
    //console.log(roles);
}

$(document).ready(function () {
    if (getAuthz() == "non") {
        window.location.href = "../non/help.html";
    }
});

var vo;
function loadVo() {
    callPerun("vosManager", "getVoByShortName", {shortName: voConfiguration.SHORT_NAME}, function (voResult) {
        if (!voResult) {
            (flowMessager.newMessage("Data", "can't be loaded.", "danger")).draw();
            return;
        }
        vo = voResult;

        var hash = document.location.hash.substring(1).split("&")[1];
        showVo();
        if (hash != undefined && hash != "vo") {
            showGroup(hash);
        }
    });
}

//require loaded global vo variable
function showVo() {
    if (!vo) {
        (flowMessager.newMessage("Can not show data", "because there was an error with communication", "error")).draw();
        return;
    }
    if (!innerTabs.containsTab("vo")) {
        addVoTab(vo);
        $("#groupsManagerLink").click(function () {
            //showVo();
        });
    }
    innerTabs.show("vo");
    fillVoInfo(vo);
    loadGroups(vo);
    loadAllMembers(vo);
}

function addVoTab(vo) {
    var voTab = new Tab('<span class="glyphicon glyphicon-home"></span>', "vo");
    innerTabs.addTab(voTab);

    var content;
    content = '<div class="page-header"><h2>' + vo.name + '</h2></div>';
    content += '<div class="btn-toolbar">';
    content += '  <div class="btn-group">';
    content += '    <button class="btn btn-primary" data-toggle="modal" data-target="#createGroupInVo">Create Group</button>';
    content += '  </div>';
    content += '  <div class="btn-group">';
    content += '    <button class="btn btn-primary" data-toggle="modal" data-target="#inviteUser">Invite User</button>';
    content += '  </div>';
    content += '</div>';
    content += '<div id="groupsTable"></div>';
    voTab.addContent(content);

    var voAuthz = new Authorization(roles, vo, null);
    var buttons = voTab.place.find('.btn-toolbar');
    voAuthz.addObject(buttons.find('button[data-target^=#createGroupInVo]'), ["PERUNADMIN", "VOADMIN", "TOPGROUPCREATOR"]);
    voAuthz.addObject(buttons.find('button[data-target^=#inviteUser]'), ["PERUNADMIN", "VOADMIN", "TOPGROUPCREATOR"]);
    voAuthz.addObject(voTab.place.find('.groupsTable'), ["PERUNADMIN", "VOOBSERVER", "VOADMIN", "GROUPADMIN", "TOPGROUPCREATOR"]);
    voAuthz.check();

    var createGroupModal = new Modal("Create group", "createGroupInVo", voTab.place);
    createGroupModal.init();
    fillModalCreateGroup(createGroupModal, vo);

    var inviteUserModal = new Modal("Invite user to " + vo.name, "inviteUser", voTab.place);
    inviteUserModal.init();
    fillModalInviteUser(inviteUserModal, vo);
}

function createGroupInVo(form, vo) {
    var name = form.find("#name");
    var description = form.find("#description");
    var newGroup = {name: name.val(), description: description.val()};
    callPerunPost("groupsManager", "createGroup", {vo: vo.id, group: newGroup},
    function (createdGroup) {
        innerTabs.place.find("#vo .modal").modal('hide');
        (flowMessager.newMessage(createdGroup.shortName, "group was created succesfuly", "success")).draw();
        addGroupToAllVoGroups(createdGroup);
        addGroupAdminRole(createdGroup.id, vo.id);
        fillGroups(allVoGroups);
        showGroup(createdGroup.id);
    }, function (error) {
        switch (error.name) {
            case "GroupExistsException":
                (flowMessager.newMessage(newGroup.name, "is already exists<br />(It may belong to another user)", "warning")).draw();
                break;
            default:
                (flowMessager.newMessage("Internal error", "Can not create group " + newGroup.name, "danger")).draw();
                break;
        }
    });
}

function inviteUser(form, vo) {
    var email = form.find("input#email");
    callPerunPost("registrarManager", "sendInvitation", {voId: vo.id, email: email.val(), language: "en"}, function() {
        innerTabs.place.find("#vo .modal").modal('hide');
        (flowMessager.newMessage("Invitation", "successfully sent to " + email.val(), "success")).draw();
        email.val("");
    });
}

function fillVoInfo(vo) {
    if (!vo) {
        (flowMessager.newMessage("Vo", "can't be fill.", "danger")).draw();
        return;
    }
    $("#vo-name").text(vo.name);
    //$('#groupsManagerLink > span').text(vo.shortName);
}

function fillModalInviteUser(modal, vo) {
    modal.clear();

    var html;
    html = '          <form role="form">';
    html += '            <div class="form-group">';
    html += '              <label for="email">User\'s e-mail</label>';
    html += '              <input type="email" class="form-control" id="email" placeholder="Users e-Mail" autofocus>';
    html += '            </div>';
    html += '            <button type="submit" class="btn btn-primary">Invite User</button>';
    html += '          </form>';
    modal.addBody(html);

    var inviteUserForm = modal.self.find("form");
    inviteUserForm.submit(function (event) {
        event.preventDefault();
        inviteUser(inviteUserForm, vo);
    });
}

function afterLogout() {
    innerTabs.clear();
}
