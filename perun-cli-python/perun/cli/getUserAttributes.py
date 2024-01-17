from perun_openapi import ApiException
from rich import print
from rich.console import Console
from rich.table import Table
from perun.rpc import PerunException
import typer
import perun.cli
from perun_openapi.model.attribute import Attribute
from perun_openapi.model.rich_user import RichUser
from perun_openapi.model.user import User


def main(user_id: int = typer.Option(3197, "-u", "--user_id", help="user ID")) -> None:
    """prints user for a given id"""
    try:
        console: Console = Console()
        # print user
        user: User = perun.cli.rpc.users_manager.get_user_by_id(user_id)
        table: Table = Table(title="user")
        table.add_column("id", justify="right")
        table.add_column("first_name")
        table.add_column("last_name")
        table.add_column("createdAt")
        table.add_row(
            str(user.id), user.first_name, user.last_name, str(user.createdAt)
        )
        console.print(table)
        # print user attributes
        user_attributes: list[
            Attribute
        ] = perun.cli.rpc.attributes_manager.get_user_attributes(user_id)
        if user_attributes:
            table = Table(title="user attributes")
            table.add_column("namespace")
            table.add_column("friendlyName")
            table.add_column("value")
            table.add_column("type")
            for a in user_attributes:
                table.add_row(
                    a["namespace"], a["friendlyName"], str(a["value"]), a["type"]
                )
            console.print(table)

    except ApiException as ex:
        print("error:", PerunException(ex).name)
        raise typer.Exit(code=1)
