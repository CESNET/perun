/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function showGroup(groupId) {
    if (!allVoGroups) {
        callMeAfter(showGroup, [groupId], loadGroups);
        return;
    }
    var group;
    if (isNumber(groupId)) {
        group = getGroupById(allVoGroups, groupId);
    } else {
        group = getGroupByName(allVoGroups, groupId);
        if (group) {
            (flowMessager.newMessage("Welcome", "You have been successfully invited into a group "+group.name, "success")).draw();
        }
    }
    if (!group) {
        (flowMessager.newMessage("Group "+groupId, "can not be shown. It doesn't exist.", "warning")).draw();
        showVo();
        return;
    }

//if (!innerTabs.containsTab(group.id)) {
    if (innerTabs.containsTab(group.parentGroupId)) {
        innerTabs.removeSuccessors(group.parentGroupId);
    } else {
        var parentGroup = getGroupById(allVoGroups, group.parentGroupId);
        if (parentGroup) {
            showGroup(parentGroup.id);
        } else {
            innerTabs.removeSuccessors("vo");
        }
    }
    addGroupTab(group);
    //}
    innerTabs.show(group.id);
    $('#group-name').text(group.name);
    //$('#groupLink > span').text(group.shortName);
    loadMembers(group);
}

function addGroupTab(group) {
    var groupTab = new Tab(group.shortName, group.id);
    innerTabs.addTab(groupTab);
    var displayName = "";
    for (var i in group.name) {
        if (group.name[i] == ":") {
            displayName += "&#8203;"; //because of line break for long names
        }
        displayName += group.name[i];
    }

    var content;
    content = '<div class="page-header"><h2>' + displayName + '</h2></div>';
    content += '<div class="btn-toolbar">';
    /*content += '  <div class="btn-group">';
    content += '    <button class="btn btn-primary" data-toggle="modal" data-target="#addMembers' + group.id + '">Add Members</button>';
    content += '    <button class="btn btn-danger" data-toggle="modal" data-target="#removeMembers' + group.id + '">Remove Members</button>';
    content += '  </div>';*/
    content += '  <div class="btn-group">';
    content += '    <button class="btn btn-primary" data-toggle="modal" data-target="#inviteUser'+group.id+'">Invite User</button>';
    content += '  </div>';
    content += '  <!--<div class="btn-group">';
    content += '    <button class="btn btn-primary" data-toggle="modal" data-target="#addManagers' + group.id + '">Add Managers</button>';
    content += '    <button class="btn btn-danger" data-toggle="modal" data-target="#removeManagers' + group.id + '">Remove Managers</button>';
    content += '  </div>-->';
    content += '  <div class="btn-group pull-right">';
    content += '    <!--<button class="btn btn-primary" data-toggle="modal" data-target="#createGroup' + group.id + '">Create Subgroup</button>-->';
    content += '    <button class="btn btn-danger" data-toggle="modal" data-target="#deleteGroup' + group.id + '">Delete Group</button>';
    content += '  </div>';
    content += '</div>';
    content += '<div class="membersTable"></div>';
    groupTab.addContent(content);
    var groupAuthz = new Authorization(roles, vo, group);
    var buttons = groupTab.place.find('.btn-toolbar');
    groupAuthz.addObject(buttons.find('button[data-target^=#createGroup]'), ["PERUNADMIN", "VOADMIN", "GROUPADMIN"]);
    groupAuthz.addObject(buttons.find('button[data-target^=#addMembers]'), ["PERUNADMIN", "VOADMIN", "GROUPADMIN"]);
    groupAuthz.addObject(buttons.find('button[data-target^=#inviteUser]'), ["PERUNADMIN", "VOADMIN", "TOPGROUPCREATOR"]);
    groupAuthz.addObject(buttons.find('button[data-target^=#addManagers]'), ["PERUNADMIN", "VOADMIN", "GROUPADMIN"]);
    groupAuthz.addObject(buttons.find('button[data-target^=#removeMembers]'), ["PERUNADMIN", "VOADMIN", "GROUPADMIN"]);
    groupAuthz.addObject(buttons.find('button[data-target^=#removeManagers]'), ["PERUNADMIN", "VOADMIN", "GROUPADMIN"]);
    groupAuthz.addObject(buttons.find('button[data-target^=#deleteGroup]'), ["PERUNADMIN", "VOADMIN", "GROUPADMIN"]);
    groupAuthz.addObject(groupTab.place.find('.membersTable'), ["PERUNADMIN", "VOOBSERVER", "VOADMIN", "GROUPADMIN", "TOPGROUPCREATOR"]);
    groupAuthz.check();
    var createGroupModal = new Modal("Create Subgroup in " + group.shortName, "createGroup" + group.id, groupTab.place);
    createGroupModal.init();
    fillModalCreateGroup(createGroupModal, vo, group);
    var addMembersModal = new Modal("Add Members to group " + group.shortName, "addMembers" + group.id, groupTab.place);
    addMembersModal.init();
    fillModalAddMembers(addMembersModal, vo, group);
    var addManagersModal = new Modal("Add Managers for group " + group.shortName, "addManagers" + group.id, groupTab.place);
    addManagersModal.init();
    fillModalAddManagers(addManagersModal, vo, group);
    var removeMembersModal = new Modal("Remove Members from group " + group.shortName, "removeMembers" + group.id, groupTab.place);
    removeMembersModal.init();
    fillModalRemoveMembers(removeMembersModal, group);
    var removeManagersModal = new Modal("Remove Managers for group " + group.shortName, "removeManagers" + group.id, groupTab.place);
    removeManagersModal.init();
    fillModalRemoveManagers(removeManagersModal, group);
    var deleteGroupModal = new Modal("Delete group " + group.shortName, "deleteGroup" + group.id, groupTab.place);
    deleteGroupModal.setType("danger");
    deleteGroupModal.init();
    fillModalDeleteGroup(deleteGroupModal, group);
    var inviteUserModal = new Modal("Invite user to group " + group.shortName, "inviteUser" + group.id, groupTab.place);
    inviteUserModal.init();
    fillModalInviteUser(inviteUserModal, vo, group);
}

