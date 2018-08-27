function entryPoint(user) {
    callPerun("authzResolver", "getPerunPrincipal", {}, function(perunPrincipal) {
        if (!perunPrincipal) {
            (flowMessager.newMessage("User","can't be loaded.","danger")).draw();
            return;
        }
        roles = perunPrincipal.roles;

        var isVoAdmin = false;
        for(var i in roles) {
            if (i === 'VOADMIN') {
                isVoAdmin = true;
                break;
            }
        }

        if (!isVoAdmin) {
            var modal = new Modal("Not registered yet.", "notVoAdmin", $("body"));
            modal.init();
            var body = "To create a new virtual organization, please <a href='" + configuration.REGISTRATION_LINK + "'>register yourself first</a>." +
                "<p class=\"help-block\">(You will be redirected back to this form after the registration.)</p>";
            modal.addBody(body);
            modal.getSelf().modal('show');
            modal.getSelf().on('hidden.bs.modal', function () {
                document.getElementById("submitButton").style.display = 'none';
                document.getElementById("alert").style.display = "block";
            })
        }
    });
}

$(document).ready(function() {
    $("form#establishForm").submit(function(e) {
        e.preventDefault();

        var voFullName = document.getElementById("fullName").value;
        var shortName = document.getElementById("shortName").value;

        callPerunPost("vosManager", "createVo", { vo :{name: voFullName, shortName: shortName, beanName : "Vo"}}, function(vo) {
            var voId = vo.id;

            createVoApplicationFormItems(voId, voFullName);

            createVoNotifications(voId);

            setVoEmailAttributes(voId);

            for (var i = 0; i < configuration.groupsToCreate.length; i++) {
                createGroup(voId, voFullName, configuration.groupsToCreate[i]);
            }

            showSuccessMessage();
        });
    });
});

function createGroup(voId, voFullName, groupToCreate) {

    callPerunPost("groupsManager", "createGroup", {vo: voId, group: groupToCreate.group}, function (createdGroup) {

        createGroupApplicationFormAndNotifications(createdGroup, voFullName, groupToCreate);
    })
}

function createGroupNotifications(group, groupToCreate) {
    for (var i = 0; i < groupToCreate.groupNotifications.length; i++) {
        var notification = groupToCreate.groupNotifications[i];

        callPerunPost("registrarManager", "addApplicationMail", {group: group.id, mail: notification})
    }
}

function createGroupApplicationFormAndNotifications(group, voFullName, groupToCreate) {
    callPerunPost("registrarManager", "createApplicationForm", {group: group.id}, function () {
        for (var i = 0; i < groupToCreate.groupFormItems.length; i++) {
            var formItem = groupToCreate.groupFormItems[i];

            if (i === 0) {
                formItem.i18n.cs.label = "<h2>Přihláška do skupiny " + groupToCreate.group.name +  " v organizaci" + voFullName + "</h2>";
                formItem.i18n.en.label = "<h2>Application for " + groupToCreate.group.name + " group within the " + voFullName + " VO</h2>";
            }
            callPerunPost("registrarManager", "addFormItem", {group: group.id, item: formItem})
        }

        createGroupNotifications(group, groupToCreate);
    });
}

function showSuccessMessage() {
    var modal = new Modal("Your VO was created.", "success", $("body"));
    modal.init();
    var body = "<a href='" + configuration.PROCEED_VO_MANAGEMENT_LINK + "'>Proceed to VO management</a>.";
    modal.addBody(body);
    modal.getSelf().modal('show');
}

function setVoEmailAttributes(voId) {

    // load user's email
    callPerunPost("attributesManager", "getAttribute", {user: user.id, attributeName: "urn:perun:user:attribute-def:def:preferredMail"}, function (userMailAttr) {

        // load toEmail attribute definition
        callPerunPost("attributesManager", "getAttributeDefinition", {attributeName: "urn:perun:vo:attribute-def:def:toEmail"}, function (toEmailAttrDef) {

            // set user's email as a vo's 'toEmail'
            toEmailAttrDef.value = [userMailAttr.value];
            callPerunPost("attributesManager", "setAttribute", {vo: voId, attribute: toEmailAttrDef});
        });

        // load toEmail attribute definition
        callPerunPost("attributesManager", "getAttributeDefinition", {attributeName: "urn:perun:vo:attribute-def:def:fromEmail"}, function (fromEmailAttrDef) {

            // set user's email as a vo's 'fromEmail'
            fromEmailAttrDef.value = userMailAttr.value;
            callPerunPost("attributesManager", "setAttribute", {vo: voId, attribute: fromEmailAttrDef});
        });
    });
}

function createVoNotifications(voId) {
    for (var i = 0; i < configuration.voNotifications.length; i++) {
        var notification = configuration.voNotifications[i];

        callPerunPost("registrarManager", "addApplicationMail", {vo: voId, mail: notification})
    }
}

function createVoApplicationFormItems(voId, voName) {
    for (var i = 0; i < configuration.voFormItems.length; i++) {
        var formItem = configuration.voFormItems[i];

        if (i === 0) {
            formItem.i18n.cs.label = "<h2>Přihláška do " + voName + " VO</h2>";
            formItem.i18n.en.label = "<h2>Application for " + voName + " VO membership</h2>";
        }
        callPerunPost("registrarManager", "addFormItem", {vo: voId, item: formItem})
    }
}
