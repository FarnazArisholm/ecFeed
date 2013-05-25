package com.testify.ecfeed.editors;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;

import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.outline.EcContentOutlinePage;
import com.testify.ecfeed.parsers.EcParser;

public class EcEditor extends TextEditor{
	
	private ColorManager fColorManager;
	private EcContentOutlinePage fContentOutline;
	private RootNode fModel;
	
	
	public EcEditor(){
		super();
		fColorManager = new ColorManager();
		setSourceViewerConfiguration(new EcViewerConfiguration(fColorManager));
		setDocumentProvider(new EcDocumentProvider());
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class required){
		if(IContentOutlinePage.class.equals(required)){
			if(fContentOutline == null){
				fContentOutline = new EcContentOutlinePage(this);
			}
			return fContentOutline;
		}
		return super.getAdapter(required);
	}
	
	public void dispose() {
		fColorManager.dispose();
		super.dispose();
	}
	
	public IDocument getDocument(){
		return getSourceViewer().getDocument();
	}

	public RootNode getModel() {
		if (fModel == null){
			fModel = createModel();
		}
		return fModel;
	}

	private RootNode createModel() {
		RootNode root = null;
		IEditorInput input = getEditorInput();
		if(input instanceof FileEditorInput){
			IFile file = ((FileEditorInput)input).getFile();
			InputStream iStream;
			try {
				EcParser parser = new EcParser();
				iStream = file.getContents();
				root = parser.parseEctFile(iStream);
				System.out.println("fModel: " + root);
			} catch (CoreException e) {
				System.out.println("Exception: " + e.getMessage());
			}
		}
		return root;
	}

	public void updateModel(RootNode model) {
		fModel = model;
//		fContentOutline = (EcContentOutlinePage) getAdapter(IContentOutlinePage.class);
		fContentOutline.refreshTree();
	}
}
