/**
 * Copyright 2014 Snowplow Analytics Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.snowplowanalytics.refererparser.scala

// Java
import java.net.URI
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

// JSON
/*
import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONArray;
*/

// json4s
import org.json4s.scalaz.JsonScalaz._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

// Specs2
import org.specs2.mutable.Specification

class JsonParseTest extends Specification {

  val testString = """[
    {
        "spec": "Google Images search",
        "uri": "http://www.google.fr/imgres?q=Ogham+the+celtic+oracle&hl=fr&safe=off&client=firefox-a&hs=ZDu&sa=X&rls=org.mozilla:fr-FR:unofficial&tbm=isch&prmd=imvnsa&tbnid=HUVaj-o88ZRdYM:&imgrefurl=http://www.psychicbazaar.com/oracles/101-ogham-the-celtic-oracle-set.html&docid=DY5_pPFMliYUQM&imgurl=http://mdm.pbzstatic.com/oracles/ogham-the-celtic-oracle-set/montage.png&w=734&h=250&ei=GPdWUIePCOqK0AWp3oCQBA&zoom=1&iact=hc&vpx=129&vpy=276&dur=827&hovh=131&hovw=385&tx=204&ty=71&sig=104115776612919232039&page=1&tbnh=69&tbnw=202&start=0&ndsp=26&ved=1t:429,r:13,s:0,i:114&biw=1272&bih=826",
        "medium": "search",
        "source": "Google Images",
        "term": "Ogham the celtic oracle",
        "known": true
    },
    {
        "spec": "Yahoo! Images search",
        "uri": "http://it.images.search.yahoo.com/images/view;_ylt=A0PDodgQmGBQpn4AWQgdDQx.;_ylu=X3oDMTBlMTQ4cGxyBHNlYwNzcgRzbGsDaW1n?back=http%3A%2F%2Fit.images.search.yahoo.com%2Fsearch%2Fimages%3Fp%3DEarth%2BMagic%2BOracle%2BCards%26fr%3Dmcafee%26fr2%3Dpiv-web%26tab%3Dorganic%26ri%3D5&w=1064&h=1551&imgurl=mdm.pbzstatic.com%2Foracles%2Fearth-magic-oracle-cards%2Fcard-1.png&rurl=http%3A%2F%2Fwww.psychicbazaar.com%2Foracles%2F143-earth-magic-oracle-cards.html&size=2.8+KB&name=Earth+Magic+Oracle+Cards+-+Psychic+Bazaar&p=Earth+Magic+Oracle+Cards&oid=f0a5ad5c4211efe1c07515f56cf5a78e&fr2=piv-web&fr=mcafee&tt=Earth%2BMagic%2BOracle%2BCards%2B-%2BPsychic%2BBazaar&b=0&ni=90&no=5&ts=&tab=organic&sigr=126n355ib&sigb=13hbudmkc&sigi=11ta8f0gd&.crumb=IZBOU1c0UHU",
        "medium": "search",
        "source": "Yahoo! Images",
        "term": "Earth Magic Oracle Cards",
        "known": true
    },
    {
        "spec": "Powered by Google",
        "uri": "http://isearch.avg.com/pages/images.aspx?q=tarot+card+change&sap=dsp&lang=en&mid=209215200c4147d1a9d6d1565005540b-b0d4f81a8999f5981f04537c5ec8468fd5234593&cid=%7B50F9298B-C111-4C7E-9740-363BF0015949%7D&v=12.1.0.21&ds=AVG&d=7%2F23%2F2012+10%3A31%3A08+PM&pr=fr&sba=06oENya4ZG1YS6vOLJwpLiFdjG91ICt2YE59W2p5ENc2c4w8KvJb5xbvjkj3ceMjnyTSpZq-e6pj7GQUylIQtuK4psJU60wZuI-8PbjX-OqtdX3eIcxbMoxg3qnIasP0ww2fuID1B-p2qJln8vBHxWztkpxeixjZPSppHnrb9fEcx62a9DOR0pZ-V-Kjhd-85bIL0QG5qi1OuA4M1eOP4i_NzJQVRXPQDmXb-CpIcruc2h5FE92Tc8QMUtNiTEWBbX-QiCoXlgbHLpJo5Jlq-zcOisOHNWU2RSHYJnK7IUe_SH6iQ.%2CYT0zO2s9MTA7aD1mNjZmZDBjMjVmZDAxMGU4&snd=hdr&tc=test1",
        "medium": "search",
        "source": "Google",
        "term": "tarot card change",
        "known": true
    },
    {
        "spec": "Google search #1",
        "uri": "http://www.google.com/search",
        "medium": "search",
        "source": "Google",
        "term": null,
        "known": true
    },
    {
        "spec": "Google search #2",
        "uri": "http://www.google.com/search?q=gateway+oracle+cards+denise+linn&hl=en&client=safari",
        "medium": "search",
        "source": "Google",
        "term": "gateway oracle cards denise linn",
        "known": true
    },
    {
        "spec": "Yahoo! search",
        "uri": "http://es.search.yahoo.com/search;_ylt=A7x9QbwbZXxQ9EMAPCKT.Qt.?p=BIEDERMEIER+FORTUNE+TELLING+CARDS&ei=utf-8&type=685749&fr=chr-greentree_gc&xargs=0&pstart=1&b=11",
        "medium": "search",
        "source": "Yahoo!",
        "term": "BIEDERMEIER FORTUNE TELLING CARDS",
        "known": true
    },
    {
        "spec": "PriceRunner search",
        "uri": "http://www.pricerunner.co.uk/search?displayNoHitsMessage=1&q=wild+wisdom+of+the+faery+oracle",
        "medium": "search",
        "source": "PriceRunner",
        "term": "wild wisdom of the faery oracle",
        "known": true
    },
    {
        "spec": "Bing Images search",
        "uri": "http://www.bing.com/images/search?q=psychic+oracle+cards&view=detail&id=D268EDDEA8D3BF20AF887E62AF41E8518FE96F08",
        "medium": "search",
        "source": "Bing Images",
        "term": "psychic oracle cards",
        "known": true
    },
    {
        "spec": "IXquick search",
        "uri": "https://s3-us3.ixquick.com/do/search",
        "medium": "search",
        "source": "IXquick",
        "term": null,
        "known": true
    },
    {
        "spec": "AOL search",
        "uri": "http://aolsearch.aol.co.uk/aol/search?s_chn=hp&enabled_terms=&s_it=aoluk-homePage50&q=pendulums",
        "medium": "search",
        "source": "AOL",
        "term": "pendulums",
        "known": true
    },
    {
        "spec": "AOL search.com",
        "uri": "http://www.aolsearch.com/search?s_pt=hp&s_gl=NL&query=voorbeeld+cv+competenties&invocationType=tb50hpcnnbie7-nl-nl",
        "medium": "search",
        "source": "AOL",
        "term": "voorbeeld cv competenties",
        "known": true
    },
    {
        "spec": "Ask search",
        "uri": "http://uk.search-results.com/web?qsrc=1&o=1921&l=dis&q=pendulums&dm=ctry&atb=sysid%3D406%3Aappid%3D113%3Auid%3D8f40f651e7b608b5%3Auc%3D1346336505%3Aqu%3Dpendulums%3Asrc%3Dcrt%3Ao%3D1921&locale=en_GB",
        "medium": "search",
        "source": "Ask",
        "term": "pendulums",
        "known": true
    },
    {
        "spec": "Mail.ru search",
        "uri": "http://go.mail.ru/search?q=Gothic%20Tarot%20Cards&where=any&num=10&rch=e&sf=20",
        "medium": "search",
        "source": "Mail.ru",
        "term": "Gothic Tarot Cards",
        "known": true
    },
    {
        "spec": "Yandex search",
        "uri": "http://images.yandex.ru/yandsearch?text=Blue%20Angel%20Oracle%20Blue%20Angel%20Oracle&noreask=1&pos=16&rpt=simage&lr=45&img_url=http%3A%2F%2Fmdm.pbzstatic.com%2Foracles%2Fblue-angel-oracle%2Fbox-small.png",
        "medium": "search",
        "source": "Yandex Images",
        "term": "Blue Angel Oracle Blue Angel Oracle",
        "known": true
    },
    {
        "spec": "Ask toolbar search",
        "uri": "http://search.tb.ask.com/search/GGmain.jhtml?cb=AYY&pg=GGmain&p2=%5EAYY%5Exdm071%5EYYA%5Eid&n=77fdaa55&qid=c2678d9147654034bb8b16daa7bfb48c&ss=sub&st=hp&ptb=F9FC6C22-EAE6-4D1E-8126-A70119B6E02F&si=flvrunner&tpr=hst&searchfor=CARA+MEMASAK+CUMI+CUMI&ots=1219016089614",
        "medium": "search",
        "source": "Ask Toolbar",
        "term": "CARA MEMASAK CUMI CUMI",
        "known": true
    },
    {
        "spec": "Ask toolbar search #2",
        "uri": "http://search.tb.ask.com/search/GGmain.jhtml?&st=hp&p2=%5EZU%5Exdm458%5EYYA%5Eus&n=77fda1bd&ptb=F0B68CA5-4791-4376-BFCC-5F0100329FB6&si=CMKg9-nX07oCFSjZQgodcikACQ&tpr=hpsbsug&searchfor=test",
        "medium": "search",
        "source": "Ask Toolbar",
        "term": "test",
        "known": true
    },
    {
        "spec": "Voila search",
        "uri": "http://search.ke.voila.fr/?module=voila&bhv=web_fr&kw=test",
        "medium": "search",
        "source": "Voila",
        "term": "test",
        "known": true
    },
    {
        "spec": "Dale search",
        "uri": "http://www.dalesearch.com/?q=+lego.nl+%2Fclub&s=web&as=0&rlz=0&babsrc=HP_ss",
        "medium": "search",
        "source": "Dalesearch",
        "term": " lego.nl /club",
        "known": true
    },  
    {
        "spec": "Twitter redirect",
        "uri": "http://t.co/chrgFZDb",
        "medium": "social",
        "source": "Twitter",
        "term": null,
        "known": true
    },
    {
        "spec": "Facebook social",
        "uri": "http://www.facebook.com/l.php?u=http%3A%2F%2Fwww.psychicbazaar.com&h=yAQHZtXxS&s=1",
        "medium": "social",
        "source": "Facebook",
        "term": null,
        "known": true
    },
    {
        "spec": "Facebook mobile",
        "uri": "http://m.facebook.com/l.php?u=http%3A%2F%2Fwww.psychicbazaar.com%2Fblog%2F2012%2F09%2Fpsychic-bazaar-reviews-tarot-foundations-31-days-to-read-tarot-with-confidence%2F&h=kAQGXKbf9&s=1",
        "medium": "social",
        "source": "Facebook",
        "term": null,
        "known": true
    },
    {
        "spec": "Odnoklassniki",
        "uri": "http://www.odnoklassniki.ru/dk?cmd=logExternal&st._aid=Conversations_Openlink&st.name=externalLinkRedirect&st.link=http%3A%2F%2Fwww.psychicbazaar.com%2Foracles%2F187-blue-angel-oracle.html",
        "medium": "social",
        "source": "Odnoklassniki",
        "term": null,
        "known": true
    },
    {
        "spec": "Tumblr social #1",
        "uri": "http://www.tumblr.com/dashboard",
        "medium": "social",
        "source": "Tumblr",
        "term": null,
        "known": true
    },
    {
        "spec": "Tumblr w subdomain",
        "uri": "http://psychicbazaar.tumblr.com/",
        "medium": "social",
        "source": "Tumblr",
        "term": null,
        "known": true
    },
    {
        "spec": "Yahoo! Mail",
        "uri": "http://36ohk6dgmcd1n-c.c.yom.mail.yahoo.net/om/api/1.0/openmail.app.invoke/36ohk6dgmcd1n/11/1.0.35/us/en-US/view.html/0",
        "medium": "email",
        "source": "Yahoo! Mail",
        "term": null,
        "known": true
    },
    {
        "spec": "Outlook.com mail",
        "uri": "http://co106w.col106.mail.live.com/default.aspx?rru=inbox",
        "medium": "email",
        "source": "Outlook.com",
        "term": null,
        "known": true
    },
    {
        "spec": "Orange Webmail",
        "uri": "http://webmail1m.orange.fr/webmail/fr_FR/read.html?FOLDER=SF_INBOX&IDMSG=8594&check=&SORTBY=31",
        "medium": "email",
        "source": "Orange Webmail",
        "term": null,
        "known": true
    },
    {
        "spec": "Internal HTTP",
        "uri": "http://www.snowplowanalytics.com/about/team",
        "medium": "internal",
        "source": null,
        "term": null,
        "known": false
    },
    {
        "spec": "Internal HTTPS",
        "uri": "https://www.snowplowanalytics.com/account/profile",
        "medium": "internal",
        "source": null,
        "term": null,
        "known": false
    },
    {
        "spec": "Internal subdomain HTTP",
        "uri": "http://www.subdomain1.snowplowanalytics.com/account/profile",
        "medium": "internal",
        "source": null,
        "term": null,
        "known": false
    },
    {
        "spec": "Internal subdomain HTTPS",
        "uri": "http://www.subdomain2.snowplowanalytics.com/account/profile",
        "medium": "internal",
        "source": null,
        "term": null,
        "known": false
    }           
]"""

  // Convert the JSON to a List of JObjects
  val testJson = (parse(testString)) match {
    case JArray(lst) => lst
  }

  val pageHost = "www.snowplowanalytics.com"

  val internalDomains = List("www.subdomain1.snowplowanalytics.com", "www.subdomain2.snowplowanalytics.com")

  def getString(node: JValue, name: String): String = 
    (node \ name) match {
      case JString(s) => s
  }

  "parse" should {

    for (test <- testJson) {

      "extract the expected details from referer with spec '%s'".format(getString(test, "spec")) in {

        Parser.parse(getString(test, "uri"), pageHost, internalDomains) must_== 
          Some(Referer(
            Medium.withName(getString(test, "medium")),
            (test \ "source") match {
              case JString(s) => Some(s)
              case _ => None
            },
            (test \ "term") match {
              case JString(s) => Some(s)
              case _ => None
            }))
      }
    }
  }

}
