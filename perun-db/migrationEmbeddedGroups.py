#!/usr/bin/env python3

import requests
import sys

# Authentication details
BASE_URL = "https://api-dev.perun-aai.org/ba/rpc/json/"
LOGIN = ""
PASSWORD = ""

# RPC methods
GET_ALL_GROUPS_FOR_AUTO_REGISTRATION = (
    "registrarManager/getAllGroupsForAutoRegistration"
)
GET_FORM_ITEMS = "registrarManager/getFormItems"  # voId
ADD_EMBEDDED_GROUPS = (
    "registrarManager/addGroupsToAutoRegistration"  # list of groups IDs, formItem id
)


def check_error_occured(data, error_name):
    """Checks, if exception with given error name was thrown for request.
    If different exception raised, exits script with error message.
    If error_name is None, exits script on any raised error.

    :param data: endpoint json response
    :param error_name: name of expected exception
    :return: true if exception was thrown, false otherwise
    """
    if data is None or isinstance(data, int):
        return
    if "errorId" in data:
        if error_name is None or ("name" in data and data["name"] != error_name):
            sys.exit(data)
        else:
            return True
    return False


def get_all_embedded_groups():
    params = {}
    r = requests.get(
        url=BASE_URL + GET_ALL_GROUPS_FOR_AUTO_REGISTRATION,
        params=params,
        auth=(LOGIN, PASSWORD),
    )
    groups_data = r.json()
    check_error_occured(groups_data, None)
    return groups_data


def get_form_items(vo_id):
    params = {"vo": vo_id}
    r = requests.post(
        url=BASE_URL + GET_FORM_ITEMS, json=params, auth=(LOGIN, PASSWORD)
    )
    form_items = r.json()
    check_error_occured(form_items, None)
    return form_items


def add_embedded_groups(groups_ids, form_item_id):
    params = {"groups": groups_ids, "formItem": form_item_id}
    r = requests.post(
        url=BASE_URL + ADD_EMBEDDED_GROUPS, json=params, auth=(LOGIN, PASSWORD)
    )
    form_items = r.json()
    check_error_occured(form_items, None)
    return form_items


def main():
    groups = get_all_embedded_groups()
    for group in groups:
        vo_id = group["voId"]
        group_id = group["id"]
        form_items = get_form_items(vo_id)
        filtered_items = [
            item["id"]
            for item in form_items
            if item["type"] == "EMBEDDED_GROUP_APPLICATION"
        ]
        if len(filtered_items) > 1:
            sys.exit(
                "Vo application of the vo with id: "
                + str(vo_id)
                + " has more then one form item with the type 'EMBEDDED_GROUP_APPLICATION'"
            )
        add_embedded_groups([group_id], filtered_items[0])
        print(
            "Inserted Group id: "
            + str(group_id)
            + ", Form item id: "
            + str(filtered_items[0])
            + " into Vo auto registration of Vo id: "
            + str(vo_id)
        )


if __name__ == "__main__":
    main()
