package Perun::AttributesAgent;

use strict;
use warnings;

use Perun::Common;

my $manager = 'attributesManager';

use fields qw(_agent _manager);

sub new
{
	my $self = fields::new(shift);
	$self->{_agent} = shift;
	$self->{_manager} = $manager;

	return $self;
}

#(facility => $facilityId)
#(vo => $voId)
#(group => $groupId)
#(resource => $resourceId)
#(resource => $resourceId, member => $memberId)
#(member => $memberId, workWithUserAttributes => $boolean)
sub getAttributes
{
	return Perun::Common::callManagerMethod('getAttributes', '[]Attribute', @_);
}

#(facility => $facilityId, attributes => $arrayOfAttributes)
#(vo => $voId, attributes => $arrayOfAttributes)
#(resource => $resourceId, attributes => $arrayOfAttributes)
#(resource => $resourceId, member => $memberId, attributes => $arrayOfAttributes)
sub setAttributes
{
	return Perun::Common::callManagerMethod('setAttributes', '', @_);
}

#(facility => $facilityId, attributeName => $string)
#(vo => $voId, attributeName => $string)
#(resource => $resourceId, attributeName => $string)
#(resource => $resourceId, member => $memberId, attributeName => $string)
#(key => $string_key, attributeName => $string)
sub getAttribute
{
	return Perun::Common::callManagerMethod('getAttribute', 'Attribute', @_);
}

#(attributeName => $string)
sub getAttributeDefinition
{
	return Perun::Common::callManagerMethod('getAttributeDefinition', 'AttributeDefinition', @_);
}

#()
sub getAttributesDefinition
{
	return Perun::Common::callManagerMethod('getAttributesDefinition', '[]AttributeDefinition', @_);
}

#(namespace => $string)
sub  getAttributesDefinitionByNamespace
{
	return Perun::Common::callManagerMethod(' getAttributesDefinitionByNamespace', '[]AttributeDefinition', @_);
}

#(id => $number)
sub getAttributeDefinitionById
{
	return Perun::Common::callManagerMethod('getAttributeDefinitionById', 'AttributeDefinition', @_);
}

#(facility => $facilityId, id => $number)
#(vo => $voId, id => $number)
#(resource => $resourceId, id => $number)
#(resource => $resourceId, member => $memberId, id => $number)
sub getAttributeById
{
	return Perun::Common::callManagerMethod('getAttributeById', 'Attribute', @_);
}

#(facility => $facilityId, attribute => $attribute)
#(vo => $voId, attribute => $attribute)
#(group => $groupId, attribute => $attribute)
#(resource => $resourceId, attribute => $attribute)
#(resource => $resourceId, member => $memberId, attribute => $attribute)
#(key => $string_key, attribute => $attribute)
sub setAttribute
{
	return Perun::Common::callManagerMethod('setAttribute', '', @_);
}

#(attribute => $attribute)
#(attribute => $attribute, defaultAttribute => $attributeId)
sub createAttribute
{
	return Perun::Common::callManagerMethod('createAttribute', 'AttributeDefinition', @_);
}

#(attribute => $attributeId)
sub deleteAttribute
{
	return Perun::Common::callManagerMethod('deleteAttribute', '', @_);
}

#(service => $serviceId, facility => $facilityId)
#(service => $serviceId, resource => $resourceId)
#(service => $serviceId, resource => $resourceId, member => $memberId)
#(facility => $facilityId)
#(resource => $resourceId)
#(resource => $resourceId, member => $memberId)
#(member => $memberId, workWithUserAttributes => $boolean)
#(user => $userId)
sub getRequiredAttributes
{
	return Perun::Common::callManagerMethod('getRequiredAttributes', '[]Attribute', @_);
}

#(service => $serviceId)
sub getRequiredAttributesDefinition
{
	return Perun::Common::callManagerMethod('getRequiredAttributesDefinition', '[]AttributeDefinition', @_);
}

#(resourceToGetServicesFrom => $resourceId, member => $memberId)
#(resourceToGetServicesFrom => $resourceId, resource => $resourceId, member => $memberId)
#(resourceToGetServicesFrom => $resourceId, user => $userId)
#(resourceToGetServicesFrom => $resourceId, facility => $facilityId, user => $userId)
#(resourceToGetServicesFrom => $resourceId, group => $groupId)
#(resourceToGetServicesFrom => $resourceId, resource => $resource2Id,  group => $groupId)
sub getResourceRequiredAttributes
{
	return Perun::Common::callManagerMethod('getResourceRequiredAttributes', '[]Attribute', @_);
}

#(facility => $facilityId)
sub getAllRequiredAttributes
{
	return Perun::Common::callManagerMethod('getAllRequiredAttributes', '[]Attribute', @_);
}

#(resource => $resourceId, attribute => $attribute)
#(resource => $resourceId, member => $memberId, attribute => $attribute)
sub fillAttribute
{
	return Perun::Common::callManagerMethod('fillAttribute', 'Attribute', @_);
}

#(resource => $resourceId, attributes => $arrayOfattributes)
#(resource => $resourceId, member => $memberId, attributes => $arrayOfattributes)
sub fillAttributes
{
	return Perun::Common::callManagerMethod('fillAttributes', '[]Attribute', @_);
}

#(facility => $facilityId, attribute => $attribute)
#(vo => $voId, attribute => $attribute)
#(resource => $resourceId, attribute => $attribute)
#(resource => $resourceId, member => $memberId, attribute => $attribute)
sub checkAttributeValue
{
	return Perun::Common::callManagerMethod('checkAttributeValue', '', @_);
}

#(facility => $facilityId, attributes => $arrayOfAttributes)
#(vo => $voId, attributes => $arrayOfAttributes)
#(resource => $resourceId, attributes => $arrayOfAttributes)
#(resource => $resourceId, member => $memberId, attributes => $arrayOfAttributes)
sub checkAttributesValue
{
	return Perun::Common::callManagerMethod('checkAttributeValue', '', @_);
}

#(facility => $facilityId, attributes => $arrayOfAttributeIds)
#(vo => $voId, attributes => $arrayOfAttributeIds)
#(resource => $resourceId, attributes => $arrayOfAttributeIds)
#(resource => $resourceId, member => $memberId, attributes => $arrayOfAttributeIds)
sub removeAttributes
{
	return Perun::Common::callManagerMethod('removeAttributes', '', @_);
}

#(facility => $facilityId, attribute => $attributeId)
#(vo => $voId, attribute => $attributeId)
#(resource => $resourceId, attribute => $attributeId)
#(resource => $resourceId, member => $memberId, attribute => $attributeId)
sub removeAttribute
{
	return Perun::Common::callManagerMethod('removeAttribute', '', @_);
}

#(facility => $facilityId)
#(vo => $voId)
#(resource => $resourceId)
#(resource => $resourceId, member => $memberId)
sub removeAllAttributes
{
	return Perun::Common::callManagerMethod('removeAllAttributes', '', @_);
}

#(member => $memberId)
sub doTheMagic
{
	return Perun::Common::callManagerMethod('doTheMagic', '', @_);
}

sub getLogins
{
	return Perun::Common::callManagerMethod('getLogins', '[]Attribute', @_);
}

sub convertAttributeToUnique
{
	return Perun::Common::callManagerMethod('convertAttributeToUnique','',@_);
}

sub getAttributeRights
{
	return Perun::Common::callManagerMethod('getAttributeRights','[]AttributeRights',@_);
}

sub setAttributeRights
{
	return Perun::Common::callManagerMethod('setAttributeRights','',@_);
}

1;
