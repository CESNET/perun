package cz.metacentrum.perun.cabinet.strategy.impl;

import org.apache.http.client.methods.HttpUriRequest
import org.junit.Test

import cz.metacentrum.perun.cabinet.model.Author
import cz.metacentrum.perun.cabinet.model.Publication
import cz.metacentrum.perun.cabinet.model.PublicationSystem

class OBD25StrategyTest {

	OBD25Strategy obdStrategy = new OBD25Strategy()
	

	@Test
	public void testGetRequestParams() {
		println 'OBD25StrategyTest.getRequestParams'
		
		String kodAutora = '20692'
		int yearSince = 2009
		int yearTill = 2010
		PublicationSystem ps = new PublicationSystem()
		ps.setUrl('http://zeus-web.zcu.cz:7080/fcgi/verso.fpl?')
		
		HttpUriRequest httpGetRequest = obdStrategy.getHttpRequest(kodAutora, yearSince, yearTill, ps)
		assert httpGetRequest.getURI().toString() == 'http://zeus-web.zcu.cz:7080/fcgi/verso.fpl?fname=obd_2_0_export_xml&_rokod=2009&_rokdo=2010&_aup=20692'
	}

	@Test
	public void testParseResponse() {
		println 'OBD25StrategyTest.parseResponse'
		
		List<Publication> publications = obdStrategy.parseResponse(obd25Response2)
		
		assert publications
		assert publications.size() == 6 // obd25Response size ==1
		
		Publication p = publications[0]
		assert p
		assert p.title == "Nová doba, stará zloba : soudobý antisemitismus v historickém kontextu"
		assert p.isbn == "978-80-7043-937-1"
		assert p.year == 2010
		
		assert p.main.contains(p.title)
		assert p.getExternalId() == 43885845
		
		def authors = p.authors
		assert authors
		assert authors[0].lastName == 'Arava-Novotná' 
		assert authors[0] instanceof Author
		assert authors[1] instanceof Author
		assert authors.size() == 4
	}
	
