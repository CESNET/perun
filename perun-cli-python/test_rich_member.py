#!/usr/bin/env python3
# -*- coding: utf8 -*-
import sys
from pprint import pprint
from cliutils import PerunRpc, options


def main(args):
	"""main function"""

	rpc = PerunRpc(args)

	# get VO by name
	vo = rpc.vos_manager().get_vo_by_short_name("cesnet")
	print('VO ', vo.id, vo.short_name, '"'+vo.name+'"')
	# get group object by its name
	group = rpc.groups_manager().get_group_by_name(vo.id, "members")
	print('Group ', group.id, group.short_name, '"'+group.name+'"')
	# get members of group
	group_members = rpc.groups_manager().get_group_members(group.id)
	rich_member = rpc.members_manager().get_rich_member(group_members[0].id)
	pprint(rich_member)
	user = rich_member.user
	pprint(user)


# calling main when invoked from CLI
if __name__ == "__main__":
	options = options("Outputs members of groups.")
	sys.exit(main(vars(options.parse_args())) or 0)
