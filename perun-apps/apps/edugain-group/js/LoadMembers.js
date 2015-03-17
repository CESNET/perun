/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



function loadMembers(group) {
    var loadImage = new LoadImage($("#" + group.id + " .membersTable"), "64px");
    callPerun("membersManager", "getCompleteRichMembers", {group: group.id,
        attrsNames: ["urn:perun:user:attribute-def:core:displayName", "urn:perun:user:attribute-def:def:preferredMail"],
        lookingInParentGroup: 0},
    function (members) {
        if (!members) {
            (flowMessager.newMessage("Members", "can't be loaded.", "danger")).draw();
            return;
        }
        fillMembers(members, group);
        loadImage.hide();
    });
}

function fillMembers(members, group) {
    if (!members) {
        (flowMessager.newMessage("Members", "can't be fill.", "danger")).draw();
        return;
    }

    var table = $("#" + group.id + " .membersTable");

    if (members.length == 0) {
        table.html("no members");
        return;
    }

    var users = [];
    for (var i in members) {
        users[i] = members[i].user;
        users[i].memberId = members[i].id;
        if (members[i].membershipType == "DIRECT") {
            users[i].membershipTypeIcon = "glyphicon-ok";
        } else {
            users[i].membershipTypeIcon = "glyphicon-minus";
        }
        var attrs = members[i].userAttributes;
        for (var j in attrs) {
            users[i][attrs[j].friendlyName] = attrs[j].value;

        }
    }

    var membersTable = new PerunTable();
    //membersTable.setClicableRows({isClicable : true, id:"id", prefix:"row-"});
    //membersTable.addColumn({type: "number", title: "#"});
    //membersTable.addColumn({type: "icon", title: "", name: "membershipTypeIcon", description: "is direct member"});
    membersTable.addColumn({type: "text", title: "Name", name: "displayName"});
    membersTable.addColumn({type: "text", title: "Preferred Mail", name: "preferredMail"});
    membersTable.addColumn({type: "button", title: "", btnText: "&times;", btnId: "memberId", btnName: "removeMember", btnType: "danger"});
    membersTable.setValues(users);
    table.html(membersTable.draw());

    table.find('[data-toggle="tooltip"]').tooltip();

    var removeMemberBtns = table.find('button[id^=removeMember]');
    removeMemberBtns.each( function () {
        var member = getMemberById(members, $(this).attr("id").split("-")[1]);
        if (member.membershipType != "DIRECT") {
            $(this).prop("disabled", true);
            $(this).parent().attr("title", "is not direct member");
            //$(this).parent().tooltip({title: "is not direct member", placement: "left"});
        }
    });
    
    var groupTab = innerTabs.getTabByName(group.id);
    removeMemberBtns.click(function () {
        var member = getMemberById(members, $(this).attr("id").split("-")[1]);
        var removeMemberModal = new Modal("Remove " + member.user.firstName + " " + member.user.lastName + 
                " from group " + group.shortName,
                "removeMember" + member.id + "-" + group.id,
                groupTab.place);
        removeMemberModal.setType("danger");
        removeMemberModal.init();
        fillModalRemoveMember(removeMemberModal, member, group);
        removeMemberModal.self.modal('show');
    });
}

var allMembers;
function loadAllMembers(vo) {
    callPerun("membersManager", "getCompleteRichMembers", {vo: vo.id, attrsNames: ["urn:perun:user:attribute-def:def:preferredMail"]}, function (members) {
        if (!members) {
            return;
        }
        allMembers = members.sort(compareMembers);
        callBackAfter(loadAllMembers);
    }, function(data) {
        if (data.name != "PrivilegeException") {
            (flowMessager.newMessage(data.name, data.message, "danger")).draw();
        } else {
            callBackAfter(loadAllMembers);
        }
    });
}

function getMemberById(members, id) {
    for (var i in members) {
        if (members[i].id == id) {
            return members[i];
        }
    }
    return null;
}

function refreshAllParentsMembers(group) {
    if (!group.parentGroupId) {
        return;
    }
    var parentGroup = getGroupById(allVoGroups, group.parentGroupId);
    if (!parentGroup) {
        return;
    }
    loadMembers(parentGroup);
    refreshAllParentsMembers(parentGroup);
}


function fillModalRemoveMember(modal, member, group) {
    modal.clear();
    var html;
    html = "<p>";
    html += "Do you really want to remove member " + member.user.firstName + " " + member.user.lastName + "?";
    html += "</p>";
    html += '<div class="btn-toolbar">';
    html += '  <div class="btn-group pull-right">';
    html += '    <button id="removeMember" class="btn btn-danger">Remove Member</button>';
    html += '  </div>';
    html += '  <div class="btn-group pull-right">';
    html += '    <button class="btn btn-default" data-dismiss="modal">Close</button>';
    html += '  </div>';
    html += '</div>';
    modal.addBody(html);
    modal.self.find("button#removeMember").click(function () {
        removeMember(member, group);
    });
}

function removeMember(member, group) {
    var name = member.user.firstName + " " + member.user.lastName;
    callPerunPost("groupsManager", "removeMember", {group: group.id, member: member.id},
    function () {
        (flowMessager.newMessage(name, "was removed sucesfuly from " + group.shortName + " group", "success")).draw();
    }, function (error) {
        switch (error.name) {
            case "NotGroupMemberException":
                (flowMessager.newMessage(name, "is not in group " + group.shortName, "warning")).draw();
                break;
            default:
                (flowMessager.newMessage("Internal error", "Can not remove member " + name + " from group " + group.shortName, "danger")).draw();
                break;
        }
    }, function () {
        innerTabs.getTabByName(group.id).place.find(".modal").modal('hide');
        showGroup(group.id);
        refreshAllParentsMembers(group);
    });
}

function compareMembers(a, b) {
    return a.user.lastName.localeCompare(b.user.lastName);
}



function getAttrByFriendlyName(attrs, friendlyName) {
    for (var i in attrs) {
        if (attrs[i].friendlyName == friendlyName) {
            return attrs[i];
        }
    }
}