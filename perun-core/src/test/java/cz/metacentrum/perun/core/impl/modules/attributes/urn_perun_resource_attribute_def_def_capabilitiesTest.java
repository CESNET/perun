package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;

import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;


public class urn_perun_resource_attribute_def_def_capabilitiesTest {

  private static PerunSessionImpl ps;
  urn_perun_resource_attribute_def_def_capabilities module;
  Resource resource;
  ArrayList<String> testedValues;
  Attribute attribute;

  @Before
  public void setUp() {
    module = new urn_perun_resource_attribute_def_def_capabilities();
    ps = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    resource = new Resource();
    testedValues = new ArrayList<>();
    testedValues.add("");
    attribute = new Attribute();
    attribute.setValue(testedValues);

  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax1() throws Exception {
    System.out.println("checkAttributeWrongSyntax1()");
    testedValues.set(0, "res");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test
  public void checkAttributeWrongSyntax2() throws Exception {
    System.out.println("checkAttributeWrongSyntax2()");
    testedValues.set(0, "res:a");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax3() throws Exception {
    System.out.println("checkAttributeWrongSyntax3()");
    testedValues.set(0, "res:a,:b");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax4() throws Exception {
    System.out.println("checkAttributeWrongSyntax4()");
    testedValues.set(0, "res:aaaa::bb");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax5() throws Exception {
    System.out.println("checkAttributeWrongSyntax5()");
    testedValues.set(0, "res:act:a");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax6() throws Exception {
    System.out.println("checkAttributeWrongSyntax6()");
    testedValues.set(0, "res:a:b:c:act:a,aa,au:");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax7() throws Exception {
    System.out.println("checkAttributeWrongSyntax7()");
    testedValues.set(0, "");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax8() throws Exception {
    System.out.println("checkAttributeWrongSyntax8()");
    testedValues.set(0, "act");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test
  public void checkAttributeWrongSyntax9() throws Exception {
    System.out.println("checkAttributeWrongSyntax9()");
    testedValues.set(0, "res:a:b:c:act:a,b,c");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax10() throws Exception {
    System.out.println("checkAttributeWrongSyntax10()");
    testedValues.set(0, "res:abbbbbb::aaaa");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax11() throws Exception {
    System.out.println("checkAttributeWrongSyntax11()");
    testedValues.set(0, "res::a");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax12() throws Exception {
    System.out.println("checkAttributeWrongSyntax12()");
    testedValues.set(0, "res:a,b,:a");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax13() throws Exception {
    System.out.println("checkAttributeWrongSyntax13()");
    testedValues.set(0, "res:a,b:,a");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test
  public void checkAttributeWrongSyntax14() throws Exception {
    System.out.println("checkAttributeWrongSyntax14()");
    testedValues.set(0, "res:a,b,a:act:ajaj$");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax15() throws Exception {
    System.out.println("checkAttributeWrongSyntax15()");
    testedValues.set(0, "res:resource:respir2:act:jedna,dva,tri:triapul,ctyri,");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test
  public void checkAttributeWrongSyntax16() throws Exception {
    System.out.println("checkAttributeWrongSyntax16()");
    testedValues.set(0, "res:resource:respir2:act:jedna,dva,tri:triapul,ctyri");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax17() throws Exception {
    System.out.println("checkAttributeWrongSyntax17()");
    testedValues.set(0, "res:resource:respir2:act:jedna,dva,tri:triapul,ctyri:");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax18() throws Exception {
    System.out.println("checkAttributeWrongSyntax18()");
    testedValues.set(0, "res:resource:respir2:act:jedna,dva,tri::triapul,ctyri");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax19() throws Exception {
    System.out.println("checkAttributeWrongSyntax19()");
    testedValues.set(0, "res:resource:respir2:act::jedna,dva,tri:triapul,ctyri");
    module.checkAttributeSyntax(ps, resource, attribute);
  }

  @Test
  public void checkAttributeWrongSyntax20() throws Exception {
    System.out.println("checkAttributeWrongSyntax20()");
    testedValues.set(0, "res:res$)(ource:res_-pir2:act:jed-na,dv*a,tr;i:tria,pul,c+tyri");
    module.checkAttributeSyntax(ps, resource, attribute);
  }


  @Test(expected = WrongAttributeValueException.class)
  public void checkAttributeWrongSyntax21() throws Exception {
    System.out.println("checkAttributeWrongSyntax21()");
    testedValues.set(0, "res:res$)(ource:res_-pir2:act");
    module.checkAttributeSyntax(ps, resource, attribute);
  }
}
