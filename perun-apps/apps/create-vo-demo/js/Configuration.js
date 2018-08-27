configuration = {
    LOADER_IMAGE: "/apps-include/img/loader_white-bg.gif",
    REGISTRATION_LINK: "https://perun.demo.eduteams.org/fed/registrar/?vo=eduTEAMS&targetnew=https%3A%2F%2Fperun.demo.eduteams.org%2Fa%2Festablish-vo-form%2Ffed%2F&targetexisting=https%3A%2F%2Fperun.demo.eduteams.org%2Fa%2Festablish-vo-form%2Ffed%2F&targetextended=https%3A%2F%2Fperun.demo.eduteams.org%2Fa%2Festablish-vo-form%2Ffed%2F",
    PROCEED_VO_MANAGEMENT_LINK: "https://perun.demo.eduteams.org/fed/gui/",
    PRIVACY_POLICY_TEXT: "eduTEAMS privacy policy",
    PRIVACY_POLICY_LINK: "https://wiki.geant.org/display/ED/eduTEAMS+Demo+Platform+Privacy+Policy",

    voFormItems: [{
        "shortname": "Heading",
        "required": false,
        "type": "HEADING",
        "federationAttribute": "",
        "perunDestinationAttribute": null,
        "regex": "",
        "applicationTypes": ["INITIAL"],
        "ordnum": 0,
        "forDelete": false,
        "i18n": {
            "en": {
                "locale": "en",
                "label": "", // will be set before sending request
                "options": null,
                "help": "",
                "errorMessage": ""
            },
            "cs": {
                "locale": "cs",
                "label": "", // will be set before sending request
                "options": null,
                "help": "",
                "errorMessage": ""
            }
        },
        "beanName": "ApplicationFormItem"
    }, {
        "shortname": "name",
        "required": true,
        "type": "TEXTFIELD",
        "federationAttribute": "displayName",
        "perunDestinationAttribute": "urn:perun:user:attribute-def:core:displayName",
        "regex": "",
        "applicationTypes": ["INITIAL"],
        "ordnum": 1,
        "forDelete": false,
        "i18n": {
            "en": {"locale": "en", "label": "Name", "options": null, "help": "", "errorMessage": ""},
            "cs": {"locale": "cs", "label": "Jméno", "options": null, "help": "", "errorMessage": ""}
        },
        "beanName": "ApplicationFormItem"
    }, {
        "shortname": "displayNameFromIdP",
        "required": false,
        "type": "FROM_FEDERATION_HIDDEN",
        "federationAttribute": "displayName",
        "perunDestinationAttribute": null,
        "regex": "",
        "applicationTypes": ["INITIAL"],
        "ordnum": 2,
        "forDelete": false,
        "i18n": {
            "en": {"locale": "en", "label": "", "options": null, "help": "", "errorMessage": ""},
            "cs": {"locale": "cs", "label": "", "options": null, "help": "", "errorMessage": ""}
        },
        "beanName": "ApplicationFormItem"
    }, {
        "shortname": "preferredMail",
        "required": true,
        "type": "VALIDATED_EMAIL",
        "federationAttribute": "mail",
        "perunDestinationAttribute": "urn:perun:user:attribute-def:def:preferredMail",
        "regex": "",
        "applicationTypes": ["INITIAL"],
        "ordnum": 3,
        "forDelete": false,
        "i18n": {
            "en": {"locale": "en", "label": "E-mail", "options": null, "help": "", "errorMessage": ""},
            "cs": {"locale": "cs", "label": "E-mail", "options": null, "help": "", "errorMessage": ""}
        },
        "beanName": "ApplicationFormItem"
    }, {
        "shortname": "mail",
        "required": false,
        "type": "FROM_FEDERATION_HIDDEN",
        "federationAttribute": "mail",
        "perunDestinationAttribute": null,
        "regex": "",
        "applicationTypes": ["INITIAL"],
        "ordnum": 4,
        "forDelete": false,
        "i18n": {
            "en": {"locale": "en", "label": "", "options": null, "help": "", "errorMessage": ""},
            "cs": {"locale": "cs", "label": "", "options": null, "help": "", "errorMessage": ""}
        },
        "beanName": "ApplicationFormItem"
    }, {
        "shortname": "submit",
        "required": false,
        "type": "SUBMIT_BUTTON",
        "federationAttribute": "",
        "perunDestinationAttribute": null,
        "regex": "",
        "applicationTypes": ["INITIAL"],
        "ordnum": 5,
        "forDelete": false,
        "i18n": {
            "en": {"locale": "en", "label": "Submit", "options": null, "help": "", "errorMessage": ""},
            "cs": {"locale": "cs", "label": "Odeslat", "options": null, "help": "", "errorMessage": ""}
        },
        "beanName": "ApplicationFormItem"
    }, {
        "shortname": "alreadyMember",
        "required": false, "type": "HTML_COMMENT",
        "federationAttribute": "",
        "perunDestinationAttribute": null,
        "regex": "",
        "applicationTypes": ["EXTENSION"],
        "ordnum": 6,
        "forDelete": false,
        "i18n": {
            "en": {
                "locale": "en",
                "label": "<p>You are already a member of this VO. Please continue to a <a href=\"https://perun.demo.eduteams.org/\">eduTEAMS Perun info page</a> to discover more.</p>",
                "options": null,
                "help": "",
                "errorMessage": ""
            },
            "cs": {
                "locale": "cs",
                "label": "<p>Již jste členem této virtuální organizace. Prosím pokračujte na <a href=\"https://perun.demo.eduteams.org/\">informační stánku o Perunovi v eduTEAMS</a>, kde se dozvíte více.</p>",
                "options": null,
                "help": "",
                "errorMessage": ""
            }
        },
        "beanName": "ApplicationFormItem"
    }],

    voNotifications: [{
        "appType": "INITIAL",
        "formId": 1,
        "mailType": "APP_CREATED_USER",
        "send": true,
        "message": {
            "en": {
                "locale": "en",
                "subject": "Confimation of application submission for VO {voName}",
                "text": "Dear user,\n\nThank you for your application. The information you submitted has been successfully received. Your application will be reviewed by a VO {voName} administrator.\n\nPlease note that if you entered a new contact email (different from the e-mail received from your identity provider), you will receive a verification e-mail. In that case, the application will be passed to the VO administrator after the e-mail is verified by you.\n\nName: {displayName} \nApplication ID: {appId}\n\nApplication state can be checked in \"Applications\" section:\n\n{appGuiUrl-fed}\n\nYou will be notified by another mail once your application approved or rejected.\n\n--------------------------------------------------\nYours sincerely {voName}"
            },
            "cs": {
                "locale": "cs",
                "subject": "Potrzení přijetí přihlášky do VO {voName}",
                "text": "Vážený uživateli,\n\nděkujeme Vám za registraci do virtuální organizace {voName}. \nPokud jste změnil(a) předvyplněný e-mail, obdržíte na nově zadanou mailovou adresu ověřovací e-mail. V takovém případě bude přihláška schválena až po jeho ověření. \n\nJméno: {displayName} \nPřihláška č. {appId}\n\nStav své přihlášky můžete sledovat v části \"Applications\" na adrese:\n\n{appGuiUrl-fed}\n\nO schválení nebo zamítnutí přihlášky budete informován(a) dalším mailem.\n\n--------------------------------------------------\nVaše {voName}"
            }
        },
        "beanName": "ApplicationMail"
    }, {
        "appType": "INITIAL",
        "formId": 1,
        "mailType": "APP_CREATED_VO_ADMIN",
        "send": true,
        "message": {
            "en": {
                "locale": "en",
                "subject": "New application for VO {voName} created",
                "text": "Dear administrator,\n\nnew application for VO {voName} was created under ID={appId} by user: {displayName}. \n\nApplication detail with all user submitted data where you can approve / reject application:\n\n{appDetailUrl-fed}\n\nIf there was an error during application creation, information follows:\n\n{errors}\n\n--------------------------------------------------\neduTEAMS Perun membership management service"
            },
            "cs": {
                "locale": "cs",
                "subject": "Nová přihláška do VO {voName}",
                "text": "Vážený administrátore,\n\nbyla podána nová přihláška do {voName} pod ID={appId} uživatelem: {displayName}. \n\nDetail přihlášky s možností přijetí/zamítnutí:\n\n{appDetailUrl-fed}\n\n\nPokud při vytvoření přihlášky došlo k chybám, výpis následuje:\n\n{errors}\n\n--------------------------------------------------\neduTEAMS Perun membership management service"
            }
        },
        "beanName": "ApplicationMail"
    }, {
        "appType": "INITIAL",
        "formId": 1,
        "mailType": "MAIL_VALIDATION",
        "send": true,
        "message": {
            "en": {
                "locale": "en",
                "subject": "{voName}: Email address verification",
                "text": "Dear user,\n\nYou have entered this email address as a preferred contact on an application in VO {voName}. Please verify your email address by clicking on the link below. Until that, your application can't be approved.\n\n{validationLink-fed}\n\n--------------------------------------------------\nYours sincerely {voName}"
            },
            "cs": {
                "locale": "cs",
                "subject": "{voName}: ověření mailové adresy",
                "text": "Vážený uživateli,\n\npři podávání přihlášky do VO {voName} jste zadali tuto mailovou adresu jako preferovaný kontakt. Pro úspěšné zpracování přihlášky je nutné ověřit Vaši adresu pomocí odkazu uvedeného níže. Bez tohoto ověření nemůže být Vaše přihláška přijata.\n\n{validationLink-fed}\n\n--------------------------------------------------\nVaše {voName}"
            }
        },
        "beanName": "ApplicationMail"
    }, {
        "appType": "INITIAL",
        "formId": 1,
        "mailType": "APP_APPROVED_USER",
        "send": true,
        "message": {
            "en": {
                "locale": "en",
                "subject": "{voName}: Application no. {appId} approved",
                "text": "Dear user,\n\nYour application for VO {voName} under ID={appId} was approved by VO administrator.\n\nName: {displayName} \n\nAdministrative interface of Perun system, where you can change your contact details and manage different settings, can be found at:\n\n{perunGuiUrl-fed}\n\n \n-------------------------------------------\nYours sincerely {voName}"
            },
            "cs": {
                "locale": "cs",
                "subject": "{voName}: Přihláška č. {appId} byla schválena",
                "text": "Vážený uživateli,\n\nVaše přihláška do VO {voName} s číslem {appId} byla schválena administrátorem VO.\n\nJméno: {displayName} \n\nAdministrativní rozhraní systému Perun, kde můžete upravovat své kontaktní údaje a spravovat různá nastavení naleznete zde:\n\n{perunGuiUrl-fed}\n\n-------------------------------------------\nVaše {voName}"
            }
        },
        "beanName": "ApplicationMail"
    }, {
        "appType": "INITIAL",
        "formId": 1,
        "mailType": "APP_REJECTED_USER",
        "send": true,
        "message": {
            "en": {
                "locale": "en",
                "subject": "Application for VO {voName} rejected",
                "text": "Dear user,\n\nyour application for membership in VO {voName} under ID={appId} was rejected by VO administrator. Reason (if attached by administrator) follows:\n\n{customMessage}\n\n--------------------------\nYour VO {voName}"
            },
            "cs": {
                "locale": "cs",
                "subject": "{voName}: Žádost ID {appId} byla zamítnuta administrátorem",
                "text": "Vážený uživateli,\n\nVaše přihláška do VO {voName} pod ID={appId} byla zamítnuta administrátorem VO. Pokud uvedl důvod zamítnutí, text následuje:\n\n{customMessage}\n\n----------------------------\nVaše VO {voName}"
            }
        },
        "beanName": "ApplicationMail"
    }, {
        "appType": "INITIAL",
        "formId": 1,
        "mailType": "USER_INVITE",
        "send": true,
        "message": {
            "en": {
                "locale": "en",
                "subject": "Invitation to {voName}",
                "text": "Dear {displayName},\n\nYou have been invited to {voName}. Please follow the link below and fill the registration form.\n\n{invitationLink-fed}\n"
            },
            "cs": {
                "locale": "cs",
                "subject": "Pozvánka do {voName}",
                "text": "Vážený {displayName},\n\nByl(a) jste pozván(a) do {voName}. Po kliknutí na následující odkaz přejdete na stránku s registračním formulářem.\n\n{invitationLink-fed}"
            }
        },
        "beanName": "ApplicationMail"
    }],

    groupsToCreate: [{
        group: {
            "parentGroupId": null,
            "name": "wp-admins",
            "description": "Wordpress Administartors",
            "shortName": "wp-admins",
            "beanName": "Group"
        },
        groupFormItems: [{
            "shortname": "Heading",
            "required": false,
            "type": "HEADING",
            "federationAttribute": "",
            "perunDestinationAttribute": null,
            "regex": "",
            "applicationTypes": ["INITIAL", "EXTENSION"],
            "ordnum": 0,
            "forDelete": false,
            "i18n": {
                "en": {
                    "locale": "en",
                    "label": "<h2>Application for wp-admin group within the eduTEAMS catch-all VO</h2>",
                    "options": null,
                    "help": "",
                    "errorMessage": ""
                },
                "cs": {
                    "locale": "cs",
                    "label": "<h2>Přihláška do eduTEAMS catch-all VO</h2>",
                    "options": null,
                    "help": "",
                    "errorMessage": ""
                }
            },
            "beanName": "ApplicationFormItem"
        }, {
            "shortname": "name",
            "required": true,
            "type": "TEXTFIELD",
            "federationAttribute": "displayName",
            "perunDestinationAttribute": "urn:perun:user:attribute-def:core:displayName",
            "regex": "",
            "applicationTypes": ["EXTENSION", "INITIAL"],
            "ordnum": 1,
            "forDelete": false,
            "i18n": {
                "en": {"locale": "en", "label": "Name", "options": null, "help": "", "errorMessage": ""},
                "cs": {"locale": "cs", "label": "Jméno", "options": null, "help": "", "errorMessage": ""}
            },
            "beanName": "ApplicationFormItem"
        }, {
            "shortname": "mail",
            "required": false,
            "type": "FROM_FEDERATION_HIDDEN",
            "federationAttribute": "mail",
            "perunDestinationAttribute": null,
            "regex": "",
            "applicationTypes": ["INITIAL", "EXTENSION"],
            "ordnum": 2,
            "forDelete": false,
            "i18n": {
                "en": {"locale": "en", "label": "", "options": null, "help": "", "errorMessage": ""},
                "cs": {"locale": "cs", "label": "", "options": null, "help": "", "errorMessage": ""}
            },
            "beanName": "ApplicationFormItem"
        }, {
            "shortname": "reason",
            "required": false,
            "type": "TEXTAREA",
            "federationAttribute": "",
            "perunDestinationAttribute": null,
            "regex": "",
            "applicationTypes": ["INITIAL", "EXTENSION"],
            "ordnum": 3,
            "forDelete": false,
            "i18n": {
                "en": {
                    "locale": "en",
                    "label": "Reason",
                    "options": null,
                    "help": "Write down the reason why you are applying for the group membership. Based on that VO manager will approve or reject your application.",
                    "errorMessage": ""
                },
                "cs": {
                    "locale": "cs",
                    "label": "Důvod",
                    "options": null,
                    "help": "Prosím uveďte důvod Vaší žádosti",
                    "errorMessage": ""
                }
            },
            "beanName": "ApplicationFormItem"
        }, {
            "shortname": "submit",
            "required": false,
            "type": "SUBMIT_BUTTON",
            "federationAttribute": "",
            "perunDestinationAttribute": null,
            "regex": "",
            "applicationTypes": ["INITIAL", "EXTENSION"],
            "ordnum": 4,
            "forDelete": false,
            "i18n": {
                "en": {"locale": "en", "label": "Submit", "options": null, "help": "", "errorMessage": ""},
                "cs": {"locale": "cs", "label": "Odeslat", "options": null, "help": "", "errorMessage": ""}
            },
            "beanName": "ApplicationFormItem"
        }],

        groupNotifications: [{
            "appType": "INITIAL",
            "formId": 7,
            "mailType": "APP_CREATED_USER",
            "send": true,
            "message": {
                "en": {
                    "locale": "en",
                    "subject": "Confimation of application submission for {groupName} group within VO {voName}",
                    "text": "Dear user,\n\nThank you for your application for {groupName} group membership. The information you submitted has been successfully received. Your application will be reviewed by a VO {voName} administrator.\n\nName: {displayName} \nApplication ID: {appId}\n\nApplication state can be checked in \"Applications\" section:\n\n{appGuiUrl-fed}\n\nYou will be notified by another mail once your application approved or rejected.\n\n--------------------------------------------------\nYours sincerely {voName}"
                },
                "cs": {
                    "locale": "cs",
                    "subject": "Potrzení přijetí přihlášky do skupiny {groupName} ve VO {voName}",
                    "text": "Vážený uživateli,\n\nděkujeme Vám za registraci do dkupiny {groupName}. \n\nJméno: {displayName} \nPřihláška č. {appId}\n\nStav své přihlášky můžete sledovat v části \"Applications\" na adrese:\n\n{appGuiUrl-fed}\n\nO schválení nebo zamítnutí přihlášky budete informován(a) dalším mailem.\n\n--------------------------------------------------\nVaše {voName}"
                }
            },
            "beanName": "ApplicationMail"
        }, {
            "appType": "INITIAL",
            "formId": 7,
            "mailType": "APP_CREATED_VO_ADMIN",
            "send": true,
            "message": {
                "en": {
                    "locale": "en",
                    "subject": "New application for group {groupName} within VO {voName} created",
                    "text": "Dear administrator,\n\nnew application for {groupName} group membership within VO {voName} was created under ID={appId} by user: {displayName}.\n\nApplication detail with all user submitted data where you can approve / reject application:\n\n{appDetailUrl-fed}\n\nIf there was an error during application creation, information follows:\n\n{errors}\n\n--------------------------------------------------\neduTEAMS Perun membership management service"
                },
                "cs": {
                    "locale": "cs",
                    "subject": "Nová přihláška do skupiny {groupName} ve VO {voName}",
                    "text": "Vážený administrátore,\n\nbyla podána nová přihláška do skupiny {groupName} ve VO {voName} pod ID={appId} uživatelem: {displayName}, {actor} / {extSource}. \n\nDetail přihlášky s možností přijetí/zamítnutí:\n\n{appDetailUrl-fed}\n\n\nPokud při vytvoření přihlášky došlo k chybám, výpis následuje:\n\n{errors}\n\n--------------------------------------------------\neduTEAMS Perun membership management service"
                }
            },
            "beanName": "ApplicationMail"
        }, {
            "appType": "INITIAL",
            "formId": 7,
            "mailType": "APP_APPROVED_USER",
            "send": true,
            "message": {
                "en": {
                    "locale": "en",
                    "subject": "{voName}: Application for {groupName} group membership approved",
                    "text": "Dear user,\n\nYour application for {groupName} group membership within VO {voName} under ID={appId} was approved by VO administrator.\n\nName: {displayName} \n \n-------------------------------------------\nYours sincerely {voName}"
                },
                "cs": {
                    "locale": "cs",
                    "subject": "{voName}: Přihláška č. {appId} byla schválena",
                    "text": "Vážený uživateli,\n\nVaše přihláška do skupiny {groupName} ve VO {voName} s číslem {appId} byla schválena administrátorem VO.\n\nJméno: {displayName} \n\n-------------------------------------------\nVaše {voName}"
                }
            },
            "beanName": "ApplicationMail"
        }, {
            "appType": "INITIAL",
            "formId": 7,
            "mailType": "APP_REJECTED_USER",
            "send": true,
            "message": {
                "en": {
                    "locale": "en",
                    "subject": "{voName}: Application for {groupName} group membership rejected",
                    "text": "Dear user,\n\nyour application for {groupName} group membership in VO {voName} under ID={appId} was rejected by VO administrator. Reason (if attached by administrator) follows:\n\n{customMessage}\n\n--------------------------\nYour VO {voName}"
                },
                "cs": {
                    "locale": "cs",
                    "subject": "{voName}: Žádost ID {appId} byla zamítnuta administrátorem",
                    "text": "Vážený uživateli,\n\nVaše přihláška do skupiny {groupName} pod ID={appId} byla zamítnuta administrátorem VO. Pokud uvedl důvod zamítnutí, text následuje:\n\n{customMessage}\n\n----------------------------\nVaše VO {voName}"
                }
            },
            "beanName": "ApplicationMail"
        }, {
            "appType": "INITIAL",
            "formId": 7,
            "mailType": "USER_INVITE",
            "send": true,
            "message": {
                "en": {
                    "locale": "en",
                    "subject": "Invitation to {groupName} group within VO {voName}",
                    "text": "Dear {displayName},\n\nYou have been invited to {groupName} group within VO {voName}. Please follow the link below and fill the registration form.\n\n{invitationLink-fed}\n"
                },
                "cs": {
                    "locale": "cs",
                    "subject": "Pozvánka do skupiny {groupName} ve VO {voName}",
                    "text": "Vážený {displayName},\n\nByl(a) jste pozván(a) do skupiny {groupName} ve VO {voName}. Po kliknutí na následující odkaz přejdete na stránku s registračním formulářem.\n\n{invitationLink-fed}"
                }
            },
            "beanName": "ApplicationMail"
        }]
    }]
};
