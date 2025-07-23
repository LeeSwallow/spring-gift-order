package gift.common.aop.annotation;

import gift.entity.UserRole;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PreAuthorize {
    UserRole value() default UserRole.ROLE_GUEST;
}
