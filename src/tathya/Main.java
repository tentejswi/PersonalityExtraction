package tathya;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import tathya.semantics.Triple;
import tathya.semantics.TripletExtractor;
import tathya.semantics.Word;
import tathya.text.tokenizer.TwitterTokenizer;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LexicalizedParser lp = new LexicalizedParser(
				"/Users/akishore/Desktop/tathya/englishPCFG.ser.gz");
		lp.setOptionFlags(new String[] { "-maxLength", "80",
				"-retainTmpSubcategories" });

		TwitterTokenizer tok = new TwitterTokenizer();

		try {
			BufferedReader fr = new BufferedReader(new FileReader(
					"/Users/akishore/Desktop/event.data"));
			BufferedWriter wr = new BufferedWriter(new FileWriter(
					"/Users/akishore/Desktop/tuples"));
			String sentence;
			while ((sentence = fr.readLine()) != null) {
				String[] tokens = sentence.split("\005");
				// String sentence =
				// "Today Apple announced the Apple iPad and like many of you, we at Adobe are looking forward to getting our hands on� http://goo.gl/fb/qUnD";
				// String sentence =
				// "Apple Unveils the \"iPad\": Apple showed off it's latest invention, the iPad, in San Francisco� http://goo.gl/fb/GcsF";
				List<String> sentences = tok.tokenize(tokens[1]);
				wr.write("\n" + tokens[0] + "\t" + tokens[1] + "\n");

				for (String s : sentences) {
					s = s.trim();
					// System.out.println(s.trim());
					String[] sent = s.split(" ");
					Tree parse = (Tree) lp.apply(Arrays.asList(sent));

					TreebankLanguagePack tlp = new PennTreebankLanguagePack();
					GrammaticalStructureFactory gsf = tlp
							.grammaticalStructureFactory();
					GrammaticalStructure gs = gsf
							.newGrammaticalStructure(parse);
					Collection tdl = gs.typedDependenciesCollapsed();
					// System.out.println(tdl);
					// System.out.println();

					TreePrint tp = new TreePrint(
							"penn,typedDependenciesCollapsed");
					// tp.printTree(parse);

					Tree[] children = parse.children();
					// System.out.println();

					TripletExtractor t = new TripletExtractor();
					ArrayList<Triple> triples = t.extract(parse);
					for (Triple triple : triples) {
						// System.out
						// .println("--------------------------------------------------");
						// if (triple.hasSubject()) {
						// System.out.println("subject:\t"
						// + triple.getSubject().getText());
						// if (triple.getSubject().hasAttributes()) {
						// System.out.println("attributes-->");
						// for (Word w : triple.getSubject()
						// .getAttributes()) {
						// System.out.println("\t\t" + w.getText());
						// }
						// }
						// }
						// if (triple.hasPredicate()) {
						// System.out.println("predicate:\t"
						// + triple.getPredicate().getText());
						// if (triple.getPredicate().hasAttributes()) {
						// System.out.println("attributes-->");
						// for (Word w : triple.getPredicate()
						// .getAttributes()) {
						// System.out.println("\t\t" + w.getText());
						// }
						// }
						// }
						// if (triple.hasObject()) {
						// System.out.println("object:\t"
						// + triple.getObject().getText());
						// if (triple.getObject().hasAttributes()) {
						// System.out.println("attributes-->");
						// for (Word w : triple.getObject()
						// .getAttributes()) {
						// System.out.println("\t\t" + w.getText());
						// }
						// }
						// }
						// System.out
						// .println("--------------------------------------------------");
						String subject = "";
						StringBuffer attr1 = new StringBuffer();
						String object = "";
						StringBuffer attr2 = new StringBuffer();
						String predicate = "";
						StringBuffer attr3 = new StringBuffer();

						if (triple.hasSubject()) {
							subject = triple.getSubject().getText();
							for (Word w : triple.getSubject().getAttributes()) {
								attr1.append(w.getText() + " ");
							}
						}

						if (triple.hasObject()) {
							object = triple.getObject().getText();
							for (Word w : triple.getObject().getAttributes()) {
								attr2.append(w.getText() + " ");
							}
						}

						if (triple.hasPredicate()) {
							predicate = triple.getPredicate().getText();
							for (Word w : triple.getPredicate().getAttributes()) {
								attr3.append(w.getText() + " ");
							}
						}

						wr.write(subject + "\t(" + attr1.toString().trim()
								+ ")\t" + predicate + "\t("
								+ attr2.toString().trim() + ")\t" + object
								+ "\t(" + attr3.toString().trim() + ")\tTweet: " + s + "\n");
						wr.flush();
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

}
