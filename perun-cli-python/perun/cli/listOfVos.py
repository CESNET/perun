from perun_openapi import ApiException
from rich import print
from rich.console import Console
from rich.table import Table
from perun.cli import PerunException
import perun.cli
import typer


def main(sort_by_id: bool = typer.Option(False, '-i', '--orderById', help='order by id'),
         sort_by_name: bool = typer.Option(False, '-n', '--orderByName', help='order by short name')
         ) -> None:
    """ prints list of all VOs """
    try:
        vos: list = perun.cli.rpc.vos_manager.get_all_vos()
        if not vos:
            print('no vos found')
            return
        if sort_by_id:
            vos.sort(key=lambda x: x.id)
        if sort_by_name:
            vos.sort(key=lambda x: x.short_name)
        # print table
        table = Table(title="all VOs ")
        table.add_column("id", justify="right")
        table.add_column("VO short name")
        table.add_column("VO name")
        for vo in vos:
            table.add_row(str(vo.id), str(vo.short_name), str(vo.name))
        console = Console()
        console.print(table)
    except ApiException as ex:
        print('error:', PerunException(ex).name)
        raise typer.Exit(code=1)
