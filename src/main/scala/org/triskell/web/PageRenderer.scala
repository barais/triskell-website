package org.triskell.web

import io.Source
import java.io._
import org.kevoree.library.javase.fileSystem.api.FileService
import collection.immutable.HashMap
import org.kevoree.library.javase.webserver.{URLHandlerScala, AbstractPage, KevoreeHttpRequest, KevoreeHttpResponse}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/03/12
 * Time: 17:15
 */

class PageRenderer(devmod: Boolean, folder: java.io.File, fs: FileService) {

  val handler = new URLHandlerScala()

  def checkForTemplateRequest(index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    val urlPattern = origin.getDictionary.get("urlpattern").toString

    println("URL = " +  request.getUrl + " "+ request.getCompleteUrl)
    handler.getLastParam(request.getUrl, urlPattern) match {
      case Some(reqP) => {
        if (reqP == "" || reqP == null || reqP == "/") {
          response.setContent(krender(index, "/", HashMap[String, String](), urlPattern))
          true
        } else {
          MenuRenderer.getItems.find(it => {
            var patternCleaned = urlPattern
            if (patternCleaned.endsWith("**")) {
              patternCleaned = patternCleaned.replace("**", "");
            }
            if (!patternCleaned.endsWith("/")) {
              patternCleaned = patternCleaned + "/";
            }

            val cleanupRequest = it._2.replace("{urlpattern}", patternCleaned)
            val cleanupRep = if (!reqP.startsWith("/")) {
              "/" + reqP
            } else {
              reqP
            }
            println(cleanupRequest + "==" + cleanupRep)

            cleanupRequest == (cleanupRep)
          }) match {
            case Some(it) => {
              response.setContent(krender(it._3, it._2, it._4, urlPattern))
              true
            }
            case None => false
          }
        }
      }
      case None => false
    }
  }

  def krender(name: String, currentURL: String, vars: HashMap[String, String], pattern: String): String = {
    println(name + " " + currentURL+ " " + vars + " " +pattern)

    val sb = new StringBuffer()
    sb.append(kheader)
    sb.append(MenuRenderer.getMenuHtml(currentURL))

    // sb.append("<div class=\"hero-unit\">\n    <div class=\"row\">\n        <div class=\"span5\">\n            <p><img src=\"img/kevoree-logo.png\"/></p>\n        </div>\n        <div class=\"span5\">\n            <p>Kevoree project aims at enabling distributed reconfigurable software development. Build around a component model, Kevoree leverage model@runtime approach to offer tools to build, adapt and synchronize distributed systems.\n                Extensible, this project already offer runtime for Standard Java Virtual Machine, Android, Arduino but also for virtualization management such as VirtualBox.\n                In short Kevoree helping you to develop your adaptable software from Cloud stack to embedded devices !\n            </p>\n        </div>\n    </div>\n</div>")
    sb.append("<div class=\"wrapper\">")
    sb.append("<div class=\"container\">")
    sb.append(replaceVariable(renderHtml(name), vars))
    sb.append("<div id=\"scriptalohaeditor\">")
    sb.append(this.replaceVariable(footerScriptPage(name), vars))
    sb.append("</div>")
    sb.append("</div>")
    sb.append("<div class=\"push\"><!--//--></div>")
    sb.append("</div>")
    sb.append(footerScript)
    sb.append("<div class=\"footer\" /><footer>\n<p>&copy; kevoree@Triskell 2012</p>\n</footer>\n\n</div>")
    sb.append("</body></html>")

    var patternCleaned = pattern
    if (patternCleaned.endsWith("**")) {
      patternCleaned = patternCleaned.replace("**", "");
    }
    if (!patternCleaned.endsWith("/")) {
      patternCleaned = patternCleaned + "/";
    }
    sb.toString.replace("{urlpattern}", patternCleaned);
  }

