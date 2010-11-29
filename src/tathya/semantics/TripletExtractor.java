package tathya.semantics;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import tathya.semantics.Word.Type;
import edu.stanford.nlp.trees.Tree;

public class TripletExtractor {

	public TripletExtractor() {
		
	}
	
	public ArrayList<Triple> extract(Tree tree) {
		if(tree == null) {
			return null;
		}
		
		ArrayList<Triple> triples = new ArrayList<Triple>();
		
		ArrayList<Tree> queue = new ArrayList<Tree>();
		Tree root = tree;
		queue.add(root);
		
		while(!queue.isEmpty()) {
			Tree topNode = queue.remove(0);
			
			// extract triple if S -> NP VP detected
			if(topNode.value().startsWith("S")) {
				Tree NP = null;
				Tree VP = null;
				Tree[] children = topNode.children();
				for(Tree c : children) {
					if(c.nodeString().startsWith("NP")) {
						NP = c;
					} else if(c.nodeString().startsWith("VP")) {
						VP = c;
					}
				}
				
				if(NP == null || VP == null) {
					//System.out.println("invalid parse tree");
				} else {
					Triple triple = new Triple();
					triple.setSubject(extractSubject(NP, tree));
					List<Word> predicateAndObj = extractPredicateAndObject(VP, tree);
					if(predicateAndObj.size() > 0) {
						triple.setPredicate(predicateAndObj.get(0));
						if(predicateAndObj.size() > 1) {
							triple.setObject(predicateAndObj.get(1));
						}
					}
					triples.add(triple);
				}
			}
			
			// add all children to queue regardless
			for(Tree c : topNode.children()) {
				queue.add(c);
			}
		}
		
		return triples;
	}
	
	private Word extractSubject(Tree NP, Tree root) {
		Word subject = null;
		Tree subjectNode = null;
		ArrayList<Tree> queue = new ArrayList<Tree>();
		StringBuffer subjBuff = new StringBuffer();
		boolean hasSubject = false;
		
		Tree[] children = NP.children();
		if(children != null) {
			for(Tree c : children) {
				queue.add(c);
			}
			
			while(!queue.isEmpty()) {
				Tree top = queue.remove(0);
				
				if(top.nodeString().startsWith("NP")) {
					hasSubject = true;
					return extractSubject(top, root);
				} else if(top.nodeString().startsWith("NN")) {
					subjectNode = top;
					subjBuff.append(top.children()[0].value() + " ");
					hasSubject = true;
				} else {
					if(hasSubject) {
						break;
					}
					
					Tree[] t_children = top.children();
					for(Tree c : t_children) {
						queue.add(c);
					}
				}
			}
		}
		
		subject = new Word(subjBuff.toString().trim(), Word.Type.SUBJECT);
		subject = extractAttributes(subjectNode, root, subject);
		return subject;
	}
	
	private List<Word> extractPredicateAndObject(Tree VP, Tree root) {
		List<Word> result = new ArrayList<Word>();
		String predicateStr = null;
		Tree predicateNode = null;
		Tree objectNode = null;
		ArrayList<Tree> siblings = new ArrayList<Tree>();
		ArrayList<Tree> queue = new ArrayList<Tree>();
		Word predicate = null;
		Word object = null;
		
		Tree[] children = VP.children();
		if(children != null) {
			for(Tree c : children) {
				queue.add(c);
			}
			
			while(!queue.isEmpty()) {
				Tree top = queue.remove(0);
				if(top.nodeString().startsWith("VB")) {
					predicateNode = top; 
					predicateStr = top.children()[0].value();
					siblings.clear();
				} else if(top.nodeString().startsWith("VP")) {
					Tree[] t_children = top.children();
					for(Tree c : t_children) {
						queue.add(c);
					}
				} else if(top.nodeString().startsWith("PP") || top.nodeString().startsWith("NP") || top.nodeString().startsWith("ADJ")) {
					siblings.add(top);
				}
			}
		}
		
		predicate = new Word(predicateStr, Word.Type.PREDICATE);
		predicate = extractAttributes(predicateNode, root, predicate);
		
		if(!siblings.isEmpty()) {
			for(Tree sib : siblings) {
				if(sib.nodeString().startsWith("NP") || sib.nodeString().startsWith("PP")) {
					object = extractSubject(sib, root);
					break;
				} else {
					object = extractAdj(sib);
					break;
				}
			}
		}
	
		if(object != null) {
			object.setType(Word.Type.OBJECT);
		}
		result.add(predicate);
		result.add(object);
		return result;
	}
	
