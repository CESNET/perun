from perun_openapi import ApiException
from rich import print
from rich.console import Console
from rich.table import Table
from cli import PerunException
import typer
import cli


def get_group_members(vo_name: str = typer.Option(..., '-v', '--voShortName', help='short name of VO'),
						group_name: str = typer.Option(..., '-g', '--groupName', help='name of group')) -> None:
	""" prints members of a group """
	rpc = cli.rpc
	try:
		vo = rpc.vos_manager.get_vo_by_short_name(vo_name)
		# get group object by its name
		group = rpc.groups_manager.get_group_by_name(vo.id, group_name)
		# get members of group
		group_members = rpc.groups_manager.get_group_members(group.id)
		# get users for members
		users = rpc.users_manager.get_users_by_ids(
			[member.user_id for member in group_members if member.status == 'VALID'])
		# print table
		table = Table(title="group " + vo.short_name + ":" + group.short_name + " members")
		table.add_column("user id", justify="right")
		table.add_column("first name")
		table.add_column("last name")
		for user in users:
			table.add_row(str(user.id), user.first_name, user.last_name)
		console = Console()
		console.print(table)
	except ApiException as ex:
		print('error:', PerunException(ex).name)
		raise typer.Exit(code=1)
