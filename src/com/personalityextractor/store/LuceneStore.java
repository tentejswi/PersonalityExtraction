package com.personalityextractor.store;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.personalityextractor.evaluation.PerfMetrics;
import com.personalityextractor.evaluation.PerfMetrics.Metric;

public class LuceneStore {
	
	private static LuceneStore instance = null;

	private static final String PAGE_INDEX_PATH = "/Users/semanticvoid/projects/PE/indicies/pages";
	private static final String CATEGORY_INDEX_PATH = "/Users/semanticvoid/projects/PE/indicies/categories";
	private static MysqlStore db = null;
	static {
		try {
			db = new MysqlStore("localhost", "root", "", "wikiminer");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static IndexSearcher pgSearcher = null;
	static IndexSearcher catSearcher = null;
	
	private LuceneStore() {
		
	}
	
	public static LuceneStore getInstance() {
		if (instance == null) {
			instance = new LuceneStore();
		}

		return instance;
	}

	public void loadIndices() {
		try {
			if(pgSearcher == null) {
				System.err.print("Loading page index...\t");
				if (new File(PAGE_INDEX_PATH).exists()) {
					pgSearcher = new IndexSearcher(new RAMDirectory(
							FSDirectory.open(new File(PAGE_INDEX_PATH))));
				}
				System.err.print("[ DONE ]\n");
			}
			if(catSearcher == null) {
				System.err.print("Loading category index...\t");
				if (new File(CATEGORY_INDEX_PATH).exists()) {
					catSearcher = new IndexSearcher(new RAMDirectory(
							FSDirectory.open(new File(CATEGORY_INDEX_PATH))));
				}
				System.err.print("[ DONE ]\n");
			}
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Query query = null; // the Query created by the QueryParser
	TopDocs hits = null; // the search results

	public List<WikipediaEntity> search(String terms) {
		Date d1 = new Date();
		List<WikipediaEntity> entities = new ArrayList<WikipediaEntity>();
		try {
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			QueryParser qp = new QueryParser(Version.LUCENE_30, "text",
					analyzer);
			query = qp.parse(terms);
			hits = pgSearcher.search(query, 20);
			int numResults = 20;
			if(hits.totalHits < 20) {
				numResults = hits.totalHits;
			}

			if (hits.totalHits != 0) {
				for (int i = 0; i < numResults; i++) {
					Document doc = pgSearcher.doc(hits.scoreDocs[i].doc);
					entities.add(new WikipediaEntity(doc.get("text"), doc
							.get("id"), Integer.valueOf(doc.get("type")), doc
							.get("inlinks")));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Date d2 = new Date();
		PerfMetrics.getInstance().addToMetrics(Metric.SEARCHPAGE, (d2.getTime()-d1.getTime()));
		return entities;
	}

	public WikipediaEntity searchPageId(String id) {
		try {
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			QueryParser qp = new QueryParser(Version.LUCENE_30, "id", analyzer);
			query = qp.parse(id);
			hits = pgSearcher.search(query, 1);

			if (hits.totalHits != 0) {
				Document doc = pgSearcher.doc(hits.scoreDocs[0].doc);
				return new WikipediaEntity(doc.get("text"), doc.get("id"),
						Integer.valueOf(doc.get("type")), doc.get("inlinks"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<WikipediaEntity> getCategories(String id) {
		Date d1 = new Date();
		List<WikipediaEntity> categories = new ArrayList<WikipediaEntity>();
		
		if(id != null) {
			List<String> categoryIds = getCategoryIds(id);
			for(String cid : categoryIds) {
				WikipediaEntity we = searchPageId(cid);
				if(we != null) {
					categories.add(we);
				}
			}
		}
		
		Date d2 = new Date();
		PerfMetrics.getInstance().addToMetrics(Metric.GETCATEGORIES, (d2.getTime()-d1.getTime()));
		return categories;
	}

	private List<String> getCategoryIds(String id) {
		List<String> categoryIds = new ArrayList<String>();
		try {
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			QueryParser qp = new QueryParser(Version.LUCENE_30, "child",
					analyzer);
			query = qp.parse(id);
			hits = catSearcher.search(query, 50);
			int numResults = 50;
			if(hits.totalHits < 50) {
				numResults = hits.totalHits;
			}

			if (hits.totalHits != 0) {
				for (int i = 0; i < numResults; i++) {
					Document doc = catSearcher.doc(hits.scoreDocs[i].doc);
					categoryIds.add(doc.get("parent"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return categoryIds;
	}
	
	public double compare(String id1, String id2) {
		double sim = 0;
		if (id1 == null || id2 == null || id1.equals("") || id2.equals("")) {
			return sim;
		}
		
		List<String> categories1 = getCategoryIds(id1);
		List<String> categories2 = getCategoryIds(id2);

		double intersection = 0;
		for (String id11 : categories1) {
			if (categories2.contains(id11)) {
				intersection++;
			}
		}

		if (intersection > 0 && (categories1.size() + categories2.size()) > 0) {
			sim = intersection * 2 / (categories1.size() + categories2.size());
		}
		
		return sim;
	}

	public void indexPages(int index) {
		ResultSet rs = null;
		int refresh = 0;
		try {
			new File(PAGE_INDEX_PATH).mkdir();
			Directory directory = new SimpleFSDirectory(new File(
					PAGE_INDEX_PATH));
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

	public void indexCategories(int index) {
		ResultSet rs = null;
		int refresh = 0;
		try {
			new File(CATEGORY_INDEX_PATH).mkdir();
			Directory directory = new SimpleFSDirectory(new File(
					CATEGORY_INDEX_PATH));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			IndexWriter iwriter = new IndexWriter(directory, analyzer, true,
					MaxFieldLength.UNLIMITED);
			do {
				String query = "SELECT * from categorylink LIMIT " + index
						+ ", 5000";

				try {
					rs = db.execute(query);
					while (rs.next()) {
						String parent = rs.getString("cl_parent");
						String child = rs.getString("cl_child");
						try {
							Document doc = new Document();
							doc.add(new Field("parent", parent,
									Field.Store.YES, Field.Index.ANALYZED));
							doc.add(new Field("child", child, Field.Store.YES,
									Field.Index.ANALYZED));
							iwriter.addDocument(doc);
						} catch (IOException e) {
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
				index += 5000;
				refresh++;
				// if (refresh % 500000 == 0) {
				// iwriter.optimize();
				// }
			} while (index < 9526832);
			iwriter.optimize();
			iwriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		LuceneStore s = new LuceneStore();

		// s.indexPages(0);
		// s.indexCategories(0);

		s.loadIndices();
		Date d1 = new Date();
		s.search("yahoo");
		Date d2 = new Date();
		System.out.println((d2.getTime() - d1.getTime()));

		d1 = new Date();
		s.search("apple");
		d2 = new Date();
		System.out.println((d2.getTime() - d1.getTime()));

		d1 = new Date();
		s.search("france");
		d2 = new Date();
		System.out.println((d2.getTime() - d1.getTime()));

		d1 = new Date();
		s.search("pakistan");
		d2 = new Date();
		System.out.println((d2.getTime() - d1.getTime()));
	}

}
