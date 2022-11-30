# Python client for Perun RPC API

This folder contains a Python version of Perun RPC client.

Python 3.10+ is required.

## Installation

The script **generate.sh** creates folder *perun_openapi* 
containing Python files generated from the [OpenAPI description of Perun RPC](../perun-openapi/openapi.yml)
using [OpenAPI Generator](https://openapi-generator.tech/docs/usage#generate) which is downloaded and run.
 
To prepare the CLI tools to run, do:
```bash
./generate.sh
apt install qrencode python3-dateutil python3-typing-extensions \
    python3-typer python3-rich python3-requests python3-jwt
```

Available commands and options can be displayed using:
```bash
./perun_cli.py --help
```
Run the CLI programs like:
```bash
./perun_cli.py getPerunPrincipal
./perun_cli.py getUser 3197
```

The client supports two types fo authentication - OIDC and HTTP Basic Auth. The default is OIDC.

## HTTP Basic Auth

```bash
./perun_cli.py --ba \
              --PERUN_URL https://cloud1.perun-aai.org/ba/rpc \
              --PERUN_USER perun \
              --PERUN_PASSWORD test \
              getPerunStatus
```
## OIDC authentication

The client contains configuration for Perun instances that support OIDC CLI client.
Instance can be selected using the `--instance` option, see `./perun_cli.py --help` for available values.

Access token and refresh token are stored encrypted in the file **~/.cache/perun/tokens**. If the file does not exist, 
all tokens are expired, or tokens belong to another Perun instance, a new authentication using OAuth Device Code grant is started. 

If only access token is expired and refresh token is valid, a new set of tokens is automatically obtained.

The password for encrypting the file with the tokens should be supplied using an environment variable, 
so that only child processes would know it:
```bash
export PERUN_INSTANCE=cesnet
export PERUN_ENCRYPT=myVeRySeCreTValuE
./perun_cli.py getPerunStatus
./perun_cli.py getPerunPrincipal
```
