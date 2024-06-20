# Python client for Perun RPC API
This folder contains a Python version of Perun RPC client.

Python 3.9+ is required.
## Installation

To prepare the CLI tools to run, do as root:

### Dependencies on Ubuntu 22.04:
```bash
apt install openjdk-17-jdk-headless qrencode python3-dateutil python3-typing-extensions \
    python3-typer python3-rich python3-requests python3-jwt
```

### Dependencies on Debian 11:
```bash
echo "deb http://deb.debian.org/debian bullseye-backports main" >>/etc/apt/sources.list
apt update
apt install -t bullseye-backports openjdk-17-jdk-headless qrencode python3-dateutil python3-typing-extensions \
    python3-typer python3-rich python3-requests python3-jwt python3-yaml
```

### Generate Python library for RPC access

The script **generate.sh** creates folder *perun_openapi* 
containing Python files generated from the [OpenAPI description of Perun RPC](../perun-openapi/openapi.yml)
using [OpenAPI Generator](https://openapi-generator.tech/docs/usage#generate) which is downloaded and run.
```bash
./generate.sh
```

## Usage

Available commands and options can be displayed using:
```bash
./perun_cli.py --help
```
Run the CLI programs like:
```bash
./perun_cli.py getPerunPrincipal
./perun_cli.py --debug getUser --user_id 3197
```

# Authentication

The client contains configuration for Perun instances.
Instance can be selected using the `--instance` option or `PERUN_INSTANCE` environment variable,
see `./perun_cli.py --help` for available values.

The client supports three types of authentication:
- OIDC with token obtained from oidc-agent
- OIDC with token obtained using Device Code flow
- HTTP Basic Auth

## OIDC with token obtained from oidc-agent

This is the default. The oidc-agent package and the Python library liboidcagent must be installed:
```bash
pip install liboidcagent
sudo apt install oidc-agent
eval `oidc-agent-service use`

./perun_cli.py -i muni getPerunPrincipal
```
If the oidc-agent configuration for the selected Perun instance was not created yet,
it is created by running oidc-gen with parameters for the selected instance.

## OIDC with token obtained using Device Code flow

Access token and refresh token are stored encrypted in the file **~/.cache/perun/tokens**. If the file does not exist, 
all tokens are expired, or tokens belong to another Perun instance, a new authentication using OAuth Device Code grant is started. 

If only access token is expired and refresh token is valid, a new set of tokens is automatically obtained.

The password for encrypting the file with the tokens should be supplied using an environment variable, 
so that only child processes would know it:
```bash
export PERUN_INSTANCE=cesnet
export PERUN_ENCRYPT=myVeRySeCreTValuE
./perun_cli.py --device-code-auth getPerunPrincipal
```
### Multi-Factor Authentication

The switch `--mfa` requests an MFA. The second factor authentication must not be older than certain time that
can be specified using the `--mfa-valid <minutes>` switch, the default is 480 minutes or 8 hours.

## HTTP Basic Auth

```bash
./perun_cli.py --http-basic-auth \
              --username perun \
              --password test \
              getPerunStatus
```
