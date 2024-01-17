#!/usr/bin/env python3
import typer
from perun_openapi.configuration import Configuration
from perun.oidc import DeviceCodeOAuth, PerunInstance
from perun.rpc import PerunRpc
import perun.cli.getFacilitiesByAttribute
import perun.cli.getFacilitiesByAttributeWA
import perun.cli.getGroupMembers
import perun.cli.getPerunPrincipal
import perun.cli.getPerunStatus
import perun.cli.getRichMember
import perun.cli.getUser
import perun.cli.getUserAttributes
import perun.cli.listOfMyVos
import perun.cli.listOfUserRoles
import perun.cli.listOfVos
import perun.cli.testAttributeValues

# see https://typer.tiangolo.com/tutorial/
app = typer.Typer(add_completion=False)
app.command(name="getFacilitiesByAttribute")(perun.cli.getFacilitiesByAttribute.main)
app.command(name="getFacilitiesByAttributeWA")(
    perun.cli.getFacilitiesByAttributeWA.main
)
app.command(name="getGroupMembers")(perun.cli.getGroupMembers.get_group_members)
app.command(name="getPerunPrincipal")(perun.cli.getPerunPrincipal.main)
app.command(name="getPerunStatus")(perun.cli.getPerunStatus.main)
app.command(name="getRichMember")(perun.cli.getRichMember.get_rich_member)
app.command(name="getUser")(perun.cli.getUser.get_user)
app.command(name="getUserAttributes")(perun.cli.getUserAttributes.main)
app.command(name="listOfMyVos")(perun.cli.listOfMyVos.main)
app.command(name="listOfUserRoles")(perun.cli.listOfUserRoles.main)
app.command(name="listOfVos")(perun.cli.listOfVos.main)
app.command(name="testAttributeValues")(
    perun.cli.testAttributeValues.test_attribute_values
)


@app.callback()
def main(
    debug: bool = typer.Option(False, "--debug", "-d", help="enable debug output"),
    perun_instance: PerunInstance = typer.Option(
        PerunInstance.einfra.value,
        "--instance",
        "-i",
        help="Perun instance for OIDC auth",
        envvar="PERUN_INSTANCE",
    ),
    encryption_password: str = typer.Option(
        "s3cr3t",
        "--encrypt",
        "-e",
        help="password for encrypting stored OIDC tokens",
        envvar="PERUN_ENCRYPT",
    ),
    mfa: bool = typer.Option(
        False, "--mfa", "-m", help="request Multi-Factor Authentication"
    ),
    mfa_valid: int = typer.Option(
        8 * 60, "--mfa-valid", "-v", help="number of minutes MFA is considered valid"
    ),
    basic_auth: bool = typer.Option(False, "--ba", "-b", help="use HTTP basic auth"),
    perun_url: str = typer.Option(
        "https://cloud1.perun-aai.org/ba/rpc",
        "--url",
        "-U",
        help="Perun RPC API URL for basic auth",
        envvar="PERUN_URL",
    ),
    perun_user: str = typer.Option(
        "perun",
        "--username",
        "-u",
        help="username for HTTP basic auth",
        envvar="PERUN_USER",
    ),
    perun_password: str = typer.Option(
        "test",
        "--password",
        "-p",
        help="password for HTTP basic auth",
        envvar="PERUN_PASSWORD",
    ),
) -> None:
    """
    Perun CLI in Python
    """
    if basic_auth:
        perun.cli.rpc = PerunRpc(
            Configuration(username=perun_user, password=perun_password, host=perun_url)
        )
    else:
        dca = DeviceCodeOAuth(
            perun_instance, encryption_password, mfa, mfa_valid, debug
        )
        perun.cli.rpc = PerunRpc(
            Configuration(
                access_token=dca.get_access_token(), host=dca.get_perun_api_url()
            )
        )
    perun.cli.rpc.config.debug = debug


# calling typer when invoked from CLI
if __name__ == "__main__":
    app()
