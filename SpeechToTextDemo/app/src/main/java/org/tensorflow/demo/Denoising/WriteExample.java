package org.tensorflow.demo.Denoising;

import android.os.Environment;

import java.io.*;

public class WriteExample
{
	public void Write(String name,double[] signal)
	{
		//	System.out.println("Inside write example - sig length");
		//System.out.println(signal.length);
		try
		{
			int sampleRate = 16000;		// Samples per second

			double duration =( signal.length/(double) sampleRate);		// Seconds
			//System.out.println(duration);
			// Calculate the number of frames required for specified duration
			long numFrames = (long)(duration * sampleRate);
			//System.out.println(numFrames);
			// Create a wav file with the name specified as the first argument
			WavFile wavFile = WavFile.newWavFile(new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC+"/denoise/"+name))), 1, numFrames, 16, sampleRate);

			// Create a buffer of 100 frames
			double[] buffer = new double[signal.length];

			// Initialise a local frame counter
			long frameCounter = 0;

			// Loop until all frames written
			while (frameCounter < numFrames)
			{
				// Determine how many frames to write, up to a maximum of the buffer size
				//long remaining = wavFile.getFramesRemaining();


				// Fill the buffer, one tone per channel
				for (int s=0 ; s<signal.length ; s++, frameCounter++)
				{
					buffer[s]=signal[s];
					//	System.out.println(buffer[s]);

				}

				// Write the buffer
				wavFile.writeFrames(buffer, signal.length);
			}
			//System.out.println("hello world");

			// Close the wavFile
			wavFile.close();
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}
}
