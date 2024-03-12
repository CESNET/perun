package cz.metacentrum.perun.cabinet.strategy.impl;

import cz.metacentrum.perun.cabinet.model.Publication;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for EuropePMC PublicationSystem strategy
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EuropePMCStrategyUnitTest {

  private static final String EUROPE_PMC_RESPONSE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><responseWrapper><version>5.2" +
      ".1</version><hitCount>5</hitCount><nextCursorMark>AoJwgJ/KqtsCKDM2NTc5ODgx</nextCursorMark><request><query" +
      ">AUTHORID:\"0000-0002- 1767-9318\" sort_date:y " +
      "PUB_YEAR:2017</query><resultType>core</resultType><synonym>false</synonym><cursorMark>*</cursorMark><pageSize" +
      ">2</pageSize><sort></sort></request><resultList><result><id>28507325</id><source>MED</source><pmid>28507325" +
      "</pmid><pmcid>PMC5432835</pmcid><doi>10.1038/srep46813</doi><title>Corrigendum: Characterisation of mental " +
      "health conditions in social media using Informed Deep Learning.</title><authorString>Gkotsis G, Oellrich A, " +
      "Velupillai S, Liakata M, Hubbard TJP, Dobson RJB, Dutta R.</authorString><authorList><author><fullName>Gkotsis" +
      " G</fullName><firstName>George</firstName><lastName>Gkotsis</lastName><initials>G</initials></author><author" +
      "><fullName>Oellrich A</fullName><firstName>Anika</firstName><lastName>Oellrich</lastName><initials>A</initials" +
      "></author><author><fullName>Velupillai S</fullName><firstName>Sumithra</firstName><lastName>Velupillai" +
      "</lastName><initials>S</initials></author><author><fullName>Liakata " +
      "M</fullName><firstName>Maria</firstName><lastName>Liakata</lastName><initials>M</initials></author><author" +
      "><fullName>Hubbard TJP</fullName><firstName>Tim J " +
      "P</firstName><lastName>Hubbard</lastName><initials>TJP</initials><authorId " +
      "type=\"ORCID\">0000-0002-1767-9318</authorId></author><author><fullName>Dobson " +
      "RJB</fullName><firstName>Richard J B</firstName><lastName>Dobson</lastName><initials>RJB</initials></author" +
      "><author><fullName>Dutta R</fullName><firstName>Rina</firstName><lastName>Dutta</lastName><initials>R" +
      "</initials></author></authorList><authorIdList><authorId " +
      "type=\"ORCID\">0000-0002-1767-9318</authorId></authorIdList><journalInfo><volume>7</volume><journalIssueId" +
      ">2552239</journalIssueId><dateOfPublication>2017 " +
      "May</dateOfPublication><monthOfPublication>5</monthOfPublication><yearOfPublication>2017</yearOfPublication" +
      "><printPublicationDate>2017-05-01</printPublicationDate><journal><title>Scientific " +
      "reports</title><ISOAbbreviation>Sci Rep</ISOAbbreviation><medlineAbbreviation>Sci " +
      "Rep</medlineAbbreviation><NLMid>101563288</NLMid><ISSN>2045-2322</ISSN><ESSN>2045-2322</ESSN></journal" +
      "></journalInfo><pubYear>2017</pubYear><pageInfo>46813</pageInfo><language>eng</language><pubModel>Electronic" +
      "</pubModel><pubTypeList><pubType>Published Erratum</pubType><pubType>correction</pubType><pubType>Journal " +
      "Article</pubType></pubTypeList><fullTextUrlList><fullTextUrl><availability>Open " +
      "access</availability><availabilityCode>OA</availabilityCode><documentStyle>pdf</documentStyle><site>Europe_PMC" +
      "</site><url>http://europepmc.org/articles/PMC5432835?pdf=render</url></fullTextUrl><fullTextUrl><availability" +
      ">Open access</availability><availabilityCode>OA</availabilityCode><documentStyle>html</documentStyle><site" +
      ">Europe_PMC</site><url>http://europepmc.org/articles/PMC5432835</url></fullTextUrl><fullTextUrl><availability" +
      ">Subscription required</availability><availabilityCode>S</availabilityCode><documentStyle>doi</documentStyle" +
      "><site>DOI</site><url>https://doi.org/10" +
      ".1038/srep46813</url></fullTextUrl></fullTextUrlList><commentCorrectionList><commentCorrection><id>28327593" +
      "</id><source>MED</source><reference>Sci Rep. 2017 Mar 22;7:45141</reference><type>Erratum " +
      "for</type><orderIn>1</orderIn></commentCorrection></commentCorrectionList><isOpenAccess>Y</isOpenAccess" +
      "><inEPMC>Y</inEPMC><inPMC>N</inPMC><hasPDF>Y</hasPDF><hasBook>N</hasBook><hasSuppl>N</hasSuppl><citedByCount>0" +
      "</citedByCount><hasReferences>Y</hasReferences><hasTextMinedTerms>N</hasTextMinedTerms><hasDbCrossReferences>N" +
      "</hasDbCrossReferences><hasLabsLinks>N</hasLabsLinks><license>cc " +
      "by</license><authMan>N</authMan><epmcAuthMan>N</epmcAuthMan><nihAuthMan>N</nihAuthMan><hasTMAccessionNumbers>N" +
      "</hasTMAccessionNumbers><dateOfCreation>2017-05-16</dateOfCreation><dateOfRevision>2017-09-07</dateOfRevision" +
      "><electronicPublicationDate>2017-05-16</electronicPublicationDate><firstPublicationDate>2017-05-16" +
      "</firstPublicationDate></result><result><id>28396521</id><source>MED</source><pmid>28396521</pmid><pmcid" +
      ">PMC5411779</pmcid><doi>10.1101/gr.213611.116</doi><title>Evaluation of GRCh38 and de novo haploid genome " +
      "assemblies demonstrates the enduring quality of the reference assembly.</title><authorString>Schneider VA, " +
      "Graves-Lindsay T, Howe K, Bouk N, Chen HC, Kitts PA, Murphy TD, Pruitt KD, Thibaud-Nissen F, Albracht D, " +
      "Fulton RS, Kremitzki M, Magrini V, Markovic C, McGrath S, Steinberg KM, Auger K, Chow W, Collins J, Harden G, " +
      "Hubbard T, Pelan S, Simpson JT, Threadgold G, Torrance J, Wood JM, Clarke L, Koren S, Boitano M, Peluso P, Li " +
      "H, Chin CS, Phillippy AM, Durbin R, Wilson RK, Flicek P, Eichler EE, Church DM" +
      ".</authorString><authorList><author><fullName>Schneider VA</fullName><firstName>Valerie " +
      "A</firstName><lastName>Schneider</lastName><initials>VA</initials><affiliation>National Center for " +
      "Biotechnology Information, National Library of Medicine, National Institutes of Health, Bethesda, Maryland " +
      "20894, USA.</affiliation></author><author><fullName>Graves-Lindsay " +
      "T</fullName><firstName>Tina</firstName><lastName>Graves-Lindsay</lastName><initials>T</initials><affiliation" +
      ">McDonnell Genome Institute at Washington University, St. Louis, Missouri 63018, USA" +
      ".</affiliation></author><author><fullName>Howe " +
      "K</fullName><firstName>Kerstin</firstName><lastName>Howe</lastName><initials>K</initials><affiliation>Wellcome" +
      " Trust Sanger Institute, Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Bouk " +
      "N</fullName><firstName>Nathan</firstName><lastName>Bouk</lastName><initials>N</initials><affiliation>National " +
      "Center for Biotechnology Information, National Library of Medicine, National Institutes of Health, Bethesda, " +
      "Maryland 20894, USA.</affiliation></author><author><fullName>Chen " +
      "HC</fullName><firstName>Hsiu-Chuan</firstName><lastName>Chen</lastName><initials>HC</initials><affiliation" +
      ">National Center for Biotechnology Information, National Library of Medicine, National Institutes of Health, " +
      "Bethesda, Maryland 20894, USA.</affiliation></author><author><fullName>Kitts PA</fullName><firstName>Paul " +
      "A</firstName><lastName>Kitts</lastName><initials>PA</initials><affiliation>National Center for Biotechnology " +
      "Information, National Library of Medicine, National Institutes of Health, Bethesda, Maryland 20894, USA" +
      ".</affiliation></author><author><fullName>Murphy TD</fullName><firstName>Terence " +
      "D</firstName><lastName>Murphy</lastName><initials>TD</initials><affiliation>National Center for Biotechnology " +
      "Information, National Library of Medicine, National Institutes of Health, Bethesda, Maryland 20894, USA" +
      ".</affiliation></author><author><fullName>Pruitt KD</fullName><firstName>Kim " +
      "D</firstName><lastName>Pruitt</lastName><initials>KD</initials><affiliation>National Center for Biotechnology " +
      "Information, National Library of Medicine, National Institutes of Health, Bethesda, Maryland 20894, USA" +
      ".</affiliation></author><author><fullName>Thibaud-Nissen " +
      "F</fullName><firstName>Françoise</firstName><lastName>Thibaud-Nissen</lastName><initials>F</initials" +
      "><affiliation>National Center for Biotechnology Information, National Library of Medicine, National Institutes" +
      " of Health, Bethesda, Maryland 20894, USA.</affiliation></author><author><fullName>Albracht " +
      "D</fullName><firstName>Derek</firstName><lastName>Albracht</lastName><initials>D</initials><affiliation" +
      ">McDonnell Genome Institute at Washington University, St. Louis, Missouri 63018, USA" +
      ".</affiliation></author><author><fullName>Fulton RS</fullName><firstName>Robert " +
      "S</firstName><lastName>Fulton</lastName><initials>RS</initials><affiliation>McDonnell Genome Institute at " +
      "Washington University, St. Louis, Missouri 63018, USA.</affiliation></author><author><fullName>Kremitzki " +
      "M</fullName><firstName>Milinn</firstName><lastName>Kremitzki</lastName><initials>M</initials><affiliation" +
      ">McDonnell Genome Institute at Washington University, St. Louis, Missouri 63018, USA" +
      ".</affiliation></author><author><fullName>Magrini " +
      "V</fullName><firstName>Vincent</firstName><lastName>Magrini</lastName><initials>V</initials><affiliation" +
      ">McDonnell Genome Institute at Washington University, St. Louis, Missouri 63018, USA" +
      ".</affiliation></author><author><fullName>Markovic " +
      "C</fullName><firstName>Chris</firstName><lastName>Markovic</lastName><initials>C</initials><affiliation" +
      ">McDonnell Genome Institute at Washington University, St. Louis, Missouri 63018, USA" +
      ".</affiliation></author><author><fullName>McGrath " +
      "S</fullName><firstName>Sean</firstName><lastName>McGrath</lastName><initials>S</initials><authorId " +
      "type=\"ORCID\">0000-0001-9064-8449</authorId><affiliation>McDonnell Genome Institute at Washington University," +
      " St. Louis, Missouri 63018, USA.</affiliation></author><author><fullName>Steinberg " +
      "KM</fullName><firstName>Karyn Meltz</firstName><lastName>Steinberg</lastName><initials>KM</initials" +
      "><affiliation>McDonnell Genome Institute at Washington University, St. Louis, Missouri 63018, USA" +
      ".</affiliation></author><author><fullName>Auger " +
      "K</fullName><firstName>Kate</firstName><lastName>Auger</lastName><initials>K</initials><affiliation>Wellcome " +
      "Trust Sanger Institute, Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Chow " +
      "W</fullName><firstName>William</firstName><lastName>Chow</lastName><initials>W</initials><authorId " +
      "type=\"ORCID\">0000-0002-9056-201X</authorId><affiliation>Wellcome Trust Sanger Institute, Wellcome Genome " +
      "Campus, Hinxton, Cambridge CB10 1SA, United Kingdom.</affiliation></author><author><fullName>Collins " +
      "J</fullName><firstName>Joanna</firstName><lastName>Collins</lastName><initials>J</initials><affiliation" +
      ">Wellcome Trust Sanger Institute, Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Harden " +
      "G</fullName><firstName>Glenn</firstName><lastName>Harden</lastName><initials>G</initials><affiliation>Wellcome" +
      " Trust Sanger Institute, Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Hubbard " +
      "T</fullName><firstName>Timothy</firstName><lastName>Hubbard</lastName><initials>T</initials><authorId " +
      "type=\"ORCID\">0000-0002-1767-9318</authorId><affiliation>Wellcome Trust Sanger Institute, Wellcome Genome " +
      "Campus, Hinxton, Cambridge CB10 1SA, United Kingdom.</affiliation></author><author><fullName>Pelan " +
      "S</fullName><firstName>Sarah</firstName><lastName>Pelan</lastName><initials>S</initials><affiliation>Wellcome " +
      "Trust Sanger Institute, Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Simpson JT</fullName><firstName>Jared " +
      "T</firstName><lastName>Simpson</lastName><initials>JT</initials><affiliation>Wellcome Trust Sanger Institute, " +
      "Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Threadgold " +
      "G</fullName><firstName>Glen</firstName><lastName>Threadgold</lastName><initials>G</initials><affiliation" +
      ">Wellcome Trust Sanger Institute, Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Torrance " +
      "J</fullName><firstName>James</firstName><lastName>Torrance</lastName><initials>J</initials><affiliation" +
      ">Wellcome Trust Sanger Institute, Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Wood JM</fullName><firstName>Jonathan " +
      "M</firstName><lastName>Wood</lastName><initials>JM</initials><affiliation>Wellcome Trust Sanger Institute, " +
      "Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Clarke " +
      "L</fullName><firstName>Laura</firstName><lastName>Clarke</lastName><initials>L</initials><affiliation>European" +
      " Molecular Biology Laboratory, European Bioinformatics Institute, Wellcome Genome Campus, Hinxton, Cambridge " +
      "CB10 1SD, United Kingdom.</affiliation></author><author><fullName>Koren " +
      "S</fullName><firstName>Sergey</firstName><lastName>Koren</lastName><initials>S</initials><affiliation>National" +
      " Human Genome Research Institute, National Institutes of Health, Bethesda, Maryland 20892, USA" +
      ".</affiliation></author><author><fullName>Boitano " +
      "M</fullName><firstName>Matthew</firstName><lastName>Boitano</lastName><initials>M</initials><affiliation" +
      ">Pacific Biosciences, Menlo Park, California 94025, USA.</affiliation></author><author><fullName>Peluso " +
      "P</fullName><firstName>Paul</firstName><lastName>Peluso</lastName><initials>P</initials><affiliation>Pacific " +
      "Biosciences, Menlo Park, California 94025, USA.</affiliation></author><author><fullName>Li " +
      "H</fullName><firstName>Heng</firstName><lastName>Li</lastName><initials>H</initials><authorId " +
      "type=\"ORCID\">0000-0003-4874-2874</authorId><affiliation>Broad Institute, Cambridge, Massachusetts 02142, USA" +
      ".</affiliation></author><author><fullName>Chin " +
      "CS</fullName><firstName>Chen-Shan</firstName><lastName>Chin</lastName><initials>CS</initials><affiliation" +
      ">Pacific Biosciences, Menlo Park, California 94025, USA.</affiliation></author><author><fullName>Phillippy " +
      "AM</fullName><firstName>Adam M</firstName><lastName>Phillippy</lastName><initials>AM</initials><affiliation" +
      ">National Human Genome Research Institute, National Institutes of Health, Bethesda, Maryland 20892, USA" +
      ".</affiliation></author><author><fullName>Durbin " +
      "R</fullName><firstName>Richard</firstName><lastName>Durbin</lastName><initials>R</initials><affiliation" +
      ">Wellcome Trust Sanger Institute, Wellcome Genome Campus, Hinxton, Cambridge CB10 1SA, United Kingdom" +
      ".</affiliation></author><author><fullName>Wilson RK</fullName><firstName>Richard " +
      "K</firstName><lastName>Wilson</lastName><initials>RK</initials><affiliation>McDonnell Genome Institute at " +
      "Washington University, St. Louis, Missouri 63018, USA.</affiliation></author><author><fullName>Flicek " +
      "P</fullName><firstName>Paul</firstName><lastName>Flicek</lastName><initials>P</initials><affiliation>European " +
      "Molecular Biology Laboratory, European Bioinformatics Institute, Wellcome Genome Campus, Hinxton, Cambridge " +
      "CB10 1SD, United Kingdom.</affiliation></author><author><fullName>Eichler EE</fullName><firstName>Evan " +
      "E</firstName><lastName>Eichler</lastName><initials>EE</initials><affiliation>Howard Hughes Medical Institute, " +
      "University of Washington, Seattle, Washington 98195, USA.</affiliation></author><author><fullName>Church " +
      "DM</fullName><firstName>Deanna M</firstName><lastName>Church</lastName><initials>DM</initials><affiliation" +
      ">National Center for Biotechnology Information, National Library of Medicine, National Institutes of Health, " +
      "Bethesda, Maryland 20894, USA.</affiliation></author></authorList><authorIdList><authorId " +
      "type=\"ORCID\">0000-0001-9064-8449</authorId><authorId type=\"ORCID\">0000-0002-9056-201X</authorId><authorId " +
      "type=\"ORCID\">0000-0002-1767-9318</authorId><authorId " +
      "type=\"ORCID\">0000-0003-4874-2874</authorId></authorIdList><journalInfo><issue>5</issue><volume>27</volume" +
      "><journalIssueId>2551752</journalIssueId><dateOfPublication>2017 " +
      "May</dateOfPublication><monthOfPublication>5</monthOfPublication><yearOfPublication>2017</yearOfPublication" +
      "><printPublicationDate>2017-05-01</printPublicationDate><journal><title>Genome " +
      "research</title><ISOAbbreviation>Genome Res.</ISOAbbreviation><medlineAbbreviation>Genome " +
      "Res</medlineAbbreviation><NLMid>9518021</NLMid><ISSN>1088-9051</ISSN><ESSN>1549-5469</ESSN></journal" +
      "></journalInfo><pubYear>2017</pubYear><pageInfo>849-864</pageInfo><abstractText>The human reference genome " +
      "assembly plays a central role in nearly all aspects of today's basic and clinical research. GRCh38 is the " +
      "first coordinate-changing assembly update since 2009; it reflects the resolution of roughly 1000 issues and " +
      "encompasses modifications ranging from thousands of single base changes to megabase-scale path " +
      "reorganizations, gap closures, and localization of previously orphaned sequences. We developed a new approach " +
      "to sequence generation for targeted base updates and used data from new genome mapping technologies and single" +
      " haplotype resources to identify and resolve larger assembly issues. For the first time, the reference " +
      "assembly contains sequence-based representations for the centromeres. We also expanded the number of alternate" +
      " loci to create a reference that provides a more robust representation of human population variation. We " +
      "demonstrate that the updates render the reference an improved annotation substrate, alter read alignments in " +
      "unchanged regions, and impact variant interpretation at clinically relevant loci. We additionally evaluated a " +
      "collection of new de novo long-read haploid assemblies and conclude that although the new assemblies compare " +
      "favorably to the reference with respect to continuity, error rate, and gene completeness, the reference still " +
      "provides the best representation for complex genomic regions and coding sequences. We assert that the " +
      "collected updates in GRCh38 make the newer assembly a more robust substrate for comprehensive analyses that " +
      "will promote our understanding of human biology and advance our efforts to improve health" +
      ".</abstractText><affiliation>National Center for Biotechnology Information, National Library of Medicine, " +
      "National Institutes of Health, Bethesda, Maryland 20894, USA" +
      ".</affiliation><language>eng</language><pubModel>Print-Electronic</pubModel><pubTypeList><pubType>research" +
      "-article</pubType><pubType>Journal Article</pubType></pubTypeList><grantsList><grant><grantId>R01 " +
      "HG002385</grantId><agency>NHGRI NIH HHS</agency><acronym>HG</acronym><orderIn>0</orderIn></grant><grant" +
      "><agency>Wellcome Trust</agency><orderIn>0</orderIn></grant><grant><grantId>U54 " +
      "HG003079</grantId><agency>NHGRI NIH HHS</agency><acronym>HG</acronym><orderIn>0</orderIn></grant><grant" +
      "><grantId>U41 HG007635</grantId><agency>NHGRI NIH " +
      "HHS</agency><acronym>HG</acronym><orderIn>0</orderIn></grant></grantsList><fullTextUrlList><fullTextUrl" +
      "><availability>Free</availability><availabilityCode>F</availabilityCode><documentStyle>pdf</documentStyle" +
      "><site>Europe_PMC</site><url>http://europepmc" +
      ".org/articles/PMC5411779?pdf=render</url></fullTextUrl><fullTextUrl><availability>Free</availability" +
      "><availabilityCode>F</availabilityCode><documentStyle>html</documentStyle><site>Europe_PMC</site><url>http" +
      "://europepmc.org/articles/PMC5411779</url></fullTextUrl><fullTextUrl><availability>Subscription " +
      "required</availability><availabilityCode>S</availabilityCode><documentStyle>doi</documentStyle><site>DOI</site" +
      "><url>https://doi.org/10.1101/gr.213611.116</url></fullTextUrl></fullTextUrlList><isOpenAccess>N</isOpenAccess" +
      "><inEPMC>Y</inEPMC><inPMC>N</inPMC><hasPDF>Y</hasPDF><hasBook>N</hasBook><hasSuppl>Y</hasSuppl><citedByCount>3" +
      "</citedByCount><hasReferences>Y</hasReferences><hasTextMinedTerms>Y</hasTextMinedTerms><hasDbCrossReferences>N" +
      "</hasDbCrossReferences><hasLabsLinks>Y</hasLabsLinks><license>cc " +
      "by</license><authMan>N</authMan><epmcAuthMan>N</epmcAuthMan><nihAuthMan>N</nihAuthMan><hasTMAccessionNumbers>N" +
      "</hasTMAccessionNumbers><dateOfCreation>2017-04-11</dateOfCreation><dateOfRevision>2017-05-18</dateOfRevision" +
      "><electronicPublicationDate>2017-04-10</electronicPublicationDate><firstPublicationDate>2017-04-10" +
      "</firstPublicationDate></result><result><id>C1240</id><source>CTX</source><title>Molecular Biology of the " +
      "Cell</title><authorString>Alberts  B.</authorString><authorList><author><fullName>Alberts  " +
      "B</fullName><lastName>Alberts </lastName><initials>B</initials></author></authorList><pubYear>2002</pubYear" +
      "><abstractText>Molecular Biology of the Cell is the classic in-dept text reference in cell biology. By " +
      "extracting the fundamental concepts from this enormous and ever-growing field, the authors tell the story of " +
      "cell biology, and create a coherent framework through which non-expert readers may approach the subject. " +
      "Written in clear and concise language, and beautifully illustrated, the book is enjoyable to read, and it " +
      "provides a clear sense of the excitement of modern biology. Molecular Biology of the Cell sets forth the " +
      "current understanding of cell biology (completely updated as of Autumn 2001), and it explores the intriguing " +
      "implications and possibilities of the great deal that remains unknown. The hallmark features of previous " +
      "editions continue in the Fourth Edition. The book is designed with a clean and open, single-column layout. The" +
      " art program maintains a completely consistent format and style, and includes over 1,600 photographs, electron" +
      " micrographs, and original drawings by the authors. Clear and concise concept headings introduce each section." +
      " Every chapter contains extensive references. Most important, every chapter has been subjected to a rigorous, " +
      "collaborative revision process where, in addition to incorporating comments from expert reviewers, each " +
      "co-author reads and reviews the other authors' prose. The result is a truly integrated work with a single " +
      "authorial voice. Features : - Places the latest hot topics sensibly in context - including genomics, protein " +
      "structure, array technology, stem cells and genetics diseases. - Incorporates and emphasises new genomic data." +
      " - All of molecular biology is brought together into one section (chapters 4-7) covering classically defined " +
      "molecular biology and molecular genetics. - Two chapters deal exclusively with methods and contain information" +
      " on the latest tools and techniques. - New chapters on \"Pathogens, Infection, and Innate Immunity\". - Cell " +
      "Biology Interactive CD-ROM is packaged with every copy of the book. - Contains over 1,600 illustrations, " +
      "electron micrographs and photographs, of which over 1,000 are originally conceived by the authors" +
      ".</abstractText><language>eng</language><bookOrReportDetails><publisher>Garland Publishing " +
      "Inc</publisher><yearOfPublication>2002</yearOfPublication><numberOfPages>1616</numberOfPages><edition>4" +
      "</edition><isbn10>0815340729</isbn10><isbn13>9780815340720</isbn13></bookOrReportDetails><fullTextUrlList" +
      "><fullTextUrl><availability>Open access</availability><availabilityCode>OA</availabilityCode><documentStyle" +
      ">html</documentStyle><site>Amazon preview</site><url>http://www.amazon.co" +
      ".uk/gp/reader/0815340729/ref=sib_dp_pt/202-6766391-0155007#reader-link</url></fullTextUrl><fullTextUrl" +
      "><availability>Open access</availability><availabilityCode>OA</availabilityCode><documentStyle>html" +
      "</documentStyle><site>NIH</site><url>https://www.ncbi.nlm.nih.gov/books/bv.fcgi?call=bv.View..ShowTOC&amp;" +
      "rid=cell.TOC&amp;depth=2</url></fullTextUrl></fullTextUrlList><isOpenAccess>N</isOpenAccess><inEPMC>N</inEPMC" +
      "><inPMC>N</inPMC><hasPDF>N</hasPDF><hasBook>N</hasBook><citedByCount>0</citedByCount><hasReferences>N" +
      "</hasReferences><hasTextMinedTerms>N</hasTextMinedTerms><hasDbCrossReferences>N</hasDbCrossReferences" +
      "><hasLabsLinks>N</hasLabsLinks><authMan>N</authMan><epmcAuthMan>N</epmcAuthMan><nihAuthMan>N</nihAuthMan" +
      "><hasTMAccessionNumbers>N</hasTMAccessionNumbers><dateOfCreation>2007-09-21</dateOfCreation><dateOfRevision" +
      ">2007-09-21</dateOfRevision><firstPublicationDate>2002-03-21</firstPublicationDate></result></resultList" +
      "></responseWrapper>";
  private EuropePMCStrategy europePMCStrategy = new EuropePMCStrategy();

  @Test
  public void testParseResponse() throws Exception {
    System.out.println("OrcIDStrategyUnitTest.parseResponse_EuropePMC");
    List<Publication> publications = europePMCStrategy.parseResponse(EUROPE_PMC_RESPONSE);
    Assert.assertTrue(publications.size() == 3);
  }

}
