package com.testify.ecfeed.parsers;

import java.io.IOException;
import java.io.OutputStream;

import com.testify.ecfeed.constants.Constants;
import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.GenericNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import nu.xom.*;

public class EcWriter {
	private OutputStream fOutputStream;

	public EcWriter(OutputStream ostream){
		fOutputStream = ostream;
	}

	public void writeXmlDocument(RootNode root) {
		Element rootElement = createElement(root);
		Document document = new Document(rootElement);
		try{
			Serializer serializer = new Serializer(fOutputStream);
			serializer.setIndent(4);
			serializer.write(document);
		}catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		}
	}

	private Element createElement(GenericNode node) {
		String name = node.getName();
		Element element = null;
		if(node instanceof RootNode){
			element = createRootElement(name);
		}
		else if(node instanceof ClassNode){
			element = createClassElement(name);
		}
		else if(node instanceof MethodNode){
			element = createMethodElement(name);
		}
		else if (node instanceof CategoryNode){
			String typeSignature = ((CategoryNode)node).getTypeSignature();
			element = createCategoryElement(name, typeSignature);
		}
		else if (node instanceof PartitionNode){
			Object value = ((PartitionNode)node).getValue();
			element = createPartitionElement(name, value);
		}
		
		for(GenericNode child : node.getChildren()){
			element.appendChild(createElement(child));
		}
		return element;
	}

	private Element createPartitionElement(String name, Object value) {
		Element partitionElement = new Element(Constants.PARTITION_NODE_NAME);
		Attribute nameAttribute = new Attribute(Constants.NODE_NAME_ATTRIBUTE, name);
		Attribute valueAttribute = new Attribute(Constants.VALUE_ATTRIBUTE, String.valueOf(value));
		partitionElement.addAttribute(nameAttribute);
		partitionElement.addAttribute(valueAttribute);
		return partitionElement;
	}

	private Element createCategoryElement(String name, String typeSignature) {
		Element categoryElement = new Element(Constants.CATEGORY_NODE_NAME);
		Attribute nameAttribute = new Attribute(Constants.NODE_NAME_ATTRIBUTE, name);
		Attribute typeSignatureAttribute = new Attribute(Constants.TYPE_SIGNATURE_ATTRIBUTE, typeSignature);
		categoryElement.addAttribute(nameAttribute);
		categoryElement.addAttribute(typeSignatureAttribute);
		return categoryElement;
	}

	private Element createMethodElement(String name) {
		Element methodElement = new Element(Constants.METHOD_NODE_NAME);
		Attribute nameAttribute = new Attribute(Constants.NODE_NAME_ATTRIBUTE, name);
		methodElement.addAttribute(nameAttribute);
		return methodElement;
	}

	private Element createClassElement(String qualifiedName) {
		Element classElement = new Element(Constants.CLASS_NODE_NAME);
		Attribute nameAttribute = new Attribute(Constants.NODE_NAME_ATTRIBUTE, qualifiedName);
		classElement.addAttribute(nameAttribute);
		return classElement;
	}

	private Element createRootElement(String name) {
		Element rootElement = new Element(Constants.ROOT_NODE_NAME);
		Attribute nameAttribute = new Attribute(Constants.NODE_NAME_ATTRIBUTE, name);
		rootElement.addAttribute(nameAttribute);
		return rootElement;
	}

}
