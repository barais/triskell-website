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
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


@ComponentType
//@DictionaryType()
@DictionaryType({
        @DictionaryAttribute(name = "folder"),
        @DictionaryAttribute(name = "siteType",defaultValue = "edit") /*edit or prod*/
})

@Requires({
        @RequiredPort(name = "createRepo", type= PortType.SERVICE, className = FileService.class,optional = true,needCheckDependency = true)
})


public class TriskellSite extends ParentAbstractPage {

    protected String basePage = "overview.html";
    protected ExecutorService executor;
    private File f;
    private PageRenderer krenderer = null;

    FileService fs;

    boolean activateCache=false;

    @Override
    public void startPage() {
        super.startPage();
        contentRawCache.clear();
        contentTypeCache.clear();
        activateCache =  super.getDictionary().get("siteType").equals("prod");

        executor = Executors.newSingleThreadExecutor();
        File f1 = new File((String) super.getDictionary().get("folder"));
        fs = this.getPortByName("createRepo",FileService.class);

        if (isPortBinded("createRepo")){
                f=new File(fs.getAbsolutePath("/src/main/resources/"));
                krenderer = new PageRenderer(true,f,fs, super.getDictionary().get("siteType").equals("edit"));
            }
                else{
                if (f1.isDirectory())
                    f=f1;
                krenderer = new PageRenderer(true,f,null,super.getDictionary().get("siteType").equals("edit"));
            }
    }

    @Override
    public void updatePage() {
        super.updatePage();

        contentRawCache.clear();
        contentTypeCache.clear();
        File f1 = new File((String) super.getDictionary().get("folder"));
        fs = this.getPortByName("createRepo",FileService.class);
        if (f1.isDirectory()){
            f=f1;
        }


    }

    @Override
    public void stopPage() {
        super.stopPage();
        contentRawCache.clear();
        contentTypeCache.clear();

    }

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if (this.activateCache){
        if (contentTypeCache.containsKey(request.getUrl()) && request.getUrl() != null && !request.getUrl().startsWith("/TestEditor/saveContent/")) {
            response.setRawContent(contentRawCache.get(request.getUrl()));
            response.getHeaders().put("Content-Type", contentTypeCache.get(request.getUrl()));
            return response;
        }                       }
        if (request.getUrl() != null && request.getUrl().startsWith("/TestEditor/saveContent/")){

            try{
                if (isPortBinded("createRepo")){
                    final String[] splits =  request.getUrl().split("/");
                    String html = request.getResolvedParams().get("htmlEditor");
                    Document doc = Jsoup.parse(html);
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


                    try{
                        scriptalohaeditor.first().remove();
                    }catch (NullPointerException e){
                        logger.error("pas de balise scriptalohaeditor");
                    }

                    final Elements conatainer   = doc.select("div.container");

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
                    Elements contenteditable = doc.select("[contenteditable]");
                    for (Node e: contenteditable)
                    {
                        e.removeAttr("contenteditable");

                    }

                    executor.execute(new Runnable() {
                         @Override
                         public void run() {
                             fs.saveFile("/src/main/resources/templates/html/" + splits[splits.length - 1], conatainer.html().getBytes());
                         }
                     });
                }
            }catch (Exception e){
                e.printStackTrace();
                return response;
            }
               response.setContent("ok");

            if (this.activateCache){
                cacheResponse(request, response);
            }
            return response;

        }
        if (krenderer.checkForTemplateRequest(basePage, this, request, response)) {
            if (this.activateCache){
                cacheResponse(request, response);
            }
            return response;
        }
        if (FileServiceHelper.checkStaticFileFromDir(basePage, this, request, response, f.getAbsolutePath())) {

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
            if (this.activateCache){
                cacheResponse(request, response);
            }
            return response;
        }
        response.setContent("Bad request");
        return response;
    }


    private HashMap<String, byte[]> contentRawCache = new HashMap<String, byte[]>();
    private HashMap<String, String> contentTypeCache = new HashMap<String, String>();


    public void cacheResponse(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        if (response.getRawContent() != null) {
            contentRawCache.put(request.getUrl(), response.getRawContent());
        } else {
            contentRawCache.put(request.getUrl(), response.getContent().getBytes());
        }
        contentTypeCache.put(request.getUrl(), response.getHeaders().get("Content-Type"));
    }

}
