package org.pm4j.testdomains.user.swt;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.pm4j.core.pb.PbMatcherMapped;

public class AppWindow extends ApplicationWindow {

//    private ResourceService resourceService = new ResourceService();
//	private ResEditSessionPm resEditSessionPm = new ResEditSessionPm();
//    private PkgResourcesPm pkgResourcesPm;
//	private TreeViewer tv;
//	private TableViewer tblView = null;
//    private PvTableForPmAttrPmList pvTable;
//    private Widget detailWidget;
    
    private PbMatcherMapped detailBuilderMap = new PbMatcherMapped();

	public AppWindow() {
		super(null);
		
//		detailBuilderMap.appendMatches(
//			"resKeyDataPm.valueList", new PvTableForPmAttrPmList("langId", "value"),
//			"pkgResourcesPm.resFilesSet", new PvTableForPmAttrPmList("fileBaseName"),
//			PmElement.class, new PvGridLayout.AllAttrs()
//		);
//
//		resEditSessionPm = new ResEditSessionPm();
//		
//		pkgResourcesPm = resEditSessionPm.getPmForBean(
//				resourceService.readResources(ClassUtil.getClassDir(FileResourcesPm.class)));
		
		addStatusLine();
		addMenuBar();
	    addToolBar(SWT.FLAT | SWT.WRAP);
	}

	
	@Override
	protected Control createContents(Composite parent) {
		getShell().setText(getClass().getName());
//	    parent.setSize(400,250);

	    final SashForm sash_form = new SashForm(parent, SWT.HORIZONTAL | SWT.NULL);
	    
//	    tv = new TreeViewer(sash_form);
//	    tv.setContentProvider(new PmTreeContentProvider());
//	    tv.setLabelProvider(new PmLabelProvider());
//	    tv.setInput(pkgResourcesPm);
//	    
	    final Composite detailComposite = new Composite(sash_form, SWT.BORDER);
	    detailComposite.setLayout(new FillLayout());
	    detailComposite.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	    detailComposite.setBackgroundMode(SWT.INHERIT_FORCE);

	    
//	    final Group detailGroup = new Group(detailComposite, SWT.NONE);
	    
//        tv.addSelectionChangedListener(new ISelectionChangedListener() {
//          public void selectionChanged(SelectionChangedEvent event) {
//            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//            Object selectedObj = selection.getFirstElement();
//            PresentationModel pm = (PresentationModel) selectedObj;
//
//            // FIXME: identifiziere das PM. Nur bei Änderung reagieren!
//            if (detailWidget != null) {
//            	// Do nothing on repeated selection of the active tree item.
//            	if (detailWidget.getData("pm") == selectedObj) {
//            		return;
//            	}
//            	
//            	for (Control c : detailComposite.getChildren()) {
//            		c.dispose();
//            	}
//            	detailWidget = null;
//            	setStatus(null);
//            }
//
//            PmViewBuilder<Widget> builder = detailBuilderMap.findBinder(pm);
//            
//            if (builder != null) {
//            	detailWidget = builder.build(detailComposite, pm);
////            	detailGroup.pack();
//            	SwtUtil.reDisplay(detailComposite);
//                setStatus(pm.getPmTitle());
//            }
//          }
//        });

        
	    return sash_form;
	}
	
	@Override
	protected MenuManager createMenuManager() {
		  MenuManager bar_menu = new MenuManager("");

		  MenuManager file_menu = new MenuManager("&File");
		  MenuManager edit_menu = new MenuManager("&Edit");
		  MenuManager view_menu = new MenuManager("&View");

		  bar_menu.add(file_menu);
		  bar_menu.add(edit_menu);
		  bar_menu.add(view_menu);

		  exitAction.setText("E&xit");
		  file_menu.add(exitAction);
		  
		  view_menu.add(new Action("hallo") {});

		  return bar_menu;
	}
	
	private Action exitAction = new Action() {
		public void run() {
			close();
		};
	};


	public static void main(String[] args) {
		AppWindow wwin = new AppWindow();
	    wwin.setBlockOnOpen(true);
	    wwin.open();
	    Display.getCurrent().dispose();
	}
}
