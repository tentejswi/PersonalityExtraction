package com.personalityextractor.store;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.personalityextractor.entity.WikipediaEntity;

public class LuceneStore {

	private static final String INDEX_PATH = "/tmp/lucene";
	private static MysqlStore db = null;
	static {
		try {
			db = new MysqlStore("localhost", "root", "", "wikiminer");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static IndexSearcher searcher = null; // the searcher used to open/search
											// the index

	static {
		try {
			if (new File(INDEX_PATH).exists()) {
				searcher = new IndexSearcher(new RAMDirectory(
						FSDirectory.open(new File(INDEX_PATH))));
			}
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

	public List<WikipediaEntity> search(String terms) {
		List<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
		try {
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			QueryParser qp = new QueryParser(Version.LUCENE_30, "text",
					analyzer);
			query = qp.parse(terms);
			hits = searcher.search(query, 20);

			if (hits.totalHits != 0) {
				for (int i = 0; i < hits.totalHits; i++) {
					Document doc = searcher.doc(hits.scoreDocs[i].doc);
					entities.add(new WikipediaEntity(doc.get("text"), doc
							.get("id"), Integer.valueOf(doc.get("type")), doc
							.get("inlinks")));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return entities;
	}

	public void index(int index) {
		ResultSet rs = null;
		int refresh = 0;
		try {
			new File(INDEX_PATH).mkdir();
			Directory directory = new SimpleFSDirectory(new File(INDEX_PATH));
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
						String inlinks = rs.getString("inlinks");
						String type = rs.getString("page_type");
						try {
							Document doc = new Document();
							doc.add(new Field("text", data, Field.Store.YES,
									Field.Index.ANALYZED));
							doc.add(new Field("id", id, Field.Store.YES,
									Field.Index.ANALYZED));
							doc.add(new Field("inlinks", id, Field.Store.YES,
									Field.Index.NO));
							doc.add(new Field("type", type, Field.Store.YES,
									Field.Index.NO));
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
		s.index(0);
		// Date d1 = new Date();
		// s.search("yahoo");
		// Date d2 = new Date();
		// System.out.println((d2.getTime() - d1.getTime()));
		//
		// d1 = new Date();
		// s.search("apple");
		// d2 = new Date();
		// System.out.println((d2.getTime() - d1.getTime()));
		//
		// d1 = new Date();
		// s.search("france");
		// d2 = new Date();
		// System.out.println((d2.getTime() - d1.getTime()));
		//
		// d1 = new Date();
		// s.search("pakistan");
		// d2 = new Date();
		// System.out.println((d2.getTime() - d1.getTime()));
	}

}