	private Word extractAdj(Tree subtree) {
		StringBuffer adj = new StringBuffer();
		ArrayList<Tree> queue = new ArrayList<Tree>();
		
		Tree[] children = subtree.children();
		if(children != null) {
			for(Tree c : children) {
				queue.add(c);
			}
			
			while(!queue.isEmpty()) {
				Tree top = queue.remove(0);
				if(top.nodeString().startsWith("JJ")) {
					adj.append(top.children()[0].value() + " ");
				} else {
					Tree[] t_children = top.children();
					for(Tree c : t_children) {
						queue.add(c);
					}
				}
			}
		}
		
		return new Word(adj.toString().trim(), Word.Type.OBJECT);
	}
	
	private Word extractAttributes(Tree node, Tree root, Word wrd) {
		if(node == null) {
			return wrd;
		}
		
		if(node.nodeString().startsWith("JJ")) {
			if(node == null || node.parent(root) == null) {
				return wrd;
			}
			
			Tree[] siblings = node.parent(root).children();
			for(Tree sibling : siblings) {
				if(sibling.nodeString().startsWith("RB")) {
					wrd.addAttribute(new Word(sibling.children()[0].value(), Word.Type.ATTRIBUTE));
				}
			}
		} else if(node.nodeString().startsWith("VB")) {
			if(node == null || node.parent(root) == null) {
				return wrd;
			}
			
			Tree[] siblings = node.parent(root).children();
			for(Tree sibling : siblings) {
				if(sibling.nodeString().startsWith("ADV")) {
					wrd.addAttribute(new Word(sibling.children()[0].value(), Word.Type.ATTRIBUTE));
				}
			}
		} else if(node.nodeString().startsWith("NN")) {
			if(node == null || node.parent(root) == null) {
				return wrd;
			}
			
			Tree[] siblings = node.parent(root).children();
			for(Tree sibling : siblings) {
				if(sibling.nodeString().startsWith("DT") || sibling.nodeString().startsWith("POS") 
						|| sibling.nodeString().startsWith("JJ") || sibling.nodeString().startsWith("PRP")
						|| sibling.nodeString().startsWith("CD") || sibling.nodeString().startsWith("ADJ")
						|| sibling.nodeString().startsWith("QP")) {
					wrd.addAttributeAll(collectAllBFS(sibling, Word.Type.ATTRIBUTE));
				} else if(sibling.nodeString().startsWith("NP")) {
					wrd.addAttributeAll(collectAllBFS(sibling, Word.Type.ATTRIBUTE));
				}
			}
		}
		
		
		// search the uncles
		if(node.parent(root).parent(root) != null) {
			Tree[] uncles = node.parent(root).parent(root).children();
			for(Tree uncle : uncles) {
				if(uncle.nodeString().startsWith("PP") || (node.nodeString().startsWith("VB") && uncle.nodeString().startsWith("PP"))) {
					wrd.addAttributeAll(collectAllBFS(uncle, Word.Type.ATTRIBUTE));
					break;
				}
			}
		}
		
		return wrd;
	}
	
	private List<Word> collectAllBFS(Tree subtree, Type t) {
		ArrayList<Tree> queue = new ArrayList<Tree>();
		ArrayList<Word> words = new ArrayList<Word>();
				
		Tree[] children = subtree.children();
		if(children != null) {
			for(Tree c : children) {
				queue.add(c);
			}
			
			while(!queue.isEmpty()) {
				Tree top = queue.remove(0);
				if(top.isLeaf()) {
					words.add(new Word(top.value(), t));
				}
				Tree[] t_children = top.children();
				for(Tree c : t_children) {
					queue.add(c);
				}
			}
		}
		
		return words;
	}
}
