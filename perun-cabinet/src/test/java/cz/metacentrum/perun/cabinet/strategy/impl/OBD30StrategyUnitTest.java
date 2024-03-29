package cz.metacentrum.perun.cabinet.strategy.impl;

import cz.metacentrum.perun.cabinet.model.Publication;
import java.util.List;
import org.junit.Test;

/**
 * Unit test for OBD 3.0 PublicationSystem strategy
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class OBD30StrategyUnitTest {

  private static final String OBD_30_RESPONSE =
      "<zaznamy><zaznam id=\"43884012\"><autor_list><autor><typ>Autor</typ><poradi>1</poradi><prijmeni>Dostal" +
      "</prijmeni><jmeno>Martin</jmeno><titul_pred>Ing" +
      ".</titul_pred><titul_za/><mentalni_podil/><garant>Ano</garant><jr>Ne</jr><zahr>Ne</zahr><stat/><kodautora" +
      ">14937</kodautora><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka>FAV" +
      "</fakulta_zkratka><dom>1</dom><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní " +
      "techniky</popis_prac><popis_fakulta>Katedra informatiky a výpočetní " +
      "techniky</popis_fakulta></autor_prac></autor><autor><typ>Autor</typ><poradi>2</poradi><prijmeni>Krčm" +
      "ář</prijmeni><jmeno>Lubomír</jmeno><titul_pred>Ing" +
      ".</titul_pred><titul_za/><mentalni_podil/><garant>Ne</garant><jr>Ne</jr><zahr>Ne</zahr><stat/><kodautora>14940" +
      "</kodautora><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka>FAV</fakulta_zkratka" +
      "><dom>1</dom><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní " +
      "techniky</popis_prac><popis_fakulta>Katedra informatiky a výpočetní " +
      "techniky</popis_fakulta></autor_prac></autor><autor><typ>Autor</typ><poradi>3</poradi><prijmeni>Ježek" +
      "</prijmeni><jmeno>Karel</jmeno><titul_pred>Doc. Ing.</titul_pred><titul_za>CSc" +
      ".</titul_za><mentalni_podil/><garant>Ne</garant><jr>Ne</jr><zahr>Ne</zahr><stat/><kodautora>10294</kodautora" +
      "><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka>FAV</fakulta_zkratka><dom>1</dom" +
      "><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní techniky</popis_prac><popis_fakulta>Katedra " +
      "informatiky a výpočetní techniky</popis_fakulta></autor_prac></autor></autor_list><titul_list><titul><nazev" +
      ">Extrakce informací z emailů typu Call for " +
      "papers</nazev><jazyk>cze</jazyk><original>Ano</original><abstrakt>Každý den dostává většina akademických " +
      "pracovníků množství emailů typu Call for papers (CFP), neboli oznámení o konferencích. Organizace těchto " +
      "emailů zabírá stále více času a aktualizace údajů v kalendáři je často více než náročná. V rámci tohoto článku" +
      " typu Work in progress bychom chtěli představit systém pro extrakci informací z těchto oznámení. Pro dolování " +
      "informací využíváme sadu jednoduchých, ale efektivních technik v nevšedním pojetí. Jde např. o extrakci " +
      "informací (EI) na základě n-gramů, nebo s využitím vlastní implementace webového rozhraní k populárnímu " +
      "nástroji GATE.</abstrakt><klicova_slova>extrakce informací, klasfifikace, web 2" +
      ".0</klicova_slova></titul><titul><nazev>Information extraction from call for papers " +
      "emails</nazev><jazyk>eng</jazyk><original>Ne</original><abstrakt>There is a lot of Call for papers (CFP) " +
      "emails received by academic workers every day. Ordering of these emails takes more and more time. Also " +
      "calendar updating is often more than demanding. In this paper (Work in progress kind) we would like to " +
      "introduce the system for information extrakcion from these Call fro papers announcements. We have described " +
      "many simple but effective technics in uncommon conception. Our approach for information excraction (IE) is " +
      "based on N-grams and GATE.</abstrakt><klicova_slova>information extraction, classification, web 2" +
      ".0</klicova_slova></titul></titul_list><odkaz_list>  " +
      "</odkaz_list><issn/><isbn>978-80-970179-3-4</isbn><typ_vlastni_prace_list>  " +
      "</typ_vlastni_prace_list><neuplat>Ne</neuplat><poznamka/><id_riv>503958</id_riv><zdroj_nazev>Informačné " +
      "Technológie - Aplikácie a Teória</zdroj_nazev><zdroj_zkratka/><konani_misto>Smrekovica</konani_misto><vydani" +
      "/><vydavatel_mesto>Seňa</vydavatel_mesto><rocnik/><zprava_cislo/><cislo/><strany>47-52</strany><prace_misto" +
      "/><prace_rozsah>6</prace_rozsah><baleni>sborník</baleni><naklad/><document_typ/><codn>EUR</codn><medium>21.09" +
      ".2010</medium><dopis_komu/><konference_nazev/><konf_ucastnici/><konf_zahr_ucastnici/><konference_typ" +
      "/><konference_stat/><ut_isi/><rok>2010</rok><vydavatel_nazev>PONT s.r.o. " +
      "Seňa</vydavatel_nazev><impakt_faktor/><dat_kon_zacatek/><dat_kon_konec>25.09" +
      ".2010</dat_kon_konec><archiv_cislo>163666</archiv_cislo><umisteni/><dostupnost/><sci/><spojka/><stat>SK</stat" +
      "><literarni_forma>Zaniklé typy</literarni_forma><rozsireni_literarni_formy>Ostatní (O)" +
      "</rozsireni_literarni_formy><stav>Publikovaný</stav></zaznam><zaznam " +
      "id=\"43894249\"><autor_list><autor><typ>Autor</typ><poradi>1</poradi><prijmeni>Ekštein</prijmeni><jmeno>Kamil" +
      "</jmeno><titul_pred>Ing.</titul_pred><titul_za>Ph.D" +
      ".</titul_za><mentalni_podil/><garant>Ano</garant><jr>Ne</jr><zahr>Ne</zahr><stat/><kodautora>100002</kodautora" +
      "><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka>FAV</fakulta_zkratka><dom>1</dom" +
      "><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní techniky</popis_prac><popis_fakulta>Katedra " +
      "informatiky a výpočetní techniky</popis_fakulta></autor_prac></autor><autor><typ>Autor</typ><poradi>2</poradi" +
      "><prijmeni>Krčmář</prijmeni><jmeno>Lubomír</jmeno><titul_pred>Ing" +
      ".</titul_pred><titul_za/><mentalni_podil/><garant>Ne</garant><jr>Ne</jr><zahr>Ne</zahr><stat/><kodautora>14940" +
      "</kodautora><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka>FAV</fakulta_zkratka" +
      "><dom>1</dom><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní " +
      "techniky</popis_prac><popis_fakulta>Katedra informatiky a výpočetní " +
      "techniky</popis_fakulta></autor_prac></autor></autor_list><titul_list><titul><nazev>Automatická detekce " +
      "synonym (za účelem rozšíření prohledávaného prostoru) pomocí " +
      "LSA</nazev><jazyk>cze</jazyk><original>Ne</original><abstrakt>Tento článek popisuje výzkum, experimenty a " +
      "teoretické úvahy vedoucí k automatické tvorbě thesauru (slovníku synonym) pomocí počítače na základě " +
      "identifikace synonym v rozsáhlých kolekcích textů pro potřeby systémů zodpovídání dotazů. Metoda je postavena " +
      "na technice latentní sémantické analýzy, která slouží jako generátor hypotéz o synonymitě" +
      ".</abstrakt><klicova_slova>synonyma, identifikace synonym, thesandrus, vyhledávání informací, hledání na webu," +
      " zodpovídání dotazů</klicova_slova></titul><titul><nazev>Automatic LSA-based Retrieval of Synonyms (for Search" +
      " Space Extension)</nazev><jazyk>eng</jazyk><original>Ano</original><abstrakt>This paper describes a research, " +
      "experiments, and theoretical considerations leading towards automatic computational thesaurus construction " +
      "based upon identification of synonyms in large sets of texts for the needs of question-answering systems. The " +
      "method is founded on Latent Semantic Analysis technique which serves as a synonymy hypothesis generator" +
      ".</abstrakt><klicova_slova>synonyms, synonym identification, thesaurus, information retrieval, web search, " +
      "question answering</klicova_slova></titul></titul_list><odkaz_list><odkaz><url/><popis/></odkaz></odkaz_list" +
      "><issn/><isbn>978-1-4244-8581-9</isbn><typ_vlastni_prace_list><typ_vlastni_prace><fakulta_soucast>ZČU v " +
      "Plzni</fakulta_soucast><tvp_nazev>Sborník z " +
      "konference</tvp_nazev></typ_vlastni_prace></typ_vlastni_prace_list><neuplat>Ne</neuplat><poznamka>MSM " +
      "6383917201 /MetaCenrum/</poznamka><id_riv>43897498</id_riv><zdroj_nazev>2011 Firts IRAST International " +
      "Conference on Data Engineering and Internet Technology (DEIT)</zdroj_nazev><zdroj_zkratka/><konani_misto>Bali " +
      "Dynastry Resort, Bali, Indonesia</konani_misto><vydani/><vydavatel_mesto>New " +
      "York</vydavatel_mesto><rocnik/><zprava_cislo/><cislo/><strany>630-633</strany><prace_misto/><prace_rozsah>4" +
      "</prace_rozsah><baleni>CD ROM</baleni><naklad/><document_typ/><codn/><medium>20110315</medium><dopis_komu" +
      "/><konference_nazev>The 2011 Firt IRAST International Conference on Data Engineering and Internet Technology " +
      "DEIT 2011</konference_nazev><konf_ucastnici/><konf_zahr_ucastnici/><konference_typ>WRD</konference_typ" +
      "><konference_stat/><ut_isi/><rok>2011</rok><vydavatel_nazev>IEEE</vydavatel_nazev><impakt_faktor" +
      "/><dat_kon_zacatek>15.03.2011</dat_kon_zacatek><dat_kon_konec>17.03" +
      ".2011</dat_kon_konec><archiv_cislo>175371</archiv_cislo><umisteni/><dostupnost/><sci/><spojka/><stat>US</stat" +
      "><literarni_forma>STAŤ VE SBORNÍKU</literarni_forma><rozsireni_literarni_formy>Stať ve sborníku (D)" +
      "</rozsireni_literarni_formy><stav>Publikovaný</stav></zaznam><zaznam " +
      "id=\"43894274\"><autor_list><autor><typ>Autor</typ><poradi>1</poradi><prijmeni>Krčmář</prijmeni><jmeno>Lubomír" +
      "</jmeno><titul_pred>Ing.</titul_pred><titul_za/><mentalni_podil/><garant>Ano</garant><jr>Ne</jr><zahr>Ne</zahr" +
      "><stat/><kodautora>14940</kodautora><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka" +
      ">FAV</fakulta_zkratka><dom>1</dom><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní " +
      "techniky</popis_prac><popis_fakulta>Katedra informatiky a výpočetní " +
      "techniky</popis_fakulta></autor_prac></autor><autor><typ>Autor</typ><poradi>2</poradi><prijmeni>Konopík" +
      "</prijmeni><jmeno>Miloslav</jmeno><titul_pred>Ing.</titul_pred><titul_za>Ph.D" +
      ".</titul_za><mentalni_podil/><garant>Ne</garant><jr>Ne</jr><zahr>Ne</zahr><stat/><kodautora>100457</kodautora" +
      "><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka>FAV</fakulta_zkratka><dom>1</dom" +
      "><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní techniky</popis_prac><popis_fakulta>Katedra " +
      "informatiky a výpočetní techniky</popis_fakulta></autor_prac></autor><autor><typ>Autor</typ><poradi>3</poradi" +
      "><prijmeni>Ježek</prijmeni><jmeno>Karel</jmeno><titul_pred>Doc. Ing.</titul_pred><titul_za>CSc" +
      ".</titul_za><mentalni_podil/><garant>Ne</garant><jr>Ne</jr><zahr>Ne</zahr><stat/><kodautora>10294</kodautora" +
      "><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka>FAV</fakulta_zkratka><dom>1</dom" +
      "><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní techniky</popis_prac><popis_fakulta>Katedra " +
      "informatiky a výpočetní techniky</popis_fakulta></autor_prac></autor></autor_list><titul_list><titul><nazev" +
      ">Zkoumání sémantických prostorů získaných z českého " +
      "korpusu</nazev><jazyk>cze</jazyk><original>Ne</original><abstrakt>Tento článek je zaměřen na sémantické vztahy" +
      " mezi českými slovy, tyto vztahy byly získány z novinových článků pomocé LSA, HAL a COALS. Vypočtené vztahy " +
      "mezi slovy byly vyhodnoceny využitím českého ekvivalentu Rubenstein-Goodenough testu. Výsledky našich " +
      "experimentů mohou sloužit jako vodítko, jestli algoritmy původně vyvinuté pro angličtinu dají použít pro české" +
      " texty.</abstrakt><klicova_slova>získávání informací, sémantický prostor, LSA, HAL, COALS, " +
      "Rubenstein-goodenough test</klicova_slova></titul><titul><nazev>Exploration of Semantic Spaces Obtained from " +
      "Czech Corpora</nazev><jazyk>eng</jazyk><original>Ano</original><abstrakt>This paper is focused in semantic " +
      "relations between czech words. We obtained these relations from newspaper articles with the help of LSA, HAL " +
      "nad COALS. The computer relations between words were evaluated using the czech equivalent of the " +
      "Rubenstein-Goodenough test - the resalts of our experiments can serve as the clue whether the algoriths " +
      "originally developed for english can be also used for czech texts.</abstrakt><klicova_slova>information " +
      "retrieval, semantic space, LSA, HAL, COALS, Rubenstein-Goodenough " +
      "test</klicova_slova></titul></titul_list><odkaz_list><odkaz><url/><popis/></odkaz></odkaz_list><issn/><isbn" +
      ">978-80-248-2391-1</isbn><typ_vlastni_prace_list><typ_vlastni_prace><fakulta_soucast>ZČU v " +
      "Plzni</fakulta_soucast><tvp_nazev>Sborník z " +
      "konference</tvp_nazev></typ_vlastni_prace></typ_vlastni_prace_list><neuplat>Ne</neuplat><poznamka>MSM " +
      "6383917201 CESNET</poznamka><id_riv>43898647</id_riv><zdroj_nazev>DATESO " +
      "2011</zdroj_nazev><zdroj_zkratka/><konani_misto>Písek</konani_misto><vydani/><vydavatel_mesto>Ostrava" +
      "</vydavatel_mesto><rocnik/><zprava_cislo/><cislo/><strany>97-107</strany><prace_misto/><prace_rozsah>11" +
      "</prace_rozsah><baleni/><naklad/><document_typ/><codn/><medium>20110420</medium><dopis_komu/><konference_nazev" +
      ">DATESO 2011</konference_nazev><konf_ucastnici/><konf_zahr_ucastnici/><konference_typ>EUR</konference_typ" +
      "><konference_stat/><ut_isi/><rok>2011</rok><vydavatel_nazev>VŠB " +
      "Ostrava</vydavatel_nazev><impakt_faktor/><dat_kon_zacatek>20.04.2011</dat_kon_zacatek><dat_kon_konec>22.04" +
      ".2011</dat_kon_konec><archiv_cislo>175384</archiv_cislo><umisteni/><dostupnost/><sci/><spojka/><stat>CZ</stat" +
      "><literarni_forma>STAŤ VE SBORNÍKU</literarni_forma><rozsireni_literarni_formy>Stať ve sborníku (D)" +
      "</rozsireni_literarni_formy><stav>Publikovaný</stav></zaznam><zaznam " +
      "id=\"43899184\"><autor_list><autor><typ>Autor</typ><poradi>1</poradi><prijmeni>Krčmář</prijmeni><jmeno>Lubomír" +
      "</jmeno><titul_pred>Ing.</titul_pred><titul_za/><mentalni_podil/><garant>Ano</garant><jr>Ne</jr><zahr>Ne</zahr" +
      "><stat/><kodautora>14940</kodautora><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka" +
      ">FAV</fakulta_zkratka><dom>1</dom><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní " +
      "techniky</popis_prac><popis_fakulta>Fakulta aplikovaných " +
      "věd</popis_fakulta></autor_prac></autor><autor><typ>Autor</typ><poradi>2</poradi><prijmeni>Ježek</prijmeni" +
      "><jmeno>Karel</jmeno><titul_pred>Prof. Ing.</titul_pred><titul_za>CSc" +
      ".</titul_za><mentalni_podil/><garant>Ne</garant><jr>Ne</jr><zahr>Ne</zahr><stat/><kodautora>10294</kodautora" +
      "><autor_prac><kodprac>52150</kodprac><zkratka>KIV</zkratka><fakulta_zkratka>FAV</fakulta_zkratka><dom>1</dom" +
      "><hodn>1</hodn><popis_prac>Katedra informatiky a výpočetní techniky</popis_prac><popis_fakulta>Fakulta " +
      "aplikovaných věd</popis_fakulta></autor_prac></autor><autor><typ>Autor</typ><poradi>3</poradi><prijmeni>Poesio" +
      "</prijmeni><jmeno>Massimo</jmeno><titul_pred/><titul_za/><mentalni_podil/><garant>Ne</garant><jr>Ne</jr><zahr" +
      ">Ano</zahr><stat>GB</stat><kodautora/></autor></autor_list><titul_list><titul><nazev>Detekce sémantické " +
      "složitelnosti využitím sémantických prostorů</nazev><jazyk>cze</jazyk><original>Ne</original><abstrakt>Každý " +
      "systém zpracovávající přirozený jazyk, který bere v úvahu význam textu, spoléhá na předpoklad sémantické " +
      "složitelnosti: význam složeniny je určen významem částí této složeniny a jejich kombinováním. Předpoklad " +
      "sémantické složitelnosti však neplatí pro mnoho idiomatických výrazů jako je &quot;blue chip&quot;. Tento " +
      "článek se zaměřuje na plně automatickou detekci těchto, dále nazývaných nesložitelné, složenin. Navrhli a " +
      "otestovali jsme intuitivní přístup založený na nahrazování částí složenin sémanticky podobnými slovy. Naše " +
      "modely určující složitelnost kombinují jednoduché statistické přístupy se sémantickým prostorem COALS. Pro " +
      "vyhodnocení byla použita data pro Distributional Semantics and Compositionality 2011 workshop (DISCO 2011). " +
      "Náš přístup jsme také porovnali s tradičně používanou technikou Pointwise Mutual Information (PMI). Naše " +
      "nejlepší modely překonávají všechny systémy soutěžící v DISCO 2011.</abstrakt><klicova_slova>DISCO 2011; " +
      "složitelnost; sémantický prostor; kolokace; COALS; PMI</klicova_slova></titul><titul><nazev>Detection of " +
      "Semantic Compositionality using Semantic Spaces</nazev><jazyk>eng</jazyk><original>Ano</original><abstrakt>Any" +
      " Natural Language Processing (NLP) system that does semantic processing relies on the assumption of semantic " +
      "compositionality: the meaning of a compound is determined by the meaning of its parts and their combination. " +
      "However, the compositionality assumption does not hold for many idiomatic expressions such as &quot;blue " +
      "chip&quot;. This paper focuses on the fully automatic detection of these, further referred to as " +
      "non-compositional compounds. We have proposed and tested an intuitive approach based on replacing the parts of" +
      " compounds by semantically related words. Our models determining the compositionality combine simple statistic" +
      " ideas with the COALS semantic space. For the evaluation, the shared dataset for the Distributional Semantics " +
      "and Compositionality 2011 workshop (DISCO 2011) is used. A comparison of our approach with the traditionally " +
      "used Pointwise Mutual Information (PMI) is also presented. Our best models outperform all the systems " +
      "competing in DISCO 2011.</abstrakt><klicova_slova>DISCO 2011; compositionality; semantic space; collocations; " +
      "COALS; PMI</klicova_slova></titul></titul_list><odkaz_list><odkaz><url>http://linkg.springer.com/chapter/10" +
      ".1007%</url><popis/></odkaz></odkaz_list><issn/><isbn>978-3-642-32789-6</isbn><typ_vlastni_prace_list>  " +
      "</typ_vlastni_prace_list><neuplat>Ne</neuplat><poznamka/><id_riv>43915977</id_riv><zdroj_nazev>TSD " +
      "2012</zdroj_nazev><zdroj_zkratka/><konani_misto>Brno</konani_misto><vydani>1</vydani><vydavatel_mesto" +
      ">Heidelberg</vydavatel_mesto><rocnik/><zprava_cislo/><cislo/><strany>353-361</strany><prace_misto" +
      "/><prace_rozsah>9</prace_rozsah><baleni/><naklad/><document_typ/><codn/><medium/><dopis_komu" +
      "/><konference_nazev>TSD 2012</konference_nazev><konf_ucastnici/><konf_zahr_ucastnici/><konference_typ>WRD" +
      "</konference_typ><konference_stat/><ut_isi/><rok>2012</rok><vydavatel_nazev>Springer</vydavatel_nazev" +
      "><impakt_faktor/><dat_kon_zacatek>03.09.2012</dat_kon_zacatek><dat_kon_konec>07.09" +
      ".2012</dat_kon_konec><archiv_cislo/><umisteni/><dostupnost/><sci/><spojka/><stat>DE</stat><literarni_forma>STA" +
      "Ť VE SBORNÍKU</literarni_forma><rozsireni_literarni_formy>Stať ve sborníku (D)" +
      "</rozsireni_literarni_formy><stav>Ke kontrole</stav></zaznam></zaznamy>";
  private OBD30Strategy obdStrategy = new OBD30Strategy();

  @Test
  public void testParseResponse() throws Exception {
    System.out.println("OBD30StrategyUnitTest.parseResponse_OBD_3.0");
    List<Publication> publications = obdStrategy.parseResponse(OBD_30_RESPONSE);
  }

}
