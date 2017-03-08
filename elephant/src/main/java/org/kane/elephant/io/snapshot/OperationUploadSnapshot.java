package org.kane.elephant.io.snapshot;

import org.kane.base.threading.OperationRunnable;
import org.kane.elephant.io.benchmark.AWSAPIKeys;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class OperationUploadSnapshot extends OperationRunnable
{
	private OperationSnapshot snapshot_operation;
	private Upload my_upload;
	
	public OperationUploadSnapshot(OperationSnapshot snapshot_operation)
	{
		this.snapshot_operation = snapshot_operation;
	}

	
	protected Result performOperation() throws Exception
	{
		TransferManager manager = new TransferManager(AWSAPIKeys.getAWSCredentialsDev()); 

		SnapshotRequest request = snapshot_operation.getSimpleRequest();
		
		PutObjectRequest put_request = new PutObjectRequest( 
				request.getSimpleDestinationBucketName(), 
				request.getSimpleDestinationKey(), 
				snapshot_operation.getSimpleDestination()
			);
		
		
		my_upload = manager.upload(put_request);

		while(true)
		{
			if ( my_upload.isDone() ) break;
			Thread.currentThread().sleep(500);
		}

		manager.shutdownNow();
		
		return Result.SUCCESS;
	}
	
	public long getBytesUploaded()
	{
		if ( my_upload != null )
			 return my_upload.getProgress().getBytesTransfered();
		
		return 0;
	}
	
}
