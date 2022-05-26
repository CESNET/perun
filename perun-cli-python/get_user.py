#!/usr/bin/env python3
# -*- coding: utf8 -*-
import sys
from cliutils import PerunRpc,options
from perun_openapi.rest import ApiException
from pprint import pprint

def main(args):
	"""main function"""

	# get manager
	users_manager = PerunRpc(args).users_manager()

	user_id = args['user']
	try:
		user = users_manager.get_user_by_id(user_id)
		pprint(user)

	except ApiException as e:
		print("Exception when calling : %s\n" % e)


# calling main when invoked from CLI
if __name__ == "__main__":
	options = options("Prints Perun user.")
	options.add_argument('-id', '--user', help='numeric id of user', type=int, required=True)
	sys.exit(main(vars(options.parse_args())) or 0)