var allVoGroups;
function loadGroups(vo) {
    if (!vo) {
        (flowMessager.newMessage("Groups", "can't be loaded because vo is not set.", "danger")).draw();
    }
    var loadImage = new LoadImage($('#groupsTable'), "20px");
    callPerun("groupsManager", "getGroups", {vo: vo.id}, function (groups) {
        if (!groups) {
            (flowMessager.newMessage("Groups", "can't be loaded.", "danger")).draw();
            return;
        }
        groupsWithMembersGroup = groups.filter(function (el) {
                                                  return el.name !== "members";
                                               });
        allVoGroups = groupsWithMembersGroup;
        callBackAfter(loadGroups);
        fillGroups(groupsWithMembersGroup);
        loadImage.hide();
    }, function(data) {
        if (data.name != "PrivilegeException") {
            (flowMessager.newMessage(data.name, data.message, "danger")).draw();
        } else {
            allVoGroups = [];
            callBackAfter(loadGroups);
            fillGroups([]);
            loadImage.hide();
        }
    });
}

function addGroupToAllVoGroups(group) {
    var groups = allVoGroups;
    if (!group.parentGroupId) {
        groups.push(group);
        return;
    }
    for (var i = groups.length - 1; i >= 0; i--) {
        if (group.parentGroupId === groups[i].id) {
            groups.splice(i + 1, 0, group)
            return;
        }
    }
    (flowMessager.newMessage("Group " + group.shorName, "can not be inserted because can not find parent group", "danger")).draw();
}

function addGroupAdminRole(groupId, voId) {
    if (!roles.GROUPADMIN) {
        roles.GROUPADMIN = {Group: [], Vo: []};
    }
    roles.GROUPADMIN.Group.push(groupId);
    if (roles.GROUPADMIN.Vo.indexOf(voId) < 0) {
        roles.GROUPADMIN.Vo.push(voId);
        loadAllMembers({id: voId});
    }
}

function fillGroups(groups) {
    if (!groups) {
        (flowMessager.newMessage("Groups", "can't be fill.", "danger")).draw();
        return;
    }

    var table = $("#groupsTable");
    table.html(getTableOfGroups(groups).draw());
    table.find('[data-toggle="tooltip"]').tooltip();
    table.find("table tr").click(function () {
        var group = getGroupById(groups, $(this).attr("id").split("-")[1]);
        showGroup(group.id);
    });
}

