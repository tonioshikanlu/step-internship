// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;


@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/html");
    List<String> login_array = new ArrayList<>();
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/index.html";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      String loginStatus = Boolean.toString(userService.isUserLoggedIn());
      login_array.add("Hello " + userEmail + "!");
      login_array.add("<p> <a href=\"" + logoutUrl + "\">Logout</a></p>");
      login_array.add(loginStatus);
      Gson gson = new Gson();
      String json = gson.toJson(login_array);
      response.getWriter().println(json);
    } else {
      String urlToRedirectToAfterUserLogsIn = "/index.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      String loginStatus = Boolean.toString(userService.isUserLoggedIn());
      login_array.add("<p>Hello stranger.</p>");
      login_array.add("<p><a href=\"" + loginUrl + "\">Login</a></p>");
      login_array.add(loginStatus);
      Gson gson = new Gson();
      String json = gson.toJson(login_array);
      response.getWriter().println(json);
    }
  }
}
