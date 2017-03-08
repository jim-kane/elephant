package org.kane.elephant.io.snapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kane.base.threading.OperationMonitor;
import org.kane.base.threading.OperationPool;
import org.kane.base.threading.OperationRunnable;
import org.kane.elephant.io.SmallDocumentWriter;
import org.kane.elephant.io.benchmark.AWSAPIKeys;

import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class OperationSnapshot extends OperationRunnable
{
	static public final int THREAD_COUNT = 100;
	
	// Request
	private SnapshotRequest request;
	
	// State data
	private File dest;
	private SmallDocumentWriter snapshot_writer;
	private AmazonS3Client s3_client; 
	private OperationPool child_operations;
	
	// Statistics
	volatile  int stats_objects_written_to_snapshot = 0;
	volatile long stats_bytes_written_to_snapshot = 0;
	
	public OperationSnapshot(SnapshotRequest request)
	{
		this.request = request;
	}
	
	public SnapshotRequest getSimpleRequest() { return request; }
	public File getSimpleDestination() { return dest; }
	public AmazonS3Client getSimpleS3Client() { return s3_client; }
	public OperationPool getSimpleChildOperations() { return child_operations; }
	
	public void addObjectToSnapshot(byte data[]) throws IOException
	{
		synchronized(snapshot_writer)
		{
			snapshot_writer.writeDocument(new String(data));
			
			stats_objects_written_to_snapshot++;
			stats_bytes_written_to_snapshot += data.length;
		}
	}
	
	protected Result performOperation() throws Exception
	{
		if ( shouldStop() ) return Result.STOPPED;

		// Setup the state data
		{
			dest = File.createTempFile("snapshot", "dat");
			
			snapshot_writer = new SmallDocumentWriter(new FileOutputStream(dest)); 
	
			s3_client = new AmazonS3Client(AWSAPIKeys.getAWSCredentialsDev());
			s3_client.setRegion(Region.getRegion(request.getSimpleRegion()));
		}

		// Setup the child operations pool
		OperationList list_operation = new OperationList(this);
		
		
		child_operations = new OperationPool(list_operation, THREAD_COUNT);
		
		Result result = OperationRunnable.executeWithMonitor(child_operations, 500, new TakeSnapshotMonitor(), Result.ERROR);
		
		snapshot_writer.close();
		
		if ( result != Result.SUCCESS ) return result;

		System.out.println(String.format("Finished taking snapshot! %,d objects written, %,d KB in %,d ms", stats_objects_written_to_snapshot, stats_bytes_written_to_snapshot/1024l, System.currentTimeMillis() - child_operations.getOptionalStartTime(0)));
		System.out.println();
		
		
		OperationUploadSnapshot upload = new OperationUploadSnapshot(this);
		
		result = OperationRunnable.executeWithMonitor(upload, 500, new UploadSnapshotMonitor(), Result.ERROR);
		
		long bytes_uploaded = upload.getBytesUploaded();
		
		System.out.println(String.format("Finished Upload! Uploaded %,d kb in %,d ms", bytes_uploaded/1024l, System.currentTimeMillis()-upload.getOptionalStartTime(0)));
		
		return result;
	}
	
	private class TakeSnapshotMonitor implements OperationMonitor
	{
		public void onOperationMonitorHeartbeat(OperationRunnable runnable)
		{
			System.out.println(String.format("Taking snapshot: %,d objects written, %,d KB in %,d ms", stats_objects_written_to_snapshot, stats_bytes_written_to_snapshot/1024l, System.currentTimeMillis() - child_operations.getOptionalStartTime(0)));
		}
		
	}
	
	private class UploadSnapshotMonitor implements OperationMonitor
	{
		public void onOperationMonitorHeartbeat(OperationRunnable runnable)
		{
			if ( !(runnable instanceof OperationUploadSnapshot) ) return;
			
			OperationUploadSnapshot upload = (OperationUploadSnapshot)runnable;
			
			long bytes_uploaded = upload.getBytesUploaded();
			
			System.out.println(String.format("Uploaded %,d kb in %,d ms", bytes_uploaded/1024l, System.currentTimeMillis()-upload.getOptionalStartTime(0)));
		}
		
	}
}
