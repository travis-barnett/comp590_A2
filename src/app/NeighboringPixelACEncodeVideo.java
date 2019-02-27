package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class NeighboringPixelACEncodeVideo {
	
	
	public static void main(String[] args) throws IOException {
		String input_file_name = "data/uncompressed-video.dat";
		String output_file_name = "data/neighboring-compressed-video.dat";

		int range_bit_width = 40;

		System.out.println("Encoding video file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int total_pixels = (int) new File(input_file_name).length();
		
				
		Integer[] intensity = new Integer[256];
		for (int i=0; i<256; i++) {
			intensity[i] = i;
		}

		// Create 256 models. Model chosen depends on value of symbol prior to 
		// symbol being encoded.
		
		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[256];
		
		for (int i=0; i<256; i++) {
			// Create new model with default count of 1 for all symbols
			models[i] = new FreqCountIntegerSymbolModel(intensity);
		}

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);
		
		// First 4 bytes are the number of pixels encoded
		bit_sink.write(total_pixels, 32);		

		// Next byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);

		// Now encode the input
		FileInputStream fis = new FileInputStream(input_file_name);
		
		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];

		
		//loop through entire document and encode one frame at a time
		int curr_pixel = 0;
		while (curr_pixel < total_pixels) {
				
			
			//fill 2d array with next frame from input file
			int[][] frame = new int[64][64];
			for (int i=0; i<frame.length; i++) {
				for (int j=0; j<frame.length; j++) {
					int next_symbol = fis.read();
					frame[i][j] = next_symbol;
				}
			}
			
			System.out.println("FRAME CREATED");
			System.out.println("current pixel = "+curr_pixel);
			
			//iterate through frame and find average for each pixel
			int avg = 0;
			for (int i=0; i<frame.length; i++) {
				for (int j=0; j<frame.length; j++) {
					if (i==0) {
						if (j==0) {
							avg = 0;
						}else if (j>=1 && j<frame.length-1) {
							avg = (frame[i][j-1]);
						}else {
							avg = (frame[i][j-1]);
						}
					}else if (i>=1 && i<frame.length-1) {
						if (j==0) {
							avg = (frame[i-1][j]+frame[i-1][j+1])/2;
						}else if (j>=1 && j<frame.length-1){
							avg = (frame[i][j-1]+ frame[i-1][j-1] + frame[i-1][j] + frame[i-1][j+1])/4;
						}else {
							avg = (frame[i][j-1] + frame[i-1][j-1] + frame[i-1][j])/3;
						}
					}else {
						if (j==0) {
							avg = (frame[i-1][j] + frame[i-1][j+1])/2;
						}else if (j>=1 && j<frame.length-1) {
							avg = (frame[i][j-1] + frame[i-1][j-1] + frame[i-1][j] + frame[i-1][j+1])/4;
						}else {
							avg = (frame[i][j-1] + frame[i-1][j-1] + frame[i-1][j])/3;
						}
					}
					
					
					
					//encode based using model for average found above
					model = models[avg];
					encoder.encode(frame[i][j], model, bit_sink);
					curr_pixel++;
					
					// Update model used
					model.addToCount(frame[i][j]);
				}
				
				
			}
		
		
		
		
			
		}
		
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
	}

}
