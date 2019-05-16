package org.opencv.test.features;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;

public class FastDetector {

	public static void main(String [] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	
	     //I added a piece of the same pic
		 File folderInput = new File("foto.jpg");
	     BufferedImage completeImage;
	     //I added a piece of the same pic
	     File folderInput2 = new File("subfoto.jpg");
	     BufferedImage subImage;
		
			completeImage = ImageIO.read(folderInput);
			subImage = ImageIO.read(folderInput2);
			boolean matches = performFeatureMatching(completeImage, subImage);
		
	    
			if(matches) {
				System.out.println("Emparejan!!");				
			}
			else {
				System.out.println("No Emparejan!!");
			}
			
	  				
		
	}
	
	public static boolean performFeatureMatching(BufferedImage largeBufferedImage, BufferedImage smallBufferedImage) throws IOException
	{
	    FastFeatureDetector fd = FastFeatureDetector.create();
	    final MatOfKeyPoint keyPointsLarge = new MatOfKeyPoint();
	    final MatOfKeyPoint keyPointsSmall = new MatOfKeyPoint();

	    Mat largeImage = bufferedImage2Mat(largeBufferedImage);
	    Mat smallImage = bufferedImage2Mat(smallBufferedImage);
	    fd.detect(largeImage, keyPointsLarge);
	    fd.detect(smallImage, keyPointsSmall);

	    System.out.println("keyPoints.size() : " + keyPointsLarge.size());
	    System.out.println("keyPoints2.size() : " + keyPointsSmall.size());

	    Mat descriptorsLarge = new Mat();
	    Mat descriptorsSmall = new Mat();

	    DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
	    extractor.compute(largeImage, keyPointsLarge, descriptorsLarge);
	    extractor.compute(smallImage, keyPointsSmall, descriptorsSmall);

	    System.out.println("descriptorsA.size() : " + descriptorsLarge.size());
	    System.out.println("descriptorsB.size() : " + descriptorsSmall.size());

	    MatOfDMatch matches = new MatOfDMatch();

	    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
	    matcher.match(descriptorsLarge, descriptorsSmall, matches);

	    System.out.println("matches.size() : " + matches.size());

	    MatOfDMatch matchesFiltered = new MatOfDMatch();

	    List<DMatch> matchesList = matches.toList();
	    List<DMatch> bestMatches = new ArrayList<>();

	    Double max_dist = 0.0;
	    Double min_dist = 100.0;

	    for (DMatch aMatchesList : matchesList)
	    {
	        Double dist = (double) aMatchesList.distance;

	        if (dist < min_dist && dist != 0)
	        {
	            min_dist = dist;
	        }

	        if (dist > max_dist)
	        {
	            max_dist = dist;
	        }

	    }

	    System.out.println("max_dist : " + max_dist);
	    System.out.println("min_dist : " + min_dist);

	    if (min_dist > 50)
	    {
	        System.out.println("No match found");
	        System.out.println("Just return ");
	        return false;
	    }

	    double threshold = 3 * min_dist;
	    double threshold2 = 2 * min_dist;

	    if (threshold > 75)
	    {
	        threshold = 75;
	    } else if (threshold2 >= max_dist)
	    {
	        threshold = min_dist * 1.1;
	    } else if (threshold >= max_dist)
	    {
	        threshold = threshold2 * 1.4;
	    }

	    System.out.println("Threshold : " + threshold);

	    for (int i = 0; i < matchesList.size(); i++)
	    {
	        double dist = (double) matchesList.get(i).distance;

	        if (dist < threshold)
	        {
	            bestMatches.add(matches.toList().get(i));
	            //System.out.println(String.format(i + " best match added : %s", dist));
	        }
	    }

	    matchesFiltered.fromList(bestMatches);

	    System.out.println("matchesFiltered.size() : " + matchesFiltered.size());


	    if (matchesFiltered.rows() >= 4)
	    {
	        System.out.println("match found");
	        return true;
	    } else
	    {
	        return false;
	    }
	}

	private static Mat bufferedImage2Mat(BufferedImage in) {
		Mat out;
		byte[] data;
		int r, g, b;
		final int width = in.getWidth();
		final int height = in.getHeight();

		if (in.getType() == BufferedImage.TYPE_INT_RGB) {
			out = new Mat(height, width, CvType.CV_8UC3);
			data = new byte[width * height * (int) out.elemSize()];
			final int[] dataBuff = in.getRGB(0, 0, width, height, null, 0, width);
			for (int i = 0; i < dataBuff.length; i++) {
				data[i * 3] = (byte) ((dataBuff[i] >> 16) & 0xFF);
				data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
				data[i * 3 + 2] = (byte) ((dataBuff[i] >> 0) & 0xFF);
			}
		} else {
			out = new Mat(height, width, CvType.CV_8UC1);
			data = new byte[width * height * (int) out.elemSize()];
			final int[] dataBuff = in.getRGB(0, 0, width, height, null, 0, width);
			for (int i = 0; i < dataBuff.length; i++) {
				r = (byte) ((dataBuff[i] >> 16) & 0xFF);
				g = (byte) ((dataBuff[i] >> 8) & 0xFF);
				b = (byte) ((dataBuff[i] >> 0) & 0xFF);
				data[i] = (byte) ((0.21 * r) + (0.71 * g) + (0.07 * b)); // luminosity
			}
		}
		out.put(0, 0, data);
		return out;
	}
	
}