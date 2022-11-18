from rich import print
import cli


def main() -> None:
	""" prints the user of the authenticated session"""
	perun_principal = cli.rpc.authz_resolver.get_perun_principal()
	print(perun_principal.user)

