#!/usr/bin/env python3
import sys
import subprocess
import typer
from enum import Enum
from perun_openapi.configuration import Configuration
from perun.oidc import DeviceCodeOAuth
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
import perun.cli.updateApplicationFormItem

# see https://typer.tiangolo.com/tutorial/
app = typer.Typer(add_completion=False)
app.command(name="updateApplicationFormItem")(perun.cli.updateApplicationFormItem.main)
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


class PerunInstance(str, Enum):
    """enumeration of Perun instances"""

    einfra = ("einfra",)
    einfra_acc = ("einfra_acc",)
    muni = ("muni",)
    muni_test = ("muni_test",)
    perun_dev = ("perun_dev",)
    elixir = ("elixir",)
    egi = ("egi",)


class PerunInstances:
    data: dict = {
        PerunInstance.einfra: {
            "issuer": "https://login.e-infra.cz/oidc/",
            "metadata_url": "https://login.e-infra.cz/oidc/.well-known/openid-configuration",
            "client_id": "363b656e-d139-4290-99cd-ee64eeb830d5",
            "scopes": "openid perun_api perun_admin offline_access",
            "perun_api_url": "https://perun-api.e-infra.cz/oauth/rpc",
            "perun_api_url_ba": "https://perun-api.e-infra.cz/ba/rpc",
            "perun_api_url_krb": "https://perun-api.e-infra.cz/krb/rpc",
            "mfa": True,
        },
        PerunInstance.einfra_acc: {
            "issuer": "https://login.e-infra.cz/oidc/",
            "metadata_url": "https://login.e-infra.cz/oidc/.well-known/openid-configuration",
            "client_id": "363b656e-d139-4290-99cd-ee64eeb830d5",
            "scopes": "openid perun_api perun_admin offline_access",
            "perun_api_url": "https://perun-api.acc.aai.e-infra.cz/oauth/rpc/",
            "perun_api_url_ba": "https://perun-api.acc.aai.e-infra.cz/ba/rpc/",
            "perun_api_url_krb": "https://perun-api.acc.aai.e-infra.cz/krb/rpc/",
            "mfa": True,
        },
        PerunInstance.perun_dev: {
            "issuer": "https://login.e-infra.cz/oidc/",
            "metadata_url": "https://login.e-infra.cz/oidc/.well-known/openid-configuration",
            "client_id": "363b656e-d139-4290-99cd-ee64eeb830d5",
            "scopes": "openid perun_api perun_admin offline_access",
            "perun_api_url": "https://api-dev.perun-aai.org/oauth/rpc",
            "perun_api_url_ba": "https://api-dev.perun-aai.org/ba/rpc",
            "perun_api_url_krb": "https://api-dev.perun-aai.org/krb/rpc",
            "mfa": False,
        },
        PerunInstance.muni: {
            "issuer": "https://id.muni.cz/oidc/",
            "metadata_url": "https://id.muni.cz/oidc/.well-known/openid-configuration",
            "client_id": "5a730abc-6553-4fc4-af9a-21c75c46e0c2",
            "scopes": "openid perun_api perun_admin offline_access",
            "perun_api_url": "https://perun-api.aai.muni.cz/oauth/rpc",
            "perun_api_url_ba": "https://perun-api.aai.muni.cz/ba/rpc",
            "perun_api_url_krb": "https://perun-api.aai.muni.cz/krb/rpc",
            "mfa": True,
        },
        PerunInstance.muni_test: {
            "issuer": "https://id.muni.cz/oidc/",
            "metadata_url": "https://id.muni.cz/oidc/.well-known/openid-configuration",
            "client_id": "5a730abc-6553-4fc4-af9a-21c75c46e0c2",
            "scopes": "openid perun_api perun_admin offline_access",
            "perun_api_url": "https://perun-api-test.aai.muni.cz/oauth/rpc",
            "perun_api_url_ba": "https://perun-api-test.aai.muni.cz/ba/rpc",
            "perun_api_url_krb": "https://perun-api-test.aai.muni.cz/krb/rpc",
            "mfa": True,
        },
        PerunInstance.elixir: {
            "issuer": "https://login.elixir-czech.org/oidc/",
            "metadata_url": "https://login.elixir-czech.org/oidc/.well-known/openid-configuration",
            "client_id": "da97db9f-b511-4c72-b71f-daab24b86884",
            "scopes": "openid perun_api perun_admin offline_access",
            "perun_api_url": "https://elixir-api.aai.lifescience-ri.eu/oauth/rpc",
            "perun_api_url_ba": "https://elixir-api.aai.lifescience-ri.eu/ba/rpc",
            "perun_api_url_krb": "",
            "mfa": True,
        },
        PerunInstance.egi: {
            "issuer": "",
            "metadata_url": "",
            "client_id": "",
            "scopes": "",
            "perun_api_url": "",
            "perun_api_url_ba": "https://api.perun.egi.eu/ba/rpc",
            "perun_api_url_krb": "https://api.perun.egi.eu/krb/rpc",
            "mfa": False,
        },
    }


