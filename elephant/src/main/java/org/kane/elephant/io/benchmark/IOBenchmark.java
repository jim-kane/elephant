package org.kane.elephant.io.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.kane.base.serialization.StandardObject;
import org.kane.elephant.io.GZIPUtils;
import org.kane.elephant.io.SmallDocumentReader;
import org.kane.elephant.io.SmallDocumentWriter;
import org.kane.elephant.io.StandardObjectBulkLoader;

// How to build an uber-jar: mvn assembly:assembly -DdescriptorId=jar-with-dependencies
// Good command line switches: -XX:+UseG1GC -Xmx2G


public class IOBenchmark 
{
	private Options options;
	private File dest;
	
	static public void main(String args[])
	{
		new IOBenchmark(args);
	} 
	
	public IOBenchmark(String args[])
	{
		setupOptions();
		
		
		if ( System.getProperty("os.name").toUpperCase().contains("WINDOWS") )
		{
			//args = new String[]{"--help"};
			//args = new String[]{"--file=c:\\test.dat", "--write=100,000"}; 
			//args = new String[]{"--file=c:\\test.dat", "--read=2"}; 
			//args = new String[]{"--file=c:\\small_spec_data.dat", "--transform=c:\\spec_data_small.xml"};
			//args = new String[]{"--file=c:\\small_spec_data.dat", "--s3write"};
			//args = new String[]{"--s3read=10,000"};
			//args = new String[]{"--file=c:\\small_spec_data.dat","--s3_big_file_write"};
			
			//args = new String[]{"--s3_bulk_read=100,000"};
			args = new String[]{"--s3_snapshot=1,000"};
		}
		
		CommandLineParser parser = new DefaultParser();
	    try 
	    {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        
	        boolean had_operation = false;
	        
	        if ( line.hasOption("file") )
	        {
	        	String file_name = line.getOptionValue("file");
	        	dest = new File(file_name);
	        }
	        else
	        {
	        	// The default file
	        	dest = new File("test.dat"); // the default file...
	        }
	        
	        if ( line.hasOption("write") )
	        {
	        	String num_objects_str = line.getOptionValue("write");
	        	if ( num_objects_str == null ) num_objects_str = "100,000";
	        	
	        	int num_objects = 100_000;
	        	
	        	try
	        	{
	        		num_objects = NumberFormat.getNumberInstance(java.util.Locale.US).parse(num_objects_str).intValue();
	        	}
	        	catch(Exception e)
	        	{
	        		System.out.println(String.format("Unable to read the number: %s, defaulting to %,d objects", num_objects_str, num_objects));
	        	}
	        	
	        	write(num_objects);
	        	had_operation = true;
	        }
	        
	        if ( line.hasOption("transform") ) 
	        {
	        	String src_file = line.getOptionValue("transform");
	        	if ( src_file == null ) src_file = "spec_data.xml";
	        	
	        	try
	        	{
	        		TestObjectProductDataTransformer xform = new TestObjectProductDataTransformer(new File(src_file),dest, false, 100_000);
	        		had_operation = true;
	        	}
	        	catch(Exception e)
	        	{
	        		e.printStackTrace();
	        		return;
	        	}
	        }
	        
	        
	        if ( line.hasOption("read") )
	        {
	        	String num_threads_string = line.getOptionValue("read");
	        	if ( num_threads_string == null ) num_threads_string = "32";
	        	
	        	int num_threads = 32;
	        	
	        	try
	        	{
	        		num_threads = NumberFormat.getNumberInstance(java.util.Locale.US).parse(num_threads_string).intValue();
	        	}
	        	catch(Exception e)
	        	{
	        		System.out.println(String.format("Unable to read the number: %s, defaulting to %,d threads", num_threads_string, num_threads));
	        	}
	        	
	        	read(num_threads);
	        	had_operation = true;
	        }
	        
	        if ( line.hasOption("s3write") )
	        {
	        	try
	        	{
	        		S3Benchmark.uploadFilesToS3(dest);
	        	}
	        	catch(Exception e)
	        	{
	        		e.printStackTrace();
	        	}
	        	
	        	had_operation = true;
	        }
	        
	        
	        if ( line.hasOption("s3_big_file_write") )
	        {
	        	try
	        	{
	        		S3Benchmark.uploadLargeFile(dest);
	        	}
	        	catch(Exception e)
	        	{
	        		e.printStackTrace();
	        	}
	        	
	        	had_operation = true;
	        }
	        
	        if ( line.hasOption("s3_bulk_read") )
	        {
	        	try
	        	{
	        		String num_objects_str = line.getOptionValue("s3_bulk_read");
		        	if ( num_objects_str == null ) num_objects_str = "1,000";
		        	
		        	int num_objects = 1_000;
		        	
		        	try
		        	{
		        		num_objects = NumberFormat.getNumberInstance(java.util.Locale.US).parse(num_objects_str).intValue();
		        	}
		        	catch(Exception e)
		        	{
		        		System.out.println(String.format("Unable to read the number: %s, defaulting to %,d objects", num_objects_str, num_objects));
		        	}
		        	
	        		S3Benchmark.s3bulkRead(num_objects);
	        	}
	        	catch(Exception e)
	        	{
	        		e.printStackTrace();
	        	}
	        	
	        	had_operation = true;
	        }
	        
	        if ( line.hasOption("s3_snapshot") )
	        {
	        	try
	        	{
	        		String num_objects_str = line.getOptionValue("s3_snapshot");
		        	if ( num_objects_str == null ) num_objects_str = "1,000";
		        	
		        	int num_objects = 1_000;
		        	
		        	try
		        	{
		        		num_objects = NumberFormat.getNumberInstance(java.util.Locale.US).parse(num_objects_str).intValue();
		        	}
		        	catch(Exception e)
		        	{
		        		System.out.println(String.format("Unable to read the number: %s, defaulting to %,d objects", num_objects_str, num_objects));
		        	}
		        	
	        		S3Benchmark.snapshot(num_objects);
	        	}
	        	catch(Exception e)
	        	{
	        		e.printStackTrace();
	        	}
	        	
	        	had_operation = true;
	        }
	        
	        if ( line.hasOption("s3read") )
	        {
	        	try
	        	{
	        		String num_objects_str = line.getOptionValue("s3read");
		        	if ( num_objects_str == null ) num_objects_str = "1,000";
		        	
		        	int num_objects = 1_000;
		        	
		        	try
		        	{
		        		num_objects = NumberFormat.getNumberInstance(java.util.Locale.US).parse(num_objects_str).intValue();
		        	}
		        	catch(Exception e)
		        	{
		        		System.out.println(String.format("Unable to read the number: %s, defaulting to %,d objects", num_objects_str, num_objects));
		        	}
		        	
	        		S3Benchmark.loadFilesFromS3(num_objects);
	        	}
	        	catch(Exception e)
	        	{
	        		e.printStackTrace();
	        	}
	        	
	        	had_operation = true;
	        }
	        
	        if ( !had_operation || line.hasOption("help") )
	        {
	        	onHelp();
	        }
	    }
	    catch( ParseException exp ) 
	    {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	        System.out.println();
	        onHelp();
	    }
	}
	
