from typing import Optional
from typer import Option
from perun_openapi import ApiException
from rich import print
from rich.console import Console
from rich.table import Table
from perun.rpc import PerunException
import perun.cli
import typer

from perun_openapi.model.attribute import Attribute
from perun_openapi.model.facility import Facility
from perun_openapi.model.facility_with_attributes import FacilityWithAttributes


def main(
         attr_name: str = Option('urn:perun:facility:attribute-def:def:administratorContact', '-a', '--attributeName', help='attribute name (namespace + : + friendlyName)'),
         attr_value: str = Option('martinkuba@gmail.com', '-v', '--attributeValue', help='short name of VO'),
         attr_names: str = typer.Option('urn:perun:facility:attribute-def:def:administratorContact', '-r', '--returnedAttributeNames', help='names of returned attributes'),
         ) -> None:
    """ facilities with attributes """
    rpc = perun.cli.rpc
    attr_names = attr_names.split(',')
    try:
        console = Console()
        facilities: list[Facility] = rpc.facilities_manager.get_facilities_by_attribute(attr_name, attr_value)
        table = Table(title="facilities")
        table.add_column("id", justify="right")
        table.add_column("name")
        table.add_column("description")
        for facility in facilities:
            table.add_row(str(facility.id), str(facility.name), str(facility.description))
        console.print(table)

        for facility in facilities:
            facility_attributes: list[Attribute] = perun.cli.rpc.attributes_manager.get_facility_attributes_by_names(facility.id, attr_names)
            table = Table(title="facility " + facility.name + " attributes")
            table.add_column("namespace")
            table.add_column("friendlyName")
            table.add_column("value")
            table.add_column("type")
            for a in facility_attributes:
                table.add_row(a['namespace'], a['friendlyName'], str(a['value']), a['type'])
            console.print(table)

        # does not work :-(
        # facilitiesWA: list[FacilityWithAttributes] \
        #    = rpc.facilities_manager.get_facilities_by_attribute_with_attributes(attr_name, attr_value, attr_names)

    except ApiException as ex:
        print('error name:', PerunException(ex).name)
        print('error message:', PerunException(ex).message)
    raise typer.Exit(code=1)
