package senna;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;

public class RunSenna {

	/**
	 * @param args
	 */

	File sennaInstallationDir = new File(
			"/Users/tejaswi/Documents/StanfordCourses/SRL/senna-v2.0");

	public String getSennaOutput(String line) {
		try {
			String cmd = "echo " + line + " | " + sennaInstallationDir
					+ "/senna ";
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(sennaInstallationDir);
			Process shell = pb.start();
			InputStream shellIn = shell.getInputStream(); 
			int shellExitStatus = shell.waitFor(); 
			int c;
			StringBuffer s = new StringBuffer();
			while ((c = shellIn.read()) != -1) {
				// System.out.write(c);
				s.append((char) c);
			}
			return s.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			String inputFile = args[0];

			File sennaInstallationDir = new File(
					"/Users/tejaswi/Documents/StanfordCourses/SRL/senna-v2.0");
			String outputFile = inputFile + ".senna.txt";
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader br = new BufferedReader(new FileReader(inputFile));

			String line = "";
			while ((line = br.readLine()) != null) {

				System.out.println(line);
				bw.write(line + "\n");
				// if(line.startsWith("<Heading>"))
				// continue;
				if (!line.startsWith("Event"))
					continue;
				line = "tejaswi went to london .";
				String cmd = "echo " + line + " | " + sennaInstallationDir
						+ "/senna -chk -srl -posvbs -usrtokens";
				ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
				pb.directory(sennaInstallationDir);
				Process shell = pb.start();
				InputStream shellIn = shell.getInputStream(); // this captures
				// the output
				// from the
				// command
				int shellExitStatus = shell.waitFor(); // wait for the shell to
				// finish and get the
				// return code
				int c;
				StringBuffer s = new StringBuffer();
				bw.write("<senna>\n");
				while ((c = shellIn.read()) != -1) {
					// System.out.write(c);
					bw.write(c);
					s.append((char) c);
				}
				for (String ss : s.toString().split("\n"))
					System.out.println("Line: " + ss);

				bw.write("</senna>");

			}
			bw.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
