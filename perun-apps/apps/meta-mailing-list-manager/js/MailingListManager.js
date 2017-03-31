// Empty initial perunSession object
var voId = 21; // voShortName: "meta";
var serviceId = 421; // serviceName: "mailman_meta";

function entryPoint(user) {
    //loadUserAttributes(user);
    getMember(user, voId);
}

function getMember(user, voId) {
    // Get VO id
    callPerun("vosManager", "getVoById", { id : voId }, function(vo) {
        // Get member
        callPerun("membersManager", "getMemberByUser", { vo : vo.id, user : user.id }, function(member) {
            getResourcesAll(serviceId, member);
        });
    });
}

function getResourcesAll(serviceId, member) {
	callPerun("resourcesManager", "getAssignedResources", { member : member.id , service: serviceId }, function(resourcesAll) {
		fillResources(resourcesAll, member);
	});
}

function fillResources(resourcesAll, member) {

    var table = new PerunTable();
    table.addColumn({type: "text", title: "Description", name: "name"});
    table.addColumn({type: "button2", title: "Action", button: "action"});
    var tableHtml = table.draw();
    $("#mailinglists-table").html(tableHtml);

    // Filter only mailingList resources
    var resources = [];
    for (var id in resourcesAll) {
        // Is member unsubscibed?
        callPerun("attributesManager", "getAttribute", { member : member.id, resource : resourcesAll[id].id, attributeName : 'urn:perun:member_resource:attribute-def:def:optOutMailingList' },
            success(member, resourcesAll[id]));
    }
    function success(member, resource) {
        return function (attr) {
            var action;
            if (attr.value == 'true') {
                action = new TableButton(resource.id, "subscribe", "subscribe", "primary", {fnc: subscribe, params: [member.id, resource.id]});
            } else {
                action = new TableButton(resource.id, "unsubscribe", "unsubscribe", "danger", {fnc: unsubscribe, params: [member.id, resource.id]});
            }
            table.addValue({name: resource.name, action: action});
            table.values.sort(function(a, b) { return a.name.localeCompare(b.name); });
            $("#mailinglists-table").html(table.draw());
        }
    }
}



function subscribe(memberId, resourceId) {
    var loadImage = new LoadImage($("#mailinglists-table #subscribe-"+resourceId), "18px");
    callPerun("attributesManager", "getAttribute", { member : memberId, resource : resourceId, attributeName : 'urn:perun:member_resource:attribute-def:def:optOutMailingList' }, function(attr) {
        attr.value = '';
        callPerunPost("attributesManager", "setAttribute", { member : memberId, resource : resourceId, attribute : attr }, function() {
            loadImage.hide();
            window.location.reload();
        });
    });
}

function unsubscribe(memberId, resourceId) {
    var loadImage = new LoadImage($("#mailinglists-table #unsubscribe-"+resourceId), "18px");
    callPerun("attributesManager", "getAttribute", { member : memberId, resource : resourceId, attributeName : 'urn:perun:member_resource:attribute-def:def:optOutMailingList' }, function(attr) {
        attr.value = 'true';
        callPerunPost("attributesManager", "setAttribute", { member : memberId, resource : resourceId, attribute : attr }, function() {
            loadImage.hide();
            window.location.reload();
        });
    });
}




function loadUserAttributes(user) {
    if (!user) {
        (flowMessager.newMessage("User attributes", "can't be loaded because user isn't loaded.", "danger")).draw();
        return;
    }

    var loadImage = new LoadImage($('.user-attributes [id^="user-"], .user-displayName'), "20px");

    callPerun("attributesManager", "getAttributes", {user: user.id}, function(userAttributes) {
        if (!userAttributes) {
            (flowMessager.newMessage("User attributes", "can't be loaded.", "danger")).draw();
            return;
        }
        var userAttributesFriendly = {};
        for (var attrId in userAttributes) {
            userAttributesFriendly[userAttributes[attrId].friendlyName] = userAttributes[attrId].value;
        }
        fillUserAttributes(userAttributesFriendly);
        //(flowMessager.newMessage("User data", "was loaded successfully.", "success")).draw();
    });
}

function fillUserAttributes(userAttributesFriendly) {
    if (!userAttributesFriendly) {
        (flowMessager.newMessage("User attributes", "can't be fill.", "danger")).draw();
        return;
    }
    for (var attrName in userAttributesFriendly) {
        var attrId = attrName.split(':').join('-');
        $(".user-"+attrId).text(userAttributesFriendly[attrName]);
    }
}
