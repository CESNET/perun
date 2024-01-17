from typer import Option
from perun_openapi import ApiException
from rich import print
from rich.console import Console
from rich.table import Table
from perun.rpc import PerunException
import perun.cli
import typer

from perun_openapi.model.attribute import Attribute
from perun_openapi.model.facility_with_attributes import FacilityWithAttributes


def main(
    attr_name: str = Option(
        "urn:perun:facility:attribute-def:def:administratorContact",
        "-a",
        "--attributeName",
        help="attribute name (namespace + : + friendlyName)",
    ),
    attr_value: str = Option(
        "martinkuba@gmail.com", "-v", "--attributeValue", help="short name of VO"
    ),
    attr_names: str = typer.Option(
        "urn:perun:facility:attribute-def:def:administratorContact",
        "-r",
        "--returnedAttributeNames",
        help="names of returned attributes",
    ),
) -> None:
    """facilities with attributes"""
    rpc = perun.cli.rpc
    attr_names = attr_names.split(",")
    try:
        console = Console()
        facilitiesWithAttributes: list[
            FacilityWithAttributes
        ] = rpc.facilities_manager.get_facilities_by_attribute_with_attributes(
            attr_name, attr_value, attr_names
        )
        table = Table(title="facilities")
        table.add_column("id", justify="right")
        table.add_column("name")
        table.add_column("description")
        for fwa in facilitiesWithAttributes:
            table.add_row(
                str(fwa.facility.id), fwa.facility.name, fwa.facility.description
            )
        console.print(table)

        for fwa in facilitiesWithAttributes:
            facility_attributes: list[Attribute] = fwa.attributes
            table = Table(title="facility " + fwa.facility.name + " attributes")
            table.add_column("namespace")
            table.add_column("friendlyName")
            table.add_column("value")
            table.add_column("type")
            for a in facility_attributes:
                table.add_row(
                    a["namespace"], a["friendlyName"], str(a["value"]), a["type"]
                )
            console.print(table)

        # does not work :-(

    except ApiException as ex:
        print("error name:", PerunException(ex).name)
        print("error message:", PerunException(ex).message)
    raise typer.Exit(code=1)
