package cn.itcast.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * @program: lucene
 * @description: ${description}
 * @author: cd
 * @create: 2018-11-15 20:29
 */
public class LuceneCreat {
    @Test
    public void indexCreate() throws Exception {
        //创建文档对象
        Document document = new Document();
        //StringField 创建索引不分词
        document.add(new StringField("id", "1", Field.Store.YES));
        //TextField  创建索引并分词
        document.add(new TextField("title", "谷歌地图之父跳槽FaceBook", Field.Store.YES));
        //创建目录对象，保存索引库的存放位置
        Directory directory = FSDirectory.open(new File("d:\\indexDir"));
        //创建分词器对象
        Analyzer analyzer = new StandardAnalyzer();
        //创建索引写入器配置对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        //创建索引写入器
        IndexWriter indexWriter = new IndexWriter(directory, config);
        //向索引库写入文档对象
        indexWriter.addDocument(document);
        //提交  关流
        indexWriter.commit();
        indexWriter.close();

    }

    @Test
    public void testCreater2() throws IOException {
        //创建文档对象集合
        ArrayList<Document> documents = new ArrayList<Document>();
        //创建文档对象
        Document document1 = new Document();
        document1.add(new StringField("id", "1", Field.Store.YES));
        document1.add(new TextField("title", "谷歌地图之父跳槽FaceBook", Field.Store.YES));
        documents.add(document1);

        // 创建文档对象
        Document document2 = new Document();
        document2.add(new StringField("id", "2", Field.Store.YES));
        document2.add(new TextField("title", "谷歌地图之父加盟FaceBook", Field.Store.YES));
        documents.add(document2);
        // 创建文档对象
        Document document3 = new Document();
        document3.add(new StringField("id", "3", Field.Store.YES));
        document3.add(new TextField("title", "谷歌地图创始人拉斯离开谷歌加盟Facebook", Field.Store.YES));
        documents.add(document3);
        // 创建文档对象
        Document document4 = new Document();
        document4.add(new StringField("id", "4", Field.Store.YES));
        document4.add(new TextField("title", "谷歌地图之父跳槽Facebook与Wave项目取消有关", Field.Store.YES));
        documents.add(document4);
        // 创建文档对象
        Document document5 = new Document();
        document5.add(new StringField("id", "5", Field.Store.YES));
        document5.add(new TextField("title", "谷歌地图之父拉斯加盟社交网站Facebook", Field.Store.YES));
        documents.add(document5);

        //索引库对象
        Directory directory = FSDirectory.open(new File("d:\\indexDir"));
        //创建索引写入器配置对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new IKAnalyzer());
        //  打开模式    CREATEf覆盖    APPEND追加
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        //创建索引写入器对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        indexWriter.addDocuments(documents);
        indexWriter.commit();
        indexWriter.close();
    }

    @Test
    public void testS() throws IOException, ParseException {
        FSDirectory directory = FSDirectory.open(new File("d:\\indexDir"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new QueryParser("title", new IKAnalyzer());
        Query query = parser.parse("谷歌地图之父拉斯");
        TopDocs topDocs = searcher.search(query, 10);
        System.out.println("本次搜索共找到" + topDocs.totalHits + "条数据");
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docID = scoreDoc.doc;
            Document doc = reader.document(docID);
            System.out.println("id: " + doc.get("id"));
            System.out.println("title: " + doc.get("title"));
            // 取出文档得分
            System.out.println("得分： " + scoreDoc.score);
        }
    }

    @Test
    public void testHighLighter() throws IOException, ParseException, InvalidTokenOffsetsException {
        FSDirectory directory = FSDirectory.open(new File("d:\\indexDir"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new QueryParser("title", new IKAnalyzer());
        Query query = parser.parse("谷歌地图");
        Formatter formatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
        Scorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        // 搜索
        TopDocs topDocs = searcher.search(query, 10);
        System.out.println("本次搜索共" + topDocs.totalHits + "条数据");

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            // 获取文档编号
            int docID = scoreDoc.doc;
            Document doc = reader.document(docID);
            System.out.println("id: " + doc.get("id"));

            String title = doc.get("title");
            // 用高亮工具处理普通的查询结果,参数：分词器，要高亮的字段的名称，高亮字段的原始值
            String hTitle = highlighter.getBestFragment(new IKAnalyzer(), "title", title);

            System.out.println("title: " + hTitle);
            // 获取文档的得分
            System.out.println("得分：" + scoreDoc.score);
        }


    }

    @Test
    public void testSortQuery() throws IOException, ParseException {
        Directory directory = FSDirectory.open(new File("d:\\indexDir"));
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new QueryParser("title", new IKAnalyzer());
        Query query = parser.parse("谷歌地图");
        Sort sort = new Sort(new SortField("id", SortField.Type.LONG, true));
        // 搜索
        TopDocs topDocs = searcher.search(query, 10, sort);
        System.out.println("本次搜索共" + topDocs.totalHits + "条数据");

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            // 获取文档编号
            int docID = scoreDoc.doc;
            Document doc = reader.document(docID);
            System.out.println("id: " + doc.get("id"));
            System.out.println("title: " + doc.get("title"));


        }
    }

    @Test
    public void testPageQuery() throws IOException, ParseException {
        // 实际上Lucene本身不支持分页。因此我们需要自己进行逻辑分页。我们要准备分页参数：
        int pageSize = 2;// 每页条数
        int pageNum = 3;// 当前页码
        int start = (pageNum - 1) * pageSize;// 当前页的起始条数
        int end = start + pageSize;// 当前页的结束条数（不能包含）

        // 目录对象
        Directory directory = FSDirectory.open(new File("d:\\indexDir"));
        // 创建读取工具
        IndexReader reader = DirectoryReader.open(directory);
        // 创建搜索工具
        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser parser = new QueryParser("title", new IKAnalyzer());
        Query query = parser.parse("谷歌地图");

        // 创建排序对象,需要排序字段SortField，参数：字段的名称、字段的类型、是否反转如果是false，升序。true降序
        Sort sort = new Sort(new SortField("id", SortField.Type.LONG, false));
        // 搜索数据，查询0~end条
        TopDocs topDocs = searcher.search(query, end,sort);
        System.out.println("本次搜索共" + topDocs.totalHits + "条数据");

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (int i = start; i < end; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            // 获取文档编号
            int docID = scoreDoc.doc;
            Document doc = reader.document(docID);
            System.out.println("id: " + doc.get("id"));
            System.out.println("title: " + doc.get("title"));
        }


    }
}