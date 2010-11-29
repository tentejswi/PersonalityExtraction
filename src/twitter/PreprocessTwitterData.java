//package dygest.text.preprocess;
package twitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PreprocessTwitterData {
    
    public String cleanText(String text) throws IOException {

    			
            			text = text.toLowerCase().replaceFirst("^rt [a-z0-9@: ]+:", "");
            			text = text.replaceAll("http://[a-z.0-9+/%&#~\\\\-_]+","");
		            	text = text.replaceAll("\\\\ \"", "");
		            	text = text.replaceAll("-[ ]+$", " .");
		            	text = text.replaceAll("[^a-zA-Z0-9\\s]", ".\n");
		            	if(!text.endsWith(".")){
		            		text+=".";
		            	}
		            	text+="\n";
              			return text;
    }
    
   
    public static void main(String[] args) throws IOException {
        PreprocessTwitterData x = new PreprocessTwitterData();
        String text = "hyd police commissioner says plane crashed in \"crowded, residential area\" #hyderabadaircrash http://www.hindu.com";
        x.cleanText(text);
         /*String t = "hyd police commissioner says plane crashed in \"crowded, residential area\" #hyderabadaircrash";
         t = t.toLowerCase().replaceFirst("^rt [a-z0-9@: ]+:", "");
         t = t.toLowerCase().replaceAll("http://[a-z.0-9+/%&#~\\\\-_]+",
         "");
         
         System.out.println(t);*/
    }
}

