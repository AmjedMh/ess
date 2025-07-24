package com.teknokote.ess.authentification.config.permissions;

import com.teknokote.ess.core.model.organization.User;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.Objects;

public class CustomMethodSecurityExpression extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
    private Object filterObject;
    private Object returnObject;

    public CustomMethodSecurityExpression(Authentication authentication) {
        super(authentication);
    }

    public boolean isUserAttachedToCustomerAccount(Long customerAccountId) {
        if (Objects.isNull(getAuthentication())) return false;
        User user = (User) getAuthentication().getPrincipal();
        return Objects.nonNull(user.getCustomerAccountId()) && user.getCustomerAccountId().equals(customerAccountId);
    }

    public boolean isUserAttachedToParentCustomerAccountOf(Long customerAccountId) {
        if (Objects.isNull(getAuthentication())) return false;
        User user = (User) getAuthentication().getPrincipal();
        return Objects.nonNull(user.getCustomerAccountId()) && user.getChildCustomerAccounts().contains(customerAccountId);
    }

   public boolean isUserAttachedToParentOrCustomerAccountOf(Long customerAccountId) {
      if (Objects.isNull(getAuthentication())) return false;
      User user = (User) getAuthentication().getPrincipal();
      return Objects.nonNull(user.getCustomerAccountId()) && user.getChildCustomerAccounts().contains(customerAccountId) || user.getCustomerAccountId().equals(customerAccountId);
   }


    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }
}
