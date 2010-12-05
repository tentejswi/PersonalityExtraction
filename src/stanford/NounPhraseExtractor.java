package stanford;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tathya.semantics.Triple;
import tathya.semantics.Word;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;

public class NounPhraseExtractor {

	private static LexicalizedParser lp;

	static {
		try {
			lp = new LexicalizedParser(
					"/Users/akishore/Desktop/tathya/englishPCFG.ser.gz");
			lp.setOptionFlags(new String[] { "-maxLength", "80",
					"-retainTmpSubcategories" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> extract(String text) {
		ArrayList<String> phrases = new ArrayList<String>();
		String[] sent = text.split(" ");
		Tree parse = (Tree) lp.apply(Arrays.asList(sent));
//		TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
//		tp.printTree(parse);
		ArrayList<Tree> queue = new ArrayList<Tree>();
		queue.add(parse);

		StringBuffer str = new StringBuffer();
		boolean flag = false;
		while (!queue.isEmpty()) {
			Tree topNode = queue.remove(0);

			if (topNode.isPreTerminal()) {
				if (topNode.value().startsWith("N")) {
					str.append(topNode.children()[0].value() + " ");
					flag = true;
				} else if (flag == true) {
					flag = false;
					phrases.add(str.toString().trim());
					str = new StringBuffer();
				}
			}

			// add all children to queue regardless
			for (Tree c : topNode.children()) {
				queue.add(c);
			}
		}

		if (flag == true) {
			phrases.add(str.toString().trim());
		}

		return phrases;
	}

	public static void main(String[] args) {
		NounPhraseExtractor.extract("Tata bring Jaguar home.");
	}
}
