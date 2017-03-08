package org.kane.elephant.io.benchmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.kane.base.serialization.ValidationException;
import org.kane.elephant.io.GZIPUtils;
import org.kane.elephant.io.SmallDocumentWriter;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class TestObjectProductDataTransformer
{
	private long start_time;

	private SmallDocumentWriter writer;
	private int max_document_count = Integer.MAX_VALUE;
	
	public TestObjectProductDataTransformer(File src, File dest, boolean use_compression, int max_document_count) throws IOException
	{
		this.max_document_count = max_document_count;
		
		OutputStream raw = new FileOutputStream(dest);
		
		if ( use_compression )
		{
			raw = new GZIPUtils.OutputStreamBestSpeed(raw);
		}
		
		writer = new SmallDocumentWriter(new OutputStreamWriter(raw));
		
		AbstractXmlDriver driver = new StaxDriver();
		HierarchicalStreamReader reader = driver.createReader(src);
		
		start_time = System.currentTimeMillis();
		
		// read each product_specs tag
		while(reader.hasMoreChildren())
		{
			reader.moveDown();
			
			if ( reader.getNodeName().equalsIgnoreCase("product_specs") )
			{
				readProductSpecs(reader);
			}
			else
			{
			}
			
			reader.moveUp();

			if ( writer.getDocumentCount() % 100 == 0 )
			{
				writer.printStatus();
				writer.flush();
			}
			
			if ( writer.getDocumentCount() > max_document_count )
				break;
		}
		
		writer.close();
		writer.printStatus();
		System.out.println("transformation complete");
	}
	
	private void readProductSpecs(HierarchicalStreamReader reader)  throws IOException
	{ 
		TestObjectProductData.Builder item_builder = new TestObjectProductData.Builder();
				
		item_builder.setBrandCode(reader.getAttribute("brand"));
		item_builder.setPN(reader.getAttribute("pn"));
		
		while(reader.hasMoreChildren())
		{
			reader.moveDown();
			
			if ( reader.getNodeName().equalsIgnoreCase("BRAND") )
			{
				// skip, read as an attribute (above)
			}
			if ( reader.getNodeName().equalsIgnoreCase("PN") )
			{
				// skip, read as an attribute (above)
			}
			else
			{
				item_builder.putKVData(reader.getNodeName(), reader.getValue());
			}
			
			reader.moveUp();
		}
		
		try
		{
			TestObjectProductData item = item_builder.create();
	
			writer.writeDocument(item.toXML());
		}
		catch(ValidationException e)
		{
			e.printStackTrace();
		}
	}
}