function getTableOfGroups(groups) {
    createAttrTableName(groups);
    var groupsTable = new PerunTable();
    groupsTable.setClicableRows({isClicable: true, id: "id", prefix: "row-"});
    //groupsTable.addColumn({type: "number", title: "#"});
    //groupsTable.addColumn({type: "button", title: "", btnText: "⌄", btnType: "default", btnId: "id"});
    groupsTable.addColumn({type: "text", title: "Name", name: "tableName"});
    groupsTable.addColumn({type: "text", title: "Description", name: "description"});
    groupsTable.setValues(groups);
    return groupsTable;
}




function createAttrTableName(groups) {
    for (var id in groups) {
        if (groups[id].shortName == voConfiguration.TEMPLATE_NAME) {
            groups.splice(id,1);
            continue;
        }
        if (groups[id].parentGroupId === null) {
            groups[id].tableName = groups[id].shortName;
        } else {
            var level = groups[id].name.split(":").length - 1;
            groups[id].tableName = "";
            for (var i = 0; i < level; i++) {
                groups[id].tableName += "<span class='space'> </span>";
            }
            groups[id].tableName += groups[id].shortName;
        }
    }
    return groups;
}


function getGroupById(groups, id) {
    for (var i in groups) {
        if (groups[i].id == id) {
            return groups[i];
        }
    }
    return null;
}
function getGroupByName(groups, shortName) {

    for (var i in groups) {
        console.log(groups[i].shortName +" / "+ shortName);
        if (groups[i].shortName == shortName) {
            return groups[i];
        }
    }
    return null;
}


function createGroup(form, group) {
    var name = form.find("#name");
    var description = form.find("#description");
    var newGroup = {name: name.val(), description: description.val()};
    callPerunPost("groupsManager", "createGroup", {parentGroup: group.id, group: newGroup}, 
    function (createdGroup) {
        (flowMessager.newMessage(createdGroup.name, "subgroup was created succesfuly", "success")).draw();
        addGroupToAllVoGroups(createdGroup);
        addGroupAdminRole(createdGroup.id, vo.id);
        fillGroups(allVoGroups);
        showGroup(createdGroup.id);
    }, function(error) {
            switch (error.name) {
                case "GroupExistsException":
                    (flowMessager.newMessage(newGroup.name, "is already exists", "warning")).draw();
                    break;
                default:
                    (flowMessager.newMessage("Internal error", "Can not create group " + newGroup.name, "danger")).draw();
                    break;
            }
    }, function() {
        innerTabs.place.find("#" + group.id + " .modal").modal('hide');
    });
}

function addMembers(form, group) {
    var membersValues = form.find("#members").val();
    var members = [];
    for (var j in membersValues) {
        members[j] = {};
        members[j].id = membersValues[j].split("-")[0];
        members[j].userId = membersValues[j].split("-")[1];
        members[j].name = membersValues[j].split("-")[2];
    }

    var count = members.length;
    for (var i in members) {
        callPerunPost("groupsManager", "addMember", {group: group.id, member: members[i].id},
        success(members[i]),
                error(members[i]),
                complete());
    }
    function success(member) {
        return function () {
            (flowMessager.newMessage(member.name, "was added sucesfuly into " + group.shortName + " group", "success")).draw();
        };
    }
    function error(member) {
        return function (error) {
            switch (error.name) {
                case "AlreadyMemberException":
                    (flowMessager.newMessage(member.name, "is already in group " + group.shortName, "warning")).draw();
                    break;
                default:
                    (flowMessager.newMessage("Internal error", "Can not add member " + member.name + " to group " + group.shortName, "danger")).draw();
                    break;
            }
        };
    }
    function complete() {
        return function () {
            count--;
            if (count == 0) {
                innerTabs.getTabByName(group.id).place.find(".modal").modal('hide');
                showGroup(group.id);
                refreshAllParentsMembers(group);
            }
        }
    }
}

function addManagers(form, group) {
    var membersValues = form.find("#members").val();
    var members = [];
    for (var j in membersValues) {
        members[j] = {};
        members[j].id = membersValues[j].split("-")[0];
        members[j].userId = membersValues[j].split("-")[1];
        members[j].name = membersValues[j].split("-")[2];
    }
    for (var id in members) {
        callPerunPost("groupsManager", "addAdmin", {group: group.id, user: members[id].userId}, function () {
            innerTabs.getTabByName(group.id).place.find(".modal").modal('hide');
            (flowMessager.newMessage(members[id].name, "is manager in " + group.shortName + " group now.", "success")).draw();
            showGroup(group.id);
        });
    }
}

