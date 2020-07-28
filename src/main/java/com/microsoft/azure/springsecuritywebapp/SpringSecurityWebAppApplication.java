// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.springsecuritywebapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class SpringSecurityWebAppApplication extends SpringBootServletInitializer {

 public static void main(String[] args) {
  SpringApplication.run(SpringSecurityWebAppApplication.class);
 }

 @Override
 protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
  return builder.sources(SpringSecurityWebAppApplication.class);
 }
}
