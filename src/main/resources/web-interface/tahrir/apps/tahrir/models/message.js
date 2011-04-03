// ==========================================================================
// Project:   Tahrir.Message
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Tahrir */

/** @class

  (Document your Model here)

  @extends SC.Record
  @version 0.1
*/
Tahrir.Message = SC.Record.extend(
/** @scope Tahrir.Message.prototype */ {
    author: SC.Record.toOne("Tahrir.Nym", {
        inverse: "messages", isMaster: YES
    }),
	text : SC.Record.attr(String),
	created : SC.Record.attr(SC.DateTime)
}) ;
