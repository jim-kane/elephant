package org.kane.elephant.io;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.kane.base.serialization.StandardObject;
import org.kane.base.serialization.Validator;

public class StandardObjectBulkLoader
{
	private BlockingQueue<String> document_queue;
	private Listener listener;
	
	private boolean has_finished = false;
	
	private boolean have_all_sources_been_added = false;
	
	private List<DocumentLoadingThread> document_loading_threads = new CopyOnWriteArrayList();
	
	public StandardObjectBulkLoader(Listener listener, int object_loading_thread_count)
	{
		Validator.notNull(listener);
		Validator.min(object_loading_thread_count, 1);
		
		document_queue = new ArrayBlockingQueue(Math.max(32, object_loading_thread_count*8));
		
		this.listener = listener;
		
		for ( int i = 0; i < object_loading_thread_count; i++ )
		{
			ObjectLoadingThread thread = new ObjectLoadingThread();
			thread.start();
		}
	}
	
	public void addSource(SmallDocumentSource source)
	{
		Validator.notNull(source);
		
		DocumentLoadingThread thread = new DocumentLoadingThread(source);
		thread.start();
		
		document_loading_threads.add(thread);
	}
	
	public void doneAddingSources() 
	{
		have_all_sources_been_added =  true;
		checkIfFinished();
	}
	
	synchronized private void checkIfFinished()
	{
		if ( has_finished ) return; // already done, nothing to check...
		if ( have_all_sources_been_added == false ) return; // can't be done... more sources may be added...
		
		if ( !document_queue.isEmpty() ) return;
		
		for ( DocumentLoadingThread thread : document_loading_threads )
		{
			if ( thread.state == LoadingThreadState.RUNNING )
				return;
		}
		
		has_finished = true;
		listener.onBulkLoaderFinished();
	}
	
	static public interface Listener
	{
		public void onObjectLoaded(StandardObject obj);
		public void onBulkLoaderFinished();
	}
	
	private class ObjectLoadingThread extends Thread
	{
		private ObjectLoadingThread()
		{
			
		}
		
		public void run()
		{
			while(true)
			{
				try 
				{
					String document = document_queue.poll(1000, TimeUnit.MILLISECONDS);
					
					if ( document == null ) continue;
					
					if ( document != null )
					{
						StandardObject obj = StandardObject.fromXML(document);
						if ( obj != null ) 
							listener.onObjectLoaded(obj);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					checkIfFinished();
				}
				
				if ( has_finished ) return;
			}
		}
	}
	
	private class DocumentLoadingThread extends Thread
	{
		private SmallDocumentSource source;
		private LoadingThreadState state;
		
		private DocumentLoadingThread(SmallDocumentSource source)
		{
			this.source = source;
			this.state = LoadingThreadState.RUNNING;
		}
		
		public void run()
		{
			while(true)
			{
				try
				{
					source.readNextDocument();  // Get a document
					
					switch( source.getSimpleState() )
					{
					case DOCUMENT_AVAILABLE:
						document_queue.put(source.getCurrentDocument(null));
						break;
					case READ_DOCUMENT_NOT_YET_ATTEMPTED:
						break;
					case NO_MORE_DOCUMENTS:
						state = LoadingThreadState.COMPLETE;
						return;
					case ERROR_ENCOUNTERED:
						state = LoadingThreadState.ERROR;
						return;
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					this.state = LoadingThreadState.ERROR;
					return;
				}
				finally
				{
					checkIfFinished();
				}
			}
		}
		
		
	}
	
	static public enum LoadingThreadState
	{
		RUNNING, 
		COMPLETE,
		ERROR;
	}
}
