from perun_openapi import ApiException
from perun_openapi.api_client import ApiClient
from perun_openapi.configuration import Configuration
from perun_openapi.api.attributes_manager_api import AttributesManagerApi
from perun_openapi.api.authz_resolver_api import AuthzResolverApi
from perun_openapi.api.database_manager_api import DatabaseManagerApi
from perun_openapi.api.ext_sources_manager_api import ExtSourcesManagerApi
from perun_openapi.api.facilities_manager_api import FacilitiesManagerApi
from perun_openapi.api.groups_manager_api import GroupsManagerApi
from perun_openapi.api.members_manager_api import MembersManagerApi
from perun_openapi.api.owners_manager_api import OwnersManagerApi
from perun_openapi.api.registrar_manager_api import RegistrarManagerApi
from perun_openapi.api.resources_manager_api import ResourcesManagerApi
from perun_openapi.api.users_manager_api import UsersManagerApi
from perun_openapi.api.utils_api import UtilsApi
from perun_openapi.api.vos_manager_api import VosManagerApi
from perun_openapi.api.services_manager_api import ServicesManagerApi
from enum import Enum
from pathlib import Path
from datetime import datetime
from cryptography.fernet import Fernet, InvalidToken
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
import base64
import time
import jwt
import requests
import typer
import json
import yaml
import os
import sys
import subprocess


class PerunRpc:
	def __init__(self, config: Configuration):
		self.config = config
		self.api_client = ApiClient(config)
		self.api_client.user_agent = "Perun OpenAPI Python"
		self.attributes_manager = AttributesManagerApi(self.api_client)
		self.users_manager = UsersManagerApi(self.api_client)
		self.authz_resolver = AuthzResolverApi(self.api_client)
		self.database_manager = DatabaseManagerApi(self.api_client)
		self.ext_sources_manager = ExtSourcesManagerApi(self.api_client)
		self.facilities_manager = FacilitiesManagerApi(self.api_client)
		self.groups_manager = GroupsManagerApi(self.api_client)
		self.members_manager = MembersManagerApi(self.api_client)
		self.owners_manager = OwnersManagerApi(self.api_client)
		self.registrar_manager = RegistrarManagerApi(self.api_client)
		self.resources_manager = ResourcesManagerApi(self.api_client)
		self.users_manager = UsersManagerApi(self.api_client)
		self.utils = UtilsApi(self.api_client)
		self.vos_manager = VosManagerApi(self.api_client)
		self.services_manager = ServicesManagerApi(self.api_client)


class PerunException:
	def __init__(self, ex: ApiException):
		self.body = json.loads(ex.body)
		self.name = self.body['name']
		self.message = self.body['message']


class PerunInstance(str, Enum):
	cesnet = "cesnet",
	perun_dev = "perun-dev",
	einfra = "e-infra.cz",
	idm = "idm",
	idm_test = "idm-test",


