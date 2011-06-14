package com.personalityextractor.store;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class LuceneStore {

	private static MysqlStore db = null;
	static {
		try {
			db = new MysqlStore("localhost", "root", "", "wikiminer");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static IndexSearcher searcher = null; // the searcher used to open/search the index
	
	static {
		try {
			searcher = new IndexSearcher(new RAMDirectory(FSDirectory.open(new File("/tmp/lucene_index"))));
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	Query query = null; // the Query created by the QueryParser
	TopDocs hits = null; // the search results

	public void search(String searchString) {
		System.out.println("Searching.... '" + searchString + "'");

		try {
//			IndexReader reader = IndexReader.open(
//					FSDirectory.open(new File("/tmp/lucene_index")), true);
			

			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);// construct
																		// our
																		// usual
																		// analyzer

			QueryParser qp = new QueryParser(Version.LUCENE_30, "text",
					analyzer);
			query = qp.parse(searchString); // parse the query and construct the
											// Query object

			hits = searcher.search(query, 50); // run the query

			if (hits.totalHits == 0) {
				System.out.println("No data found.");
			} else {
//				for (int i = 0; i < hits.totalHits; i++) {
//					Document doc = searcher.doc(hits.scoreDocs[i].doc); // get
//																		// the
//																		// next
//					String text = doc.get("text"); // get its path field
////					System.out.println("Found in :: " + text);
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void index(int index) {
		ResultSet rs = null;
		int refresh = 0;
		try {
			Directory directory = new SimpleFSDirectory(new File(
					"/tmp/lucene_index"));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			IndexWriter iwriter = new IndexWriter(directory, analyzer, true,
					MaxFieldLength.UNLIMITED);
			do {
				String query = "SELECT * from page_indexed LIMIT " + index
						+ ", 1000";

				try {
					rs = db.execute(query);
					while (rs.next()) {
						String id = rs.getString("page_id");
						String data = rs.getString("page_title");
						try {
							Document doc = new Document();
							doc.add(new Field("text", data, Field.Store.YES,
									Field.Index.ANALYZED));
							doc.add(new Field("id", id, Field.Store.YES,
									Field.Index.ANALYZED));
							iwriter.addDocument(doc);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (rs != null) {
						try {
							rs.close();
							db.closeStmt();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				System.out.println(index);
				// if(index%1000000 == 0 && refresh != 0) {
				// break;
				// }
				index += 1000;
				refresh++;
				if (refresh % 500000 == 0) {
					iwriter.optimize();
				}
			} while (index < 6700000);
			iwriter.optimize();
			iwriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		LuceneStore s = new LuceneStore();
		// s.index(0);
		Date d1 = new Date();
		s.search("yahoo");
		Date d2 = new Date();
		System.out.println((d2.getTime()-d1.getTime()));
		
		d1 = new Date();
		s.search("apple");
		d2 = new Date();
		System.out.println((d2.getTime()-d1.getTime()));
		
		d1 = new Date();
		s.search("france");
		d2 = new Date();
		System.out.println((d2.getTime()-d1.getTime()));
		
		d1 = new Date();
		s.search("pakistan");
		d2 = new Date();
		System.out.println((d2.getTime()-d1.getTime()));
	}

}
