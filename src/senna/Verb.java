package senna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Verb {
	
	public String text;
	public HashMap<String, String> argumentToText = new HashMap<String, String>();
	public HashMap<String, List<String>> argumentToNPs = new HashMap<String, List<String>>();
	
}
