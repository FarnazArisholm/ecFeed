package com.testify.ecfeed.editors;

import java.util.Vector;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.Section;

import com.testify.ecfeed.constants.Strings;
import com.testify.ecfeed.dialogs.TestClassSelectionDialog;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.utils.EcModelUtils;

public class ClassNodeDetailsPage extends GenericNodeDetailsPage{
	
	private ClassNode fSelectedNode;
	private Section fMainSection;
	private Label fQualifiedNameLabel;
	private Vector<MethodNode> fObsoleteMethods;
	private Vector<MethodNode> fNotContainedMethods;
	private ColorManager fColorManager;
	private Table methodsTable;
	private Table otherMathodsTable;
	private CheckboxTableViewer fOtherMethodsViewer;
	private CheckboxTableViewer fMethodsViewer;
	private Section fOtherMethodsSection;
	private Composite fMainComposite;
	private boolean fOtherMethodsSectionCreated;
	
	private class ChangeNameButtonSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			IType selectedClass = selectClass();

			if(selectedClass != null){
				String qualifiedName = selectedClass.getFullyQualifiedName();
				if(!EcModelUtils.classExists((RootNode)fSelectedNode.getRoot(), qualifiedName)){
					fSelectedNode.setName(qualifiedName);
					updateModel((RootNode)fSelectedNode.getRoot());
				}
				else{
					MessageDialog infoDialog = new MessageDialog(Display.getDefault().getActiveShell(), 
							"Class exists", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION), 
							"Selected class is already contained in the model", MessageDialog.INFORMATION
							, new String[] {"OK"}, 0);
					infoDialog.open();
				}
			}
		}

		private IType selectClass() {
			TestClassSelectionDialog dialog = new TestClassSelectionDialog(Display.getDefault().getActiveShell());
			
			if (dialog.open() == Window.OK) {
				return (IType)dialog.getFirstResult();
			}
			return null;
		}
	}

	private class RemoveMethodsButtonSelectionAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			MessageDialog infoDialog = new MessageDialog(Display.getDefault().getActiveShell(), 
					"Remove methods", Display.getDefault().getSystemImage(SWT.ICON_QUESTION), 
					"Remove selected methods from the model?\nAll generated test cases will be lost.",
					MessageDialog.QUESTION_WITH_CANCEL, new String[] {"OK", "Cancel"}, 0);
			if(infoDialog.open() == 0){
				removeMethods(fMethodsViewer.getCheckedElements());
			}
		}

		private void removeMethods(Object[] checkedElements) {
			for(Object method : checkedElements){
				fSelectedNode.removeChild((MethodNode)method);
				updateModel((RootNode)fSelectedNode.getRoot());
			}
		}
	}
	
	/**
	 * Create the details page.
	 */
	public ClassNodeDetailsPage(EcMultiPageEditor editor, ModelMasterDetailsBlock parentBlock) {
		super(editor, parentBlock);
		fColorManager = new ColorManager();
	}

	/**
	 * Create contents of the details page.
	 * @param parent
	 */
	public void createContents(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		fMainSection = getToolkit().createSection(parent, Section.TWISTIE | Section.TITLE_BAR);
		getToolkit().paintBordersFor(fMainSection);
		fMainSection.setText("New Section");
		fMainSection.setExpanded(true);

		Composite textComposite = new Composite(fMainSection, SWT.NONE);
		getToolkit().adapt(textComposite);
		getToolkit().paintBordersFor(textComposite);
		fMainSection.setTextClient(textComposite);
		textComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Button refreshButton = getToolkit().createButton(textComposite, "refresh", SWT.NONE);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				refresh();
			}
		});
		
		Button removeButton = getToolkit().createButton(textComposite, "remove", SWT.NONE);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				MessageDialog infoDialog = new MessageDialog(Display.getDefault().getActiveShell(), 
						Strings.DIALOG_REMOVE_CLASS_TITLE, Display.getDefault().getSystemImage(SWT.ICON_WARNING), 
						Strings.DIALOG_REMOVE_CLASS_MESSAGE,
						MessageDialog.QUESTION_WITH_CANCEL, new String[] {"OK", "Cancel"}, 0);
				if(infoDialog.open() == 0){
					RootNode root = (RootNode)fSelectedNode.getParent(); 
					root.removeChild(fSelectedNode);
					getParentBlock().selectNode(root);
				}
			}
		});
		
		fMainComposite = new Composite(fMainSection, SWT.NONE);
		getToolkit().adapt(fMainComposite);
		getToolkit().paintBordersFor(fMainComposite);
		fMainSection.setClient(fMainComposite);
		fMainComposite.setLayout(new GridLayout(1, false));
		
		createQualifiedNameComposite(fMainComposite);
		createMethodsSection(fMainComposite);
		
	}

	private void createQualifiedNameComposite(Composite mainComposite) {
		Composite qualifiedNameComposite = new Composite(mainComposite, SWT.NONE);
		qualifiedNameComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		getToolkit().adapt(qualifiedNameComposite);
		getToolkit().paintBordersFor(qualifiedNameComposite);
		qualifiedNameComposite.setLayout(new GridLayout(2, false));
		
		fQualifiedNameLabel = new Label(qualifiedNameComposite, SWT.NONE);
		fQualifiedNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		getToolkit().adapt(fQualifiedNameLabel, true, true);
		fQualifiedNameLabel.setText("Qualified name: ");
		
		Button changeButton = new Button(qualifiedNameComposite, SWT.NONE);
		changeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		getToolkit().adapt(changeButton, true, true);
		changeButton.setText("Change");
		changeButton.addSelectionListener(new ChangeNameButtonSelectionAdapter());
	}
	
	private void createMethodsSection(Composite composite) {
		Section methodsSection = getToolkit().createSection(composite, Section.TITLE_BAR);
		methodsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		getToolkit().paintBordersFor(methodsSection);
		methodsSection.setText("Methods");
		methodsSection.setExpanded(true);
		
		Composite methodsComposite = getToolkit().createComposite(methodsSection, SWT.NONE);
		getToolkit().paintBordersFor(methodsComposite);
		methodsSection.setClient(methodsComposite);
		methodsComposite.setLayout(new GridLayout(1, false));
		
		fMethodsViewer = CheckboxTableViewer.newCheckList(methodsComposite, SWT.BORDER | SWT.FULL_SELECTION);
		fMethodsViewer.setContentProvider(new ArrayContentProvider());
		fMethodsViewer.addDoubleClickListener(new ChildrenViewerDoubleClickListener());
		
		methodsTable = fMethodsViewer.getTable();
		methodsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		getToolkit().paintBordersFor(methodsTable);
		
		TableViewerColumn methodColumn = createTableViewerColumn(fMethodsViewer, "Method", 150, 0); 
		methodColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				MethodNode method = (MethodNode)element;
				String result = method.toString();
				if(methodObsolete(method)){
					result += " [obsolete]";
				}
				return result;
			}
	
			@Override
			public Color getForeground(Object element){
				MethodNode method = (MethodNode)element;
				if(methodObsolete(method)){
					return fColorManager.getColor(ColorConstants.OBSOLETE_METHOD);
				}
				return null;
			}
		});
		
		Button removeSelectedButton = new Button(methodsComposite, SWT.NONE);
		getToolkit().adapt(removeSelectedButton, true, true);
		removeSelectedButton.setText("Remove selected");
		removeSelectedButton.addSelectionListener(new RemoveMethodsButtonSelectionAdapter());
	}

	private void createOtherMethodsSection(Composite composite) {
		fOtherMethodsSection = getToolkit().createSection(composite, Section.TITLE_BAR);
		fOtherMethodsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		getToolkit().paintBordersFor(fOtherMethodsSection);
		fOtherMethodsSection.setText("Other methods");
		fOtherMethodsSection.setExpanded(true);
		
		Composite otherMathodsComposite = new Composite(fOtherMethodsSection, SWT.NONE);
		getToolkit().adapt(otherMathodsComposite);
		getToolkit().paintBordersFor(otherMathodsComposite);
		fOtherMethodsSection.setClient(otherMathodsComposite);
		otherMathodsComposite.setLayout(new GridLayout(1, false));
		
		fOtherMethodsViewer = CheckboxTableViewer.newCheckList(otherMathodsComposite, SWT.BORDER | SWT.FULL_SELECTION);
		fOtherMethodsViewer.setContentProvider(new ArrayContentProvider());
		otherMathodsTable = fOtherMethodsViewer.getTable();
		otherMathodsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		getToolkit().paintBordersFor(otherMathodsTable);
		
		Button addSelectedButton = new Button(otherMathodsComposite, SWT.NONE);
		getToolkit().adapt(addSelectedButton, true, true);
		addSelectedButton.setText("Add selected");
		addSelectedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				for(Object method : fOtherMethodsViewer.getCheckedElements()){
					fSelectedNode.addMethod((MethodNode)method);
					updateModel((RootNode)fSelectedNode.getRoot());
				}
			}
		});
		
		fOtherMethodsSectionCreated = true;
	}

	@Override
	public void dispose(){
		fColorManager.dispose();
		super.dispose();
	}

	private boolean methodObsolete(MethodNode method) {
		for(MethodNode obsoleteMethod : fObsoleteMethods){
			if(obsoleteMethod.toString().equals(method.toString())){
				return true;
			}
		}
		return false;
	}
	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		if(structuredSelection.getFirstElement() instanceof ClassNode){
			fSelectedNode = (ClassNode)structuredSelection.getFirstElement();
			refresh();
		}
	}
	
	public void refresh(){
		fObsoleteMethods = EcModelUtils.getObsoleteMethods(fSelectedNode, fSelectedNode.getQualifiedName());
		fNotContainedMethods = EcModelUtils.getNotContainedMethods(fSelectedNode, fSelectedNode.getQualifiedName());
		if(fNotContainedMethods.size() == 0){
			fOtherMethodsSection.dispose();
			fOtherMethodsSectionCreated = false;
		}
		else {
			if(!fOtherMethodsSectionCreated){
				createOtherMethodsSection(fMainComposite);
			}
			fOtherMethodsSection.setText("Other test methods in " + fSelectedNode.getLocalName());
			fOtherMethodsViewer.setInput(fNotContainedMethods);
		}
		fMainSection.setText("Class " + fSelectedNode.getLocalName());
		fQualifiedNameLabel.setText("Qualified name: " + fSelectedNode.getName());
		fMethodsViewer.setInput(fSelectedNode.getMethods());
	}
}