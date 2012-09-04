package org.triskell.web;


import org.kevoree.annotation.*;
import org.kevoree.library.javase.fileSystem.api.FileService;
import org.kevoree.library.javase.webserver.FileServiceHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;
import org.triskell.web.PageRenderer;

import java.io.File;

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
                    String[] splits =  request.getUrl().split("/");
                    fs.saveFile("src/main/resources/templates/html/"+splits[splits.length-1],request.getResolvedParams().get("htmlEditor").getBytes());
                }
            }catch (Exception e){

                e.printStackTrace();
                return response;
            }
        //response.setContent("Bad request");
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
            return response;
        }
        if (krenderer.checkForTemplateRequest(basePage, this, request, response)) {
            return response;
        }

        response.setContent("Bad request");
        return response;
    }


}
