// Empty initial perunSession object
var voShortName = 'meta';

$(document).ready(function(){

     var serviceName = "mailman_meta";

     // Get VO id
     var vo = {};
     callPerunSync("vosManager", "getVoByShortName", vo, { shortName : voShortName });
     // Get member
     var member = {};
     callPerunSync("membersManager", "getMemberByUser", member, { vo : vo.id, user : user.id });
     // Get service
     var service = {};
     callPerunSync("servicesManager", "getServiceByName", service, { name : serviceName });

     // Fill basic info about the user
     $("#user-firstName").text(user.firstName);
     $("#user-lastName").text(user.lastName);
     $("#user-organization").text(userAttributes.organization);
     $("#user-preferredMail").text(userAttributes.preferredMail);
     $("#user-phone").text(userAttributes.phone);
     $("#user-preferredLanguage").text(userAttributes.preferredLanguage);
     $("#user-login").text(userAttributes['login-namespace:einfra']);

      $("#mailinglists-table").html(Configuration.LOADER_IMAGE);

      var resourcesAll = {};
      callPerunSync("resourcesManager", "getAssignedResources", resourcesAll, { member : member.id , service: service.id });

      // Filter only mailingList resources
      var resources = [];
      for (resourceId in resourcesAll) {
        //if (resourcesAll[resourceId].description.substring(0, 'mailing'.length) === 'mailing') {
          var resource = resourcesAll[resourceId];
          // Is member unsubscibed?
          var attr = {};
          callPerunSync("attributesManager", "getAttribute", attr, { member : member.id, resource : resource.id, attributeName : 'urn:perun:member_resource:attribute-def:def:optOutMailingList' });
          if (attr.value == 'true') {
            resource.actionUrl = '<a href="javascript:subscribe(' + member.id + ',' + resource.id + ')">Subscribe</a>';
          } else {
            resource.actionUrl = '<a href="javascript:unsubscribe(' + member.id + ',' + resource.id + ')">Unsubscribe</a>';
          }
          resources.push(resource);
        //}
      }

      var table = PerunTable.create();
      table.addColumn("description", "Name");
      table.addColumn("actionUrl", "Operation");
      table.add(resources);
      var tableHtml = table.draw();
      $("#mailinglists-table").html(tableHtml);

});

function subscribe(memberId, resourceId) {
  var attr = {};
  callPerunSync("attributesManager", "getAttribute", attr, { member : memberId, resource : resourceId, attributeName : 'urn:perun:member_resource:attribute-def:def:optOutMailingList' });
  attr.value = '';
  callPerunSyncPost("attributesManager", "setAttribute", attr, { member : memberId, resource : resourceId, attribute : attr });
  window.location.reload();
}

function unsubscribe(memberId, resourceId) {
  var attr = {};
  callPerunSync("attributesManager", "getAttribute", attr, { member : memberId, resource : resourceId, attributeName : 'urn:perun:member_resource:attribute-def:def:optOutMailingList' });
  attr.value = 'true';
  callPerunSyncPost("attributesManager", "setAttribute", attr, { member : memberId, resource : resourceId, attribute : attr })
  window.location.reload();
}
