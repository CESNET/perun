#!/usr/bin/env python3
import typer
from perun_openapi.configuration import Configuration
from cli import PerunRpc, DeviceCodeOAuth, PerunInstance
import cli.getGroupMembers
import cli.getPerunPrincipal
import cli.getPerunStatus
import cli.getRichMember
import cli.getUser
import cli.listOfUserRoles
import cli.listOfVos
import cli.testAttributeValues

# see https://typer.tiangolo.com/tutorial/
app = typer.Typer()
app.command(name="getGroupMembers")(cli.getGroupMembers.get_group_members)
app.command(name="getPerunPrincipal")(cli.getPerunPrincipal.main)
app.command(name="getPerunStatus")(cli.getPerunStatus.main)
app.command(name="getRichMember")(cli.getRichMember.get_rich_member)
app.command(name="getUser")(cli.getUser.get_user)
app.command(name="listOfUserRoles")(cli.listOfUserRoles.main)
app.command(name="listOfVos")(cli.listOfVos.main)
app.command(name="testAttributeValues")(cli.testAttributeValues.test_attribute_values)


@app.callback()
def main(debug: bool = typer.Option(False, "--debug", "-d", help="enable debug output"),
		perun_instance: PerunInstance = typer.Option("cesnet",
												 "--instance",
												 "-i",
												 help="Perun instance for OIDC auth",
												 envvar="PERUN_INSTANCE"),
		encryption_password: str = typer.Option("s3cr3t",
												 "--encrypt",
												 "-e",
												 help="password for encrypting stored OIDC tokens",
												 envvar="PERUN_ENCRYPT"),
		basic_auth: bool = typer.Option(False, "--ba", '-b', help="use HTTP basic authorization"),
		perun_url: str = typer.Option("https://cloud1.perun-aai.org/ba/rpc",
									"--PERUN_URL",
									"-H",
									help="URL of Perun RPC API if needed for basic auth",
									envvar="PERUN_URL"),

		perun_user: str = typer.Option("perun",
									"--PERUN_USER",
									"-U",
									help="username if needed for HTTP basic auth",
									envvar="PERUN_USER"),
		perun_password: str = typer.Option("test",
										"--PERUN_PASSWORD",
										"-P",
										help="password if needed for HTTP basic auth",
										envvar="PERUN_PASSWORD")
		) -> None:
	"""
	Perun CLI in Python
	"""
	if basic_auth:
		cli.rpc = PerunRpc(Configuration(username=perun_user, password=perun_password, host=perun_url))
	else:
		dca = DeviceCodeOAuth(perun_instance, encryption_password, debug)
		cli.rpc = PerunRpc(Configuration(access_token=dca.get_access_token(), host=dca.get_perun_api_url()))
	cli.rpc.config.debug = debug


# calling typer when invoked from CLI
if __name__ == "__main__":
	app()