  def renderHtml(name: String): String = {
    //Source.fromFile(new File(getClass.getClassLoader.getResource("templates/../").getPath+"../../src/main/resources/templates/html/" + name)).getLines().mkString("\n")

    var st: InputStream = null
    if (fs != null){
       println("pass par la")
       return new String(fs.getFileContent( "src/main/resources/templates/html/" + name ))
    } else{
     if (devmod)  {
       println("pass par la1")
        st = new FileInputStream(new File(folder.getAbsolutePath + java.io.File.separator + "templates" + java.io.File.separator + "html" + java.io.File.separator + name))
    }else
        st = getClass.getClassLoader.getResourceAsStream("templates/html/" + name)

      if (st != null) {
        return Source.fromInputStream(st).getLines().mkString("\n")
      } else {
        return "not found"
      }
  }

  }

  def kheader: String = {
    "<html lang=\"en\">" +
      "<head><meta charset=\"utf-8\">" +
      "<title>Triskell Project</title>" +
      "<meta name=\"description\" content=\"Triskell INRIA Team \">" +
      "<meta name=\"author\" content=\"François Fouquet\">" +
      "<!-- Le HTML5 shim, for IE6-8 support of HTML elements -->\n    <!--[if lt IE 9]>\n    <script src=\"//html5shim.googlecode.com/svn/trunk/html5.js\"></script>\n    <![endif]-->" +
      "<link rel=\"stylesheet/less\" type=\"text/css\" href=\"{urlpattern}less/bootstrap.less\">\n<script src=\"{urlpattern}js/less-1.3.0.min.js\" type=\"text/javascript\"></script>" +
      "<link rel=\"stylesheet/less\" type=\"text/css\" href=\"{urlpattern}less/bootstrap-responsive.less\">" +
      "<link rel=\"stylesheet/less\" type=\"text/css\" href=\"{urlpattern}less/carousel.less\">" +
      "<link rel=\"stylesheet\" type=\"text/css\" href=\"{urlpattern}css/jquery.tweet.css\">" +
      //"<script type=\"text/javascript\" src=\"{urlpattern}js/bootstrap-carousel.js\"></script>" +
      "<link href=\"{urlpattern}css/kevoree.css\" rel=\"stylesheet\">" +
      "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>\n" +
      "<link href=\"{urlpattern}js/google-code-prettify/prettify.css\" type=\"text/css\" rel=\"stylesheet\"/>\n" +
      "<script type=\"text/javascript\" src=\"{urlpattern}js/google-code-prettify/prettify.js\"></script>" +
      "</head>" +
      "<body onload=\"prettyPrint()\">\n"
  }


  def replaceVariable(html: String, vars: HashMap[String, String]): String = {
    var content = html
    vars.foreach(v => {
      content = content.replace("{" + v._1 + "}", v._2)
    })
    content
  }


