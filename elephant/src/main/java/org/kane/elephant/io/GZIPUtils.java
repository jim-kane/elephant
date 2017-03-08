package org.kane.elephant.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.common.io.ByteStreams;

public class GZIPUtils 
{
	/**
	 * Convenience method to GZIP an array of bytes
	 * 
	 * @param src The bytes to compress
	 * @param default_value The value to return in the event of an error
	 * @return The compressed bytes, or default value otherwise
	 */
	static public byte[] gzipBytes(byte src[], byte default_value[])
	{
		if ( isCompressedUsingGZIP(src) ) return src;
		
		try
		{
			ByteArrayOutputStream out_raw = new ByteArrayOutputStream();
			GZIPOutputStream out = new GZIPOutputStream(out_raw);
			
			out.write(src);
			out.close();
			
			return out_raw.toByteArray();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return default_value;
		}
	}
	
	/**
	 * Determine if an array of bytes contains gzipped data. 
	 * 
	 * This is accomplished by checking to see if the first two bytes are the gzip magic number, 0x1f, 0x8b
	 * 
	 * @param src The (possibly compressed via gzip) byte array
	 * 
	 * @return True if src can be positive identified as gzip data, false otherwise
	 */
	static public boolean isCompressedUsingGZIP(byte src[])
	{
		if ( src == null || src.length < 2 ) return false;
		
		return src[0] == (byte)0x1f && src[1] == (byte)0x8b; 
	}
	
	/**
	 * Check if src[] is compressed using gzip.  If it is, uncompress it, otherwise, do nothing to it
	 * 
	 * @param src The bytes to de-compress (if needed)
	 * 
	 * @param default_value the value to return if there is an error
	 * 
	 * @return an uncompressed version of src[] or default_value[] if an error occurs
	 */
	static public byte[] gunzipBytesIfNeeded(byte src[], byte default_value[])
	{
		if ( !isCompressedUsingGZIP(src) ) 
			return src;
		
		try
		{
			ByteArrayInputStream in_raw = new ByteArrayInputStream(src);
			GZIPInputStream in = new GZIPInputStream(in_raw);
			
			return ByteStreams.toByteArray(in);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return default_value;
		}
	}
	
	/**
	 * Check if the input stream's data is gzipped, if so, return an input
	 * stream that will (in line) de-compress the data
	 * 
	 * @param in
	 *            The input stream (must support mark)
	 * @return An input stream that will decompress data (if required)
	 * @throws IOException
	 */
	static public InputStream gunzipStreamIfNeeded(InputStream in) throws IOException
	{
		if ( !in.markSupported() ) 
			in = new BufferedInputStream(in);
		
		in.mark(5);
		
		byte arr[] = new byte[3];
		int amount_read = in.read(arr);
		in.reset();
		
		if ( amount_read != arr.length )
		{
			return in;
		}
		
		if ( isCompressedUsingGZIP(arr) )
		{
			return new GZIPInputStream(in);
		}
		
		return in;
	}
	
	static public class OutputStreamBestSpeed extends GZIPOutputStream
	{
		public OutputStreamBestSpeed(OutputStream out) throws IOException
		{
			super(out);
	      	def.setLevel(Deflater.BEST_SPEED);
		}
	    
	}
	
}
