#!/usr/bin/python3
import sys
import keyring #pip install keyring
import yaml #pip install pyyaml
import time
import requests
import os

def authentication_request():
	payload = {'client_id': get_configuration()['client_id'], 'scope': get_configuration()['scopes']}
	headers = {'content-type': "application/x-www-form-urlencoded"}

	res = requests.post(get_configuration()['oidc_device_code_uri'], headers=headers, data=payload)

	return res.json()


def token_request(params):
	payload = {'grant_type': 'urn:ietf:params:oauth:grant-type:device_code',
			   'client_id': get_configuration()['client_id'],
			   'device_code': params['device_code']}
	headers = {'content-type': "application/x-www-form-urlencoded"}

	res = requests.post(get_configuration()['oidc_token_endpoint_uri'], headers=headers, data=payload)

	return res.json()


def polling(params):
	interval = params['interval'] if 'interval' in params else 10

	while True:
		time.sleep(interval)
		response = token_request(params)
		if 'error' in response:
			error = response['error']
			if error == 'authorization_pending':
				continue
			elif error == 'slow_down':
				interval = interval + 2
				continue
			elif error == 'expired_token':
				print("Expired token, try again....")  # TODO start auth again
				return
			else:
				print(response['error'] + ": " + response['error_description'])
				exit
		else:
			return response


def authentication():
	response = authentication_request()
	if 'error' in response:
		print(response['error'] + ": " + response['error_description'])
		exit
	print('Please, authenticate at ' + response['verification_uri_complete'], file=sys.stderr)

	return polling(response)


def refresh(refresh_token):
	payload = {'grant_type': 'refresh_token',
			   'client_id': get_configuration()['client_id'],
			   'refresh_token': refresh_token}
	headers = {'content-type': "application/x-www-form-urlencoded"}

	res = requests.post(get_configuration()['oidc_token_endpoint_uri'], headers=headers, data=payload)

	return res.json()


def refresh_access_token():
	if get_refresh_token() != None:
		res = refresh(get_refresh_token())
		if 'error' not in res:
			keyring.set_password("perun_oidc", "access_token", res['access_token'])
			keyring.set_password("perun_oidc", "refresh_token", res['refresh_token'])
			return
	auth = authentication()
	res = refresh(auth['refresh_token'])
	keyring.set_password("perun_oidc", "access_token", res['access_token'])
	keyring.set_password("perun_oidc", "refresh_token", res['refresh_token'])

def get_access_token():
	return keyring.get_password("perun_oidc", "access_token")

def get_refresh_token():
	return keyring.get_password("perun_oidc", "refresh_token")

def set_access_token(access_token):
	keyring.set_password("perun_oidc", "access_token", access_token)

def set_refresh_token(refresh_token):
	keyring.set_password("perun_oidc", "refresh_token", refresh_token)

def get_configuration():
	with open(os.path.dirname(os.path.realpath(__file__)) + "/oidc_config.yml", "r") as stream:
		try:
			return yaml.safe_load(stream)
		except yaml.YAMLError as exc:
			print(exc)
			exit()

def main(arguments):
	"""
	if called with '-g' just gets access token, if none saved gets new one
	if called with '-r' refreshes and returns new access token saving new tokens
	"""
	if len(arguments) < 2 or (arguments[1] != "-r" and arguments[1] != "-g"):
		print("oidc_auth.py error: expected '-r' or '-g' argument!")
	elif arguments[1] == "-g":
		access_token = get_access_token()
		if access_token == None:
			refresh_access_token()
			print("access_token:" + get_access_token(), end="")
		else:
			print("access_token:" + access_token, end="")
	else:
		refresh_access_token()
		print("access_token:" + get_access_token(), end="")


if __name__ == "__main__":
	main(sys.argv)
