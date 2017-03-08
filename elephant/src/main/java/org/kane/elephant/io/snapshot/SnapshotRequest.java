package org.kane.elephant.io.snapshot;

import java.util.Objects;

import org.kane.base.immutability.StandardImmutableObject;
import org.kane.base.serialization.Optional;
import org.kane.base.serialization.Validator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.common.collect.ComparisonChain;

public class SnapshotRequest extends StandardImmutableObject
{
	private Regions region = Regions.US_WEST_2; // required
	
	private String source_bucket_name; // required
	private String source_list_prefix = ""; // required
	
	private String destination_bucket_name; // required
	private String destination_key; // required
	
	private int maximum_object_count = Integer.MAX_VALUE; // required
	
	private SnapshotRequest(Builder builder)
	{
	}
	
	
	/**
	 * Get the AWS region that will be used as the API end point for all S3 API
	 * calls needed to take this snapshot
	 * 
	 * @return The region
	 */
	public Regions getSimpleRegion() { return region; }
	
	
	public String getSimpleSourceBucketName() { return source_bucket_name; }
	public String getSimpleSourceListPrefix() { return source_list_prefix; }
	
	public String getSimpleDestinationBucketName() { return destination_bucket_name; }
	public String getSimpleDestinationKey() { return destination_key; }
	
	public int getSimpleMaximumObjectCount() { return maximum_object_count; }
	

	public void freeze()
	{
	}

	public void normalize()
	{
	}

	public void validate()
	{
		Validator.notNull(region);
		
		Validator.notNull(source_bucket_name);
		Validator.notNull(source_list_prefix);
		
		Validator.notNull(destination_bucket_name);
		Validator.notNull(destination_key);
	}

	public int hashCode()
	{
		return Objects.hash( 
				getSimpleRegion().getName(),
				getSimpleSourceBucketName(), 
				getSimpleSourceListPrefix(), 
				getSimpleDestinationBucketName(), 
				getSimpleDestinationKey(), 
				getSimpleMaximumObjectCount()
			);
	} 
	
	public int compareTo(Object o)
	{
		if ( !(o instanceof SnapshotRequest) ) return 0;
		
		SnapshotRequest other = (SnapshotRequest)o;
		
		return ComparisonChain.start()
				.compare(getSimpleRegion(), other.getSimpleRegion())
				
				.compare(getSimpleSourceBucketName(), other.getSimpleSourceBucketName())
				.compare(getSimpleSourceListPrefix(), other.getSimpleSourceListPrefix())
				
				.compare(getSimpleDestinationBucketName(), other.getSimpleDestinationBucketName())
				.compare(getSimpleDestinationKey(), other.getSimpleDestinationKey())
				
				.compare(getSimpleMaximumObjectCount(), other.getSimpleMaximumObjectCount())
				
				.result();
	}
	
	public boolean equals(Object obj)
	{
		return compareTo(obj) == 0;
	}
	
	static public class Builder 
	{
		private SnapshotRequest under_construction;
		
		public Builder()
		{
			under_construction = new SnapshotRequest(this);
		}
		
		public Builder(SnapshotRequest starting_point)
		{
			under_construction = (SnapshotRequest)starting_point.deepMutableCloneForBuilder();
		}
		
		public void setRegion(Regions region) { under_construction.region = region; }
		

		public void setSourceBucketName(String bucket_name) { under_construction.source_bucket_name = bucket_name; }
		public void setSourceListPrefix(String prefix) { under_construction.source_list_prefix = prefix; }
		
		public void setDestinationBucketName(String bucket_name) { under_construction.destination_bucket_name = bucket_name; }
		public void setDestinationKey(String key) { under_construction.destination_key = key; }
		
		public void setMaximumObjectCount(int count) { under_construction.maximum_object_count = count; }
		
		public SnapshotRequest create()
		{
			return (SnapshotRequest)under_construction.deepClone();
		}
	}
}
