package cz.metacentrum.perun.cabinet.bl;

/**
 * Enum with standard ErrorCodes for CabinetException
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public enum ErrorCodes {

  MEMBER_NOT_FOUND, USER_NOT_EXISTS, NOT_SAME_PERSON, HTTP_IO_EXCEPTION, MALFORMED_HTTP_RESPONSE,
  MALFORMED_XML_RESPONSE, AUTHOR_ALREADY_EXISTS, IO_EXCEPTION, CODE_NOT_SET, PERUN_EXCEPTION,
  PUBLICATION_ALREADY_EXISTS, PUBLICATION_NOT_EXISTS, AUTHORSHIP_ALREADY_EXISTS, AUTHORSHIP_NOT_EXISTS, NOT_AUTHORIZED,
  PUBLICATION_HAS_AUTHORS_OR_THANKS, THANKS_ALREADY_EXISTS, PUBLICATION_SYSTEM_NOT_EXISTS,
  NO_IDENTITY_FOR_PUBLICATION_SYSTEM, CATEGORY_NOT_EXISTS, CATEGORY_HAS_PUBLICATIONS, THANKS_NOT_EXISTS,
  AUTHOR_NOT_EXISTS;

}
