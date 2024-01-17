from typer import Option
from perun_openapi import ApiException
from rich import print
from rich.console import Console
from rich.table import Table
from perun.rpc import PerunException
import perun.cli
import typer

from perun_openapi.model.facility import Facility


def main(
    attr_name: str = Option(
        ...,
        "-a",
        "--attributeName",
        help="attribute name (namespace + : + friendlyName)",
    ),
    attr_value: str = Option(..., "-v", "--attributeValue", help="short name of VO"),
    sort_by_id: bool = typer.Option(False, "-i", "--orderById", help="order by id"),
    sort_by_name: bool = typer.Option(
        False, "-n", "--orderByName", help="order by short name"
    ),
) -> None:
    """search for facilities by attributeName and attributeValue"""
    rpc = perun.cli.rpc
    try:
        facilities: list[Facility] = rpc.facilities_manager.get_facilities_by_attribute(
            attr_name, attr_value
        )
        if sort_by_id:
            facilities.sort(key=lambda x: x.id)
        if sort_by_name:
            facilities.sort(key=lambda x: x.name)
        console = Console()
        # print user
        table = Table(title="facilities")
        table.add_column("id", justify="right")
        table.add_column("name")
        table.add_column("description")
        for facility in facilities:
            table.add_row(
                str(facility.id), str(facility.name), str(facility.description)
            )
        console.print(table)

    except ApiException as ex:
        print("error name:", PerunException(ex).name)
        print("error message:", PerunException(ex).message)
    raise typer.Exit(code=1)
