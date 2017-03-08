package org.kane.elephant.io.benchmark;

import java.util.Map;

import org.kane.base.immutability.StandardImmutableObject;
import org.kane.base.immutability.collections.FieldStringStringHashMap;
import org.kane.base.serialization.Normalizer;
import org.kane.base.serialization.RandomData;
import org.kane.base.serialization.Validator;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class TestObjectProductData extends StandardImmutableObject 
{
	private String brand_code;
	private String pn;
	
	private FieldStringStringHashMap kv_data = new FieldStringStringHashMap();
	
	public TestObjectProductData()
	{
		complete();
	}
	
	private TestObjectProductData(Builder builder)
	{
		
	}
	
	public String getSimpleBrandCode() { return brand_code; }
	public String getSimplePN() { return pn; }
	public Map<String,String> getSimpleKVData() { return kv_data; }
	public String getSimpleKey() { return String.format("%s:%s", brand_code,pn); }
	 
	public int compareTo(Object o)
	{
		if ( !(o instanceof TestObjectProductData) ) 
			return 0;
		
		TestObjectProductData other = (TestObjectProductData)o;
		
		return pn.compareTo(other.pn);
	}
	public void freeze()
	{
		kv_data.freeze();
	}

	public void normalize()
	{
		brand_code = Normalizer.upperCase(brand_code);
		pn = Normalizer.upperCase(pn);
	}

	public void validate()
	{
		Validator.notNull(brand_code);
		Validator.notNull(pn);
	}
	
	public int hashCode()
	{
		return pn.hashCode();
	}


	public boolean equals(Object obj)
	{
		return false;
	}

	static public class Builder
	{
		private RandomData random = new RandomData();
		private TestObjectProductData under_construction;
		
		public Builder()
		{
			under_construction = new TestObjectProductData(this);
		}
		
		public void setBrandCode(String brand_code)
		{
			under_construction.brand_code = brand_code;
		}
		
		public void setPN(String pn)
		{
			under_construction.pn = pn;
		}
		
		public void putKVData(String key, String value)
		{
			if ( key == null || value == null ) return;
			
			key = key.toUpperCase().intern();
			
			under_construction.kv_data.put(key.toUpperCase(), value);
		}
		
		public TestObjectProductData create()
		{
			return (TestObjectProductData)under_construction.deepClone();
		}
		
		public TestObjectProductData createRandomProductData()
		{
			return createRandomProductData(random.randomInt(20, 200));
		}
		
		public TestObjectProductData createRandomProductData(int num_features)
		{
			setBrandCode(random.randomStringOfLength(RandomData.ALPHABET_ALPHA_UPPER_CASE, 4,12));
			setPN(random.randomStringOfLength(RandomData.ALPHABET_ALPHA_UPPER_CASE, 8,17));
			
			for ( int i = 0; i < num_features; i++ )
			{
				putKVData(random.randomStringOfLength(RandomData.ALPHABET_ALPHA_NUMERIC_UPPER_CASE, 10, 20), random.randomStringOfLength(RandomData.ALPHABET_ALPHA_NUMERIC, 35, 200));
			}
			
			return create();
		}
	}
	
}