function removeMembers(form, group) {
    var membersValues = form.find("#members").val();
    var members = [];
    for (var j in membersValues) {
        members[j] = {};
        members[j].id = membersValues[j].split("-")[0];
        members[j].userId = membersValues[j].split("-")[1];
        members[j].name = membersValues[j].split("-")[2];
    }
    var count = members.length;
    for (var j in members) {
        callPerunPost("groupsManager", "removeMember", {group: group.id, member: members[j].id},
        success(members[j]),
                error(members[j]),
                complete());
    }
    function success(member) {
        return function () {
            (flowMessager.newMessage(member.name, "was removed sucesfuly from " + group.shortName + " group", "success")).draw();
        };
    }
    function error(member) {
        return function (error) {
            switch (error.name) {
                case "NotGroupMemberException":
                    (flowMessager.newMessage(member.name, "is not in group " + group.shortName, "warning")).draw();
                    break;
                default:
                    (flowMessager.newMessage("Internal error", "Can not remove member " + member.name + " from group " + group.shortName, "danger")).draw();
                    break;
            }
        };
    }
    function complete() {
        return function () {
            count--;
            if (count == 0) {
                innerTabs.getTabByName(group.id).place.find(".modal").modal('hide');
                showGroup(group.id);
                refreshAllParentsMembers(group);
            }
        }
    }
}

function removeManagers(form, group) {
    var membersValues = form.find("#members").val();
    var members = [];
    for (var j in membersValues) {
        members[j] = {};
        members[j].id = membersValues[j].split("-")[0];
        members[j].userId = membersValues[j].split("-")[1];
        members[j].name = membersValues[j].split("-")[2];
    }
    for (var id in members) {
        callPerunPost("groupsManager", "removeAdmin", {group: group.id, user: members[id].userId}, function () {
            innerTabs.getTabByName(group.id).place.find(".modal").modal('hide');
            (flowMessager.newMessage(members[id].name, "is not manager in " + group.shortName + " group now.", "success")).draw();
            showGroup(group.id);
        });
    }
}

function deleteGroup(group) {
    callPerunPost("groupsManager", "deleteGroup", {group: group.id, force: 1}, 
    function () {
        innerTabs.getTabByName(group.id).place.find(".modal").modal('hide');
        innerTabs.removeTab(group.id);
        (flowMessager.newMessage(group.name, "was deleted successfully", "success")).draw();
        loadGroups(vo);
        if (group.parentGroupId) {
            showGroup(group.parentGroupId);
        } else {
            showVo();
        }
    }, function(error) {
            switch (error.name) {
                case "GroupNotExistsException":
                    (flowMessager.newMessage(group.shortName, "not exists", "warning")).draw();
                    break;
                default:
                    (flowMessager.newMessage("Internal error", "Can not delete group " + group.name, "danger")).draw();
                    break;
            }
    });
}

function inviteUser(form, vo, group) {
    var email = form.find("input#email");

    callPerunPost("registrarManager", "sendInvitation", {voId: vo.id, groupId: group.id, email: email.val(), language: "en"}, function() {
        innerTabs.place.find("#" + group.id + " .modal").modal('hide');
        (flowMessager.newMessage("Invitation", "successfully sent to " + email.val(), "success")).draw();
        email.val("");
    });
}


function fillModalCreateGroup(modal, vo, group) {
    modal.clear();
    var html;
    html = '          <form role="form">';
    html += '            <div class="form-group">';
    html += '              <label for="name">Group name</label>';
    html += '              <input type="text" class="form-control" id="name" placeholder="Group name" autofocus>';
    html += '            </div>';
    html += '            <div class="form-group">';
    html += '              <label for="description">Description</label>';
    html += '              <input type="text" class="form-control" id="description" placeholder="Description">';
    html += '            </div>';
    html += '            <button type="submit" class="btn btn-primary">Create Group</button>';
    html += '          </form>';
    modal.addBody(html);
    var createGroupForm = modal.self.find("form");
    createGroupForm.submit(function (event) {
        event.preventDefault();
        if (group) {
            createGroup(createGroupForm, group);
        } else {
            createGroupInVo(createGroupForm, vo);
        }
    });
}

