package com.personalityextractor.entity.extractor;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class SennaNounPhraseExtractor implements IEntityExtractor{
	private static File sennaInstallDir;
	
	static {
		sennaInstallDir = new File("/Users/tejaswi/Documents/StanfordCourses/SRL/senna-v2.0");	
	}
	
	public static String getSennaOutput(String line) {
		try {
			String cmd = "echo " + line + " | " + sennaInstallDir + "/senna ";
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(sennaInstallDir);
			Process shell = pb.start();
			InputStream shellIn = shell.getInputStream();
			shell.waitFor();
			int c;
			StringBuffer s = new StringBuffer();
			while ((c = shellIn.read()) != -1) {
				s.append((char) c);
			}

			return s.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public ArrayList<String> getNounPhrases(String sennaOutput) {
		ArrayList<String> nounPhrases = new ArrayList<String>();
		String[] lineArr = sennaOutput.split("\n");

		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> posTags = new ArrayList<String>();
		ArrayList<String> chunkerTokens = new ArrayList<String>();
		try {
			for (String line : lineArr) {
				String[] tokens = line.trim().split("[ \t]+");
				if (tokens.length < 3)
					continue;
				words.add(tokens[0].trim());
				posTags.add(tokens[1].trim());
				chunkerTokens.add(tokens[2].trim());
			}
			
			boolean flag = false;
			StringBuffer npBuf= new StringBuffer();			
			for(int i=0; i < posTags.size(); i++){
				if(posTags.get(i).startsWith("NN") || posTags.get(i).startsWith("JJ")){
					flag = true;
					npBuf.append(words.get(i)+" ");
				} else if(flag == true){
					flag = false;
					nounPhrases.add(npBuf.toString().trim().toLowerCase());
					npBuf = new StringBuffer();
				}
			}
			if(flag==true)
				nounPhrases.add(npBuf.toString().trim().toLowerCase());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nounPhrases;
	}

	@Override
	public List<String> extract(String line) {
		String sennaOutput = getSennaOutput(line);
		System.out.println(sennaOutput);
		return getNounPhrases(sennaOutput);
	}
	
	public static void main(String[] args){
		SennaNounPhraseExtractor sn = new SennaNounPhraseExtractor();
		String line = "Tejaswi went to see London";
		System.out.println(sn.extract(line));
	}

}
