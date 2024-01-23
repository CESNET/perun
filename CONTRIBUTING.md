# Contributing to Perun AAI

## General guidelines

See general guidelines for [contributing to Perun AAI](https://gitlab.ics.muni.cz/perun/common/-/blob/main/CONTRIBUTING.md).

Additional rules are outlined in this document.

## Commit Message Guidelines

Use the name of IdM component which is affected as the scope of the commit message where applicable:

- core
- openapi
- cli
- gui
- engine
- registrar
- dispatcher
- mvn

### Breaking Changes

Use `BREAKING CHANGE:`

- for backward-incompatible RPC API changes (method removed, parameter removed, modified behavior, etc.)
- change of DB version
- new required configuration
  - new required configuration file
  - new required property/value in a configuration file