	private void setupOptions()
	{
		options = new Options();
		
		Option cur;
		
		{
			cur = new Option(null, "file", true, "the file to use for i/o purposes.  If not specified, test.dat is used");
			cur.setOptionalArg(false);
			cur.setArgName("FILENAME");
			options.addOption(cur);
		}
		
		{
			cur = new Option(null, "write", true, "the number of objects to create (in thousands).  If not specified, then 100,000 objects are created.");
			cur.setOptionalArg(true);
			
			options.addOption(cur);
		}
		
		{
			cur = new Option(null, "read", true, "Using the specified number of threads, load all objects from the file into RAM, measuring the time it takes to do so");
			cur.setOptionalArg(true);
			cur.setArgName("THREAD COUNT");
			options.addOption(cur);
		}
		
		{
			cur = new Option(null, "transform", true, "Transform the old XML Spec Data file into TestObjectProductData objects");
			cur.setOptionalArg(false);
			cur.setArgName("Spec Data XML File");
			options.addOption(cur);
		}
		
		{
			cur = new Option(null, "s3read", true, "Read objects from s3 as quickly as possible");
			cur.setOptionalArg(true);
			cur.setArgName("OBJECT COUNT");
			options.addOption(cur);
		}
		
		{
			cur = new Option(null, "s3_bulk_read", true, "Read objects (from a large file) from s3 as quickly as possible");
			cur.setOptionalArg(true);
			cur.setArgName("OBJECT COUNT");
			options.addOption(cur);
		}
		
		{
			cur = new Option(null, "s3write", false, "Write objects into s3 as quickly as possible");
			
			options.addOption(cur);
		}
		
		{
			cur = new Option(null, "s3_big_file_write", false, "Write a big file into S3");
			options.addOption(cur);
		}
		
		{
			cur = new Option(null, "s3_snapshot", true, "Test out the snapshot code");
			cur.setOptionalArg(true);
			cur.setArgName("OBJECT COUNT");
			options.addOption(cur);
		}
		
		
		{
			cur = new Option(null, "help", false, "Print out help content");
			cur.setOptionalArg(true);
			
			options.addOption(cur);
		}
	}
	