@app.callback()
def main(
    debug: bool = typer.Option(False, "--debug", "-d", help="enable debug output"),
    perun_instance: PerunInstance = typer.Option(
        PerunInstance.einfra.value,
        "--instance",
        "-i",
        help="Perun instance",
        envvar="PERUN_INSTANCE",
    ),
    oidc_agent_auth: bool = typer.Option(
        True, "--oidc-agent-auth", "-a", help="use oidc-agent for authentication"
    ),
    basic_auth: bool = typer.Option(
        False, "--http-basic-auth", "-b", help="use HTTP basic authentication"
    ),
    krb_auth: bool = typer.Option(
        False, "--http-krb-auth", "-k", help="use HTTP Kerberos authentication"
    ),
    device_code_auth: bool = typer.Option(
        False,
        "--device-code-auth",
        "-c",
        help="use OIDC Device Code flow for authentication",
    ),
    perun_user: str = typer.Option(
        "perun",
        "--user",
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
    encryption_password: str = typer.Option(
        "s3cr3t",
        "--encrypt",
        "-e",
        help="password for encrypting stored OIDC tokens for device code authentication",
        envvar="PERUN_ENCRYPT",
    ),
    use_mfa: bool = typer.Option(
        False,
        "--mfa",
        "-m",
        help="request Multi-Factor Authentication during device code authentication",
    ),
    mfa_valid: int = typer.Option(
        8 * 60,
        "--mfa-valid",
        "-v",
        help="number of minutes MFA is considered valid during device code authentication",
    ),
) -> None:
    """
    Perun CLI in Python
    """
    config_data: dict = PerunInstances.data.get(perun_instance)
    if basic_auth:
        perun.cli.rpc = PerunRpc(
            Configuration(
                username=perun_user,
                password=perun_password,
                host=config_data["perun_api_url_ba"],
            )
        )
    elif krb_auth:
        perun.cli.rpc = PerunRpc(
            Configuration(
                username=perun_user,
                password=perun_password,
                host=config_data["perun_api_url_krb"],
            )
        )
    elif device_code_auth:
        dca = DeviceCodeOAuth(
            perun_instance.name,
            config_data["client_id"],
            config_data["scopes"],
            config_data["metadata_url"],
            encryption_password,
            use_mfa,
            config_data["mfa"],
            mfa_valid,
            debug,
        )
        perun.cli.rpc = PerunRpc(
            Configuration(
                access_token=dca.get_access_token(), host=config_data["perun_api_url"]
            )
        )
    elif oidc_agent_auth:
        import liboidcagent

        try:
            access_token = liboidcagent.get_access_token(perun_instance.name)
        except liboidcagent.OidcAgentError as e:
            print("ERROR oidc-agent: {}".format(e))
            subprocess.run(
                [
                    "oidc-gen",
                    "--manual",
                    "--client-id=" + config_data["client_id"],
                    "--client-secret=",
                    "--issuer=" + config_data["issuer"],
                    "--scope=openid perun_api perun_admin offline_access",
                    "--flow=device",
                    "--redirect-uri=",
                    perun_instance.name,
                ]
            )
            access_token = liboidcagent.get_access_token(perun_instance.name)
        perun.cli.rpc = PerunRpc(
            Configuration(access_token=access_token, host=config_data["perun_api_url"])
        )
    else:
        print("ERROR: no authentication selected", file=sys.stderr)
        raise typer.Exit(code=1)
    perun.cli.rpc.config.debug = debug


# calling typer when invoked from CLI
if __name__ == "__main__":
    app()
