package org.kane.elephant.io.benchmark;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

public class DynamoDBExperiemnts
{
	static private String TABLE_NAME = "product_db";
	
	static public void main(String args[])
	{
		BasicAWSCredentials credentials = new BasicAWSCredentials("foo","bar"); // nothing fancy needed, the local store accepts anything...
		
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials).withEndpoint("http://localhost:8000");

		DynamoDB db = new DynamoDB(client);
		
		createTableIfNotExists(db);
		
		
		Item foo = new Item();
		
		foo.withPrimaryKey(new PrimaryKey("key","foo:bar", "sort_key", "foo:bar"));
		foo.withString("some_document", "this is some document data");
		
		Table table = db.getTable(TABLE_NAME);
		table.putItem(foo);
		
		
		Item bar = table.getItem(new PrimaryKey("key","foo:bar","sort_key", "foo:bar"));
		
		System.out.println(bar);
	}
	
	static private void createTableIfNotExists(DynamoDB db)
	{
		try 
		{
			Table table;
			
			try
			{
				table = db.getTable(TABLE_NAME);
				table.describe();
				
				System.out.println(String.format("Table existed: %s",TABLE_NAME));
				return;
			}
			catch(Exception e)
			{	
				// An exception is thrown if the table does not exist, leading to the create table code...
			}
			
			System.out.println(String.format("Table does not exist, creating table: %s",TABLE_NAME));
			
			CreateTableRequest request = new CreateTableRequest();
			
			request.setTableName(TABLE_NAME);
			
			List<KeySchemaElement> key_schema = new ArrayList();
			{
				key_schema.add(new KeySchemaElement("key", KeyType.HASH)); // partion key
				key_schema.add(new KeySchemaElement("sort_key", KeyType.RANGE)); // sort key
				
				request.setKeySchema(key_schema); 
			}
			
			List<AttributeDefinition> attribute_definitions = new ArrayList();
			{
				attribute_definitions.add(new AttributeDefinition("key", ScalarAttributeType.S));
				attribute_definitions.add(new AttributeDefinition("sort_key", ScalarAttributeType.S));
				
				request.setAttributeDefinitions(attribute_definitions);
			}
			
			request.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
		
			table = db.createTable(request);

			table.waitForActive();
			System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());
		 } 
		 catch (Exception e) 
		 {
			 System.err.println(String.format("Unable to create table: %s",TABLE_NAME));
			 System.err.println(e.getMessage());
		 }
	}
	
}
