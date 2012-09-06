package org.triskell.web

import scala.collection.immutable.HashMap

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/03/12
 * Time: 16:59
 */

object MenuRenderer {

  def getMenuHtml(currentURL: String): String = {
    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="i-bar">
              <i class="icon-chevron-down icon-white"></i>
            </span>
          </a>
          <a class="brand" href={"{urlpattern}"}>Triskell</a>
          <div class="nav-collapse">
            <ul class="nav">
              {getItems.map(it => {
              <li class={if (currentURL == it._2) {
                "active"
              } else {
                "noactive"
              }}>
                <a href={it._2}>
                  {it._1}
                </a>
              </li>
            })}
            </ul>
            <ul class="nav pull-right">
              <li>
                <a href="http://www.inria.fr">inria.fr</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>.toString()
  }


  def getItems: List[Tuple4[String, String, String, HashMap[String, String]]] = {
    List(
      // ("Home", "/", "overview.html"),
      //  ("Research", "/research", "research.html"),
      ("Objectives", "{urlpattern}context", "context.html", HashMap[String, String]()),
      ("Team", "{urlpattern}team", "team.html", HashMap[String, String]()),
      ("Bibliography", "{urlpattern}biblio", "biblio.html", HashMap[String, String]()),
      // ("Tools", "/tools", ""),
      //("Platforms", "/platform", ""),
      ("Software", "{urlpattern}software", "software.html", HashMap[String, String]()),
      ("National projects", "{urlpattern}collaborations", "national.html", HashMap[String, String]()),
      ("International projects", "{urlpattern}collaborative_projects", "international.html", HashMap[String, String]()),
      ("Activity reports", "{urlpattern}activity_reports", "activity_reports.html", HashMap[String, String]()),
      ("Jobs", "{urlpattern}jobs", "jobs.html", HashMap[String, String]())//,
     // ("inria", "http://www.inria.fr", "", HashMap[String, String]())
    )
  }

}
