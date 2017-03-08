package org.kane.elephant.io.snapshot;

import org.kane.base.serialization.Validator;
import org.kane.base.threading.OperationRunnable;
import org.kane.base.threading.OperationRunnable.Result;
import org.kane.elephant.io.SmallDocumentWriter;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

public class OperationDownloadObject extends OperationRunnable
{
	private S3ObjectSummary object_summary;
	private OperationSnapshot snapshot_operation;
	
	public OperationDownloadObject(OperationSnapshot snapshot_operation, S3ObjectSummary object_summary)
	{
		Validator.notNull(snapshot_operation);
		Validator.notNull(object_summary);
		
		this.object_summary = object_summary;
		this.snapshot_operation = snapshot_operation;
	}
	
	protected Result performOperation() throws Exception
	{
		if ( shouldStop() ) 
			return Result.STOPPED;
		
		if ( object_summary.getSize() > Constants.MAXIMUM_OBJECT_SIZE ) return Result.SUCCESS; // don't add the object to the snapshot, it is too large (THIS IS NOT AN ERROR -- SILENTLY SKIP)

		GetObjectRequest request = new GetObjectRequest(object_summary.getBucketName(),object_summary.getKey());

		S3Object s3_obj = snapshot_operation.getSimpleS3Client().getObject(request);
		
		if ( shouldStop() ) 
			return Result.STOPPED;

		byte[] data = IOUtils.toByteArray(s3_obj.getObjectContent());

		if ( shouldStop() ) 
			return Result.STOPPED;

		snapshot_operation.addObjectToSnapshot(data);

		return Result.SUCCESS;
	}
}
