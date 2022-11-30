from rich import print
import cli


def main() -> None:
	""" prints perun status"""
	rpc = cli.rpc
	print('Status from', rpc.config.host)
	print(rpc.utils.get_perun_status())
