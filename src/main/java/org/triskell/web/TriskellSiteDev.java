package org.triskell.web;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.kevoree.annotation.*;
import org.kevoree.library.javase.fileSystem.api.FileService;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;
import org.triskell.web.PageRenderer;

import java.io.File;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/03/12
 * Time: 11:12
 */

@ComponentType
//@DictionaryType()
@DictionaryType({
        @DictionaryAttribute(name = "folder")})

@Requires({
        @RequiredPort(name = "createRepo", type= PortType.SERVICE, className = FileService.class,optional = true)
})


public class TriskellSiteDev extends ParentAbstractPage {

    protected String basePage = "overview.html";


    private File f;
    private PageRenderer krenderer = null;

    FileService fs;


    @Override
    public void startPage() {
        super.startPage();
        File f1 = new File((String) super.getDictionary().get("folder"));
        if (f1.isDirectory()){
            f=f1;
            krenderer = new PageRenderer(true,f,fs);
        }

         fs = this.getPortByName("createRepo",FileService.class);
        System.out.println(fs);
    }

    @Override
    public void updatePage() {
        super.updatePage();
        File f1 = new File((String) super.getDictionary().get("folder"));
        fs = this.getPortByName("createRepo",FileService.class);
        if (f1.isDirectory()){
            f=f1;
        }
        krenderer = new PageRenderer(true,f,fs);


    }

    @Override
    public void stopPage() {
        super.stopPage();
    }

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {


        if (request.getUrl() != null && request.getUrl().startsWith("/TestEditor/saveContent/")){
            //System.err.println(request.getResolvedParams().get("htmlEditor"));
            try{
                //System.out.println(request.getUrl().split("/")[3]);
                if (isPortBinded("createRepo")){
                    System.out.println("should save " + request.getUrl());
                    String[] splits =  request.getUrl().split("/");
                    String html = request.getResolvedParams().get("htmlEditor");
                    Document doc = Jsoup.parse(html);
                    //Elements newsHeadlines = doc.select("#mp-itn b a");
                    Elements twitter = doc.select("div#twitter");
                    Document doc1 = Jsoup.parseBodyFragment("<div id=\"twitter\" class=\"span4 noteditable\">" +
                            "<h1>Twitter</h1>" +
                            "<p class=\"before-twitter\">Latest <a href=\"https://twitter.com/#!/triskell-team\">@Triskell</a> buzz:</p>" +
                            "<div id=\"tweets\" class=\"tweets\"></div></div>");

                    if (twitter.size()>0){

                        Node twitter1 = doc1.select("#twitter").first();
                      twitter.first().replaceWith(twitter1);
                    }
                    Elements scriptalohaeditor   = doc.select("#scriptalohaeditor");
                    scriptalohaeditor.first().remove();
                    Elements conatainer   = doc.select("div.container");

                    Elements links = doc.select("a[href^=/]"); // a with href
                    for (Node e: links)
                        {
                        e.attr("href",e.attr("href").replaceFirst("/","{urlpattern}"));
                    }
                    Elements imgs = doc.select("img[src^=/]");
                    for (Node e: imgs)
                    {
                        e.attr("src",e.attr("src").replaceFirst("/","{urlpattern}"));
                    }



                    System.out.println("should save " + conatainer.html());


                    fs.saveFile("/src/main/resources/templates/html/" + splits[splits.length - 1], doc.html().getBytes());
                }
            }catch (Exception e){
                e.printStackTrace();
                return response;
            }
        //response.setContent("Bad request");
        return response;

        }
        File f1=null;
        if (isPortBinded("createRepo")){
            f1=new File(fs.getAbsolutePath("/src/main/resources/"));
            //System.out.println("path = " +f1);
        }else{
           f1=f;
        }
        //System.out.println("path = " +f1);
        if (FileServiceHelper.checkStaticFileFromDir(basePage, this, request, response, f1.getAbsolutePath())) {
            if (request.getUrl().equals("/") || request.getUrl().endsWith(".html") || request.getUrl().endsWith(".css")) {
                String pattern = getDictionary().get("urlpattern").toString();
                if (pattern.endsWith("**")) {
                    pattern = pattern.replace("**", "");
                }
                if (!pattern.endsWith("/")) {
                    pattern = pattern + "/";
                }
                response.setContent(response.getContent().replace("{urlpattern}", pattern));
            }
            return response;
        }
        if (krenderer.checkForTemplateRequest(basePage, this, request, response)) {
            return response;
        }

        response.setContent("Bad request");
        return response;
    }


}