  def footerScript: String = {
    <script src={"{urlpattern}js/jquery.js"}></script>
      <script src={"{urlpattern}js/bootstrap-transition.js"}></script>
      <script src={"{urlpattern}js/bootstrap-alert.js"}></script>
      <script src={"{urlpattern}js/bootstrap-modal.js"}></script>
      <script src={"{urlpattern}js/bootstrap-dropdown.js"}></script>
      <script src={"{urlpattern}js/bootstrap-scrollspy.js"}></script>
      <script src={"{urlpattern}js/bootstrap-tab.js"}></script>
      <script src={"{urlpattern}js/bootstrap-tooltip.js"}></script>
      <script src={"{urlpattern}js/bootstrap-popover.js"}></script>
      <script src={"{urlpattern}js/bootstrap-button.js"}></script>
      <script src={"{urlpattern}js/bootstrap-collapse.js"}></script>
      <script src={"{urlpattern}js/bootstrap-carousel.js"}></script>
      <script src={"{urlpattern}js/jquery.livetwitter.js"}></script>
      <script src={"{urlpattern}js/livetwitter.js"}></script>
      <script src={"{urlpattern}js/jquery.tweet.js"}></script>
      <script src={"{urlpattern}js/bootstrap-typeahead.js"}></script>.mkString




    }
  def footerScriptPage(name:String) : String ={
      var v = new StringBuilder;

    v.append("<link rel=\"stylesheet\" href=\"{urlpattern}aloha/css/aloha.css\" type=\"text/css\">\n")
      v.append("<script src=\"{urlpattern}aloha/lib/require.js\"></script>\n")
      v.append("<script src=\"{urlpattern}aloha/lib/aloha.js\" \n")
      v.append("        data-aloha-plugins=\"common/align, common/ui,\n")
      v.append("  common/format,\n")
      v.append("  common/highlighteditables, \n")
      v.append("  common/link,\n")
      v.append("  common/undo,\n")
      v.append("common/list\">\n")
    v.append("</script>\n")

    //v.append(footerScriptXML)
    v.append("<script type=\"text/javascript\">\n")
    v.append("Aloha = window.Aloha || {};\n")
    v.append("Aloha.settings = {\n")
    v.append("logLevels: { 'error': true, 'warn': true, 'info': true, 'debug': false, 'deprecated': true },\n")
    v.append("errorhandling: false,     \n")
    v.append("ribbon: {enable: true},  \n")
    v.append("locale: 'en',  \n")
    v.append("plugins: {   \n")
    v.append("  format: {  \n")
    v.append("  config: [  'a', 'b', 'i', 'p', 'sub', 'sup', 'del', 'title', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'pre', 'removeFormat' ]\n")
    v.append("}  \n")
    v.append("},  \n")
    v.append("plugins: {    \n")
    v.append("  image: {    \n")
    v.append("  config: [ 'img' ]    \n")
    v.append("}  \n")
    v.append("},  \n")
    v.append("sidebar: {      \n")
    v.append("  disabled: false       \n")
    v.append("}    \n")
    v.append("};  \n")


    v.append("function  savePage (){   \n")
    v.append("publishContent(document.querySelector(\".wrapper\").innerHTML)     \n")
    v.append("};    \n")

      v.append("Aloha.ready( function() {        \n")
      v.append("Aloha.jQuery('.span1').aloha();       \n")
      v.append("Aloha.jQuery('.span2').aloha();  \n")
      v.append("Aloha.jQuery('.span3').aloha();   \n")
      v.append("Aloha.jQuery('.span4').aloha(); \n")
      v.append("Aloha.jQuery('.span5').aloha();  \n")
      v.append("Aloha.jQuery('.span6  ').aloha();  \n")
    v.append("Aloha.jQuery('.editable').aloha();  \n")

    v.append("Aloha.bind('aloha-editable-deactivated', function() {  \n")
      //savePage();
    v.append("savePage();     \n")
    v.append("}); \n")
      v.append("Aloha.bind('aloha-editable-activated', function() {   \n")

      v.append("});   \n")
      v.append(" });\n")

    v.append("function publishContent(content) { \n")
      v.append("url  =  '/TestEditor/saveContent/"+name+"';\n")
      v.append("if (window.XMLHttpRequest) {                \n")
      v.append(" obj = new XMLHttpRequest();                    \n")
    v.append("obj.onreadystatechange = function(){                  \n")
   //   v.append("document.ajax.dyn.value=\"Wait server...\";\n")
    v.append("if(obj.readyState == 4)              \n")
      v.append("{                                     \n")
      v.append(" if(obj.status == 200)                   \n")
      v.append(" {alert(\"Received:\" + obj.responseText); \n")
     // v.append("   document.ajax.dyn.value=\"Received:\" + obj.responseText;\n")
    v.append("  } \n")
      v.append("   else \n")
   //   v.append("  {         \n")
        v.append(" {alert(\"Received:\" + obj.statusText); \n")

        //v.append("    document.ajax.dyn.value=\"Error: returned status code \" + \n")
    //v.append("      obj.status + \" \" + obj.statusText;                       \n")
    v.append("  }              \n")
      v.append("  }               \n")
      v.append("};                   \n")
    v.append("obj.open(\"POST\", url, true);                        \n")
      v.append("obj.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');\n")
      v.append("if (content != null){   \n")
      v.append("  obj.send('htmlEditor='+content);  \n")
      v.append("} else       \n")
      v.append("  obj.send('htmlEditor='+document.myform.htmlEditor.value);\n")
      v.append("} else {   \n")
      v.append("alert(\"Your browser does not support AJAX\"); \n")
      v.append("}   \n")
      v.append("};    \n")
      v.append("</script>\n")
      return v.toString()

  }

}
