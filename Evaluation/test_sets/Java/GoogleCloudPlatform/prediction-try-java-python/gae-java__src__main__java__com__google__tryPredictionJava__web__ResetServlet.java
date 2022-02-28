/*
 * Copyright 2012 Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Author: Marc Cohen
 *
 */

package com.google.tryPredictionJava.web;

import java.io.IOException;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, 
                       HttpServletResponse response) throws 
                         ServletException, IOException {

    // Store empty credentials in app engine datastore. This wipes out
    // the server credentials so that the next user to hit the site will
    // trigger the OAuth 2.0 authorization sequence.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key credsKey = KeyFactory.createKey("Credentials", "Credentials");
    datastore.delete(credsKey);

    // Server credentials cleared - redirect session to the main page.
    response.sendRedirect("/");
  }
}
