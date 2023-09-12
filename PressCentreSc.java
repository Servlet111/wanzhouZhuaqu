package com.zhua.scraper;

import com.zhua.dao.PressCentreDao;
import com.zhua.service.PressCentreService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@Component
public class PressCentreSc {
    private int Cid = 0; // 将Cid声明为成员变量
    @Autowired
    PressCentreService pressCentreService;
    public void scrapePressCentre() {
        /* dataList 用于存储抓取到的首页信息，每个首页信息都表示为一个 ShouyeDao 对象，
         然后将这些对象添加到 dataList 中。最后，通过调用 shouyeService.saveBatch(dataList);
         方法，将 dataList 中的数据批量插入到数据库中，以完成数据持久化操作。*/

        String baseUrl = "http://www.wz.gov.cn/jkq/wzjkq/xwzx/zwyw_106886/";
        // 政府信息的基础 URL
        String govBaseUrl = "http://www.wz.gov.cn/jkq/wzjkq/xwzx/zwyw_106886/";

        // 企业信息的基础 URL
        String corpBaseUrl = "http://www.wz.gov.cn/jkq/wzjkq/xwzx/qydt/";
        List<PressCentreDao> dataList = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(new URL(baseUrl), 30000);
            // 选择指定<div>下的<a>标签
            // 选择指定<div>下的所有<a>标签
            Element divElement = doc.select("div.left").first();
            Elements aElements = divElement.select("a");
            // 遍历所有<a>标签，获取链接和文本内容
            int Bid = 1;
            String lastLink = baseUrl; // 用于保存上一个链接
            for (Element link : aElements) {
                String href = link.attr("href");
                String text = link.text();
                System.out.println("链接: " + href);
                System.out.println("文本内容: " + text);
                // 处理链接
                String newHref;
                // 处理第一个链接，将 "./" 替换为空字符串
                if (Bid == 1) {
                    newHref = lastLink + href.replace("./", "");
                } else {
                    // 找到 baseUrl 中倒数第二个斜杠的位置
                    int lastSlashIndex = baseUrl.lastIndexOf("/", baseUrl.length() - 2);
                    if (lastSlashIndex != -1) {
                        // 删除 baseUrl 中倒数第二个斜杠之后的内容
                        String baseUrlWithoutLastSegment = baseUrl.substring(0, lastSlashIndex + 1);
                        // 将 "../" 替换为 "/"
                        newHref = baseUrlWithoutLastSegment + href.replace("../", "");
                    } else {
                        // 如果没有倒数第二个斜杠，直接使用 href
                        newHref = href;
                    }
                }
                System.out.println(newHref);
                try {// 连接政府信息的 URL 并解析页面内容
                    Document govDoc = Jsoup.connect(newHref).timeout(30000).get(); // 增加连接超时时间为60秒
                    String govHtmlContent = govDoc.html();
                    // 使用正则表达式匹配createPage函数调用
                    Pattern pattern = Pattern.compile("createPage\\((\\d+)");
                    Matcher matcher = pattern.matcher(govHtmlContent);
                    while (matcher.find()) {
                        //是用来表示是企业还是政府govBid
                        String param1 = matcher.group(1);
                        // 将param1字符串转换为整数
                        int param1Int = (Integer.parseInt(param1))-1;
                        // 构建政府信息的新链接
                        for (int i = 0; i <=param1Int; i++) {
                            String govNewUrl;
                            if (i == 0) {
                                govNewUrl = newHref + "index.html"; // 当 i 为0时使用这个连接
                            } else {
                                govNewUrl = newHref + "index_" + i + ".html";
                            }
                            //curretbid用来记数
                            extractInformation(newHref,dataList,govNewUrl, text, Bid);
                        }
                    }
                    // 连接企业信息的 URL 并解析页面内容
                    /*Document corpDoc = Jsoup.parse(new URL(corpBaseUrl), 30000);
                    String corpHtmlContent = corpDoc.html();

            // 重新使用正则表达式匹配createPage函数调用

            Matcher matcher2 = pattern.matcher(corpHtmlContent);

            while (matcher2.find()) {
                int corpBid= 2;

                String param2 = matcher2.group(1);
                int param2Int = (Integer.parseInt(param2))-1;
                // 构建企业信息的新链接
                for (int i = 0; i <=param2Int; i++) {
                    //corpBid //表示是企业信息id
                    String corpNewUrl;
                    if (i == 0) {
                        corpNewUrl = corpBaseUrl + "index.html"; // 当 i 为0时使用这个连接
                    } else {
                        corpNewUrl = corpBaseUrl + "index_" + i + ".html";
                    }
                    //currentid//用来及数字
                    extractInformation(corpBaseUrl,dataList,corpNewUrl, "企业信息", corpBid);
                }

            }*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bid++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pressCentreService.saveBatch(dataList);
        System.out.println("数据插入成功！");
    }

    private int extractInformation(String baseUrl, List<PressCentreDao> dataList, String NewUrl, String category, int bid) throws IOException {
        Document doc = Jsoup.connect(NewUrl).get();
        //抓取新闻标题
        Elements newsItems = doc.select("ul.right-list li.clearfix");
        // 获取右侧标题
        String rightTitle = doc.select("div.right p.right-title").text();
        for (Element newsItem : newsItems) {
            /*for 循环中的 newsItem 变量是用来代表 newsItems 中的每个元素，
            使得可以在循环体内对每个新闻项进行操作，比如提取新闻内容或执行其他相关操作。
            这个循环的作用是对多个新闻项进行相同或类似的处理，以便提取和处理网页中的多个新闻条目*/
            Cid++; // 递增成员变量Cid的值
            String title = newsItem.select("a.lf").text();
            String date = newsItem.select("span.rt").text();
            String relativeLink = newsItem.select("a.lf").attr("href");
            String bname = newsItem.select("p.right-title").text();
            // 拼接完整连接
            String fullLink = baseUrl+relativeLink;
            String articleContent="";
            String ctitle="";
            String csource = "";
            if (relativeLink.startsWith("https://") || relativeLink.startsWith("http://")) {
                articleContent = "第三方连接404!!: " + relativeLink;
                // 这里可以将提示信息存起来备用，或者执行其他逻辑
                // 如果不需要下载，可以使用continue跳过当前循环
                // 设置其他参数为默认值或其他适当的值
                ctitle = "未知标题";
                csource = "未知来源";
                date = "未知来源！！";
                // 其他参数的初始化赋值，根据实际情况设置

                continue;


            } else {
                // 如果不以"https://"开头，进行拼接
                fullLink = baseUrl + relativeLink;
                fullLink = fullLink.replace("/./", "/");
                // 在这里可以执行其他需要的操作，如正常下载
            }

            // 在这里添加代码来检查连接是否存在
            int responseCode = Jsoup.connect(fullLink).execute().statusCode();
            if (responseCode == 404) {
                // 处理连接不存在的情况
                articleContent = "连接不存在 (HTTP 404): " + fullLink;
                ctitle = "未知标题";
                csource = "未知来源";
                date = "未知来源！！";
                // 其他参数的初始化赋值，根据实际情况设置

                // 记录错误信息
                System.err.println("HTTP错误：" + responseCode + "，连接：" + fullLink);

                // 继续处理下一条数据
                continue;
            } else if (responseCode != 200) {
                // 处理其他错误的情况
                // 可根据需要设置相应的逻辑
                System.out.println("HTTP错误：" + responseCode + "，连接：" + fullLink);
                continue;
            }
            // 具体文章的  来提取文章内容
            Document articleDoc = Jsoup.connect(fullLink).get();

            // 提取标题
            ctitle = articleDoc.select("div.zwxl-title p.tit").text();

            // 提取来源
            // 选择包含在<script>标签中的内容
            Element scriptElement = articleDoc.select("div.zwxl-title div.zwxl-bar script").first();

            if (scriptElement != null) {
                // 获取<script>标签中的 JavaScript 代码
                String scriptText = scriptElement.html();
                // 使用Jsoup提取<script>中的内容
                Document scriptDocument = Jsoup.parse(scriptText);
                // 选择<script>中的<span class="con">元素
                Elements conElements = scriptDocument.select("span.con");
                // 提取第一个<span class="con">元素的文本内容
                if (!conElements.isEmpty()) {
                    csource = conElements.first().text();
                }
            }


            //提取正文
            // 使用一个选择器来选择正文内容
            Elements ccontentElements = articleDoc.select("div.view.TRS_UEDITOR.trs_paper_default.trs_web p, div.view.TRS_UEDITOR.trs_paper_default.trs_word p, div.zwxl-article span, div.view TRS_UEDITOR.trs_paper_default.trs_word p,div.view.TRS_UEDITOR.trs_paper_default.trs_external p,div.view.TRS_UEDITOR.trs_paper_default.trs_word p span");
            // 使用 StringBuilder 构建文本
            StringBuilder ccontentBuilder = new StringBuilder();
            for (Element contentElement : ccontentElements) {
                ccontentBuilder.append(contentElement.text()).append("\n");
            }
            // 最后添加一个换行符，以确保最后一行后也有一个换行符
            ccontentBuilder.append("\n");
            // 获取提取的正文内容
            articleContent = ccontentBuilder.toString().trim();

            PressCentreDao pressCentreDao = new PressCentreDao();
            pressCentreDao.setId(3);
            pressCentreDao.setName("新闻中心");
            pressCentreDao.setBname(category);
            pressCentreDao.setBid(bid);
            pressCentreDao.setBurl(NewUrl);
            pressCentreDao.setCid(Cid);
            pressCentreDao.setCname(ctitle);
            pressCentreDao.setCtime(date);
            pressCentreDao.setCurl(relativeLink);
            pressCentreDao.setDname(ctitle);
            pressCentreDao.setDtime(date);
            pressCentreDao.setDyuan(csource);
            pressCentreDao.setNeirong(articleContent);
            dataList.add(pressCentreDao);

/*

            System.out.println("模块类别"+category);
            System.out.println("模块id"+bid);
            System.out.println("模块路径"+NewUrl);
            System.out.println("消息id"+currentBid);
            System.out.println("消息标题"+ctitle);
            System.out.println("消息来源"+csource);
            System.out.println("消息日期"+date);
            System.out.println("消息连接"+fullLink);
            System.out.println("具体消息"+articleContent);*/



        }

        return bid;
    }




}