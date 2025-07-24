package com.teknokote.ess.http.logger;

import com.teknokote.ess.core.model.organization.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
@Getter
@Setter
public class EntityActionEvent extends ApplicationEvent {
    private String action;
    private User user;
    private transient HttpServletRequest servletRequest;
    public EntityActionEvent(Object source, String action,User user,  HttpServletRequest servletRequest) {
        super(source);
        this.action = action;
        this.user=user;
        this.servletRequest=servletRequest;
    }
}