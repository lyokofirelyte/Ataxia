package com.github.lyokofirelyte.Ataxia.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.lyokofirelyte.Ataxia.data.Role;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface MessageHandler {
	String[] aliases();
	String desc() default "An Ataxia Command";
	String usage() default "Usage tip here!";
	Role role() default Role.MEMBER;
	Channel channel() default Channel.ANY;
	boolean noHelp() default false;
}