function fillModalAddMembers(modal, vo, group) {
    modal.clear();
    var loadImage = new LoadImage(modal.self.find(".modal-body"), "64px");
    if (!allMembers) {
//(flowMessager.newMessage("Members", "can't be loaded.", "warning")).draw();
        callMeAfter(fillModalAddMembers, [modal, vo, group], loadAllMembers);
        return;
    }
    loadImage.hide();
    if (allMembers.length === 0) {
        (new Message("", "No users found", "warning", modal.self.find(".modal-body"))).draw();
        return;
    }

    var html;
    html = '   <form role="form">';
    html += '      <div class="form-group">';
    html += '         <label for="member">Select members</label>';
    html += '         <input id="member" type="text" class="form-control" placeholder="Type user\'s e-mail or name">';
    html += '         <select id="members" multiple class="form-control">';
    html += '         </select>';
    html += '      </div>';
    html += '      <button type="submit" class="btn btn-primary">Add Members</button>';
    html += '   </form>';
    modal.addBody(html);
    var addMembersForm = modal.self.find("form");
    addMembersForm.submit(function (event) {
        event.preventDefault();
        addMembers(addMembersForm, group);
    });
    var queryInput = addMembersForm.find("#member");
    var select = modal.self.find("select#members");
    findMemberAndDo("", allMembers, function (member) { //first fill
        addMemberInSelect(member, select);
    });
    var searchStack = 0;
    queryInput.keyup(function (event) {
        searchStack++;
        setTimeout(
                function () {
                    if (searchStack == 1) {
                        select.html("");
                        var count = findMemberAndDo(queryInput.val(), allMembers, function (member) {
                            addMemberInSelect(member, select);
                        });
                        if (count === 1) {
                            select.find("option").attr("selected", "true");
                        }
                        searchStack = 0;
                    } else {
                        searchStack--;
                    }
                }, 100);
    });
    function addMemberInSelect(member, select) {
        var option;
        var email = getAttrByFriendlyName(member.userAttributes, "preferredMail").value;
        option = '<option value="' + member.id + '-' +
                member.user.id + '-' +
                member.user.firstName + ' ' + member.user.lastName + '">';
        option += member.user.lastName + " " + member.user.firstName + " &nbsp; - &nbsp; " + email;
        option += '</option>';
        select.append(option);
    }
}

function fillModalAddManagers(modal, vo, group) {
    modal.clear();
    var loadImage = new LoadImage(modal.self.find(".modal-body"), "64px");
    if (!allMembers) {
        callMeAfter(fillModalAddManagers, [modal, vo, group], loadAllMembers);
        return;
    }
    loadImage.hide();
    if (allMembers.length === 0) {
        (new Message("", "No users found", "warning", modal.self.find(".modal-body"))).draw();
        return;
    }

    var html;
    html = '          <form role="form">';
    html += '            <div class="form-group">';
    html += '              <label for="members">Select users</label>';
    html += '              <select id="members" multiple class="form-control">';
    html += '              </select>';
    html += '            </div>';
    html += '            <button type="submit" class="btn btn-primary">Add Managers</button>';
    html += '          </form>';
    modal.addBody(html);
    var select = modal.self.find("select#members");
    for (var id in allMembers) {
        var option;
        option = '<option value="' + allMembers[id].id + '-' +
                allMembers[id].user.id + '-' +
                allMembers[id].user.firstName + ' ' + allMembers[id].user.lastName + '">';
        option += allMembers[id].user.lastName + " " + allMembers[id].user.firstName;
        option += '</option>';
        select.append(option);
    }

    var addManagersForm = modal.self.find("form");
    addManagersForm.submit(function (event) {
        event.preventDefault();
        addManagers(addManagersForm, group);
    });
}

