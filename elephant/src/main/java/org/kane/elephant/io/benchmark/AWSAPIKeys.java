package org.kane.elephant.io.benchmark;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public class AWSAPIKeys 
{
	static private String getDevelopmentAWSID() { return System.getenv("AWSS3IDDEV"); }
	static private String getDevelopmentAWSSecretKeyDoNotPubliclyExpose() { return System.getenv("AWSS3KEYDEV"); } // Be careful never to expose this key
	
	static private String getProductionAWSID() { return System.getenv("AWSS3IDPROD"); }
	static private String getProductionAWSSecretKeyDoNotPubliclyExpose() { return System.getenv("AWSS3KEYPROD"); } // Be careful never to expose this key
	
	static public AWSCredentials getAWSCredentialsDev()
	{
		return new BasicAWSCredentials(getDevelopmentAWSID(),getDevelopmentAWSSecretKeyDoNotPubliclyExpose());
	}
}
