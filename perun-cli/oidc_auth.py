#!/usr/bin/python3
import sys
import keyring #pip install keyring
import yaml #pip install pyyaml

def refreshAccessToken():
	if getRefreshToken() != None:
		# if available, use refresh token to get new AT
		# print new access token
		pass
	else:
		# get new access token
		#print new access token
		pass


def getAccessToken():
	#returns None if no access token is saved
	return keyring.get_password("perun_oidc", "access_token")

def getRefreshToken():
	#returns None if no refresh token is saved
	return keyring.get_password("perun_oidc", "refresh_token")

def setAccessToken(accessToken):
	keyring.set_password("perun_oidc", "access_token", accessToken)

def setRefreshToken(refreshToken):
	keyring.set_password("perun_oidc", "refresh_token", refreshToken)

def getConfiguration():
	# usage: getConfiguration()['client_id']
	with open("oidc_config.yml", "r") as stream:
		try:
			return yaml.safe_load(stream)
		except yaml.YAMLError as exc:
			print(exc)
			exit()

def main(arguments):
	# if called with '-g' just gets access token, if none saved gets new one
	# if called with '-r' refreshes and returns new access token saving new tokens
	if len(arguments) < 2 or (arguments[1] != "-r" and arguments[1] != "-g"):
		print("oidc_auth.py error: expected '-r' or '-g' argument!")
	elif arguments[1] == "-g":
		access_token = getAccessToken()
		if access_token == None:
			refreshAccessToken()
		else:
			print(access_token)
	else:
		refreshAccessToken()


if __name__ == "__main__":
	main(sys.argv)