function fillModalRemoveMembers(modal, group) {
    modal.clear();
    var loadImage = new LoadImage(modal.self.find(".modal-body"), "64px");
    callPerun("groupsManager", "getGroupRichMembers", {group: group.id}, function (members) {
        if (!members) {
            (flowMessager.newMessage("Members", "can't be loaded.", "danger")).draw();
            return;
        }
        loadImage.hide();

        for (var i = members.length - 1; i >= 0; i--) {
            if (members[i].membershipType != "DIRECT") {
                members.splice(i, 1);
            }
        }

        if (members.length === 0) {
            (new Message("", "No users found", "info", modal.self.find(".modal-body"))).draw();
            return;
        }
        members = members.sort(compareMembers);

        var html;
        html = '          <form role="form">';
        html += '            <div class="form-group">';
        html += '              <label for="members">Select users</label>';
        html += '              <input id="member" type="text" class="form-control" placeholder="Search member">';
        html += '              <select id="members" multiple class="form-control">';
        html += '              </select>';
        html += '            </div>';
        html += '            <button type="submit" class="btn btn-danger">Remove Members</button>';
        html += '          </form>';
        modal.addBody(html);
        var removeMembersForm = modal.self.find("form");
        removeMembersForm.submit(function (event) {
            event.preventDefault();
            removeMembers(removeMembersForm, group);
        });
        var queryInput = removeMembersForm.find("#member");
        var select = modal.self.find("select#members");
        findMemberAndDo("", members, function (member) { //first fill
            addMemberInSelect(member, select);
        });
        var searchStack = 0;
        queryInput.keyup(function (event) {
            searchStack++;
            setTimeout(
                    function () {
                        if (searchStack == 1) {
                            select.html("");
                            var count = findMemberAndDo(queryInput.val(), members, function (member) {
                                addMemberInSelect(member, select);
                            });
                            if (count === 1) {
                                select.find("option").attr("selected", "true");
                            }
                            searchStack = 0;
                        } else {
                            searchStack--;
                        }
                    }, 100);
        });
        function addMemberInSelect(member, select) {
            var option;
            option = '<option value="' + member.id + '-' +
                    member.user.id + '-' +
                    member.user.firstName + ' ' + member.user.lastName + '">';
            option += member.user.lastName + " " + member.user.firstName;
            option += '</option>';
            select.append(option);
        }
    });
}

function fillModalRemoveManagers(modal, group) {
    modal.clear();
    var loadImage = new LoadImage(modal.self.find(".modal-body"), "64px");
    callPerun("groupsManager", "getAdmins", {group: group.id}, function (managers) {
        if (!managers) {
            (flowMessager.newMessage("Members", "can't be loaded.", "danger")).draw();
            return;
        }
        loadImage.hide();
        if (managers.length === 0) {
            (new Message("", "No users found", "info", modal.self.find(".modal-body"))).draw();
            return;
        }

        var html;
        html = '          <form role="form">';
        html += '            <div class="form-group">';
        html += '              <label for="members">Select users</label>';
        html += '              <select id="members" multiple class="form-control">';
        html += '              </select>';
        html += '            </div>';
        html += '            <button type="submit" class="btn btn-danger">Remove Managers</button>';
        html += '          </form>';
        modal.addBody(html);
        for (var id in managers) {
            var option;
            option = '<option value="' + managers[id].id + '-' +
                    managers[id].id + '-' +
                    managers[id].firstName + ' ' + managers[id].lastName + '">';
            option += managers[id].lastName + " " + managers[id].firstName;
            option += '</option>';
            modal.self.find("select#members").append(option);
        }

        var removeManagersForm = modal.self.find("form");
        removeManagersForm.submit(function (event) {
            event.preventDefault();
            removeManagers(removeManagersForm, group);
        });
    });
}

function fillModalDeleteGroup(modal, group) {
    modal.clear();
    var html;
    html = "<p>";
    html += "Do you really want to delete whole group?";
    html += "</p>";
    html += '<div class="btn-toolbar">';
    html += '  <div class="btn-group pull-right">';
    html += '    <button id="deleteGroup" class="btn btn-danger">Delete Group</button>';
    html += '  </div>';
    html += '  <div class="btn-group pull-right">';
    html += '    <button class="btn btn-default" data-dismiss="modal">Close</button>';
    html += '  </div>';
    html += '</div>';
    modal.addBody(html);
    modal.self.find("button#deleteGroup").click(function () {
        deleteGroup(group);
    });
}

function fillModalInviteUser(modal, vo, group) {
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
        inviteUser(inviteUserForm, vo, group);
    });
}

function findMemberAndDo(query, members, action) {
    if (!members) {
        var members = allMembers;
    }
    query = unAccent(query.toLowerCase().trim());
    var count = 0;
    for (var id in members) {
        var email = "";
        if (getAttrByFriendlyName(members[id].userAttributes, "preferredMail")) {
            email = unAccent(getAttrByFriendlyName(members[id].userAttributes, "preferredMail").value.toLowerCase().trim());
        }
        var firstName = unAccent(members[id].user.firstName.toLowerCase().trim());
        var lastName = unAccent(members[id].user.lastName.toLowerCase().trim());
        if (((firstName + " " + lastName).indexOf(query) >= 0)
                || ((lastName + " " + firstName).indexOf(query) >= 0)
                || (email.indexOf(query) >= 0)) {
            action(members[id]);
            count++;
        }
    }
    return count;
}
