package com.zhua.scraper;

import com.zhua.dao.GaikuangDao;
import com.zhua.mapper.ShouyeMapper;
import com.zhua.service.GaiKuangService;
import com.zhua.service.ShouyeService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@Component
public class GaikuangSc {

    @Autowired
    private GaiKuangService gaiKuangService;

    public void scrapeGaikuang() {
        /* dataList 用于存储抓取到的首页信息，每个首页信息都表示为一个 ShouyeDao 对象，
         然后将这些对象添加到 dataList 中。最后，通过调用 shouyeService.saveBatch(dataList);
         方法，将 dataList 中的数据批量插入到数据库中，以完成数据持久化操作。*/
        List<GaikuangDao> dataList = new ArrayList<>();

        try {
            String baseUrl = "http://www.wz.gov.cn/jkq/wzjkq/jggk/202005/t20200509_7331217.html";
            Document doc = Jsoup.connect(baseUrl).get();
            //提取当前大标题
            Element bigTitle = doc.select("div.h-menu.wrap.g-h-menu.jkqguide span.g-sp").first();
            String bTitle = bigTitle.text();
            //提取链接
            Element jkqgkLink = doc.select("a[href*=wzjkq/jggk/]").first();
            String jkqgkUrl = jkqgkLink.attr("href");
            System.out.println("经开区概况链接: " + jkqgkUrl);

            // 提取标题
            Elements titleElement = doc.select("div.zwxl-title p.tit");
            String title = titleElement.text();
            System.out.println("标题: " + title);

            // 提取日期
            Elements dateElement = doc.select("div.zwxl-title span.con");
            String date = dateElement.text();
            System.out.println("日期: " + date);

            // 提取来源
            //<script>if("万州经开区办公室"!=""){document.write('<span class="tit"> 来源：</span><span class="con">万州经开区办公室</span>');}</script>
            /*.*: 这部分匹配零个或多个字符，直到下一个部分的模式开始。
            来源：</span><span class="con">: 这是要匹配的文字文本，表示“来源：</span><span class="con">”，即在 HTML 中的来源标签和具体来源之间的文本。

            (.*?): 这是一个捕获组，用于捕获我们想提取的来源信息。括号表示一个捕获组，.*? 表示匹配任意字符（.*），但尽可能少地匹配字符（?）。
            </span>.*: 这部分匹配 </span> 之后的任意字符。*/
            Elements sourceElement = doc.select("div.zwxl-title script");
            String sourceScript = sourceElement.html();
            String source = sourceScript.replaceAll(".*来源：</span><span class=\"con\">(.*?)</span>.*", "$1");


            // 提取正文内容
            Elements contentElement = doc.select("div.view");
            String content = contentElement.text();
            System.out.println("正文内容: " + content);
            GaikuangDao gaikuangDao=  new GaikuangDao();
            gaikuangDao.setId(2);
            gaikuangDao.setName(bTitle);
            gaikuangDao.setBid(1);
            gaikuangDao.setBname(title);
            gaikuangDao.setBurl(jkqgkUrl);
            gaikuangDao.setBtime(date);
            gaikuangDao.setByuan(source);
            gaikuangDao.setBneirong(content);

            dataList.add(gaikuangDao);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 插入数据到数据库
        gaiKuangService.saveBatch(dataList);
        System.out.println("数据插入成功！");
    }
}
