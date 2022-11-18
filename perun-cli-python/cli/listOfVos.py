from perun_openapi import ApiException
from rich import print
from rich.console import Console
from rich.table import Table
from cli import PerunException
import typer
import cli


def main(sortById: bool = typer.Option(False, '-i', '--orderById', help='order by id'),
		 sortByName: bool = typer.Option(False, '-n', '--orderByName', help='order by short name')
		 ) -> None:
	""" prints list of all VOs """
	rpc = cli.rpc
	try:
		vos: list = cli.rpc.vos_manager.get_all_vos()
		if not vos:
			print('no vos found')
			return
		if sortById:
			vos.sort(key=lambda x: x.id)
		if sortByName:
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
