package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;


/*
decoding 4096 pixels
current index = [i][j]
calculate average based on previously decoded pixels in the frame array
model = model[avg]
give decoder a model and the bit source, and receive an integer pixel value
when pixel value is returned, add to frame array.
update model


*/

public class TemporalNeighboringPixelACDecodeVideo {
	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "data/temporal-compressed-video.dat";
		String output_file_name = "data/temporal-uncompressed-video.dat";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

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
		
		// Read in number of pixels encoded
		int num_pixels = bit_source.next(32);
		System.out.println("num_pixels = "+ num_pixels);

		// Read in range bit width and setup the decoder
		int range_bit_width = bit_source.next(8);
		System.out.println("range bit = "+range_bit_width);
		
		//setup decoder
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of encoded symbols: " + num_pixels);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];

		int curr_pixel = 0;
		while (curr_pixel < num_pixels) {
			
			//create array for current frame
			int[][][] frame = new int[300][64][64];
			
			//loop through frame, calculate average and then add symbol to frame.
			int avg = 0;
			for (int x=0; x<300; x++) {
				for (int i=0; i<frame[0].length; i++) {
					for (int j=0; j<frame[0].length; j++) {
						if (x>=1) {
							if (i==0) {
								if (j==0) {
									avg = (frame[x-1][i][j]);
								}else if (j>=1 && j<frame[0].length-1) {
									avg = (frame[x][i][j-1]+frame[x-1][i][j-1]+frame[x-1][i][j]+frame[x-1][i][j+1]+frame[x-1][i+1][j-1]+frame[x-1][i+1][j]+frame[x-1][i+1][j+1])/7;
								}else {
									avg = (frame[x][i][j-1]+frame[x-1][i][j-1]+frame[x-1][i][j]+frame[x-1][i+1][j-1]+frame[x-1][i+1][j])/5;
								}
							}else if (i>=1 && i<frame[0].length-1) {
								if (j==0) {
									avg = (frame[x][i-1][j]+frame[x][i-1][j]+frame[x-1][i-1][j+1]+frame[x-1][i][j]+frame[x-1][i][j+1]+frame[x-1][i+1][j]+frame[x-1][i+1][j+1])/7;
								}else if (j>=1 && j<frame[0].length-1){
									avg = (frame[x][i][j-1]+ frame[x][i-1][j-1] + frame[x][i-1][j] + frame[x][i-1][j+1]+frame[x-1][i-1][j-1]+frame[x-1][i-1][j]+frame[x-1][i-1][j+1]+frame[x-1][i][j-1]+frame[x-1][i][j]+frame[x-1][i][j+1]+frame[x-1][i+1][j-1]+frame[x-1][i+1][j]+frame[x-1][i+1][j+1])/13;
								}else {
									avg = (frame[x][i][j-1] + frame[x][i-1][j-1] + frame[x][i-1][j]+frame[x-1][i-1][j-1]+frame[x-1][i-1][j]+frame[x-1][i][j-1]+frame[x-1][i][j]+frame[x-1][i+1][j-1]+frame[x-1][i+1][j])/9;
								}
							}else {
								if (j==0) {
									avg = (frame[x][i-1][j] + frame[x][i-1][j+1]+frame[x-1][i-1][j]+frame[x-1][i-1][j+1]+frame[x-1][i][j]+frame[x-1][i][j+1])/6;
								}else if (j>=1 && j<frame[0].length-1) {
									avg = (frame[x][i][j-1] + frame[x][i-1][j-1] + frame[x][i-1][j] + frame[x][i-1][j+1]+frame[x-1][i-1][j-1]+frame[x-1][i-1][j]+frame[x-1][i-1][j+1]+frame[x-1][i][j-1]+frame[x-1][i][j]+frame[x-1][i][j+1])/10;
								}else {
									avg = (frame[x][i][j-1] + frame[x][i-1][j-1] + frame[x][i-1][j]+frame[x-1][i-1][j-1]+frame[x-1][i-1][j]+frame[x-1][i][j-1]+frame[x-1][i][j])/7;
								}
							}
						}
						else {
							if (i==0) {
								if (j==0) {
									avg = 0;
								}else if (j>=1 && j<frame[0].length-1) {
									avg = (frame[x][i][j-1]);
								}else {
									avg = (frame[x][i][j-1]);
								}
							}else if (i>=1 && i<frame[0].length-1) {
								if (j==0) {
									avg = (frame[x][i-1][j]+frame[x][i-1][j+1])/2;
								}else if (j>=1 && j<frame[0].length-1){
									avg = (frame[x][i][j-1]+ frame[x][i-1][j-1] + frame[x][i-1][j] + frame[x][i-1][j+1])/4;
								}else {
									avg = (frame[x][i][j-1] + frame[x][i-1][j-1] + frame[x][i-1][j])/3;
								}
							}else {
								if (j==0) {
									avg = (frame[x][i-1][j] + frame[x][i-1][j+1])/2;
								}else if (j>=1 && j<frame[0].length-1) {
									avg = (frame[x][i][j-1] + frame[x][i-1][j-1] + frame[x][i-1][j] + frame[x][i-1][j+1])/4;
								}else {
									avg = (frame[x][i][j-1] + frame[x][i-1][j-1] + frame[x][i-1][j])/3;
								}
							}
						}
					
					//decode using model for average found above
					model = models[avg];
					int pixel = decoder.decode(model, bit_source);
					fos.write(pixel);
					//add pixel to frame array
					frame[x][i][j] = pixel;
					curr_pixel++;
					//update model
					model.addToCount(pixel);
					System.out.println("pixels decoded = "+curr_pixel);
					
					
				}
			}
			
			
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}

}
}

