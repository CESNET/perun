from perun_openapi import ApiException
from rich import print
from cli import PerunException
import typer
import cli


def get_user(user_id: int = typer.Option(..., '-u', '--user_id', help='user ID')) -> None:
	""" prints user for a given id"""
	try:
		user = cli.rpc.users_manager.get_user_by_id(user_id)
		print(user)
	except ApiException as ex:
		print('error:', PerunException(ex).name)
		raise typer.Exit(code=1)

