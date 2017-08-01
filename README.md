# ctsi-mcw-deid-service
NLP Software to deidentify protected health information from clinical notes.

Jay Urbain, PhD

Java web app maven project
- war file included under target directory, load directly into Tomcat.

Accessible through web page (default index.jsp), or web service.

The web page and web service links below are provided for evaluaton only, not production use.
Web Page: 
[https://cis.ctsi.mcw.edu/deid/](https://cis.ctsi.mcw.edu/deid/) 

Web service:

$ curl -H "Content-Type: application/json" -X POST -d '{"dateoffset":"10","name":"Jay","recordlist":["Jay Urbain, jay.urbain@gmail.com, born December 6, 2156 is an elderly caucasian male suffering from illusions of grandeur and LBP.", "He is married to Kimberly Urbain, who is much better looking.", "Patient father, Francis Urbain has a history of CAD and DM.", "Jay has been prescribed meloxicam, and venti americano.", "He lives at 9050 N. Tennyson Dr., Disturbia, WI with his wife and golden retriever Mel.", "You can reach him at 414-745-5102."]}' https://cis.ctsi.mcw.edu/deid/deidservice

{"name":"Jay","dateoffset":"10","deidlist":["[PATIENT[PERSON] [PERSON],  [xxx@xxx.xxx] , born  [12_16_2156]  is an elderly caucasian male suffering from illusions of grandeur and LBP.  ","He is married to [PERSON] [PERSON], who is much better looking.  ","Patient father, [PERSON] [PERSON] has a history of CAD and DM.  ","[PATIENT] has been prescribed meloxicam, and venti americano.  ","He lives at  [xxxxx x. xxxxx]  Dr., Disturbia, WI with his wife and golden retriever [PERSON].  ","You can reach him at  [xxx_xxx_xxxx] .  "]}Jays-MacBook-Pro-2:ctsi-mcw-deid-service jayurbain$ 




Credits:

This software uses [http://stanfordnlp.github.io/CoreNLP/](http://stanfordnlp.github.io/CoreNLP/) for named entity identification.

Manning, Christopher D., Mihai Surdeanu, John Bauer, Jenny Finkel, Steven J. Bethard, and David McClosky. 2014. The Stanford CoreNLP Natural Language Processing Toolkit In Proceedings of the 52nd Annual Meeting of the Association for Computational Linguistics: System Demonstrations, pp. 55-60. [pdf](https://nlp.stanford.edu/pubs/StanfordCoreNlp2014.pdf)

