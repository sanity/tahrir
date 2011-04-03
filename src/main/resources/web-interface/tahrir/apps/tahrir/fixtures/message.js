// ==========================================================================
// Project:   Tahrir.Message Fixtures
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Tahrir */

sc_require('models/message');

Tahrir.Message.FIXTURES = [

	{
		guid: 1,
		author: 1,
		text: "This is a test message",
		created: SC.DateTime.parse('08/05/2009 01:00:22', '%d/%m/%Y %H:%M:%S')
	},
	{
		guid: 2,
		author: 1,
		text: "This is another test message",
		created: SC.DateTime.parse('08/05/2009 00:30:22', '%d/%m/%Y %H:%M:%S')
	}

  // TODO: Add your data fixtures here.
  // All fixture records must have a unique primary key (default 'guid').  See 
  // the example below.

  // { guid: 1,
  //   firstName: "Michael",
  //   lastName: "Scott" },
  //
  // { guid: 2,
  //   firstName: "Dwight",
  //   lastName: "Schrute" },
  //
  // { guid: 3,
  //   firstName: "Jim",
  //   lastName: "Halpert" },
  //
  // { guid: 4,
  //   firstName: "Pam",
  //   lastName: "Beesly" },
  //
  // { guid: 5,
  //   firstName: "Ryan",
  //   lastName: "Howard" }

];
