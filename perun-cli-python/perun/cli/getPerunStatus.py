from rich import print
from perun_openapi import ApiException
from perun.rpc import PerunException
import perun.cli
import typer


def main() -> None:
    """ prints perun status"""
    rpc = perun.cli.rpc
    print('Status from', rpc.config.host)
    try:
        print(rpc.utils.get_perun_status())
    except ApiException as ex:
        print('error:', PerunException(ex))
        raise typer.Exit(code=1)
