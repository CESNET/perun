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
var template;
function loadVo() {
    callPerun("vosManager", "getVoByShortName", {shortName: voConfiguration.SHORT_NAME}, function (voResult) {
        if (!voResult) {
            (flowMessager.newMessage("Vo", "can't be loaded.", "danger")).draw();
            return;
        }
        vo = voResult;

        callPerun("groupsManager", "getGroupByName", { "vo" : vo.id , "name" : voConfiguration.TEMPLATE_NAME }, function (group) {
            if (!group) {
                (flowMessager.newMessage("Template Group", "can't be loaded.", "danger")).draw();
                return;
            }
            template = group;

            var hash = document.location.hash.substring(1).split("&")[1];
            showVo();
            if (hash != undefined && hash != "vo") {
                showGroup(hash);
            }
        });

    });

}

//require loaded global vo variable
function showVo() {
    if (!vo) {
        (flowMessager.newMessage("Can not show data", "because there was an error with communication", "danger")).draw();
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
    content += '</div>';
    content += '<div id="groupsTable"></div>';
    voTab.addContent(content);

    var voAuthz = new Authorization(roles, vo, null);
    var buttons = voTab.place.find('.btn-toolbar');
    voAuthz.addObject(buttons.find('button[data-target^=#createGroupInVo]'), ["PERUNADMIN", "VOADMIN", "TOPGROUPCREATOR"]);
    voAuthz.addObject(voTab.place.find('.groupsTable'), ["PERUNADMIN", "VOOBSERVER", "VOADMIN", "GROUPADMIN", "TOPGROUPCREATOR"]);
    voAuthz.check();

    var createGroupModal = new Modal("Create group", "createGroupInVo", voTab.place);
    createGroupModal.init();
    fillModalCreateGroup(createGroupModal, vo);
}

function createGroupInVo(form, vo) {
    var name = form.find("#name");
    var description = form.find("#description");
    var newGroup = {name: name.val(), description: description.val()};
    callPerunPost("groupsManager", "createGroup", {vo: vo.id, group: newGroup},
    function (createdGroup) {

        callPerunPost("registrarManager", "createApplicationForm", {group: createdGroup.id},
            function (none) {

                var copyForm = callPerunPost("registrarManager", "copyForm", {
                    fromGroup: template.id,
                    toGroup: createdGroup.id
                });
                var copyMails = callPerunPost("registrarManager", "copyMails", {
                    fromGroup: template.id,
                    toGroup: createdGroup.id
                });
                var getForm = callPerun("registrarManager", "getApplicationForm", {
                    group: createdGroup.id
                });
                $.when(copyForm, copyMails, getForm).done(success(createdGroup));

                function success(createdGroup) {
                    return function (none0, none1, formResp) {
                        form = formResp[0];
                        form.automaticApproval = true;
                        form.automaticApprovalExtension = true;
                        callPerunPost("registrarManager", "updateForm", {form: form},
                            function (updatedForm) {
                                if (!updatedForm) {
                                    (flowMessager.newMessage("Internal error", "Group "+newGroup.name+" was created but settings of group failed.", "danger")).draw();
                                    return;
                                }
                                innerTabs.place.find("#vo .modal").modal('hide');
                                (flowMessager.newMessage(createdGroup.shortName, "group was created succesfuly", "success")).draw();
                                name.val("");
                                description.val("");
                                addGroupToAllVoGroups(createdGroup);
                                addGroupAdminRole(createdGroup.id, vo.id);
                                fillGroups(allVoGroups);
                                showGroup(createdGroup.id);
                            });
                    }
                }
            });

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

function fillVoInfo(vo) {
    if (!vo) {
        (flowMessager.newMessage("Vo", "can't be fill.", "danger")).draw();
        return;
    }
    $("#vo-name").text(vo.name);
    //$('#groupsManagerLink > span').text(vo.shortName);
}

function afterLogout() {
    innerTabs.clear();
}
