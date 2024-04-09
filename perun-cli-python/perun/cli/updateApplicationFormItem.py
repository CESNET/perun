from typing import List, Optional

import typer
from rich import print
from typing_extensions import Annotated

import perun.cli
from perun.rpc import PerunException
from perun_openapi import ApiException
from perun_openapi.model.application_form_item import ApplicationFormItem
from perun_openapi.model.input_update_form_item_texts import InputUpdateFormItemTexts


def main(
    form_item_id: int = typer.Option(
        None, "-id", "--form_item_id", help="application form item ID"
    ),
    en_options: Annotated[
        Optional[List[str]], typer.Option(default=None)
    ] = typer.Option(None, "-en", "--en", help="english option, use multiple times"),
    cs_options: Annotated[
        Optional[List[str]], typer.Option(default=None)
    ] = typer.Option(None, "-cs", "--cs", help="czech option, use multiple times"),
) -> None:
    """
    updates options in english and czech labels in a form item.

    For form items that have options (SELECTIONBOX, COMBOBOX, CHECKBOX, RADIO)
    replaces options separately for English and Czech languages.
    For a language, if no option is provided, options are removed.
    """
    rpc = perun.cli.rpc
    try:
        # get form item by its id
        form_item: ApplicationFormItem = rpc.registrar_manager.get_form_item_by_id(
            form_item_id
        )
        print("BEFORE")
        print(form_item)
        # update options in form item
        form_item.i18n["en"].options = (
            None if not en_options else "|".join([x + "#" + x for x in en_options])
        )
        form_item.i18n["cs"].options = (
            None if not cs_options else "|".join([x + "#" + x for x in cs_options])
        )
        rpc.registrar_manager.update_form_item_texts(
            InputUpdateFormItemTexts(form_item=form_item)
        )
        # check new value
        form_item: ApplicationFormItem = rpc.registrar_manager.get_form_item_by_id(
            form_item_id
        )
        print("AFTER")
        print(form_item)
    except ApiException as ex:
        print("error name:", PerunException(ex).name)
        print("error message:", PerunException(ex).message)
        raise typer.Exit(code=1)
