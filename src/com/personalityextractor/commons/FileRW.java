package com.personalityextractor.commons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class FileRW {
	public static List<String> getLinesinFile(String file){
		List<String> lines = new ArrayList<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			while((line=br.readLine())!=null){
				lines.add(line.trim());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return lines;
		
	}
	
	public static void writeLinesToFile(String file, List<String> lines){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for(String line : lines){
				bw.write(line+"\n");
			}
			bw.flush();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void writeLineToFile(String file, String line){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(line);
			bw.flush();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}
