// ==========================================================================
// Project:   Tahrir.Nym
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Tahrir */

/** @class

  (Document your Model here)

  @extends SC.Record
  @version 0.1
*/
Tahrir.Nym = SC.Record.extend(
/** @scope Tahrir.Nym.prototype */ {
  pubkey: SC.Record.attr(String),
  nickname: SC.Record.attr(String),
  messages: SC.Record.toMany("Tahrir.Message", {
	inverse: "author", isMaster: NO
	})
}) ;
