from perun_openapi import ApiException
from rich import print
from rich.console import Console
from rich.table import Table
from perun.cli import PerunException
import perun.cli
import typer


def main(user_id: int = typer.Option(..., '-u', '--user_id', help='user ID')) -> None:
    """ prints list of user's roles"""
    try:
        role_names = perun.cli.rpc.authz_resolver.get_user_role_names(user=user_id)
        if not role_names:
            print('no role found')
            return
        # print table
        table = Table(title="roles of user " + str(user_id))
        table.add_column("role name")
        for role_name in role_names:
            table.add_row(str(role_name).upper())
        console = Console()
        console.print(table)
    except ApiException as ex:
        print('error:', PerunException(ex).name)
        raise typer.Exit(code=1)
