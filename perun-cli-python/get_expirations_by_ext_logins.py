#!/usr/bin/env python3
# -*- coding: utf8 -*-
import sys
from cliutils import PerunRpc,options
from pprint import pprint

def main(args):
	"""main function"""

	# get managers
	rpc = PerunRpc(args)
	users_manager = rpc.users_manager()
	members_manager = rpc.members_manager()
	attributes_manager = rpc.attributes_manager()

	f = open(args['file'], "r")
	for x in f:
		x = x.strip(' \n') + '@idp.e-infra.cz'
		user = users_manager.get_user_by_ext_source_name_and_ext_login(ext_login=x,ext_source_name=args['extSourceName'])
		member = members_manager.get_member_by_user(vo=args['voId'], user=user.id)
		attr = attributes_manager.get_member_attribute_by_name(member.id,"urn:perun:member:attribute-def:def:membershipExpiration")
		print('user', user.first_name, user.last_name, 'status', member.status, 'expires', attr.value)
	f.close()

# calling main when invoked from CLI
if __name__ == "__main__":
	options = options("Outputs member expirations in given VO.")
	options.add_argument('-f', '--file', help='file with user logins', required=True)
	options.add_argument('-v', '--voId', help='id of VO', default=21, required=False, type=int)
	options.add_argument('-E', '--extSourceName', help='name of external source', required=False, default='https://idp.e-infra.cz/idp/')
	sys.exit(main(vars(options.parse_args())) or 0)