	private void onHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "io_bench", options );
	}
	
	private void write(int num_objects)
	{
		System.out.println(String.format("Writing %,d objects to %s", num_objects, dest.getAbsolutePath()));
		System.out.println();
		
		TestObjectProductData.Builder builder = new TestObjectProductData.Builder();

		long t1 = System.currentTimeMillis();

		List<TestObjectProductData> items = new ArrayList();

		for ( int i = 0; i < num_objects; i++ )
		{
			items.add(builder.createRandomProductData());

			if ( i % 1_000 == 0 )
				System.out.println(String.format("Building: %,d", i));
		}

		long t2 = System.currentTimeMillis();

		System.out.println();
		System.out.println(String.format("Build Time: %,d ms", (t2-t1)));
		System.out.println();


		try
		{
			//GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(new File("c:\\test.gz")));
			
			OutputStream raw = new FileOutputStream(dest);
			SmallDocumentWriter out = new SmallDocumentWriter(new OutputStreamWriter(raw));
	
			int write_count = 0;
	
			t1 = System.currentTimeMillis();
	
			for ( TestObjectProductData item : items )
			{
				write_count++;
	
				out.writeDocument(item.toXML());
	
				if ( write_count % 10_000 == 0 )
					System.out.println(String.format("Writing: %,d", write_count));
	
			}
	
			out.close();
	
			t2 = System.currentTimeMillis();
	
			System.out.println();
			System.out.println(String.format("Write Time: %,d ms", (t2-t1)));
			System.out.println(); 
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void read(int thread_count)
	{
		System.out.println(String.format("Reading objects from %s using %,d threads", dest.getAbsolutePath(), thread_count));
		System.out.println();

		try
		{
			if ( !dest.exists() )
			{
				System.out.println("No such file: "+dest.getAbsolutePath());
				return;
			}
			
			InputStream in_raw = GZIPUtils.gunzipStreamIfNeeded(new FileInputStream(dest));
			Reader reader_raw = new BufferedReader(new InputStreamReader(in_raw,"UTF-8"));
			SmallDocumentReader reader = new SmallDocumentReader(reader_raw);
	
			MyListener listener = new MyListener();
			StandardObjectBulkLoader loader = new StandardObjectBulkLoader(listener,thread_count);
			loader.addSource(reader);
			loader.doneAddingSources();
	
			long t1 = System.currentTimeMillis();
			int count = 0;
	
	
			while(true)
			{
				count++;
	
				if ( count % 10 == 0 )
				{
					System.out.println(String.format("loading objects: %,d loaded in %,d ms", listener.data.size(), System.currentTimeMillis()-t1));
				}
	
				try { Thread.currentThread().sleep(100); } catch(Exception e) { e.printStackTrace(); }
				if ( listener.done ) break;
			}
	
			long t2 = System.currentTimeMillis();
	
	
			System.out.println();
			System.out.println(String.format("loading finished: %,d loaded in %,d ms", listener.data.size(), System.currentTimeMillis()-t1));
			
			System.out.println();
			
			int write_count = 0;
			
			//while(true)
			{
				Thread.currentThread().sleep(1000); 
				
				write_count++;
				System.out.println(String.format("Starting write: %d",write_count));
				
				OutputStream out = new GZIPUtils.OutputStreamBestSpeed(new FileOutputStream(new File(dest.getParentFile(),"tmp_write_data.xml.gz")));
				SmallDocumentWriter writer = new SmallDocumentWriter(out);
				
				for ( TestObjectProductData item : listener.data )
				{
					writer.writeDocument(item.toXML());
				
					writer.printStatus(10_000);
				}
				
				writer.close();
			}
	
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	static private class MyListener implements StandardObjectBulkLoader.Listener
	{
		private boolean done = false;
		private Set<TestObjectProductData> data = Collections.newSetFromMap(new ConcurrentHashMap());
		
		public void onObjectLoaded(StandardObject obj)
		{
			data.add((TestObjectProductData)obj);
		}

		@Override
		public void onBulkLoaderFinished()
		{
			System.out.println("Done loading objects!");
			done = true;
		}
	}
	
	static public void sleepForever()
	{
		try
		{
			while(true) Thread.currentThread().sleep(1000);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
