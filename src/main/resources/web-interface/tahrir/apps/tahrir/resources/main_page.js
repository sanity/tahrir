// ==========================================================================
// Project:   Tahrir - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Tahrir */

// This page describes the main user interface for your application.  
Tahrir.mainPage = SC.Page.design({

  // The main pane is made visible on screen as soon as your app is loaded.
  // Add childViews to this pane for views to display immediately on page 
  // load.
  mainPane: SC.MainPane.design({
    childViews: 'middleView topView bottomView'.w(),

    topView: SC.ToolbarView.design({
      layout: { top: 0, left: 0, right: 0, height: 36 },
	  childViews: 'tahrirLogo newMessageButton'.w(),
      anchorLocation: SC.ANCHOR_TOP,
      tahrirLogo: SC.LabelView.design({
        layout: { centerY: 0, height: 24, left: 8, width: 200 },
        controlSize: SC.LARGE_CONTROL_SIZE,
        fontWeight: SC.BOLD_WEIGHT,
        value:   'Tahrir'
      }),
      
      newMessageButton: SC.ButtonView.design({
        layout: { centerY: 0, height: 24, right: 12, width: 120 },
        title:  "New Message"
      })
    }),

    middleView: SC.ScrollView.design({
      hasHorizontalScroller: NO,
      layout: { top: 36, bottom: 32, left: 0, right: 0 },
      backgroundColor: 'white',

      contentView: SC.ListView.design({
		contentBinding: 'Tahrir.messagesController.arrangedObjects',
		selectionBinding: 'Tahrir.messagesController.selection',
		contentValueKey: "text",
		rowHeight: 42
      })
    }),

    bottomView: SC.ToolbarView.design({
      layout: { bottom: 0, left: 0, right: 0, height: 32 },
	  childViews: 'statusView'.w(),
	  statusView: SC.LabelView.design({
	        layout: { centerY: 0, height: 18, left: 20, right: 20 },
	        textAlign: SC.ALIGN_CENTER,
	        value: "Connected to 14 peers"
	      }),
      anchorLocation: SC.ANCHOR_BOTTOM
    })
  })

});
