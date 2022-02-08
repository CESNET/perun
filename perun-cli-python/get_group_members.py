#!/usr/bin/env python3
# -*- coding: utf8 -*-
import sys
from pprint import pprint
from cliutils import PerunRpc, options


def main(args):
	"""main function"""

	vo_name = args['voShortName']
	group_name = args['groupName']

	rpc = PerunRpc(args)

	# get VO by name
	vo = rpc.vos_manager().get_vo_by_short_name(vo_name)
	print('VO ', vo.id, vo.short_name, '"'+vo.name+'"')
	# get group object by its name
	group = rpc.groups_manager().get_group_by_name(vo.id, group_name)
	print('Group ', group.id, group.short_name, '"'+group.name+'"')
	# get members of group
	group_members = rpc.groups_manager().get_group_members(group.id)
	# get users for members
	users = rpc.users_manager().get_users_by_ids([member.user_id for member in group_members if member.status == 'VALID'])
	for user in users:
		print(user.id,user.first_name,user.last_name)


# calling main when invoked from CLI
if __name__ == "__main__":
	options = options("Outputs members of groups.")
	options.add_argument('-v', '--voShortName', help='short name of VO', required=True)
	options.add_argument('-g', '--groupName', help='name of group', required=True)
	sys.exit(main(vars(options.parse_args())) or 0)
