import perun_openapi
import perun_openapi.api
import perun_openapi.api.attributes_manager_api
import perun_openapi.api.authz_resolver_api
import perun_openapi.api.database_manager_api
import perun_openapi.api.ext_sources_manager_api
import perun_openapi.api.facilities_manager_api
import perun_openapi.api.groups_manager_api
import perun_openapi.api.members_manager_api
import perun_openapi.api.owners_manager_api
import perun_openapi.api.registrar_manager_api
import perun_openapi.api.resources_manager_api
import perun_openapi.api.users_manager_api
import perun_openapi.api.utils_api
import perun_openapi.api.vos_manager_api
import perun_openapi.api.services_manager_api
import os
import sys
import argparse

def options(description="Some command"):
	parser = argparse.ArgumentParser(description=description)
	parser.add_argument('-D', '--debug', help='debugging output', action='store_const', const=True, default=False, required=False)
	parser.add_argument('-U', '--PERUN_URL', help='URL of Perun RPC', required=False)
	parser.add_argument('-P', '--PERUN_USER', help='user/password', required=False)
	return parser

class PerunRpc:
	def __init__(self, args):
		# configure authentication
		perun_user = None
		if os.getenv('PERUN_USER'):
			perun_user = os.getenv('PERUN_USER')
		elif args['PERUN_USER']:
			perun_user = args['PERUN_USER']
		else:
			print("user for authentication is unknown, user PERUN_USER env var or -P/--PERUN_USER option",
				  file=sys.stderr)
			exit(1)
		sa = perun_user.split('/', maxsplit=1)
		if len(sa) < 2:
			print("PERUN_USER must have format username/password", file=sys.stderr)
			exit(1)

		self.config = PerunCliConfiguration(username=sa[0], password=sa[1])
		# configure debugging
		self.config.debug = args['debug']
		# configure URL
		if os.getenv('PERUN_URL'):
			self.config.host = os.getenv('PERUN_URL')
		if args['PERUN_URL']:
			self.config.host = args['PERUN_URL']

		self.api_client = perun_openapi.api_client.ApiClient(self.config)
		self.api_client.user_agent = "Perun OpenAPI Python CLI"

	def attributes_manager(self):
		return perun_openapi.api.attributes_manager_api.AttributesManagerApi(self.api_client)

	def authz_resolver(self):
		return perun_openapi.api.authz_resolver_api.AuthzResolverApi(self.api_client)

	def database_manager(self):
		return perun_openapi.api.database_manager_api.DatabaseManagerApi(self.api_client)

	def ext_sources_manager(self):
		return perun_openapi.api.ext_sources_manager_api.ExtSourcesManagerApi(self.api_client)

	def facilities_manager(self):
		return perun_openapi.api.facilities_manager_api.FacilitiesManagerApi(self.api_client)

	def groups_manager(self):
		return perun_openapi.api.groups_manager_api.GroupsManagerApi(self.api_client)

	def members_manager(self):
		return perun_openapi.api.members_manager_api.MembersManagerApi(self.api_client)

	def owners_manager(self):
		return perun_openapi.api.owners_manager_api.OwnersManagerApi(self.api_client)

	def registrar_manager(self):
		return perun_openapi.api.registrar_manager_api.RegistrarManagerApi(self.api_client)

	def resources_manager(self):
		return perun_openapi.api.resources_manager_api.ResourcesManagerApi(self.api_client)

	def users_manager(self):
		return perun_openapi.api.users_manager_api.UsersManagerApi(self.api_client)

	def utils(self):
		return perun_openapi.api.utils_api.UtilsApi(self.api_client)

	def vos_manager(self):
		return perun_openapi.api.vos_manager_api.VosManagerApi(self.api_client)

	def services_manager(self):
		return perun_openapi.api.services_manager_api.ServicesManagerApi(self.api_client)

class PerunCliConfiguration(perun_openapi.configuration.Configuration):
	def auth_settings(self):
		"""Overrides Auths generated from openapi.yml because the Python client uses them all at once"""
		return {
			'BasicAuth':
				{
					'type': 'basic',
					'in': 'header',
					'key': 'Authorization',
					'value': self.get_basic_auth_token()
				},
		}