	String obd25Response = '''<zaznamy>
<zaznam>
<autori>
<autor kod="1" poradi="1">
<prijmeni>
Arava-Novotná
</prijmeni>
<jmeno>
Lena
</jmeno>
<titul_pred>
PhDr.
</titul_pred>
<titul_za/>
<kodprac/>
<kodautora/>
</autor>
<autor kod="1" poradi="2">
<prijmeni>
Budil
</prijmeni>
<jmeno>
Ivo
</jmeno>
<titul_pred>
Prof. RNDr.
</titul_pred>
<titul_za/>
<kodprac>
33130
</kodprac>
<kodautora>
11364
</kodautora>
</autor>
<autor kod="1" poradi="3">
<prijmeni>
Tydlitátová
</prijmeni>
<jmeno>
Věra
</jmeno>
<titul_pred>
Mgr.
</titul_pred>
<titul_za/>
<kodprac>
33130
</kodprac>
<kodautora>
20692
</kodautora>
</autor>
<autor kod="1" poradi="4">
<prijmeni>
Tarant
</prijmeni>
<jmeno>
Zbynek
</jmeno>
<titul_pred>
Mgr.
</titul_pred>
<titul_za/>
<kodprac>
33130
</kodprac>
<kodautora>
102470
</kodautora>
</autor>
</autori>
<ID>
43885845
</ID>
<TITUL_BEZ_CLENU>
Nová doba, stará zloba : soudobý antisemitismus v historickém kontextu
</TITUL_BEZ_CLENU>
<ROK>
2010
</ROK>
<ZDROJ/>
<NAZEV_BIBLIO/>
<PLACE_PUBLICATION>
Plzeň
</PLACE_PUBLICATION>
<ISSUE_ID/>
<ISSN_ISBN>
978-80-7043-937-1
</ISSN_ISBN>
<ZKRATKA/>
<PUBLISHER_NAME>
Západočeská univerzita
</PUBLISHER_NAME>
<PLACE_MEETING/>
<EDITION/>
<VOLUME_ID>
;
</VOLUME_ID>
<REPORT_ID/>
<STRANY/>
<LOCATION_WORK/>
<EXTENT_WORK>
195
</EXTENT_WORK>
<PACKAGING_METHOD/>
<REPRODUCTION_RATIO>
210
</REPRODUCTION_RATIO>
<DOCUMENT_TYPE>
AC
</DOCUMENT_TYPE>
<CODN/>
<MEDIUM_DESIGNATOR/>
<LETTER_TO/>
<NOTES/>
<CALL_NUMBER>
163354
</CALL_NUMBER>
<STORAGE_LOCATION/>
<AVAILABILITY/>
<CESTA/>
<DESCRIPTORS>
antisemitismus, Židé, nacismus, rasová ideologie
</DESCRIPTORS>
<VEDLEJSI_KW>
anti-Semitism, Jews, nazism, racial ideology
</VEDLEJSI_KW>
<CONNECTIV_PHRASE/>
<LANG>
cze
</LANG>
<TITUL_ORIG>
Nová doba, stará zloba : soudobý antisemitismus v historickém kontextu
</TITUL_ORIG>
<TITUL_ENG>
New Age, the Same Old Hatred : Contemporary Anti-Semitism in its Historical Context
</TITUL_ENG>
<DAT_KON/>

<SCI/>
<DATUM/>
<IDTYPPRACE>
Nevybrán
</IDTYPPRACE>
<FORMAOBD>
B - Monografie RIV
</FORMAOBD>
<UT_ISI/>
<projekty>
<projekt>
<cislo>
SVK2-2010-001
</cislo>
</projekt>
</projekty>
</zaznam>
</zaznamy>'''
String obd25Response2 = ''' <?xml version='1.0' encoding='Windows-1250' ?>          <zaznamy> <zaznam>  <autori> <autor kod="1" poradi="1"> <prijmeni>Arava-Novotná</prijmeni> <jmeno>Lena</jmeno> <titul_pred>PhDr.</titul_pred> <titul_za></titul_za> <kodprac></kodprac> <kodautora></kodautora> </autor><autor kod="1" poradi="2"> <prijmeni>Budil</prijmeni> <jmeno>Ivo</jmeno> <titul_pred>Prof. RNDr.</titul_pred> <titul_za></titul_za> <kodprac>33130</kodprac> <kodautora>11364</kodautora> </autor><autor kod="1" poradi="3"> <prijmeni>Tydlitátová</prijmeni> <jmeno>Věra</jmeno> <titul_pred>Mgr.</titul_pred> <titul_za></titul_za> <kodprac>33130</kodprac> <kodautora>20692</kodautora> </autor><autor kod="1" poradi="4"> <prijmeni>Tarant</prijmeni> <jmeno>Zbynek</jmeno> <titul_pred>Mgr.</titul_pred> <titul_za></titul_za> <kodprac>33130</kodprac> <kodautora>102470</kodautora> </autor> </autori>
 <ID>43885845</ID>
 <TITUL_BEZ_CLENU>Nová doba, stará zloba : soudobý antisemitismus v historickém kontextu</TITUL_BEZ_CLENU>
 <ROK>2010</ROK>
 <ZDROJ></ZDROJ>
 <ABSTRAKT>Výzkum antisemitismu není jen reflexí jedné staré zášti. Víme-li, že skrze antisemitismus dochází k promítání vlastních negativních charakterových vlastností na podobiznu ?Žida?, pak analýzou této podobizny můžeme zpětně odhalit to, co si naši předkové i současníci přáli ukrýt, spálit a pohřbít. Reflexe antisemitismu tak poskytuje širší kontext pro pochopení pozadí historických událostí či motivů konkrétních aktérů. Publikace autorského kolektivu Centra blízkovýchodních studií ZČU v Plzni mapuje nejstarší kořeny protižidovské nenávisti, rasové ideologie a honby za ?čistotou krve.? V dalších kapitolách pak tato kniha poukazuje na úlohu antisemitismu v revolučních proměnách Francie 19. století a problematizuje představu o antisemitismu jakožto ?iracionální zášti nevzdělanců?. V českém kontextu se pak autoři ptají, zda lze i v České republice nalézt doklady o spolupráci islamismu s neonacismem. Knihu uzavírá unikátní svědectví pamětnice, která měla možnost poznat každodenní důsledky antisemitismu během druhé světové války a poválečného období.</ABSTRAKT>
 <NAZEV_BIBLIO></NAZEV_BIBLIO>
 <PLACE_PUBLICATION>Plzeň</PLACE_PUBLICATION>
 <ISSUE_ID></ISSUE_ID>
 <ISSN_ISBN>978-80-7043-937-1</ISSN_ISBN>
 <ZKRATKA></ZKRATKA>
 <PUBLISHER_NAME>Západočeská univerzita</PUBLISHER_NAME>
 <PLACE_MEETING></PLACE_MEETING>
 <EDITION></EDITION>
 <VOLUME_ID>;</VOLUME_ID>
 <REPORT_ID></REPORT_ID>
 <STRANY></STRANY>
 <LOCATION_WORK></LOCATION_WORK>
 <EXTENT_WORK>195</EXTENT_WORK>
 <PACKAGING_METHOD></PACKAGING_METHOD>
 <REPRODUCTION_RATIO>210</REPRODUCTION_RATIO>
 <DOCUMENT_TYPE>AC</DOCUMENT_TYPE>
 <CODN></CODN>
 <MEDIUM_DESIGNATOR></MEDIUM_DESIGNATOR>
 <LETTER_TO></LETTER_TO>
 <NOTES></NOTES>
 <CALL_NUMBER>163354</CALL_NUMBER>
 <STORAGE_LOCATION></STORAGE_LOCATION>
 <AVAILABILITY></AVAILABILITY>
 <CESTA></CESTA>
 <DESCRIPTORS>antisemitismus, Židé, nacismus, rasová ideologie</DESCRIPTORS>
 <VEDLEJSI_KW>anti-Semitism, Jews, nazism, racial ideology</VEDLEJSI_KW>
 <CONNECTIV_PHRASE></CONNECTIV_PHRASE>
 <LANG>cze</LANG>
 <TITUL_ORIG>Nová doba, stará zloba : soudobý antisemitismus v historickém kontextu</TITUL_ORIG>
 <TITUL_ENG>New Age, the Same Old Hatred : Contemporary Anti-Semitism in its Historical Context</TITUL_ENG>
 <DAT_KON></DAT_KON>
 <ABSTRAKT_ENG>The content of this book, entitled New Age, the Same Old Hatred: Antisemitism in its Historical Context, is not limited to a plain reflection of the expressions of anti-Semitic tendencies. Its aim is to introduce the anti-Semitism in its wider historical context and to question some of the simplified beliefs about its nature and origin. 
The first chapter, written by Lena Arava-Novotná, helps the reader to understand the historical roots of anti-Semitism, the way it was influenced by the teaching of the Church and, at the same time, how was the anti-Semitic ideology of the Church reflected in the folk tradition and popular belief. At the end of her chapter, dr. Arava-Novotná identifies what may be a direct predecessor of the modern racial ideology. 
In the consequent chapter, prof. Ivo Budil comments on the rise and evolution of the modern racial anti-Semitism in the 19 th century Europe. Ivo Budil disputes Hannah Arendt&apos;s theses on the nature and origin of the French anti-Semitism by analyzing the theories of the 19th century racist thinkers like Eduard-Adolph Drumont or Edward Augustus Freeman in order to explain how did the utopical and palingenetical visions of these 19th century European thinkers influence the later racial ideology of the German Nazism during WW II. 

Věra Tydlitátová then doubts the widespread notion of anti-Semitism as an ?irrational hatred of non-educated societies?. Her polemical chapter asks crucial questions about the role of education in the history and impacts of anti-Semitism. How can we consider anti-Semitism ?irrational hatred? while knowing that most of the Nazis were actually graduated? Tydlitátová warns, together with Zygmunt Bauman or Yehuda Bauer, that academic education does not prevent anti-Semitism at all. On the contrary ? knowledge and intelligence can make
it even more sofisticated and thus more dangerous. 

In the following chapter, Zbyněk Tarant examines the hypothesis of the alleged connection between neo-Nazis and muslim activists in the Czech republic. By content-analysis of both neo-Nazi and muslim websites and by investigating selected particular events, Tarant offers an insight to the question of neo-Nazi vs. Islamist interaction, which is far more complex than it may look like at the first glance.

The last chapter is a WW II memoir of a Polish woman. Included into this book as an intensive case-study, the breathtaking life story of Marie Pauerová allows us to understand the real, day-to-day impacts of anti-Semitism more closely. This is not a typical holocaust survivor story. The narrative of a woman whose only ?crime? was that
she married a Jew questions the basic constructs of identity in the war struck central Europe.</ABSTRAKT_ENG>
 <SCI></SCI>
 <DATUM></DATUM>
 <IDTYPPRACE>Nevybrán</IDTYPPRACE>
 <FORMAOBD>B - Monografie RIV</FORMAOBD>
 <ABSTRAKT_CZE>Výzkum antisemitismu není jen reflexí jedné staré zášti. Víme-li, že skrze antisemitismus dochází k promítání vlastních negativních charakterových vlastností na podobiznu ?Žida?, pak analýzou této podobizny můžeme zpětně odhalit to, co si naši předkové i současníci přáli ukrýt, spálit a pohřbít. Reflexe antisemitismu tak poskytuje širší kontext pro pochopení pozadí historických událostí či motivů konkrétních aktérů. Publikace autorského kolektivu Centra blízkovýchodních studií ZČU v Plzni mapuje nejstarší kořeny protižidovské nenávisti, rasové ideologie a honby za ?čistotou krve.? V dalších kapitolách pak tato kniha poukazuje na úlohu antisemitismu v revolučních proměnách Francie 19. století a problematizuje představu o antisemitismu jakožto ?iracionální zášti nevzdělanců?. V českém kontextu se pak autoři ptají, zda lze i v České republice nalézt doklady o spolupráci islamismu s neonacismem. Knihu uzavírá unikátní svědectví pamětnice, která měla možnost poznat každodenní důsledky antisemitismu během druhé světové války a poválečného období.</ABSTRAKT_CZE>
 <UT_ISI></UT_ISI>
 <projekty> <projekt> <cislo>SVK2-2010-001</cislo> </projekt> </projekty>
  </zaznam><zaznam>  <autori> <autor kod="1" poradi="1"> <prijmeni>Tydlitátová</prijmeni> <jmeno>Věra</jmeno> <titul_pred></titul_pred> <titul_za></titul_za> <kodprac>33190</kodprac> <kodautora>20692</kodautora> </autor> </autori>
 <ID>63520</ID>
 <TITUL_BEZ_CLENU>Jakou odpovědnost si zvolíme?</TITUL_BEZ_CLENU>
 <ROK>2009</ROK>
 <ZDROJ></ZDROJ>
 <ABSTRAKT></ABSTRAKT>
 <NAZEV_BIBLIO>Maskil</NAZEV_BIBLIO>
 <PLACE_PUBLICATION></PLACE_PUBLICATION>
 <ISSUE_ID>4</ISSUE_ID>
 <ISSN_ISBN>ISSN E14877</ISSN_ISBN>
 <ZKRATKA></ZKRATKA>
 <PUBLISHER_NAME></PUBLISHER_NAME>
 <PLACE_MEETING></PLACE_MEETING>
 <EDITION>4</EDITION>
 <VOLUME_ID>8</VOLUME_ID>
 <REPORT_ID></REPORT_ID>
 <STRANY>11-11</STRANY>
 <LOCATION_WORK></LOCATION_WORK>
 <EXTENT_WORK></EXTENT_WORK>
 <PACKAGING_METHOD>TI</PACKAGING_METHOD>
 <REPRODUCTION_RATIO></REPRODUCTION_RATIO>
 <DOCUMENT_TYPE></DOCUMENT_TYPE>
 <CODN></CODN>
 <MEDIUM_DESIGNATOR></MEDIUM_DESIGNATOR>
 <LETTER_TO></LETTER_TO>
 <NOTES></NOTES>
 <CALL_NUMBER>139415</CALL_NUMBER>
 <STORAGE_LOCATION></STORAGE_LOCATION>
 <AVAILABILITY>http://www.maskil.cz/5769-4/index.htm</AVAILABILITY>
 <CESTA></CESTA>
 <DESCRIPTORS></DESCRIPTORS>
 <VEDLEJSI_KW></VEDLEJSI_KW>
 <CONNECTIV_PHRASE></CONNECTIV_PHRASE>
 <LANG></LANG>
 <TITUL_ORIG></TITUL_ORIG>
 <TITUL_ENG></TITUL_ENG>
 <DAT_KON></DAT_KON>
 <ABSTRAKT_ENG></ABSTRAKT_ENG>
 <SCI>0</SCI>
 <DATUM></DATUM>
 <IDTYPPRACE>Článek/Popularizační článek</IDTYPPRACE>
 <FORMAOBD>Články z novin, časopisů</FORMAOBD>
 <ABSTRAKT_CZE></ABSTRAKT_CZE>
 <UT_ISI></UT_ISI>
 <projekty>  </projekty>
  </zaznam><zaznam>  <autori> <autor kod="1" poradi="1"> <prijmeni>Tydlitátová</prijmeni> <jmeno>Věra</jmeno> <titul_pred>Mgr.</titul_pred> <titul_za></titul_za> <kodprac>33130</kodprac> <kodautora>20692</kodautora> </autor> </autori>
 <ID>43866056</ID>
 <TITUL_BEZ_CLENU>Anti-Semitism as an Instrument of the Last Election Campaign in the Czech Republic</TITUL_BEZ_CLENU>
 <ROK>2009</ROK>
 <ZDROJ>Reflections on Anti-Semitism: Anti-Semitism in historical and anthropological perspectives . 2009;1:135-136</ZDROJ>
 <ABSTRAKT>The great problem of young Czech democracy is its relation between liberty and civic responsibility. A new and difficult question is populism in our political life. This could be seen in the last election campaign in October 2008. Many local politicians and political parties have used arguments from the field of xenophobia, racism, anit-semitism and intolerance.</ABSTRAKT>
 <NAZEV_BIBLIO>Reflections on Anti-Semitism</NAZEV_BIBLIO>
 <PLACE_PUBLICATION>V Plzni</PLACE_PUBLICATION>
 <ISSUE_ID></ISSUE_ID>
 <ISSN_ISBN>978-80-7043-808-4</ISSN_ISBN>
 <ZKRATKA></ZKRATKA>
 <PUBLISHER_NAME>Západočeská univerzita</PUBLISHER_NAME>
 <PLACE_MEETING></PLACE_MEETING>
 <EDITION>1.</EDITION>
 <VOLUME_ID>;</VOLUME_ID>
 <REPORT_ID>138</REPORT_ID>
 <STRANY>135-136</STRANY>
 <LOCATION_WORK></LOCATION_WORK>
 <EXTENT_WORK>2</EXTENT_WORK>
 <PACKAGING_METHOD></PACKAGING_METHOD>
 <REPRODUCTION_RATIO>200</REPRODUCTION_RATIO>
 <DOCUMENT_TYPE>AD</DOCUMENT_TYPE>
 <CODN></CODN>
 <MEDIUM_DESIGNATOR></MEDIUM_DESIGNATOR>
 <LETTER_TO>anti-semitism in historical and anthropological perspectives</LETTER_TO>
 <NOTES></NOTES>
 <CALL_NUMBER>149316</CALL_NUMBER>
 <STORAGE_LOCATION></STORAGE_LOCATION>
 <AVAILABILITY></AVAILABILITY>
 <CESTA></CESTA>
 <DESCRIPTORS>volby, populismus, antisemitismus, rasismus, xenofobie, politické strany</DESCRIPTORS>
 <VEDLEJSI_KW>Anti-Semitism, election, populsim, rasism, xenophobia, political parties</VEDLEJSI_KW>
 <CONNECTIV_PHRASE></CONNECTIV_PHRASE>
 <LANG>eng</LANG>
 <TITUL_ORIG>Anti-semitismus jako nástroj poslední volební kampaně v České republice</TITUL_ORIG>
 <TITUL_ENG>Anti-Semitism as an Instrument of the Last Election Campaign in the Czech Republic</TITUL_ENG>
 <DAT_KON></DAT_KON>
 <ABSTRAKT_ENG>The great problem of young Czech democracy is its relation between liberty and civic responsibility. A new and difficult question is populism in our political life. This could be seen in the last election campaign in October 2008. Many local politicians and political parties have used arguments from the field of xenophobia, racism, anit-semitism and intolerance.</ABSTRAKT_ENG>
 <SCI></SCI>
 <DATUM></DATUM>
 <IDTYPPRACE>Nevybrán</IDTYPPRACE>
 <FORMAOBD>C - Kapitoly v knize RIV</FORMAOBD>
 <ABSTRAKT_CZE>Velkým problémem mladé české demokracie je její vztah mezi svobodou a občanskou odpovědností. Novou a obtížnou otázkou je populismus v našem politickém životě. To jsme mohli pozorovat při poslední volební kampani v říjnu 2008. Mnoho místních politiků a politické strany někdy argumentovaly termíny z oblasti xenofobie, rasismu, anti-semitismu a intolerance.</ABSTRAKT_CZE>
 <UT_ISI></UT_ISI>
 <projekty>  </projekty>
  </zaznam><zaznam>  <autori> <autor kod="1" poradi="1"> <prijmeni>Tydlitátová</prijmeni> <jmeno>Věra</jmeno> <titul_pred>Mgr.</titul_pred> <titul_za></titul_za> <kodprac>33130</kodprac> <kodautora>20692</kodautora> </autor> </autori>
 <ID>43886974</ID>
 <TITUL_BEZ_CLENU>Dějiny přemýšlení o náboženství a víře</TITUL_BEZ_CLENU>
 <ROK>2010</ROK>
 <ZDROJ></ZDROJ>
 <ABSTRAKT>Publikace zabývající se dějinami religionistiky a filozofie náboženství z různých aspektů pohledu.</ABSTRAKT>
 <NAZEV_BIBLIO></NAZEV_BIBLIO>
 <PLACE_PUBLICATION>Plzeň</PLACE_PUBLICATION>
 <ISSUE_ID></ISSUE_ID>
 <ISSN_ISBN>978-80-7043-939-5</ISSN_ISBN>
 <ZKRATKA></ZKRATKA>
 <PUBLISHER_NAME>Západočeská univerzita</PUBLISHER_NAME>
 <PLACE_MEETING></PLACE_MEETING>
 <EDITION>2010</EDITION>
 <VOLUME_ID>;</VOLUME_ID>
 <REPORT_ID></REPORT_ID>
 <STRANY></STRANY>
 <LOCATION_WORK></LOCATION_WORK>
 <EXTENT_WORK>211</EXTENT_WORK>
 <PACKAGING_METHOD></PACKAGING_METHOD>
 <REPRODUCTION_RATIO>210</REPRODUCTION_RATIO>
 <DOCUMENT_TYPE>AA</DOCUMENT_TYPE>
 <CODN></CODN>
 <MEDIUM_DESIGNATOR></MEDIUM_DESIGNATOR>
 <LETTER_TO></LETTER_TO>
 <NOTES></NOTES>
 <CALL_NUMBER>163275</CALL_NUMBER>
 <STORAGE_LOCATION></STORAGE_LOCATION>
 <AVAILABILITY></AVAILABILITY>
 <CESTA></CESTA>
 <DESCRIPTORS>Religionistika, Filozofie, Dějiny, Náboženství</DESCRIPTORS>
 <VEDLEJSI_KW>History of Religion, Philosophy, History, Religion</VEDLEJSI_KW>
 <CONNECTIV_PHRASE></CONNECTIV_PHRASE>
 <LANG>cze</LANG>
 <TITUL_ORIG>Dějiny přemýšlení o náboženství a víře</TITUL_ORIG>
 <TITUL_ENG>History of Thinking about Religion and Faith</TITUL_ENG>
 <DAT_KON></DAT_KON>
 <ABSTRAKT_ENG>This monography treats of the history and philosophy of religion from various aspects of human perspective.</ABSTRAKT_ENG>
 <SCI></SCI>
 <DATUM></DATUM>
 <IDTYPPRACE>Nevybrán</IDTYPPRACE>
 <FORMAOBD>B - Monografie RIV</FORMAOBD>
 <ABSTRAKT_CZE>Publikace zabývající se dějinami religionistiky a filozofie náboženství z různých aspektů pohledu.</ABSTRAKT_CZE>
 <UT_ISI></UT_ISI>
 <projekty>  </projekty>
  </zaznam><zaznam>  <autori> <autor kod="1" poradi="1"> <prijmeni>Tydlitátová</prijmeni> <jmeno>Věra</jmeno> <titul_pred>Mgr.</titul_pred> <titul_za></titul_za> <kodprac>33130</kodprac> <kodautora>20692</kodautora> </autor> </autori>
 <ID>43872571</ID>
 <TITUL_BEZ_CLENU>Revival of Religious Anti-Judaism in the Post-Modern Spectrum of Ideologies</TITUL_BEZ_CLENU>
 <ROK>2009</ROK>
 <ZDROJ></ZDROJ>
 <ABSTRAKT>These study attest to an alliance between apolitical ultra-traditionalist views of the world as represented by the Society of St. Pius X or also by conservative nd fundamentalist believers on the one hand, and strongly political, active and extreme inclination to neo-Nazism, including its violent anti-biblical, and anti-humanist accents on the other hand.</ABSTRAKT>
 <NAZEV_BIBLIO>Anatomy of Hatred : Essays on Anti-Semitism</NAZEV_BIBLIO>
 <PLACE_PUBLICATION>Plzeň</PLACE_PUBLICATION>
 <ISSUE_ID></ISSUE_ID>
 <ISSN_ISBN>978-80-7043-861-9</ISSN_ISBN>
 <ZKRATKA></ZKRATKA>
 <PUBLISHER_NAME>Západočeská univerzita</PUBLISHER_NAME>
 <PLACE_MEETING></PLACE_MEETING>
 <EDITION>1.</EDITION>
 <VOLUME_ID>;</VOLUME_ID>
 <REPORT_ID>113</REPORT_ID>
 <STRANY>71-76</STRANY>
 <LOCATION_WORK></LOCATION_WORK>
 <EXTENT_WORK>5</EXTENT_WORK>
 <PACKAGING_METHOD></PACKAGING_METHOD>
 <REPRODUCTION_RATIO>210</REPRODUCTION_RATIO>
 <DOCUMENT_TYPE>AB</DOCUMENT_TYPE>
 <CODN></CODN>
 <MEDIUM_DESIGNATOR></MEDIUM_DESIGNATOR>
 <LETTER_TO></LETTER_TO>
 <NOTES></NOTES>
 <CALL_NUMBER>152052</CALL_NUMBER>
 <STORAGE_LOCATION></STORAGE_LOCATION>
 <AVAILABILITY></AVAILABILITY>
 <CESTA></CESTA>
 <DESCRIPTORS>Antisemitismus, antijudaismus, katolicismus, ortodoxie, fundamentalismus, neonacismus, extremismus</DESCRIPTORS>
 <VEDLEJSI_KW>Anti-Semitism, Anti-Judaism, Catholicism, Orthodoxy, Fundamentalism,</VEDLEJSI_KW>
 <CONNECTIV_PHRASE></CONNECTIV_PHRASE>
 <LANG>eng</LANG>
 <TITUL_ORIG>Oživení náboženského antijudaismu v postmoderním spektru ideologií</TITUL_ORIG>
 <TITUL_ENG>Revival of Religious Anti-Judaism in the Post-Modern Spectrum of Ideologies</TITUL_ENG>
 <DAT_KON></DAT_KON>
 <ABSTRAKT_ENG>These study attest to an alliance between apolitical ultra-traditionalist views of the world as represented by the Society of St. Pius X or also by conservative nd fundamentalist believers on the one hand, and strongly political, active and extreme inclination to neo-Nazism, including its violent anti-biblical, and anti-humanist accents on the other hand.</ABSTRAKT_ENG>
 <SCI></SCI>
 <DATUM></DATUM>
 <IDTYPPRACE>Nevybrán</IDTYPPRACE>
 <FORMAOBD>C - Kapitoly v knize RIV</FORMAOBD>
 <ABSTRAKT_CZE>Tato studie ukazuje sblížení mezi apolitickým ultratradicionalistickým pohledem na svět jak je reprezentovaný Kněžským bratrstvem Sv. Pia X, nebo také konzervativními a fundamentalistickými věřícími na jedné straně a ryze politicky zaměřenými extremisty včetně neonacistů a to i jejich násilného protibiblického a antihumanistického zaměření na druhé straně.</ABSTRAKT_CZE>
 <UT_ISI></UT_ISI>
 <projekty>  </projekty>
  </zaznam><zaznam>  <autori> <autor kod="1" poradi="1"> <prijmeni>Tydlitátová</prijmeni> <jmeno>Věra</jmeno> <titul_pred>Mgr.</titul_pred> <titul_za></titul_za> <kodprac>33130</kodprac> <kodautora>20692</kodautora> </autor> </autori>
 <ID>43873089</ID>
 <TITUL_BEZ_CLENU>Symbolika hradeb a bran v hebrejské bibli</TITUL_BEZ_CLENU>
 <ROK>2010</ROK>
 <ZDROJ></ZDROJ>
 <ABSTRAKT>Monografie se zabývá významem opevnění a jeho stavebních prvků v souvislosti s religionistickým zkoumáním funkce předělu mezi posvátným a profánním prostorem.</ABSTRAKT>
 <NAZEV_BIBLIO></NAZEV_BIBLIO>
 <PLACE_PUBLICATION>Praha</PLACE_PUBLICATION>
 <ISSUE_ID></ISSUE_ID>
 <ISSN_ISBN>978-80-7387-305-9</ISSN_ISBN>
 <ZKRATKA></ZKRATKA>
 <PUBLISHER_NAME>TRITON</PUBLISHER_NAME>
 <PLACE_MEETING></PLACE_MEETING>
 <EDITION>1.</EDITION>
 <VOLUME_ID>;</VOLUME_ID>
 <REPORT_ID></REPORT_ID>
 <STRANY></STRANY>
 <LOCATION_WORK></LOCATION_WORK>
 <EXTENT_WORK>178</EXTENT_WORK>
 <PACKAGING_METHOD></PACKAGING_METHOD>
 <REPRODUCTION_RATIO>500</REPRODUCTION_RATIO>
 <DOCUMENT_TYPE>AA</DOCUMENT_TYPE>
 <CODN></CODN>
 <MEDIUM_DESIGNATOR></MEDIUM_DESIGNATOR>
 <LETTER_TO></LETTER_TO>
 <NOTES></NOTES>
 <CALL_NUMBER>152182</CALL_NUMBER>
 <STORAGE_LOCATION></STORAGE_LOCATION>
 <AVAILABILITY></AVAILABILITY>
 <CESTA></CESTA>
 <DESCRIPTORS>bible, Starý zákon, město, hradby, brány</DESCRIPTORS>
 <VEDLEJSI_KW>Bible, Old testament, town, walls, gates</VEDLEJSI_KW>
 <CONNECTIV_PHRASE></CONNECTIV_PHRASE>
 <LANG>cze</LANG>
 <TITUL_ORIG>Symbolika hradeb a bran v hebrejské bibli</TITUL_ORIG>
 <TITUL_ENG>Symbolism of walls and gates in the Hebrew Bible</TITUL_ENG>
 <DAT_KON></DAT_KON>
 <ABSTRAKT_ENG>The Monography deals with the meaning of fortification and its architectural elements in relation with research of function of border between sacral and prophane space in perspective of history of religion.</ABSTRAKT_ENG>
 <SCI></SCI>
 <DATUM></DATUM>
 <IDTYPPRACE>Nevybrán</IDTYPPRACE>
 <FORMAOBD>B - Monografie RIV</FORMAOBD>
 <ABSTRAKT_CZE>Monografie se zabývá významem opevnění a jeho stavebních prvků v souvislosti s religionistickým zkoumáním funkce předělu mezi posvátným a profánním prostorem.</ABSTRAKT_CZE>
 <UT_ISI></UT_ISI>
 <projekty>  </projekty>
  </zaznam> </zaznamy>'''
}
