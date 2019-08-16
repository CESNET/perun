package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for virtual attribute modules marking.
 *
 * Virtual attributes that has a module, which are annotated with this interface, are skipped
 * while checking attributes value during dependencies check. This provides an optimization.
 * It is not necessary to calculate virtual attributes value if its module has empty
 * value check methods.
 *
 * @see AttributesManagerBlImpl#checkAttributeDependencies(PerunSession, RichAttribute)
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SkipValueCheckDuringDependencyCheck {
}
