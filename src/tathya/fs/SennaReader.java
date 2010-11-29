package tathya.fs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import tathya.semantics.Event;

public class SennaReader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(args[0]));
			String line;
			ArrayList<String> lines = new ArrayList<String>();
			while((line = fr.readLine()) != null) {
				line = line.trim();
				if(!line.equals("")) {
					lines.add(line);
				} else {
					// @TODO create Event obj
					Event e = new Event(lines);
					List<String> entities = e.getEntities();
					for(String entity : entities) {
						System.out.println(entity);
					}
					lines = new ArrayList<String>();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