class DeviceCodeOAuth:
	"""
	Class for authentication using OAuth Device Code grant
	"""
	def __init__(self, perun_instance: PerunInstance, encryption_password: str, debug: bool):
		self.debug = debug
		self.config_data_all = {
			PerunInstance.cesnet: {
				'metadata_url': 'https://login.cesnet.cz/oidc/.well-known/openid-configuration',
				'client_id': '363b656e-d139-4290-99cd-ee64eeb830d5',
				'scopes': 'openid perun_api perun_admin offline_access',
				'perun_api_url': 'https://perun.cesnet.cz/oauth/rpc'
			},
			PerunInstance.perun_dev: {
				'metadata_url': 'https://login.cesnet.cz/oidc/.well-known/openid-configuration',
				'client_id': '363b656e-d139-4290-99cd-ee64eeb830d5',
				'scopes': 'openid perun_api perun_admin offline_access',
				'perun_api_url': 'https://perun-dev.cesnet.cz/oauth/rpc'
			},
			PerunInstance.einfra: {
				'metadata_url': 'https://login.e-infra.cz/oidc/.well-known/openid-configuration',
				'client_id': '363b656e-d139-4290-99cd-ee64eeb830d5',
				'scopes': 'openid perun_api perun_admin offline_access',
				'perun_api_url': 'https://perun-api.e-infra.cz/oauth/rpc'
			},
			PerunInstance.idm_test: {
				'metadata_url': 'https://oidc.muni.cz/oidc/.well-known/openid-configuration',
				'client_id': '5a730abc-6553-4fc4-af9a-21c75c46e0c2',
				'scopes': 'openid perun_api perun_admin offline_access profile authn_details',
				'perun_api_url': 'https://idm-test.ics.muni.cz/oauth/rpc'
			},
			PerunInstance.idm: {
				'metadata_url': 'https://oidc.muni.cz/oidc/.well-known/openid-configuration',
				'client_id': '5a730abc-6553-4fc4-af9a-21c75c46e0c2',
				'scopes': 'openid perun_api perun_admin offline_access profile authn_details',
				'perun_api_url': 'https://idm.ics.muni.cz/oauth/rpc'
			}
		}
		self.config_data = self.config_data_all.get(perun_instance)
		self.CLIENT_ID = self.__get_oidc_option('client_id')
		self.SCOPES = self.__get_oidc_option('scopes')
		self.PERUN_API_URL = self.__get_oidc_option('perun_api_url')
		metadata = requests.get(self.__get_oidc_option('metadata_url')).json()
		self.ISSUER = metadata['issuer']
		self.DEVICE_CODE_URI = metadata['device_authorization_endpoint']
		self.TOKEN_ENDPOINT_URI = metadata['token_endpoint']
		self.JWKS_URI = metadata['jwks_uri']
		self.password_bytes = bytes(encryption_password, 'utf-8')
		self.tokens_path = self.__cache_dir() / 'tokens'
		self.salt_path = self.__cache_dir() / 'salt'
		self.tokens = self.__read_tokens_from_file()

	def get_perun_api_url(self) -> str:
		return self.PERUN_API_URL

	def get_access_token(self) -> str:
		if self.tokens:
			access_token = self.tokens.get('access_token')
			if self.__verify_token(access_token, 'access'):
				return access_token
			refresh_token = self.tokens.get('refresh_token')
			if self.__verify_token(refresh_token, 'refresh'):
				return self.__refresh_tokens(refresh_token)
		return self.__login()

	def __get_oidc_option(self, opt_name: str) -> str:
		opt_env_name = 'OIDC_' + opt_name.upper()
		opt_value = os.getenv(opt_env_name)
		if opt_value:
			return opt_value
		if self.config_data and opt_name in self.config_data:
			return self.config_data.get(opt_name)
		else:
			print('ERROR: value for option', opt_name, ' is not known', file=sys.stderr)
			raise typer.Exit(code=1)

	def __cache_dir(self) -> Path:
		"""
		Creates directory ~/.cache/perun if it does not exist yet
		:return:
		"""
		token_cache_dir = Path.home() / '.cache' / 'perun'
		if not token_cache_dir.exists():
			if self.debug:
				print('creating directory ', token_cache_dir)
			token_cache_dir.mkdir(mode=0o700, parents=True)
		return token_cache_dir

	def __store_tokens(self, token_data: dict) -> None:
		"""
		Stores tokens encrypted in the file ~/.cache/perun/tokens
		:param token_data:
		"""
		salt = os.urandom(16)
		kdf = PBKDF2HMAC(
			algorithm=hashes.SHA256(),
			length=32,
			salt=salt,
			iterations=390000,
		)
		key = base64.urlsafe_b64encode(kdf.derive(self.password_bytes))
		encrypted_message = Fernet(key).encrypt(bytes(yaml.safe_dump(token_data), 'utf-8'))

		def opener(path, flags):
			return os.open(path, mode=0o700, flags=flags)
		with open(self.tokens_path, 'wb', opener=opener) as f:
			f.write(encrypted_message)
		with open(self.salt_path, 'wb', opener=opener) as f:
			f.write(salt)

	def __read_tokens_from_file(self) -> dict | None:
		"""
		Reads tokens from encrypted file.
		"""
		try:
			with open(self.tokens_path, 'rb') as f:
				encrypted_message = f.read()
			with open(self.salt_path, 'rb') as f:
				salt = f.read()
			kdf = PBKDF2HMAC(algorithm=hashes.SHA256(),	length=32, salt=salt, iterations=390000)
			key = base64.urlsafe_b64encode(kdf.derive(self.password_bytes))
			if self.debug:
				print('reading stored tokens from ', self.tokens_path)
			return yaml.safe_load(Fernet(key).decrypt(encrypted_message))
		except (OSError, InvalidToken) as e:
			if self.debug:
				print('tokens not available in ', self.tokens_path)
				print(e)
			return None

	def __verify_token(self, token: str, token_type: str) -> bool:
		signing_key = jwt.PyJWKClient(self.JWKS_URI).get_signing_key_from_jwt(token)
		try:
			decoded_token = jwt.decode(token, signing_key.key, algorithms=['RS256', 'ES256'], audience=self.CLIENT_ID)
			iss = decoded_token.get('iss')
			if iss != self.ISSUER:
				if self.debug:
					print('issuer in stored', token_type, 'token', iss, 'does not match issuer from OIDC server', self.ISSUER)
				return False
			if self.debug:
				print(token_type, 'token verified')
				print(' issuer:', iss)
				iat = decoded_token.get('iat')
				if iat:
					print(' issued at: ', datetime.fromtimestamp(iat))
				exp = decoded_token.get('exp')
				if exp:
					print(' expiration:', datetime.fromtimestamp(exp))
			return True
		except jwt.ExpiredSignatureError:
			if self.debug:
				print(token_type, 'token has expired')
		except jwt.InvalidAudienceError:
			if self.debug:
				print(token_type, 'token is for another audience')
		except jwt.exceptions.InvalidSignatureError:
			if self.debug:
				print(token_type, 'token is from other OIDC server')
		return False

	def __login(self) -> str:
		"""
		Authenticate using OAUth 2.0 Device Code grant flow
		:return: access_token
		"""
		if self.debug:
			print('doing AuthRequest')
		device_code_response = requests.post(self.DEVICE_CODE_URI, data={'client_id': self.CLIENT_ID, 'scope': self.SCOPES})
		if device_code_response.status_code != 200:
			print('Error generating the device code')
			raise typer.Exit(code=1)
		device_code_data = device_code_response.json()
		print('**************************************************************************************')
		print('For authentication, navigate to: ', device_code_data['verification_uri_complete'])
		print('You can open the URL on your mobile phone by scanning the following QR code:')
		subprocess.run(['qrencode', '-t', 'UTF8', device_code_data['verification_uri_complete']])
		print('**************************************************************************************')
		print()
		token_payload = {
			'grant_type': 'urn:ietf:params:oauth:grant-type:device_code',
			'device_code': device_code_data['device_code'],
			'client_id': self.CLIENT_ID
		}
		authenticated = False
		while not authenticated:
			token_response = requests.post(self.TOKEN_ENDPOINT_URI, data=token_payload)
			token_data = token_response.json()
			if token_response.status_code == 200:
				if self.debug:
					print('authenticated')
				authenticated = True
			elif token_data['error'] not in ('authorization_pending', 'slow_down'):
				print(token_data['error_description'])
				raise typer.Exit(code=1)
			else:
				time.sleep(5)
		return self.__store_token_data(token_data)

	def __refresh_tokens(self, refresh_token: str) -> str:
		"""
		Use refresh_token to get new access_token and refresh_token
		:return: access token
		"""
		if self.debug:
			print('doing refresh')
		refresh_response = requests.post(self.TOKEN_ENDPOINT_URI, data={'grant_type': 'refresh_token', 'client_id': self.CLIENT_ID, 'refresh_token': refresh_token})
		if refresh_response.status_code != 200:
			print('Error refreshing tokens')
			if self.debug:
				print(refresh_response.json())
			raise typer.Exit(code=1)
		token_data = refresh_response.json()
		return self.__store_token_data(token_data)

	def __store_token_data(self, token_data: dict) -> str:
		access_token = token_data['access_token']
		refresh_token = token_data['refresh_token']
		if self.__verify_token(access_token, 'access') and self.__verify_token(refresh_token, 'refresh'):
			self.tokens = token_data
			self.__store_tokens(token_data)
			return access_token
		else:
			print('ERROR: obtained tokens are not valid', file=sys.stderr)
			raise typer.Exit(code=1)


rpc: PerunRpc


def rpc_client():
	return rpc


