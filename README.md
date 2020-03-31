# ctsi-mcw-deid-service

### NLP Software to deidentify protected health information from clinical notes.

Jay Urbain, PhD  
jay.urbain@gmail.com

George Kowalski
gkowalski@mcw.edu

Java Maven web service app. The application is accessible through a web page (index.jsp) or a JSON-based web service.

The web app and web service links provided below are for evaluation only and not for production use.

### Requirements
- APpche Tomcat 8.X ( Support for Servlet Spec 3 )

### Web-based user interface
[https://cis.ctsi.mcw.edu/deid/](https://cis.ctsi.mcw.edu/deid/)

![Web Screen Shot](https://github.com/jayurbain/ctsi-mcw-deid-service/blob/master/src/main/webapp/img/web_screen_shot.png)

### JSON web-service interface

[https://cis.ctsi.mcw.edu/deid/deidservice](https://cis.ctsi.mcw.edu/deid/deidservice)

$ curl -H "Content-Type: application/json" -X POST -d '{"dateoffset":"10","name":"Jay","recordlist":["Jay Urbain, jay.urbain@gmail.com, born December 6, 2156 is an elderly caucasian male suffering from illusions of grandeur and LBP.", "He is married to Kimberly Urbain, who is much better looking.", "Patient father, Francis Urbain has a history of CAD and DM.", "Jay has been prescribed meloxicam, and venti americano.", "He lives at 9050 N. Tennyson Dr., Disturbia, WI with his wife and golden retriever Mel.", "You can reach him at 414-745-5102."]}' https://cis.ctsi.mcw.edu/deid/deidservice

{"name":"Jay","dateoffset":"10","deidlist":["[PATIENT[PERSON] [PERSON],  [xxx@xxx.xxx] , born  [12_16_2156]  is an elderly caucasian male suffering from illusions of grandeur and LBP.  ","He is married to [PERSON] [PERSON], who is much better looking.  ","Patient father, [PERSON] [PERSON] has a history of CAD and DM.  ","[PATIENT] has been prescribed meloxicam, and venti americano.  ","He lives at  [xxxxx x. xxxxx]  Dr., Disturbia, WI with his wife and golden retriever [PERSON].  ","You can reach him at  [xxx_xxx_xxxx] .  "]}

### License
"CTSI MCW Deidentification" is licensed under the GNU General Public License (v3 or later; in general "CTSI MCW Deidentification" code is GPL v2+, but "CTSI MCW Deidentification" uses several Apache-licensed libraries, and so the composite is v3+). Note that the license is the full GPL, which allows many free uses, but not its use in proprietary software which is distributed to others. For distributors of proprietary software, "CTSI MCW Deidentification" is also available from CTSI of Southeast Wisconsin under a commercial licensing You can contact us at jay.urbain@gmail.com. 

The application uses [Stanford's Core NLP] (https://github.com/stanfordnlp/CoreNLP) for named entity identification.

Manning, Christopher D., Mihai Surdeanu, John Bauer, Jenny Finkel, Steven J. Bethard, and David McClosky. 2014. The Stanford CoreNLP Natural Language Processing Toolkit In Proceedings of the 52nd Annual Meeting of the Association for Computational Linguistics: System Demonstrations, pp. 55-60. [pdf](http://nlp.stanford.edu/pubs/StanfordCoreNlp2014.pdf).

