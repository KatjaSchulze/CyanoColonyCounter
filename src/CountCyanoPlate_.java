import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.OpenDialog;

import java.io.File;
import java.io.FilenameFilter;

import ij.plugin.*;
import ij.plugin.frame.*;

public class CountCyanoPlate_ implements PlugIn {

	public void run(String arg) {
		
		FilenameFilter filter  = new FilenameFilter()
		{
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".bmp") || name.toLowerCase().endsWith(".tif") || name.toLowerCase().endsWith(".tiff") ||
					   name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") ;

		}};

		OpenDialog od = new OpenDialog("Open first image ...", arg);
		String directory = od.getDirectory();
		String fileName = od.getFileName();
			
		if (fileName==null) return;
		String[] imageList = new File(directory).list(filter);
		java.util.Arrays.sort(imageList);
		
		int imagecount = 0;
		int total = 0;
		
		for (int k = 0; k<imageList.length; k++){
			String path = directory+imageList[k];	
			ImagePlus imp = new ImagePlus(path);
			imp.show(); 
			RoiManager rm = RoiManager.getInstance();
			if (rm==null) rm = new RoiManager();
			rm.runCommand("reset");
			IJ.run("Select All", "");
			IJ.run(imp, "Copy", "");
			IJ.run("Internal Clipboard", "");
			ImagePlus imp2 = WindowManager.getImage("Clipboard");
			WindowManager.getImage("Clipboard").hide();

			// threshold brightness of hsb
			ImagePlus hsb1 = this.makeHSBStack(imp2);
			ImageProcessor ip = hsb1.getStack().getProcessor(3);
			imp2.setProcessor(ip);
			
			if(imp2.getProcessor().getStatistics().max < 25.0){
				imp.changes = false;
				imp2.changes = false;
				imp.close();
				continue; 
			}
			else{imagecount++;}
			AutoThresholder adjust = new AutoThresholder();
			int globalThreshold = adjust.getThreshold("Minimum", imp2.getProcessor().getHistogram());
			imp2.getProcessor().threshold(globalThreshold);
			imp2.getProcessor().invert();
			
			IJ.run(imp2, "Analyze Particles...", "size=10-Infinity circularity=0.00-1.00 exclude add");
			rm.runCommand("Show All");
			IJ.wait(1000);
			int count = rm.getCount();
			total = total +count;
			
			imp.changes = false;
			imp2.changes = false;
			imp.close();
			
			
		}
		WindowManager.getFrame("ROI Manager").dispose();
		WindowManager.getFrame("Results").dispose();
		IJ.log("Total amount: "+total);
	}
	
	
	public ImagePlus makeHSBStack(ImagePlus img) {
		/*returns an HSBStack Image for further analysis
		 *input: RGB image 
		 */
		ColorProcessor cp = (ColorProcessor) img.getProcessor();

		int width  = cp.getWidth();
	    int height = cp.getHeight();
	    
		ImagePlus hsb = NewImage.createByteImage("HSB Stack", width, height, 3, 1);
	    
		hsb.setStack(cp.getHSBStack());
	    
		return hsb;
		
	}
	

}
