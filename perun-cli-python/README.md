# Python client for Perun RPC API

This folder contains a Python version of Perun RPC client.

## Installation

The script **generate.sh** creates folder *perun_openapi* 
containing Python files generated from the [OpenAPI description of Perun RPC](../perun-openapi/openapi.yml)
using [OpenAPI Generator](https://openapi-generator.tech/docs/usage#generate) which is downloaded and run.

The generated classes depend on "dateutil" library, which needs to be installed.
 
To prepare the CLI tools to run, do:
```bash
./generate.sh
apt install python3-dateutil python3-typing-extensions
```

Run the CLI programs like:
```bash
./get_user.py --PERUN_URL https://cloud1.perun-aai.org/ba/rpc \
              --PERUN_USER 'perun/test' \
              -id 1
```
